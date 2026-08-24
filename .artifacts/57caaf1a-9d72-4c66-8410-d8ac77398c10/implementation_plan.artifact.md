# Поддержка 16KB Page Size

Для обеспечения совместимости с Android 15 и устройствами с размером страницы 16KB, необходимо обновить Android Gradle Plugin (AGP), версию Gradle и LibGDX (чтобы обновить нативные библиотеки).

## Предложенные изменения

### Инфраструктура Gradle

#### [MODIFY] [gradle-wrapper.properties](file:///H:/StudioProjects/TD/gradle/wrapper/gradle-wrapper.properties)
Обновление Gradle до версии 8.9 для совместимости с новым AGP.

#### [MODIFY] [build.gradle](file:///H:/StudioProjects/TD/build.gradle)
- Обновление AGP до версии 8.5.1.
- Обновление LibGDX до версии 1.14.2.
- Обновление GDX Controllers до версии 2.2.4.

### Модуль Android

#### [MODIFY] [android/build.gradle](file:///H:/StudioProjects/TD/android/build.gradle)
- Обновление `compileSdk` и `targetSdkVersion` до 35.
- Переход на современный блок `packaging` вместо `packagingOptions`.
- Убедиться, что нативные библиотеки не сжимаются (поведение по умолчанию в AGP 8.5.1+ для 16KB).

## План верификации

### Автоматизированные тесты
- Выполнение `./gradlew :android:assembleDebug` для проверки успешности сборки.
- Проверка выравнивания APK с помощью `zipalign -c -P 16 -v 4` (если инструмент доступен).

### Ручная верификация
- Запуск приложения на эмуляторе с поддержкой 16KB (если доступен).
