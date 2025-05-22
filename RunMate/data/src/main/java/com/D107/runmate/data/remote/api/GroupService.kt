package com.D107.runmate.data.remote.api

import GroupResponse
import com.D107.runmate.data.remote.common.ApiResponse
import com.D107.runmate.data.remote.common.ServerResponse
import com.D107.runmate.data.remote.request.group.GroupCreateRequest
import com.D107.runmate.data.remote.response.group.GroupJoinResponse
import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST


interface GroupService {
    @POST("groups/create")
    suspend fun createGroup(
        @Body createGroupRequest: GroupCreateRequest
    ): Response<ServerResponse<GroupResponse>>

    @GET("groups/current")
    suspend fun getCurrentGroup(): Response<ServerResponse<GroupResponse>>

    @DELETE("groups/leave")
    suspend fun leaveGroup(): Response<ServerResponse<Any>>

    @POST("groups/join")
    suspend fun joinGroup(
        @Body
        @Json (name= "inviteCode")
        inviteCode: String
    ): Response<ServerResponse<GroupJoinResponse>>

    @POST("groups/start")
    suspend fun groupStart(): Response<ServerResponse<Any>>

    @POST("groups/finish")
    suspend fun groupFinish(): Response<ServerResponse<Any>>

    @GET("groups/hasGroupHistory")
    suspend fun hasGroupHistory(): Response<ServerResponse<Boolean>>

}