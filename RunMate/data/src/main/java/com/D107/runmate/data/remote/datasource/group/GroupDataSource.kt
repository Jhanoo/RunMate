package com.D107.runmate.data.remote.datasource.group

import GroupResponse
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.request.group.GroupCreateRequest
import com.D107.runmate.data.remote.response.group.GroupJoinResponse
import retrofit2.Response

interface GroupDataSource  {
    suspend fun createGroup(createGroupRequest: GroupCreateRequest): ApiResponse<GroupResponse>
    suspend fun getCurrentGroup(): ApiResponse<GroupResponse>
    suspend fun leaveGroup(): ApiResponse<Any>
    suspend fun joinGroup(inviteCode: String): ApiResponse<GroupJoinResponse?>
    suspend fun startGroup(): ApiResponse<Any>
    suspend fun finishGroup(): ApiResponse<Any>
    suspend fun hasGroupHistory(): ApiResponse<Boolean>
}