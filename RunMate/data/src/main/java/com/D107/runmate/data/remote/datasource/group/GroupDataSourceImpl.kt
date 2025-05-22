package com.D107.runmate.data.remote.datasource.group

import GroupResponse
import com.D107.runmate.data.remote.api.GroupService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ApiResponseHandler
import com.D107.runmate.data.remote.request.group.GroupCreateRequest
import com.D107.runmate.data.remote.response.group.GroupJoinResponse
import retrofit2.Response
import javax.inject.Inject

class GroupDataSourceImpl @Inject constructor(
    private val groupService: GroupService,
    private val handler: ApiResponseHandler
) : GroupDataSource {
    override suspend fun createGroup(createGroupRequest: GroupCreateRequest): ApiResponse<GroupResponse> = handler.handle { groupService.createGroup(createGroupRequest) }
    override suspend fun getCurrentGroup(): ApiResponse<GroupResponse> = handler.handle { groupService.getCurrentGroup() }
    override suspend fun leaveGroup(): ApiResponse<Any> = handler.handle { groupService.leaveGroup() }
    override suspend fun joinGroup(inviteCode: String): ApiResponse<GroupJoinResponse> = handler.handle { groupService.joinGroup(inviteCode) }
    override suspend fun startGroup(): ApiResponse<Any> = handler.handle { groupService.groupStart() }
    override suspend fun finishGroup(): ApiResponse<Any> = handler.handle { groupService.groupFinish() }
    override suspend fun hasGroupHistory(): ApiResponse<Boolean> = handler.handle { groupService.hasGroupHistory() }
}