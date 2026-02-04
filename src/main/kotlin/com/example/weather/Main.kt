@file:OptIn(ExperimentalStdlibApi::class)

package com.example.weather

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.prefs.Preferences
import javax.imageio.ImageIO

@Serializable
data class WeatherResp(val weather: List<Weather>, val main: Main) {
    @Serializable data class Weather(val description: String, val icon: String)
    @Serializable data class Main(val temp: Double)
}

suspend fun fetchWeather(client: HttpClient, city: String, apiKey: String): WeatherResp =
    client.get("https://api.openweathermap.org/data/2.5/weather") {
        parameter("q", city)
        parameter("appid", apiKey)
        parameter("units", "metric")
        parameter("lang", "pt")
    }.body()

suspend fun loadIconBitmap(client: HttpClient, iconCode: String): ImageBitmap? {
    // icon loading disabled for quick local run (convert via Skiko if needed).
    return null
}

@Composable
fun WeatherWidget(temp: String, desc: String, lastUpdate: String, icon: ImageBitmap?, onOpenSettings: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(6.dp)
            .clip(RoundedCornerShape(14.dp)),
        elevation = 6.dp,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier
            .padding(8.dp)
            .heightIn(min = 72.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF0F0F5)),
                contentAlignment = Alignment.Center) {
                if (icon != null) {
                    Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(28.dp))
                } else {
                    Text(text = "☀", fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = temp, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1C1E))
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = desc.replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.body2, color = Color(0xFF3C3C43), maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = lastUpdate, style = MaterialTheme.typography.caption, color = Color(0xFF6C6C70))
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF2F2F7))
                .clickable { onOpenSettings() },
                contentAlignment = Alignment.Center) {
                Text("⚙", fontSize = 14.sp)
            }
        }
    }
}  

@Composable
@Preview
fun App(apiKeyEnv: String, initialCity: String, intervalSec: Long) {
    val prefs = Preferences.userRoot().node("com.example.weatherwidget")
    val client = remember {
        HttpClient(CIO) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    }

    var temp by remember { mutableStateOf("--°C") }
    var desc by remember { mutableStateOf("carregando...") }
    var lastUpdate by remember { mutableStateOf("nunca") }
    var iconCode by remember { mutableStateOf<String?>(null) }
    var iconBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // persisted settings
    var city by remember { mutableStateOf(prefs.get("city", initialCity)) }
    var interval by remember { mutableStateOf(prefs.getLong("intervalSec", intervalSec)) }
    var showSettings by remember { mutableStateOf(false) }
    var autostartEnabled by remember { mutableStateOf(false) }
    var statusMsg by remember { mutableStateOf("") }

    LaunchedEffect(iconCode) {
        if (iconCode != null) {
            iconBitmap = loadIconBitmap(client, iconCode!!)
        }
    }

    LaunchedEffect(city, interval, apiKeyEnv) {
        val apiKey = apiKeyEnv
        val mock = System.getenv("WEATHER_MOCK")?.lowercase()?.let { it == "1" || it == "true" } ?: false
        while (true) {
            try {
                if (mock) {
                    // Simulated data for demo
                    temp = "23.4°C"
                    desc = "Ensolarado"
                    lastUpdate = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault()).format(Instant.now())
                } else {
                    val w = fetchWeather(client, city, apiKey)
                    temp = "${"%.1f".format(w.main.temp)}°C"
                    desc = w.weather.firstOrNull()?.description ?: ""
                    val code = w.weather.firstOrNull()?.icon
                    if (code != null) iconCode = code
                    lastUpdate = DateTimeFormatter.ofPattern("HH:mm:ss")
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.now())
                }
            } catch (e: Exception) {
                desc = "erro ao buscar: ${e.message}"
                println("[WeatherWidget] falha ao buscar clima: ${e::class.java.name} - ${e.message}")
                e.printStackTrace()
            }
            delay(interval * 1000)
        }
    }

    MaterialTheme {
        Column(modifier = Modifier
            .padding(6.dp)
            .width(260.dp)) {
            WeatherWidget(temp, desc, lastUpdate, iconBitmap) { showSettings = true }

            if (showSettings) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    Surface(elevation = 12.dp, shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp), modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box(modifier = Modifier
                                .fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFFDDDDDD)))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Cidade") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(value = interval.toString(), onValueChange = { v -> interval = v.toLongOrNull() ?: interval }, label = { Text("Intervalo (s)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                Button(onClick = {
                                    prefs.put("city", city)
                                    prefs.putLong("intervalSec", interval)
                                    statusMsg = "Configurações salvas"
                                    showSettings = false
                                }) {
                                    Text("Salvar")
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(onClick = {
                                    // try to install LaunchAgent for autostart
                                    try {
                                        val appPath = "/Applications/WeatherWidget.app"
                                        val la = java.nio.file.Paths.get(System.getProperty("user.home"), "Library", "LaunchAgents", "com.example.weatherwidget.plist")
                                        val plist = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>Label</key>
  <string>com.example.weatherwidget</string>
  <key>ProgramArguments</key>
  <array>
    <string>$appPath/Contents/MacOS/WeatherWidget</string>
  </array>
  <key>RunAtLoad</key>
  <true/>
</dict>
</plist>
"""
                                        java.nio.file.Files.createDirectories(la.parent)
                                        java.nio.file.Files.writeString(la, plist)
                                        // try to load
                                        val proc = ProcessBuilder("launchctl", "unload", la.toString()).inheritIO().start()
                                        proc.waitFor()
                                        val p2 = ProcessBuilder("launchctl", "load", "-w", la.toString()).inheritIO().start()
                                        p2.waitFor()
                                        autostartEnabled = true
                                        statusMsg = "Autostart instalado (verifique se o .app está em /Applications)"
                                    } catch (e: Exception) {
                                        statusMsg = "Falha ao instalar autostart: ${e.message}"
                                    }
                                }) {
                                    Text("Instalar autostart")
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(onClick = {
                                    showSettings = false
                                }) {
                                    Text("Fechar")
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            if (statusMsg.isNotEmpty()) Text(statusMsg, style = MaterialTheme.typography.caption)
                        }
                    }
                }
            }
        }
    }
}

fun main() = application {
    val mockMode = System.getenv("WEATHER_MOCK")?.lowercase()?.let { it == "1" || it == "true" } ?: false
    val apiKey = if (mockMode) {
        println("[WeatherWidget] running in MOCK mode")
        System.getenv("OPENWEATHER_API_KEY") ?: ""
    } else {
        System.getenv("OPENWEATHER_API_KEY") ?: run {
            println("ERROR: set OPENWEATHER_API_KEY in environment or set WEATHER_MOCK=1 to run mock")
            return@application
        }
    }
    val city = System.getenv("WEATHER_CITY") ?: "São Paulo"
    val interval = (System.getenv("WEATHER_UPDATE_INTERVAL")?.toLongOrNull() ?: 60L)

    Window(onCloseRequest = ::exitApplication, undecorated = true, transparent = true) {
        // Force always-on-top via AWT once the window is created
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(200)
            java.awt.Window.getWindows().find { it.isVisible }?.isAlwaysOnTop = true
        }
        App(apiKey, city, interval)
    }
}
