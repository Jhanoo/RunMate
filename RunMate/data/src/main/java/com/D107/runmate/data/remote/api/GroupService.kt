package com.D107.runmate.data.remote.api

import GroupResponse
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.request.group.GroupCreateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface GroupService {
    @POST("groups/create")
    suspend fun createGroup(
        @Body createGroupRequest: GroupCreateRequest
    ): ApiResponse<GroupResponse>
}