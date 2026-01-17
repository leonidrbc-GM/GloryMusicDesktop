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
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Exe, // Добавлен EXE формат
                TargetFormat.Deb,
                TargetFormat.Rpm // Можно добавить RPM для Linux
            )
            packageName = "GloryMusic26"
            packageVersion = project.version.toString()
            vendor = "Ibelieve"
            description = "Music Player for Christian Music"
            copyright = "© 2024 Ibelieve. All rights reserved."
            licenseFile.set(project.file("LICENSE")) // Создай файл LICENSE в корне проекта

            // Windows настройки
            windows {
                shortcut = true
                menu = true
                menuGroup = "Sound & Video"
                // Для EXE и MSI можно использовать разные иконки
                iconFile.set(project.file("src/jvmMain/resources/icons/gm_icon.ico"))

                // Дополнительные параметры для EXE
                perUserInstall = true
                dirChooser = true

                // Для создания MSI потребуется WiX Toolset
                msi {
                    // Уникальный идентификатор приложения (генерируй новый для каждой версии)
                    upgradeUuid = "A7F5A3B2-1D4C-4E8F-B9C7-6D1234567890"
                }

                // Настройки для EXE (используется Inno Setup)
                exe {
                    // Можно настроить разные параметры для EXE
                }
            }

            // Linux настройки
            linux {
                shortcut = true
                menuGroup = "Music"
                iconFile.set(project.file("src/jvmMain/resources/icons/gm_icon.png"))
            }

            // macOS настройки
            macOS {
                dockName = "Glory Music"
                iconFile.set(project.file("src/jvmMain/resources/icons/gm_icon.icns")) // Нужен .icns файл
                bundleID = "ru.ibelieve.glorymusic25"
            }

            // Общие настройки для всех платформ
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))

            // Включаем JRE в установщик (увеличит размер, но не требует установки Java у пользователя)
            includeAllModules = true

            // Настройки модулей Java
            modules {
                // Обязательные модули для Compose Desktop
                module("java.base")
                module("java.desktop")
                module("java.sql")
                module("jdk.unsupported")

                // Модули для Compose
                module("org.jetbrains.compose.runtime")
                module("org.jetbrains.compose.foundation")
                module("org.jetbrains.compose.material3")

                // Модули для Kotlin
                module("org.jetbrains.kotlin.stdlib")
                module("org.jetbrains.kotlinx.coroutines.core")
            }
        }
    }
}

// Задача для создания fatJar
tasks.register<Jar>("fatJar") {
    group = "build"
    manifest {
        attributes["Main-Class"] = "ru.ibelieve.glorymusic25.MainKt"
        attributes["Implementation-Version"] = project.version
        attributes["Created-By"] = "Ibelieve"
    }
    archiveBaseName.set("GloryMusic")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("fat")

    from(sourceSets.main.get().output)
    val runtimeClasspath = configurations.runtimeClasspath.get()
    from({
        runtimeClasspath.filter { it.exists() }.map { if (it.isDirectory) it else zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}

// Задача для сборки всех установщиков
tasks.register("buildAllInstallers") {
    group = "build"
    description = "Build installers for all platforms"
    dependsOn("packageReleaseDistributionForCurrentOS")
}

// Задача для сборки только Windows установщиков
tasks.register("buildWindowsInstallers") {
    group = "build"
    description = "Build Windows installers (MSI and EXE)"
    dependsOn("packageMsiDistributionForCurrentOS", "packageExeDistributionForCurrentOS")
}

// Задача для копирования артефактов в папку production
tasks.register<Copy>("collectProductionArtifacts") {
    group = "build"
    dependsOn("fatJar", "packageReleaseDistributionForCurrentOS")

    val productionDir = layout.projectDirectory.dir("production").asFile
    into(productionDir)

    doFirst {
        if (productionDir.exists()) productionDir.deleteRecursively()
        productionDir.mkdirs()
    }

    // Копируем JAR файл
    from(layout.buildDirectory.dir("libs")) {
        include("GloryMusic*.jar")
    }

    // Копируем установщики
    from(layout.buildDirectory.dir("compose/binaries")) {
        into("installers")
        include("**/*.exe", "**/*.msi", "**/*.deb", "**/*.dmg")
    }

    // Копируем музыкальные данные
    from(layout.projectDirectory.dir("music_data")) {
        into("music_data")
    }

    // Копируем README и лицензию
    from(layout.projectDirectory) {
        include("README.md", "LICENSE")
    }
}

// Задача для создания дистрибутива для GitHub Release
tasks.register<Zip>("createReleasePackage") {
    group = "distribution"
    description = "Create a zip package for GitHub Release"

    dependsOn("collectProductionArtifacts")

    archiveFileName.set("GloryMusic-${project.version}.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    from(layout.projectDirectory.dir("production")) {
        include("**/*")
        exclude("**/.DS_Store")
    }

    doLast {
        println("Release package created: ${archiveFile.get()}")
    }
}