package com.D107.runmate.data.repository

import GroupResponse.Companion.toDomainModel
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.datasource.group.GroupDataSource
import com.D107.runmate.data.remote.request.group.GroupCreateRequest
import com.D107.runmate.domain.model.group.GroupCreateInfo
import com.D107.runmate.domain.model.group.GroupData
import com.D107.runmate.domain.repository.group.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupDataSource: GroupDataSource
): GroupRepository {
    override suspend fun createGroup(groupCreateInfo: GroupCreateInfo): GroupData? {
        try {
            val response = groupDataSource.createGroup(
                GroupCreateRequest(
                    groupName = groupCreateInfo.groupName,
                    courseId = groupCreateInfo.courseId,
                    startTime = groupCreateInfo.startTime.toString(),
                    startLocation = groupCreateInfo.startLocation,
                    latitude = groupCreateInfo.latitude,
                    longitude = groupCreateInfo.longitude
                )
            )

            val body = response.body()
            Timber.d("$body")
            if (response.isSuccessful && body != null) {
                if (body is ApiResponse.Success) {
                    return(body.data.toDomainModel())
                } else {

                }
            }
        } catch (e: Exception) {
            Timber.e("${e.message}")
        }
        return null
    }

}