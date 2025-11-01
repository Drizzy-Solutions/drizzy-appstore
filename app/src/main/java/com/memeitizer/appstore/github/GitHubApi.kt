package com.memeitizer.appstore.github

import retrofit2.http.*

interface GitHubApi {
    @POST("/repos/{owner}/{repo}/actions/workflows/{workflow}/dispatches")
    suspend fun dispatchWorkflow(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("workflow") workflow: String,
        @Body body: DispatchBody
    )

    @GET("/repos/{owner}/{repo}/actions/runs")
    suspend fun listRuns(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("event") event: String = "workflow_dispatch",
        @Query("per_page") perPage: Int = 5
    ): RunsResponse

    @GET("/repos/{owner}/{repo}/releases/tags/{tag}")
    suspend fun getReleaseByTag(
        @Header("Authorization") auth: String?,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("tag") tag: String
    ): Release
}

data class DispatchBody(val ref: String, val inputs: Map<String, String>)
data class RunsResponse(val workflow_runs: List<Run>)
data class Run(val id: Long, val status: String?, val conclusion: String?, val created_at: String)
data class Release(val tag_name: String, val assets: List<Asset>)
data class Asset(val name: String, val browser_download_url: String)
