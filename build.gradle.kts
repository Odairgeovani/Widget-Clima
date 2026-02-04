plugins {
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.compose") version "1.5.2"
    kotlin("plugin.serialization") version "1.9.10"
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("io.ktor:ktor-client-cio:2.3.4")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

compose.desktop {
    application {
        mainClass = "com.example.weather.MainKt"
        nativeDistributions {
            macOS {
                iconFile.set(project.file("src/main/resources/WeatherWidget.icns"))
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}