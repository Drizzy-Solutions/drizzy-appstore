package com.memeitizer.appstore.auth

import com.squareup.moshi.Json
import retrofit2.http.*

interface OAuthApi {
    @FormUrlEncoded
    @POST("/login/device/code")
    suspend fun start(@Field("client_id") clientId: String, @Field("scope") scope: String = "repo actions") : DeviceCode

    @FormUrlEncoded
    @POST("/login/oauth/access_token")
    suspend fun poll(
        @Field("client_id") clientId: String,
        @Field("device_code") deviceCode: String,
        @Field("grant_type") grantType: String = "urn:ietf:params:oauth:grant-type:device_code"
    ): TokenResponse
}

data class DeviceCode(
    @Json(name="device_code") val deviceCode: String,
    @Json(name="user_code") val userCode: String,
    @Json(name="verification_uri") val verificationUri: String,
    val interval: Int
)

data class TokenResponse(
    @Json(name="access_token") val accessToken: String?,
    val error: String?
)
