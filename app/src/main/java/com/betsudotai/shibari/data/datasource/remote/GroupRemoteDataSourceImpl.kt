package com.betsudotai.shibari.data.datasource.remote

import com.betsudotai.shibari.data.dto.GroupDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class GroupRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : GroupRemoteDataSource {

    private val groupsCollection = firestore.collection("groups")

    override suspend fun createGroup(groupDto: GroupDto): GroupDto {
        val newGroupId = groupsCollection.document().id
        val invitationCode = groupDto.invitationCode.ifEmpty { generateInvitationCode() }
        val groupWithId = groupDto.copy(id = newGroupId, invitationCode = invitationCode)
        groupsCollection.document(newGroupId).set(groupWithId).await()
        return groupWithId
    }

    private fun generateInvitationCode(): String =
        UUID.randomUUID().toString().replace("-", "").take(12).uppercase()

    override suspend fun joinGroup(groupId: String, userId: String) {
        firestore.runTransaction { transaction ->
            val groupRef = groupsCollection.document(groupId)
            val groupSnapshot = transaction.get(groupRef)
            val groupDto = groupSnapshot.toObject(GroupDto::class.java)

            if (groupDto != null && !groupDto.memberIds.contains(userId)) {
                val updatedMemberIds = groupDto.memberIds.toMutableList().apply { add(userId) }
                transaction.update(groupRef, "memberIds", updatedMemberIds)
            }
            null
        }.await()
    }

    override suspend fun getGroupDetails(groupId: String): GroupDto? {
        return groupsCollection.document(groupId).get().await().toObject(GroupDto::class.java)
    }

    override suspend fun getGroupByInvitationCode(invitationCode: String): GroupDto? {
        return groupsCollection
            .whereEqualTo("invitationCode", invitationCode)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(GroupDto::class.java)
    }
}
