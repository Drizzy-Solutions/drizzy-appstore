package com.memeitizer.appstore.ui
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.memeitizer.appstore.model.Catalog
import com.memeitizer.appstore.model.CatalogApp

data class AppCardModel(val owner:String,val repo:String,val branch:String,val iconUrl:String?,val app:CatalogApp)

@Composable
fun NeoStore(
    title:String,
    catalog: Catalog?,
    authorized:Boolean,
    status:String,
    onLoad:(String)->Unit,
    onAuth:()->Unit,
    onBuild:(String,String,String,CatalogApp)->Unit
) {
    val dark = isSystemInDarkTheme()
    NeoTheme(dark) {
        Box(Modifier.fillMaxSize()) {
            NeonGradientBackdrop()
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    AssistChip(
                        label = { Text(if (authorized) "Connected" else "Authorize GitHub") },
                        onClick = onAuth,
                        leadingIcon = { Icon(if (authorized) Icons.Outlined.LockOpen else Icons.Outlined.Lock, null) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (authorized) MaterialTheme.colorScheme.primary.copy(.18f) else MaterialTheme.colorScheme.secondary.copy(.18f)
                        )
                    )
                }
                Spacer(Modifier.height(10.dp))
                var url by remember { mutableStateOf("") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(url, { url = it }, modifier = Modifier.weight(1f), singleLine = true, placeholder = { Text("Catalog config.json URL") })
                    Spacer(Modifier.width(8.dp))
                    PrimaryGlowButton("Load") { onLoad(url) }
                }
                Spacer(Modifier.height(16.dp))
                val cards = remember(catalog) { catalog?.repos?.flatMap { r -> r.apps.map { a -> AppCardModel(r.owner,r.repo,r.branch,r.icon,a) } } ?: emptyList() }
                LazyVerticalGrid(columns = GridCells.Adaptive(260.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(cards, key = { it.app.id }) { m ->
                        GlassCard(glow = MaterialTheme.colorScheme.secondary) {
                            AsyncImage(
                                model = m.iconUrl ?: "",
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(MaterialTheme.shapes.large),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(m.app.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            if (!m.app.description.isNullOrBlank()) Text(m.app.description ?: "", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                StatusPill(if (authorized) "Ready" else "Auth required",
                                    if (authorized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                                PrimaryGlowButton(if (authorized) "Build & Download" else "Authorize") {
                                    if (authorized) onBuild(m.owner,m.repo,m.branch,m.app) else onAuth()
                                }
                            }
                        }
                    }
                }
            }
            if (status.isNotBlank()) {
                Surface(Modifier.align(Alignment.BottomCenter).padding(12.dp),
                    color = Color(0x99000000), contentColor = Color(0xFFEDEDF7), tonalElevation = 8.dp, shadowElevation = 8.dp, shape = MaterialTheme.shapes.large) {
                    Text(status, Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
