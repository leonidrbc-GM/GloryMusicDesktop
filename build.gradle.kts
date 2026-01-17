import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.3"
}

group = "ru.ibelieve.glorymusic25"
version = "1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("com.googlecode.soundlibs:jlayer:1.0.1.4")
    implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
}

compose.desktop {
    application {
        mainClass = "ru.ibelieve.glorymusic25.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            
            packageName = "GloryMusic"
            packageVersion = project.version.toString()
            vendor = "Ibelieve"
            
            windows {
                iconFile.set(project.file("gm_icon.ico"))
            }
            
            includeAllModules = true
        }
    }
}
