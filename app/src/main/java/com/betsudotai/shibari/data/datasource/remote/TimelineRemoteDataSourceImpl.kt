package com.betsudotai.shibari.data.datasource.remote

import android.net.Uri
import com.betsudotai.shibari.data.dto.CommentDto
import com.betsudotai.shibari.data.dto.TimelinePostDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class TimelineRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : TimelineRemoteDataSource {

    // リアルタイム更新の肝！
    override fun getTimelineStream(): Flow<List<TimelinePostDto>> = callbackFlow {
        val collection = firestore.collection("timelines")
            .orderBy("createdAt", Query.Direction.DESCENDING) // 新しい順

        // リスナー登録
        val registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // エラー時はFlowを閉じる
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val posts = snapshot.documents.mapNotNull { it.toObject(TimelinePostDto::class.java) }
                trySend(posts) // Flowにデータを流す
            }
        }

        // Flowがキャンセルされたらリスナーも解除する (メモリリーク防止)
        awaitClose { registration.remove() }
    }

    override suspend fun uploadMedia(file: File, path: String): String {
        val ref = storage.reference.child(path)
        // File -> Uri 変換 (AndroidのContext依存を避けるためFileで受け取る)
        val uri = Uri.fromFile(file)

        // アップロード実行
        ref.putFile(uri).await()

        // ダウンロードURL取得
        return ref.downloadUrl.await().toString()
    }

    override suspend fun createPost(postDto: TimelinePostDto) {
        // IDを指定して保存（DTOのIDを使う）
        firestore.collection("timelines").document(postDto.id).set(postDto).await()
    }

    override suspend fun updateVote(postId: String, userId: String, vote: String) {
        val docRef = firestore.collection("timelines").document(postId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentVotes = snapshot.get("votes") as? Map<*, *> ?: emptyMap<String, String>()

            // 既に投票済みなら何もしない（または上書き）
            // ここでは簡易的に「承認数の再計算」を行う
            val newVotes = currentVotes.toMutableMap()
            newVotes[userId] = vote

            val approvalCount = newVotes.values.count { it == "APPROVE" }

            // ステータス更新ロジック (2票以上で承認など)
            val newStatus = if (approvalCount >= 2) "approved" else "pending"

            transaction.update(docRef, mapOf(
                "votes" to newVotes,
                "approvalCount" to approvalCount,
                "status" to newStatus
            ))
        }.await()
    }

    override fun getCommentsStream(postId: String): Flow<List<CommentDto>> = callbackFlow {
        val collection = firestore.collection("timelines").document(postId).collection("comments")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.ASCENDING) // 古い順（上から下へ表示）

        val registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val comments = snapshot.documents.mapNotNull { it.toObject(CommentDto::class.java) }
                trySend(comments)
            }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun addComment(postId: String, commentDto: CommentDto) {
        firestore.collection("timelines").document(postId)
            .collection("comments").document(commentDto.id).set(commentDto).await()
    }
}