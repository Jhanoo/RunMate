package com.D107.runmate.data.remote.api

import GroupResponse
import com.D107.runmate.data.remote.common.ApiResponse
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
    ): ApiResponse<GroupResponse>

    @GET("groups/current")
    suspend fun getCurrentGroup(): ApiResponse<GroupResponse>

    @DELETE("groups/leave")
    suspend fun leaveGroup(): ApiResponse<Any?>

    @POST("groups/join")
    suspend fun joinGroup(
        @Body
        @Json (name= "inviteCode")
        inviteCode: String
    ): ApiResponse<GroupJoinResponse?>

    @POST("groups/start")
    suspend fun groupStart(): ApiResponse<Any?>

    @POST("groups/finish")
    suspend fun groupFinish(): ApiResponse<Any?>

}