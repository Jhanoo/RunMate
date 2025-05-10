package com.D107.runmate.data.remote.datasource.group

import GroupResponse
import com.D107.runmate.data.remote.api.GroupService
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.request.group.GroupCreateRequest
import retrofit2.Response
import javax.inject.Inject

class GroupDataSourceImpl @Inject constructor(
    private val groupService: GroupService
) : GroupDataSource {
    override suspend fun createGroup(createGroupRequest: GroupCreateRequest): Response<ApiResponse<GroupResponse>> = groupService.createGroup(createGroupRequest)

}