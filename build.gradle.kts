import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

group = "ru.ibelieve.glorymusic25"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

// Настройка путей для desktop структуры
sourceSets {
    main {
        kotlin.srcDirs("src/jvmMain/kotlin")
        resources.srcDirs("src/jvmMain/resources")
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.components.resources)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(compose.desktop.currentOs)

    // Поддержка MP3
    implementation("com.googlecode.soundlibs:jlayer:1.0.1.4")
    implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
    implementation("com.googlecode.soundlibs:tritonus-share:0.3.7.4")
}

compose.desktop {
    application {
        mainClass = "ru.ibelieve.glorymusic25.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, // Установщик Windows
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb  // Установщик Linux (Ubuntu/Debian)
            )
            packageName = "GloryMusic"
            packageVersion = "1.0.0"

            linux {
                shortcut = true // Создать ярлык в меню
                menuGroup = "Music"
            }
            windows {
                shortcut = true
                iconFile.set(project.file("icon.ico")) // Если есть иконка
                // Для создания MSI на Windows потребуется WiX Toolset
            }
        }
    }
}

tasks.register<Jar>("fatJar") {
    group = "build"
    manifest {
        attributes["Main-Class"] = "ru.ibelieve.glorymusic25.MainKt"
    }
    from(sourceSets.main.get().output)
    val runtimeClasspath = configurations.runtimeClasspath.get()
    from({
        runtimeClasspath.filter { it.exists() }.map { if (it.isDirectory) it else zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    archiveFileName.set("GloryMusic.jar")
}

tasks.register<Copy>("collectProductionArtifacts") {
    group = "build"
    dependsOn("fatJar")
    val productionDir = layout.projectDirectory.dir("production").asFile
    into(productionDir)
    doFirst {
        if (productionDir.exists()) productionDir.deleteRecursively()
        productionDir.mkdirs()
    }
    from(layout.buildDirectory.dir("libs")) { include("GloryMusic.jar") }
    from(layout.projectDirectory.dir("music_data")) { into("music_data") }
}