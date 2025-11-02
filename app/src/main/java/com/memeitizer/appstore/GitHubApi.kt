package com.memeitizer.appstore.github
import retrofit2.http.*
interface GitHubApi {
    @POST("/repos/{owner}/{repo}/actions/workflows/{workflow}/dispatches")
    suspend fun dispatchWorkflow(
        @Header("Authorization") auth: String,
        @Path("owner") owner: String, @Path("repo") repo: String, @Path("workflow") workflow: String,
        @Body body: DispatchBody
    )
    @GET("/repos/{owner}/{repo}/releases/tags/{tag}")
    suspend fun getReleaseByTag(
        @Header("Authorization") auth: String?, @Path("owner") owner: String, @Path("repo") repo: String, @Path("tag") tag: String
    ): Release
}
data class DispatchBody(val ref:String, val inputs: Map<String, String>)
data class Release(val tag_name:String, val assets: List<Asset>)
data class Asset(val name:String, val browser_download_url:String)
