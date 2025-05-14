package com.D107.runmate.data.remote.datasource.group

import GroupResponse
import com.D107.runmate.data.remote.api.GroupService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.request.group.GroupCreateRequest
import com.D107.runmate.data.remote.response.group.GroupJoinResponse
import retrofit2.Response
import javax.inject.Inject

class GroupDataSourceImpl @Inject constructor(
    private val groupService: GroupService
) : GroupDataSource {
    override suspend fun createGroup(createGroupRequest: GroupCreateRequest): ApiResponse<GroupResponse> = groupService.createGroup(createGroupRequest)
    override suspend fun getCurrentGroup(): ApiResponse<GroupResponse> = groupService.getCurrentGroup()
    override suspend fun leaveGroup(): ApiResponse<Any?> = groupService.leaveGroup()
    override suspend fun joinGroup(inviteCode: String): ApiResponse<GroupJoinResponse?> = groupService.joinGroup(inviteCode)
    override suspend fun startGroup(): ApiResponse<Any?>  = groupService.groupStart()
    override suspend fun finishGroup(): ApiResponse<Any?> = groupService.groupFinish()

}