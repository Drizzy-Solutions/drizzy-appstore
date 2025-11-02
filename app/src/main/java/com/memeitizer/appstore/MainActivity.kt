package com.memeitizer.appstore
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.memeitizer.appstore.model.*
import com.memeitizer.appstore.github.*
import com.memeitizer.appstore.auth.*
import com.memeitizer.appstore.ui.NeoStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NeoApp() }
    }
}
@Composable
fun NeoApp() {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var catalog by remember { mutableStateOf<Catalog?>(null) }
    var log by remember { mutableStateOf("") }
    var token by remember { mutableStateOf<String?>(null) }

    val moshi = remember { Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build() }
    val http = remember { OkHttpClient() }
    fun retrofit(base:String) = Retrofit.Builder().baseUrl(base).client(http).addConverterFactory(MoshiConverterFactory.create(moshi)).build()
    val gh = remember { retrofit("https://api.github.com").create(GitHubApi::class.java) }
    val oauth = remember { retrofit("https://github.com").create(OAuthApi::class.java) }

    NeoStore(
        title = "Drizzy AppStore Neo",
        catalog = catalog,
        authorized = token != null,
        status = log,
        onLoad = { url ->
            scope.launch {
                log = "Fetching catalog…"
                try {
                    val txt = java.net.URL(url).readText()
                    catalog = moshi.adapter(Catalog::class.java).fromJson(txt)
                    log = "Loaded catalog: ${catalog?.title}"
                } catch (e: Exception) { log = e.toString() }
            }
        },
        onAuth = {
            val clientId = "REPLACE_WITH_GITHUB_OAUTH_CLIENT_ID"
            scope.launch {
                try {
                    val code = oauth.start(clientId)
                    log = "Open ${code.verificationUri} and enter ${code.userCode}"
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(code.verificationUri)))
                    while (true) {
                        val t = oauth.poll(clientId, code.deviceCode)
                        if (t.accessToken != null) { token = t.accessToken; break }
                        delay((code.interval + 1) * 1000L)
                    }
                    log = "Authorized."
                } catch (e: Exception) { log = e.toString() }
            }
        },
        onBuild = { owner, repo, branch, app ->
            val t = token ?: run { log = "Authorize first"; return@NeoStore }
            val tag = "build-${app.id}-${UUID.randomUUID()}"
            scope.launch {
                try {
                    log = "Dispatching build for ${app.name}…"
                    gh.dispatchWorkflow(
                        auth = "Bearer $t", owner = owner, repo = repo, workflow = app.workflow,
                        body = DispatchBody(ref = branch, inputs = mapOf(
                            "app_id" to app.id,
                            "inject_json" to moshi.adapter(Map::class.java).toJson(app.inject),
                            "release_tag" to tag,
                            "catalog_ref" to (catalog?.title ?: "")
                        ))
                    )
                    log = "Build started. Waiting for $tag…"
                    while (true) {
                        delay(8000)
                        try {
                            val rel = gh.getReleaseByTag("Bearer $t", owner, repo, tag)
                            val asset = rel.assets.firstOrNull { it.name.endsWith(".apk") }
                            if (asset != null) {
                                val req = DownloadManager.Request(Uri.parse(asset.browser_download_url))
                                    .setTitle("${app.name}.apk")
                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                dm.enqueue(req)
                                log = "Downloading ${asset.name}…"
                                break
                            }
                        } catch (_: Exception) { /* keep polling */ }
                    }
                } catch (e: Exception) { log = e.toString() }
            }
        }
    )
}
