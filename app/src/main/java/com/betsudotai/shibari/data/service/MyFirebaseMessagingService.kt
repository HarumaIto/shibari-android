package com.betsudotai.shibari.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.betsudotai.shibari.domain.model.AppNotification
import com.betsudotai.shibari.domain.repository.AuthRepository
import com.betsudotai.shibari.domain.repository.NotificationRepository
import com.betsudotai.shibari.domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    // トークンが新しく生成・更新された時に呼ばれる
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                userRepository.updateFcmToken(userId, token)
            }
        }
    }

    // アプリを開いている最中（フォアグラウンド）に通知を受信した時に呼ばれる
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // 通知のタイトルと本文を取得
        val title = message.notification?.title ?: message.data["title"] ?: "新着通知"
        val body = message.notification?.body ?: message.data["body"] ?: ""

        showNotification(title, body)
        saveNotificationToFirestore(title, body)
    }

    private fun saveNotificationToFirestore(title: String, body: String) {
        scope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val notification = AppNotification(
                id = UUID.randomUUID().toString(),
                title = title,
                body = body,
                isRead = false,
                createdAt = LocalDateTime.now()
            )
            notificationRepository.saveNotification(userId, notification)
                .onFailure { it.printStackTrace() }
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "shibari_default_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "縛り 通知",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "縛りアプリからの重要な通知"
        }
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            // ※アプリアイコンがある場合は R.drawable.ic_notification 等に差し替えてください
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // 通知を表示 (IDはユニークにするため時間を活用)
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // メモリリーク防止
    }
}
