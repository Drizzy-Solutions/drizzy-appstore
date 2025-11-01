package com.memeitizer.appstore

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.memeitizer.appstore.model.*
import com.memeitizer.appstore.github.*
import com.memeitizer.appstore.auth.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlinx.coroutines.delay
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var catalogUrl by remember { mutableStateOf("") }
    var catalog by remember { mutableStateOf<Catalog?>(null) }
    var log by remember { mutableStateOf("") }
    var token by remember { mutableStateOf<String?>(null) }

    val moshi = remember {
        Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    }
    val http = remember { OkHttpClient() }

    fun retrofit(base:String) = Retrofit.Builder()
        .baseUrl(base)
        .client(http)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val gh = remember { retrofit("https://api.github.com").create(GitHubApi::class.java) }
    val oauth = remember { retrofit("https://github.com").create(OAuthApi::class.java) }

    MaterialTheme {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = catalogUrl,
                onValueChange = { catalogUrl = it },
                label = { Text("Catalog config.json URL") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.padding(top = 8.dp)) {
                Button(onClick = {
                    log = "Fetching catalog..."
                    try {
                        val txt = java.net.URL(catalogUrl).readText()
                        val adapter = moshi.adapter(Catalog::class.java)
                        catalog = adapter.fromJson(txt)
                        log = "Loaded catalog: ${catalog?.title}"
                    } catch (e: Exception) { log = e.toString() }
                }) { Text("Load Catalog") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val clientId = "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" // public, no secret
                    androidx.lifecycle.lifecycleScope.launchWhenCreated {
                        try {
                            val code = oauth.start(clientId)
                            log = "Go to ${code.verificationUri} and enter ${code.userCode}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(code.verificationUri))
                            ctx.startActivity(intent)
                            while (true) {
                                val t = oauth.poll(clientId, code.deviceCode)
                                if (t.accessToken != null) { token = t.accessToken; break }
                                delay((code.interval + 1) * 1000L)
                            }
                            log = "Authorized."
                        } catch (e: Exception) { log = e.toString() }
                    }
                }) { Text(if (token==null) "Authorize GitHub" else "Authorized") }
            }

            Spacer(Modifier.height(12.dp))

            catalog?.repos?.forEach { r ->
                Text(r.displayName, style = MaterialTheme.typography.titleMedium)
                r.apps.forEach { a ->
                    Card(Modifier
                        .padding(vertical = 6.dp)
                        .fillMaxWidth()
                        .clickable {
                            val t = token ?: return@clickable
                            val tag = "build-${a.id}-${UUID.randomUUID()}"
                            androidx.lifecycle.lifecycleScope.launchWhenCreated {
                                try {
                                    log = "Dispatching build for ${a.name}..."
                                    gh.dispatchWorkflow(
                                        auth = "Bearer $t",
                                        owner = r.owner,
                                        repo = r.repo,
                                        workflow = a.workflow,
                                        body = DispatchBody(
                                            ref = r.branch,
                                            inputs = mapOf(
                                                "app_id" to a.id,
                                                "inject_json" to moshi.adapter(Map::class.java).toJson(a.inject),
                                                "release_tag" to tag,
                                                "catalog_ref" to (catalog?.title ?: "")
                                            )
                                        )
                                    )
                                    log = "Build started. Waiting for release $tag..."
                                    while (true) {
                                        delay(8000)
                                        try {
                                            val rel = gh.getReleaseByTag("Bearer $t", r.owner, r.repo, tag)
                                            val asset = rel.assets.firstOrNull { it.name.endsWith(".apk") }
                                            if (asset != null) {
                                                val req = DownloadManager.Request(Uri.parse(asset.browser_download_url))
                                                    .setTitle("${a.name}.apk")
                                                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                                dm.enqueue(req)
                                                log = "Downloading ${asset.name}..."
                                                break
                                            }
                                        } catch (_: Exception) { /* not live yet */ }
                                    }
                                } catch (e: Exception) { log = e.toString() }
                            }
                        }) {
                        Column(Modifier.padding(12.dp)) {
                            Text(a.name, style = MaterialTheme.typography.titleSmall)
                            Text(a.description ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Text("Status: \n$log")
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                ctx.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES))
            }) { Text("Manage install permissions") }
        }
    }
}
