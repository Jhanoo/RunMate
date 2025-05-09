package com.D107.runmate.data.remote.api

import com.D107.runmate.data.remote.request.group.CreateGroupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface GroupService {
    @POST("/groups/create")
    suspend fun createGroup(
        @Body createGroupRequest: CreateGroupRequest
    ): Response<CreateGroupResponse>

}