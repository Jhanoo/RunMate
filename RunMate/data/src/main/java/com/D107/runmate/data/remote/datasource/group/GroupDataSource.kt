package com.D107.runmate.data.remote.datasource.group

import GroupResponse
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.request.group.GroupCreateRequest
import retrofit2.Response

interface GroupDataSource  {
    suspend fun createGroup(createGroupRequest: GroupCreateRequest): ApiResponse<GroupResponse>
}