package com.uopeople.bugtracker.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface IssueApiService {

    @GET("issues")
    suspend fun getIssues(): List<IssueDto>

    @GET("issues/{id}")
    suspend fun getIssue(@Path("id") id: Long): IssueDto

    @POST("issues")
    suspend fun createIssue(@Body request: CreateIssueRequest): IssueDto

    @PUT("issues/{id}")
    suspend fun updateIssue(
        @Path("id") id: Long,
        @Body request: CreateIssueRequest
    ): IssueDto

    @DELETE("issues/{id}")
    suspend fun deleteIssue(@Path("id") id: Long): Response<Unit>
}
