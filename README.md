This is a Kotlin Multiplatform project targeting Desktop (JVM).


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## 📥 Скачать

Последнюю версию можно скачать со [страницы релизов](https://github.com/ваш-юзернейм/GloryMusicDesktop/releases).

**Поддерживаемые платформы:**
- Windows (.exe, .msi)
- Linux (.deb)
- macOS (.dmg)

## 🛠️ Сборка из исходников

Для сборки проекта локально:

1. Установите JDK 17 или выше
2. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/ваш-юзернейм/GloryMusicDesktop.git
   cd GloryMusicDesktop
Соберите проект:

bash
./gradlew build
Создайте установщик для вашей ОС:

bash
# Для Windows
./gradlew packageExeDistributionForCurrentOS

# Для всех платформ
./gradlew buildAllInstallers
text

## 🚀 Пошаговый план выполнения

1. **Создай недостающие файлы:**
   - `LICENSE` в корне проекта
   - `.github/workflows/build.yml`
   - Обнови `README.md`

2. **Подготовь иконки для всех платформ:**
   - Убедись, что есть `gm_icon.ico` (Windows)
   - Создай `gm_icon.icns` (macOS) - можно сконвертировать из PNG
   - `gm_icon.png` уже есть для Linux

3. **Настрой репозиторий на GitHub:**
   - Запушь изменения
   - Убедись, что Actions включены в настройках репозитория

4. **Создай первый релиз:**
   - Создай тег: `git tag -a v1.0.0 -m "First release"`
   - Запушь тег: `git push origin v1.0.0`
   - GitHub Actions автоматически соберёт и создаст релиз

5. **Проверь установщики:**
   - Скачай `.exe` с релиза
   - Протестируй установку на Windows
   - Проверь работу приложения

## 🔧 Дополнительные улучшения (по желанию)

1. **Подпись кода для Windows** (требуется сертификат):
   ```kotlin
   windows {
       signing {
           sign.set(true)
           keyStore.set(project.file("certificate.pfx"))
           keyStorePassword.set(System.getenv("KEYSTORE_PASSWORD"))
           keyAlias.set(System.getenv("KEY_ALIAS"))
           keyPassword.set(System.getenv("KEY_PASSWORD"))
       }
   }
