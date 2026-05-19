# 曳影 (Yeying) 输入法实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 基于 Rime 引擎从零构建一个 Android 输入法，支持九键/全键拼音、英文、手写输入，Compose UI + Liquid Glass 主题。

**Architecture:** 四层架构——Compose UI 层、桥接层、引擎层、数据层。InputMethodService 通过 ComposeView 桥接到 Compose 渲染。引擎层封装 Rime JNI 调用，自行编写全部 Kotlin 代码。

**Tech Stack:** Kotlin 2.1.21, Compose BOM 2026.03.01, Material3, Room 2.8.4, DataStore 1.1.4, Lifecycle 2.8.7, Coroutines 1.10.1, minSdk 31, targetSdk 35, compileSdk 35, Java 17

**依赖版本对齐:** `D:/Dev/project/joy-tune/gradle/libs.versions.toml`

---

## Phase 1: 项目脚手架

### Task 1: 创建 Android 项目结构

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts` (root)
- Create: `app/build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `gradle.properties`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/res/xml/method.xml`

- [ ] **Step 1: 创建 Gradle 版本目录**

```toml
# gradle/libs.versions.toml
[versions]
agp = "8.9.2"
kotlin = "2.1.21"
ksp = "2.1.21-2.0.2"
composeBom = "2026.03.01"
room = "2.8.4"
datastore = "1.1.4"
lifecycle = "2.8.7"
coreKtx = "1.16.0"
activityCompose = "1.10.1"
kotlinxCoroutines = "1.10.1"
emojiCompat = "1.1.0"
projectMinSdk = "31"
projectTargetSdk = "35"
projectCompileSdk = "35"

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activityCompose" }
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-material-icons-extended = { module = "androidx.material:material-icons-extended" }
lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-service = { module = "androidx.lifecycle:lifecycle-service", version.ref = "lifecycle" }
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version = "2.5.1" }
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "kotlinxCoroutines" }
emoji-compat = { module = "androidx.emoji2:emoji2", version.ref = "emojiCompat" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
```

- [ ] **Step 2: 创建根 settings.gradle.kts**

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "yeying"
include(":app")
```

- [ ] **Step 3: 创建根 build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}
```

- [ ] **Step 4: 创建 app/build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "dev.yeying.ime"
    compileSdk = libs.versions.projectCompileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.yeying.ime"
        minSdk = libs.versions.projectMinSdk.get().toInt()
        targetSdk = libs.versions.projectTargetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    dependenciesInfo {
        includeInApk = false
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.service)

    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.sqlite.bundled)

    implementation(libs.datastore.preferences)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.emoji.compat)
}
```

- [ ] **Step 5: 创建 gradle.properties**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 6: 创建 AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="false"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.Yeying">

        <service
            android:name=".YeyingImeService"
            android:exported="true"
            android:permission="android.permission.BIND_INPUT_METHOD">
            <intent-filter>
                <action android:name="android.view.InputMethod" />
            </intent-filter>
            <meta-data
                android:name="android.view.im"
                android:resource="@xml/method" />
        </service>

    </application>

</manifest>
```

- [ ] **Step 7: 创建输入法声明文件**

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- app/src/main/res/xml/method.xml -->
<input-method xmlns:android="http://schemas.android.com/apk/res/android"
    android:settingsActivity="dev.yeying.ime.ui.settings.SettingsActivity"
    android:isDefault="false">
    <subtype
        android:label="@string/subtype_label"
        android:imeSubtypeMode="keyboard"
        android:imeSubtypeLocale="zh_CN"
        android:isAsciiCapable="true" />
</input-method>
```

- [ ] **Step 8: 创建资源文件**

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- app/src/main/res/values/strings.xml -->
<resources>
    <string name="app_name">曳影</string>
    <string name="subtype_label">拼音</string>
</resources>
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- app/src/main/res/values/themes.xml -->
<resources>
    <style name="Theme.Yeying" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

- [ ] **Step 9: 验证构建**

Run: `cd D:/Dev/project/ime/yeying && ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL（无 .so 和源码时可能有链接错误，后续 Task 解决）

- [ ] **Step 10: 提交**

```bash
git add settings.gradle.kts build.gradle.kts app/ gradle/ gradle.properties
git commit -m "feat: scaffold Android project with Compose + Room + DataStore"
```

---

### Task 2: 复制原生库

**Files:**
- Copy: `libyuyanime.so` → `app/src/main/jniLibs/arm64-v8a/`
- Copy: `libhandwriting.so` → `app/src/main/jniLibs/arm64-v8a/`
- Copy: `libhwInterface.so` → `app/src/main/jniLibs/arm64-v8a/`
- Copy: `libgpen_handwriter.so` → `app/src/main/jniLibs/arm64-v8a/`
- Copy: `libSogouShell.so` → `app/src/main/jniLibs/arm64-v8a/`

> **策略变更**: JNI 符号均为静态注册（`Java_com_yuyan_inputmethod_core_Rime_*` 等），.so 文件保留原名不作重命名，对应 Kotlin 侧 JNI 声明从 yuyan 直接采纳。仅支持 arm64-v8a（minSdk 31+ 设备全为 64 位）。`libsogou_interface.so` 在 yuyansdk 中缺失，可能需从编译环境补充，或通过调整加载顺序规避。

> **来源**: `D:/Dev/project/ime/yuyansdk/libs/arm64-v8a/`

- [ ] **Step 1: 复制全部 5 个 .so 文件**

从 yuyansdk 编译产物目录复制到 `app/src/main/jniLibs/arm64-v8a/`，文件名保持不变。

- [ ] **Step 2: 验证 .so 加载**

在 Task 4/13 创建 JNI 类时通过 `System.loadLibrary` 验证能正常加载。

- [ ] **Step 3: 提交**

```bash
git add app/src/main/jniLibs/
git commit -m "feat: add native libraries (rime engine + handwriting chain)"
```

---

## Phase 2: 引擎层

### Task 3: Rime 数据结构（采纳 yuyan）

**策略**: JNI 返回值类型必须与 .so 匹配，从 yuyan 的 `Structs.kt` 直接采纳，放置于 `com.yuyan.inputmethod.core` 包。

**Files:**
- Adopt: `app/src/main/java/com/yuyan/inputmethod/core/Structs.kt`

- [ ] **Step 1: 从 yuyan 采纳数据结构**

从 `D:/Dev/project/ime/yuyansdk/src/main/java/com/yuyan/inputmethod/core/Structs.kt` 复制到项目，包含 `RimeContext`、`RimeMenu`、`RimeComposition`、`RimeCommit`、`RimeStatus`、`CandidateItem`、`SchemaItem` 等 JNI 必要类型。

- [ ] **Step 2: 提交**

```bash
git add app/src/main/java/com/yuyan/inputmethod/core/Structs.kt
git commit -m "feat: adopt Rime data structures from yuyan"
```

---

### Task 4: Rime JNI 封装

**策略**: `external fun` 声明因 JNI 静态符号 `Java_com_yuyan_inputmethod_core_Rime_*` 硬编码，从 yuyan 直接采纳，放置于 `com.yuyan.inputmethod.core` 包。高级封装自行编写，位于 `dev.yeying.ime.engine`。

**Files:**
- Adopt: `app/src/main/java/com/yuyan/inputmethod/core/Rime.kt`（从 yuyan 复制）
- Create: `app/src/main/java/dev/yeying/ime/engine/RimeEngine.kt`

- [ ] **Step 1: 采纳 yuyan 的 JNI 桥接文件**

从 `D:/Dev/project/ime/yuyansdk/src/main/java/com/yuyan/inputmethod/core/Rime.kt` 复制到项目同名路径，移除不需要的 associate 接口和 yuyan 特有依赖（CustomConstant/Launcher），其余完整保留。

- [ ] **Step 2: 自行编写 RimeEngine 适配层**

```kotlin
package dev.yeying.ime.engine

import android.content.Context
import com.yuyan.inputmethod.core.Rime
import java.io.File

class RimeEngine private constructor() {

    companion object {
        val instance: RimeEngine by lazy { RimeEngine() }
    }

    var isInitialized = false
        private set

    fun startup(context: Context, fullCheck: Boolean = false) {
        val sharedDir = copyAssetsToShared(context)
        val userDir = File(context.filesDir, "rime/user").apply { mkdirs() }.absolutePath

        Rime.startupRime(context, sharedDir, userDir, fullCheck)
        Rime.setRimePageSize(5)
        isInitialized = true
    }

    fun shutdown() {
        Rime.exitRime()
        isInitialized = false
    }

    fun processKey(keycode: Int, mask: Int = 0): Boolean =
        Rime.processRimeKey(keycode, mask)

    fun replaceKey(caretPos: Int, length: Int, key: String): Boolean =
        Rime.replaceRimeKey(caretPos, length, key)

    fun clearComposition() = Rime.clearRimeComposition()

    fun getContext(): RimeContext? = Rime.getRimeContext()

    fun getCommit(): RimeCommit? = Rime.getRimeCommit()

    fun getStatus(): RimeStatus? = Rime.getRimeStatus()

    fun selectCandidate(index: Int): Boolean =
        Rime.selectRimeCandidate(index)

    fun selectSchema(schemaId: String): Boolean =
        Rime.selectRimeSchema(schemaId)

    fun getCurrentSchema(): String =
        Rime.getCurrentRimeSchema()

    fun setOption(option: String, value: Boolean) =
        Rime.setRimeOption(option, value)

    private fun copyAssetsToShared(context: Context): String {
        val sharedDir = File(context.filesDir, "rime/shared")
        if (!sharedDir.exists()) {
            copyAssetDir(context, "rime", sharedDir)
        }
        return sharedDir.absolutePath
    }

    private fun copyAssetDir(context: Context, assetPath: String, dest: File) {
        val assetManager = context.assets
        val files = assetManager.list(assetPath) ?: return
        dest.mkdirs()
        for (file in files) {
            val src = "$assetPath/$file"
            val dst = File(dest, file)
            if (assetManager.list(src)?.isNotEmpty() == true) {
                copyAssetDir(context, src, dst)
            } else {
                assetManager.open(src).use { input ->
                    dst.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}
```

> `RimeEngine` 作为适配层，内部调用 `com.yuyan.inputmethod.core.Rime` 的 JNI 方法，对外暴露 `dev.yeying.ime` 自有 API。后续所有代码只依赖 `RimeEngine`，不直接接触 `com.yuyan` 包。

- [ ] **Step 3: 提交**

```bash
git add app/src/main/java/com/yuyan/inputmethod/core/Rime.kt
git add app/src/main/java/dev/yeying/ime/engine/RimeEngine.kt
git commit -m "feat: adopt Rime JNI bridge from yuyan, add RimeEngine wrapper"
```


### Task 5: Rime 词库部署

**策略**: 词库采用 rime-ice 雾凇拼音源文件，首次运行时由 Rime 编译为 `.table.bin`。版本号管理增量升级，不覆盖用户自定义文件。

**Files:**
- Copy: rime-ice 词库文件 → `app/src/main/assets/rime/`
- Create: `app/src/main/assets/rime/rime_version.txt`

- [ ] **Step 1: 从 rime-ice 复制词库文件**

来源: `D:/Dev/project/ime/rime-ice/`

```
assets/rime/
  ├─ rime_version.txt               ← 自建，内容 "1"
  ├─ rime_ice.schema.yaml           ← 主拼音方案
  ├─ rime_ice.dict.yaml             ← 主词库入口
  ├─ t9.schema.yaml                 ← T9 方案（继承 rime_ice）
  ├─ melt_eng.schema.yaml           ← 英文方案
  ├─ melt_eng.dict.yaml             ← 英文词库
  ├─ radical_pinyin.schema.yaml     ← 部首查字（rime_ice 依赖）
  ├─ radical_pinyin.dict.yaml       ← 部首词库
  ├─ symbols_v.yaml                 ← v 模式符号
  ├─ default.yaml                   ← 全局配置（自定义）
  ├─ custom_phrase.txt              ← 用户短语模板
  ├─ cn_dicts/
  │    ├─ 8105.dict.yaml            (123KB)
  │    ├─ base.dict.yaml            (17.2MB)
  │    ├─ ext.dict.yaml             (12.3MB)
  │    ├─ tencent.dict.yaml         (18.3MB)
  │    └─ others.dict.yaml          (18KB)
  ├─ en_dicts/
  │    ├─ en.dict.yaml              (387KB)
  │    ├─ en_ext.dict.yaml          (52KB)
  │    └─ cn_en.txt                 (17KB)
  └─ opencc/
       ├─ emoji.json + emoji.txt    (138KB)
       └─ others.txt                (47KB)
```

> **不纳入**: `cn_dicts/41448.dict.yaml`（默认注释掉）、全部 `double_pinyin_*`（7 个双拼方案）、`en_dicts/cn_en_*.txt`（双拼变体）、`others/`（Hamster 等桌面端配置）、`squirrel.yaml` / `weasel.yaml`。

- [ ] **Step 2: 更新 RimeEngine 版本管理逻辑**

```kotlin
// RimeEngine.kt — copyAssetsToShared 改为版本感知

private fun copyAssetsToShared(context: Context): String {
    val sharedDir = File(context.filesDir, "rime/shared")
    val versionFile = File(sharedDir, "rime_version.txt")
    val currentVersion = readAssetVersion(context)

    if (!sharedDir.exists() || readVersion(versionFile) != currentVersion) {
        // 覆盖内置文件，但不删除用户可能放进去的额外文件
        copyAssetDir(context, "rime", sharedDir)
        versionFile.writeText(currentVersion.toString())
    }
    return sharedDir.absolutePath
}

private fun readAssetVersion(context: Context): Int {
    return context.assets.open("rime/rime_version.txt").bufferedReader().use {
        it.readText().trim().toIntOrNull() ?: 1
    }
}

private fun readVersion(file: File): Int {
    return if (file.exists()) file.readText().trim().toIntOrNull() ?: 0 else 0
}
```

> **原则**: 升级时覆盖 shared_dir，user_dir 永不触碰。Rime 首次编译 `.dict.yaml` → `.table.bin` 耗时 30s–2min，后续启动即用。

- [ ] **Step 3: 自定义 default.yaml**

从 rime-ice 的 `default.yaml` 精简，只启用 `rime_ice` 和 `t9` 两个 schema，去掉双拼方案列表。

- [ ] **Step 4: 提交**

```bash
git add app/src/main/assets/rime/
git commit -m "feat: add rime-ice dictionary assets with version management"
```

---
---
### Task 6: T9 拼音映射

**Files:**
- Create: `app/src/main/java/dev/yeying/ime/engine/T9Mapper.kt`

- [ ] **Step 1: 实现 T9 数字→拼音映射**

参考 yuyansdk 的 T9PinYinUtils.kt 的映射规则（约 225 条），自行实现映射逻辑。核心是 T9 数字序列到拼音组合的映射表。

```kotlin
package dev.yeying.ime.engine

object T9Mapper {

    /** 字母 → T9 按键 */
    private val charToT9 = mapOf(
        'a' to '2', 'b' to '2', 'c' to '2',
        'd' to '3', 'e' to '3', 'f' to '3',
        'g' to '4', 'h' to '4', 'i' to '4',
        'j' to '5', 'k' to '5', 'l' to '5',
        'm' to '6', 'n' to '6', 'o' to '6',
        'p' to '7', 'q' to '7', 'r' to '7', 's' to '7',
        't' to '8', 'u' to '8', 'v' to '8',
        'w' to '9', 'x' to '9', 'y' to '9', 'z' to '9',
    )

    /** T9 按键 → 对应字母 */
    private val t9ToChars = mapOf(
        '2' to "abc", '3' to "def", '4' to "ghi",
        '5' to "jkl", '6' to "mno", '7' to "pqrs",
        '8' to "tuv", '9' to "wxyz",
    )

    /**
     * T9 数字序列 → 所有可能的拼音组合
     * 例如 "26" → ["an","ao","am","bo","bn","co","cm"]
     */
    fun t9ToPinyin(t9Sequence: String): Array<String> {
        // 实现映射逻辑：展开所有可能的字母组合，过滤有效拼音
        // 参考 yuyansdk 的 pinyinMap 或使用在线拼音表过滤
        TODO("实现 T9→拼音映射")
    }

    /** 拼音 → T9 数字序列 */
    fun pinyinToT9(pinyin: String): String =
        pinyin.map { charToT9[it.lowercaseChar()] ?: it }.joinToString("")
}
```

> **注意:** `t9ToPinyin` 的完整映射表较大（~225 条规则），实现时需参考 yuyansdk 的 `T9PinYinUtils.pinyinMap` 构建完整的映射数据。可以在代码中硬编码映射表，或从 assets 读取。

- [ ] **Step 2: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/engine/T9Mapper.kt
git commit -m "feat: add T9 pinyin mapping"
```


### Task 7: 核心单元测试

**策略**: 只测自有逻辑，不测 JNI 桥接和 UI 视觉效果。覆盖 T9Mapper 映射和 KeyboardViewModel 状态迁移。

**Files:**
- Create: `app/src/test/java/dev/yeying/ime/engine/T9MapperTest.kt`
- Create: `app/src/test/java/dev/yeying/ime/ui/keyboard/KeyboardViewModelTest.kt`

- [ ] **Step 1: 添加测试依赖**

在 `app/build.gradle.kts` 中添加：

```kotlin
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("app.cash.turbine:turbine:1.0.0")
}
```

- [ ] **Step 2: T9Mapper 测试**

```kotlin
package dev.yeying.ime.engine

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class T9MapperTest {

    @Test
    fun `single key D returns e d f`() {
        val result = T9Mapper.t9ToPinyin("D")
        assertArrayEquals(arrayOf("e", "d", "f"), result)
    }

    @Test
    fun `sequence ADG returns bei ben etc`() {
        val result = T9Mapper.t9ToPinyin("ADG")
        assertTrue(result.contains("bei"))
        assertTrue(result.contains("ben"))
    }

    @Test
    fun `pinyin to T9 reverse mapping`() {
        assertEquals("ADG", T9Mapper.pinyinToT9("bei"))
    }

    @Test
    fun `empty sequence returns empty`() {
        val result = T9Mapper.t9ToPinyin("")
        assertEquals(0, result.size)
    }
}
```

- [ ] **Step 3: KeyboardViewModel 状态迁移测试**

```kotlin
package dev.yeying.ime.ui.keyboard

import app.cash.turbine.test
import dev.yeying.ime.engine.RimeEngine
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class KeyboardViewModelTest {

    @Test
    fun `switch keyboard updates state`() = runTest {
        val vm = KeyboardViewModel()
        vm.state.test {
            assertEquals(KeyboardType.QWERTY, awaitItem().activeKeyboard)
            vm.onAction(KeyboardAction.SwitchKeyboard(KeyboardType.T9))
            assertEquals(KeyboardType.T9, awaitItem().activeKeyboard)
        }
    }

    @Test
    fun `clear composition action`() = runTest {
        val vm = KeyboardViewModel()
        vm.onAction(KeyboardAction.ClearComposition)
        val state = vm.state.value
        assertEquals("", state.composingText)
        assertTrue(state.candidates.isEmpty())
    }
}
```

> **不测**: Rime JNI 调用（二进制接口）、Compose UI 布局（Preview 足够）、视觉效果。

- [ ] **Step 4: 提交**

```bash
git add app/src/test/
git commit -m "test: add core unit tests for T9Mapper and KeyboardViewModel"
```

---
---

## Phase 3: 桥接层

### Task 8: InputMethodService + ComposeView 桥接

**Files:**
- Create: `app/src/main/java/dev/yeying/ime/YeyingImeService.kt`
- Create: `app/src/main/java/dev/yeying/ime/bridge/ComposeBridge.kt`

- [ ] **Step 1: 创建 ComposeBridge**

```kotlin
package dev.yeying.ime.bridge

import android.view.View
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class ComposeBridge : LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun onStart() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    fun createComposeView(
        composeView: ComposeView,
        content: @Composable () -> Unit,
    ): View {
        composeView.setViewTreeLifecycleOwner(this)
        composeView.setViewTreeSavedStateRegistryOwner(this)
        composeView.setContent(content)
        return composeView
    }
}
```

- [ ] **Step 2: 创建 YeyingImeService**

```kotlin
package dev.yeying.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import dev.yeying.ime.bridge.ComposeBridge
import dev.yeying.ime.engine.RimeEngine

class YeyingImeService : InputMethodService() {

    private val bridge = ComposeBridge()
    private lateinit var composeView: ComposeView

    override fun onCreate() {
        super.onCreate()
        bridge.onCreate()
        RimeEngine.instance.startup(this)
    }

    override fun onCreateInputView(): View {
        composeView = ComposeView(this)
        bridge.onStart()
        return bridge.createComposeView(composeView) {
            YeyingKeyboard()
        }
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        // 通知 UI 当前输入框类型，调整键盘状态
    }

    override fun onDestroy() {
        bridge.onDestroy()
        RimeEngine.instance.shutdown()
        super.onDestroy()
    }
}
```

- [ ] **Step 3: 创建占位 YeyingKeyboard Composable**

```kotlin
package dev.yeying.ime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun YeyingKeyboard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("曳影输入法")
    }
}
```

- [ ] **Step 4: 验证构建和运行**

Run: `./gradlew assembleDebug`
安装到设备后在系统设置中启用输入法，切换后应显示「曳影输入法」文字。

- [ ] **Step 5: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/
git commit -m "feat: add InputMethodService with Compose bridge"
```

---

## Phase 4: 主题系统

### Task 9: Liquid Glass 效果

**Files:**
- Create: `app/src/main/java/dev/yeying/ime/ui/theme/LiquidGlass.kt`
- Create: `app/src/main/java/dev/yeying/ime/ui/theme/Theme.kt`

- [ ] **Step 1: 实现 Modifier.liquidGlass()**

参考 `D:/Dev/project/GitHub-Store/composeApp/src/commonMain/kotlin/zed/rainxch/githubstore/app/navigation/BottomNavigation.kt` 的 drawBehind 实现。

```kotlin
package dev.yeying.ime.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class GlassParams(
    val baseAlpha: Float = 0.90f,
    val glassHighAlpha: Float = 0.30f,
    val glassLowAlpha: Float = 0.10f,
    val specularAlpha: Float = 0.45f,
    val innerGlowAlpha: Float = 0.08f,
    val borderAlpha: Float = 0.08f,
)

fun Modifier.liquidGlass(
    cornerRadius: Dp = 16.dp,
    isDark: Boolean = false,
    params: GlassParams = if (isDark) GlassParams(
        glassHighAlpha = 0.12f,
        glassLowAlpha = 0.04f,
        specularAlpha = 0.18f,
        innerGlowAlpha = 0.03f,
    ) else GlassParams(),
): Modifier = this.drawBehind {
    val r = CornerRadius(cornerRadius.toPx())
    val w = size.width
    val h = size.height

    // 1. 底色（高 alpha，遮住底层）
    drawRoundRect(
        color = Color.White.copy(alpha = params.baseAlpha),
        cornerRadius = r,
    )

    // 2. 主体渐变
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = params.glassHighAlpha),
                Color.White.copy(alpha = params.glassLowAlpha),
            )
        ),
        cornerRadius = r,
    )

    // 3. 高光条（顶部）
    drawRoundRect(
        brush = Brush.horizontalGradient(
            listOf(
                Color.Transparent,
                Color.White.copy(alpha = params.specularAlpha),
                Color.Transparent,
            ),
            startX = w * 0.15f,
            endX = w * 0.85f,
        ),
        topLeft = Offset(w * 0.15f, 3.dp.toPx()),
        size = Size(w * 0.7f, 1.5.dp.toPx()),
        cornerRadius = CornerRadius(1.dp.toPx()),
    )

    // 4. 内发光（底部）
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color.Transparent, Color.White.copy(alpha = params.innerGlowAlpha))
        ),
        topLeft = Offset(4.dp.toPx(), h - 8.dp.toPx()),
        size = Size(w - 8.dp.toPx(), 4.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx()),
    )

    // 5. 边框（仅深色主题）
    if (isDark && params.borderAlpha > 0f) {
        drawRoundRect(
            color = Color.White.copy(alpha = params.borderAlpha),
            cornerRadius = r,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
        )
    }
}
```

- [ ] **Step 2: 创建主题 Composable**

```kotlin
package dev.yeying.ime.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    surface = Color(0xFFF5F5F5),
    onSurface = Color(0xFF1C1B1F),
    surfaceContainer = Color.White.copy(alpha = 0.90f),
)

private val DarkColors = darkColorScheme(
    surface = Color(0xFF2B2B2B),
    onSurface = Color(0xFFE6E1E5),
    surfaceContainer = Color.Black.copy(alpha = 0.90f),
)

@Composable
fun YeyingTheme(
    isDark: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors,
        content = content,
    )
}
```

- [ ] **Step 3: 在 YeyingKeyboard 中应用主题**

更新 YeyingImeService 中的 `bridge.createComposeView` 调用：

```kotlin
return bridge.createComposeView(composeView) {
    YeyingTheme {
        YeyingKeyboard()
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/ui/theme/
git commit -m "feat: add Liquid Glass modifier and theme"
```

---

## Phase 5: 基础键盘 UI
## Phase 5: 输入管线 + 基础键盘 UI

### Task 10: 输入管线 ViewModel

**职责**: 创建 `KeyboardViewModel`，作为输入法核心逻辑的唯一持有者。所有 UI 事件通过 `KeyboardAction` 流入，处理后更新 `KeyboardState`，Compose 自动重组。

**Files:**
- Create: `app/src/main/java/dev/yeying/ime/ui/keyboard/KeyboardState.kt`
- Create: `app/src/main/java/dev/yeying/ime/ui/keyboard/KeyboardViewModel.kt`

- [ ] **Step 1: 定义状态类型和 Action**

```kotlin
package dev.yeying.ime.ui.keyboard

import dev.yeying.ime.engine.CandidateItem

enum class KeyboardType { T9, QWERTY, ENGLISH, SYMBOL, HANDWRITING }
enum class CapsState { NONE, ONCE, LOCK }

data class KeyboardState(
    val activeKeyboard: KeyboardType = KeyboardType.QWERTY,
    val candidates: List<CandidateItem> = emptyList(),
    val composingText: String = "",
    val capsState: CapsState = CapsState.NONE,
    val symbolPage: Int = 0,
    val hasNextPage: Boolean = false,
    val page: Int = 0,
)

sealed class KeyboardAction {
    data class KeyPress(val keycode: Int, val mask: Int = 0) : KeyboardAction()
    data class CandidateSelect(val index: Int) : KeyboardAction()
    data class SwitchKeyboard(val type: KeyboardType) : KeyboardAction()
    data object ClearComposition : KeyboardAction()
}
```

- [ ] **Step 2: 实现 KeyboardViewModel**

```kotlin
package dev.yeying.ime.ui.keyboard

import androidx.lifecycle.ViewModel
import dev.yeying.ime.engine.RimeEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class KeyboardViewModel : ViewModel() {

    private val _state = MutableStateFlow(KeyboardState())
    val state: StateFlow<KeyboardState> = _state

    fun onAction(action: KeyboardAction) {
        when (action) {
            is KeyboardAction.KeyPress -> handleKeyPress(action)
            is KeyboardAction.CandidateSelect -> handleCandidateSelect(action)
            is KeyboardAction.SwitchKeyboard -> handleSwitchKeyboard(action)
            is KeyboardAction.ClearComposition -> handleClearComposition()
        }
    }

    private fun handleKeyPress(action: KeyboardAction.KeyPress) {
        val engine = RimeEngine.instance
        if (!engine.isInitialized) return

        engine.processKey(action.keycode, action.mask)

        val commit = engine.getCommit()
        if (commit != null && commit.commitText.isNotEmpty()) {
            // TODO: 通过 IME service 提交文本 (Phase 3 实现)
            engine.clearComposition()
        }

        refreshState()
    }

    private fun handleCandidateSelect(action: KeyboardAction.CandidateSelect) {
        RimeEngine.instance.selectCandidate(action.index)
        val commit = RimeEngine.instance.getCommit()
        if (commit != null && commit.commitText.isNotEmpty()) {
            // TODO: 提交文本
        }
        refreshState()
    }

    private fun handleSwitchKeyboard(action: KeyboardAction.SwitchKeyboard) {
        _state.update { it.copy(activeKeyboard = action.type) }
    }

    private fun handleClearComposition() {
        RimeEngine.instance.clearComposition()
        refreshState()
    }

    private fun refreshState() {
        val ctx = RimeEngine.instance.getContext()
        val status = RimeEngine.instance.getStatus()
        _state.update { s ->
            s.copy(
                candidates = ctx?.candidates?.toList() ?: emptyList(),
                composingText = ctx?.composition?.preedit ?: "",
                hasNextPage = ctx?.menu?.isLastPage == false,
                page = ctx?.menu?.pageNo ?: 0,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel 销毁时不清除 RimeEngine，它由 IME Service 生命周期管理
    }
}
```

> **注意**: `commitText()` 调用依赖 `InputMethodService.currentInputConnection`，在 Compose 侧通过回调传递给 ViewModel。具体桥接方式在 Task 6 中实现。

- [ ] **Step 3: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/ui/keyboard/KeyboardState.kt
git add app/src/main/java/dev/yeying/ime/ui/keyboard/KeyboardViewModel.kt
git commit -m "feat: add input pipeline ViewModel"
```

---

### Task 11: 全键 QWERTY 键盘

**职责**: 纯 UI Composable，接收 `KeyboardViewModel`，画按键布局，发 `KeyboardAction.KeyPress`。不做任何引擎调用或状态管理。

**Files:**
- Create: `app/src/main/java/dev/yeying/ime/ui/keyboard/KeyLayout.kt`
- Create: `app/src/main/java/dev/yeying/ime/ui/keyboard/QwertyKeyboard.kt`

- [ ] **Step 1: 定义按键数据和布局**

```kotlin
package dev.yeying.ime.ui.keyboard

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KeyDef(
    val label: String,
    val code: Int,
    val width: Dp = 0.dp, // 0 = 弹性宽度
    val isRepeatable: Boolean = false,
)

object QwertyLayout {

    val row1 = listOf(
        KeyDef("q", 'q'.code), KeyDef("w", 'w'.code),
        KeyDef("e", 'e'.code), KeyDef("r", 'r'.code),
        KeyDef("t", 't'.code), KeyDef("y", 'y'.code),
        KeyDef("u", 'u'.code), KeyDef("i", 'i'.code),
        KeyDef("o", 'o'.code), KeyDef("p", 'p'.code),
    )

    val row2 = listOf(
        KeyDef("a", 'a'.code), KeyDef("s", 's'.code),
        KeyDef("d", 'd'.code), KeyDef("f", 'f'.code),
        KeyDef("g", 'g'.code), KeyDef("h", 'h'.code),
        KeyDef("j", 'j'.code), KeyDef("k", 'k'.code),
        KeyDef("l", 'l'.code),
    )

    val row3 = listOf(
        KeyDef("⇧", KEYCODE_SHIFT),
        KeyDef("z", 'z'.code), KeyDef("x", 'x'.code),
        KeyDef("c", 'c'.code), KeyDef("v", 'v'.code),
        KeyDef("b", 'b'.code), KeyDef("n", 'n'.code),
        KeyDef("m", 'm'.code),
        KeyDef("⌫", KEYCODE_DELETE, isRepeatable = true),
    )

    val row4 = listOf(
        KeyDef("?123", KEYCODE_SYMBOL),
        KeyDef(",", KEYCODE_COMMA, width = 40.dp),
        KeyDef("空格", KEYCODE_SPACE = 32),
        KeyDef(".", KEYCODE_PERIOD, width = 40.dp),
        KeyDef("↵", KEYCODE_ENTER),
    )

    val rows = listOf(row1, row2, row3, row4)
}
```
// 特殊功能键常量 — 负值不会被 Rime 处理，在 KeyboardViewModel 中拦截
const val KEYCODE_SHIFT = -1
const val KEYCODE_DELETE = -2
const val KEYCODE_SYMBOL = -3


> **编码策略**: KeyDef.code 直接存储 Rime 所需的字符码点。字母键存 ASCII 小写（`'q'.code`=113），T9 分组键存 ASCII 大写（`'D'.code`=68）。特殊功能键用负值：SHIFT=-1, DELETE=-2, SYMBOL=-3。无需 Android KeyEvent 映射层。

- [ ] **Step 2: 实现 QWERTY 键盘 Composable**

```kotlin
package dev.yeying.ime.ui.keyboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QwertyKeyboard(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        QwertyLayout.rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { key ->
                    KeyButton(
                        key = key,
                        onClick = { viewModel.onAction(KeyboardAction.KeyPress(key.code)) },
                        modifier = Modifier.weight(if (key.width == 0.dp) 1f else 0f),
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    key: KeyDef,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .then(if (key.width > 0.dp) Modifier.width(key.width) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = key.label, fontSize = 18.sp)
    }
}
```

> **注意**: `KeyButton` 为简单占位，完整实现（点击态、Liquid Glass 背景、按键振动）在后续 Task 中补充。

- [ ] **Step 3: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/ui/keyboard/KeyLayout.kt
git add app/src/main/java/dev/yeying/ime/ui/keyboard/QwertyKeyboard.kt
git commit -m "feat: add QWERTY keyboard layout and composable"
```

---

### Task 12: 候选词栏

**职责**: 纯 UI Composable，显示 composing text 和候选词列表，点击候选词发 `KeyboardAction.CandidateSelect`。

**Files:**
- Create: `app/src/main/java/dev/yeying/ime/ui/candidate/CandidateBar.kt`

- [ ] **Step 1: 实现候选词栏**

```kotlin
package dev.yeying.ime.ui.candidate

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.yeying.ime.engine.CandidateItem
import dev.yeying.ime.ui.keyboard.KeyboardViewModel
import dev.yeying.ime.ui.keyboard.KeyboardAction

@Composable
fun CandidateBar(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.composingText.isNotEmpty()) {
            Text(
                text = state.composingText,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.candidates.forEachIndexed { index, candidate ->
                Text(
                    text = "${index + 1}. ${candidate.text}",
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        viewModel.onAction(KeyboardAction.CandidateSelect(index))
                    }
                )
            }
        }
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/ui/candidate/CandidateBar.kt
git commit -m "feat: add candidate bar"
```

---
## Phase 6: T9 九键键盘

### Task 13: T9 键盘 UI

**Files:**
- Create: `app/src/main/java/dev/yeying/ime/ui/keyboard/T9Keyboard.kt`

- [ ] **Step 1: 定义 T9 布局和按键**

```kotlin
package dev.yeying.ime.ui.keyboard

object T9Layout {
    data class T9Key(
        val label: String,
        val subLabel: String = "",
        val code: Int,
    )

    val row1 = listOf(
        T9Key("1", "符号", KEYCODE_SYMBOL),
        T9Key("'", "'", KEYCODE_QUOTE),
        T9Key("ABC", "2", KEYCODE_T9_2),
        T9Key("DEF", "3", KEYCODE_T9_3),
        T9Key("⌫", "删除", KEYCODE_DELETE),
    )

    val row2 = listOf(
        T9Key("GHI", "4", KEYCODE_T9_4),
        T9Key("JKL", "5", KEYCODE_T9_5),
        T9Key("MNO", "6", KEYCODE_T9_6),
        T9Key("清除", "", KEYCODE_CLEAR),
    )

    val row3 = listOf(
        T9Key("PQRS", "7", KEYCODE_T9_7),
        T9Key("TUV", "8", KEYCODE_T9_8),
        T9Key("WXYZ", "9", KEYCODE_T9_9),
        T9Key("⇧", "", KEYCODE_SHIFT),
    )

    val rows = listOf(row1, row2, row3)
}

const val KEYCODE_T9_2 = 'A'.code  // ABC/2
const val KEYCODE_T9_3 = 'D'.code  // DEF/3
const val KEYCODE_T9_4 = 'G'.code  // GHI/4
const val KEYCODE_T9_5 = 'J'.code  // JKL/5
const val KEYCODE_T9_6 = 'M'.code  // MNO/6
const val KEYCODE_T9_7 = 'P'.code  // PQRS/7
const val KEYCODE_T9_8 = 'T'.code  // TUV/8
const val KEYCODE_T9_9 = 'W'.code  // WXYZ/9
const val KEYCODE_QUOTE = '"'.code
const val KEYCODE_CLEAR = -10
```

- [ ] **Step 2: 实现 T9 键盘 Composable**

布局为 3 行按键，参考 yuyansdk T9TextContainer 的布局。每个按键显示主标签（字母组）和副标签（数字）。

- [ ] **Step 3: 接入 T9 输入链路**

按键事件通过 T9Mapper.t9ToPinyin() 获取候选拼音 → 用户选择 → RimeEngine.replaceKey() → 获取候选词。

- [ ] **Step 4: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/ui/keyboard/T9Keyboard.kt
git commit -m "feat: add T9 keyboard layout and input chain"
```

---

## Phase 7: 符号/Emoji 面板

### Task 14: 符号面板

**Files:**
- Create: `app/src/main/java/dev/yeying/ime/ui/symbol/SymbolPanel.kt`

- [ ] **Step 1: 定义符号分类和数据**

符号分页：常用符号、中文标点、英文标点、Emoji 表情、特殊符号。每页为 Grid 布局。

- [ ] **Step 2: 实现 SymbolPanel Composable**

Grid 布局 + 分类切换 Tab + Liquid Glass 背景。

- [ ] **Step 3: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/ui/symbol/
git commit -m "feat: add symbol and emoji panel"
```

---

## Phase 8: 手写输入

### Task 15: 手写输入（采纳 yuyan + 搜狗 .so 链）

**策略**: 手写链路涉及 `libhandwriting.so` → `libhwInterface.so` → `libgpen_handwriter.so` + `libSogouShell.so` 等 5 个 .so，依赖关系复杂。JNI 声明从 yuyan 直接采纳（`com.yuyan.inputmethod.core.HandWriting`），HandwritingEngine 封装层自行编写。

**Files:**
- Adopt: `app/src/main/java/com/yuyan/inputmethod/core/HandWriting.kt`（从 yuyan 复制）
- Create: `app/src/main/java/dev/yeying/ime/engine/HandwritingEngine.kt`
- Create: `app/src/main/java/dev/yeying/ime/ui/keyboard/HandwritingBoard.kt`

> **.so 依赖链**: `libhandwriting.so` → `libhwInterface.so` → `libgpen_handwriter.so` + `libSogouShell.so`。注意 `libhwInterface.so` 还依赖 `libsogou_interface.so`（yuyansdk 中缺失），实际加载时可能需要调整顺序或从编译环境补充。

- [ ] **Step 1: 采纳 yuyan 的手写 JNI 桥接**

从 `D:/Dev/project/ime/yuyansdk/src/main/java/com/yuyan/inputmethod/core/HandWriting.kt` 复制到项目同名路径。该文件含 `System.loadLibrary("handwriting")` 和全部手写 JNI 声明（`initWithDirectory`、`inputHWPoints`、`getCandidates` 等）。

- [ ] **Step 2: 自行编写 HandwritingEngine 封装**

```kotlin
package dev.yeying.ime.engine

import android.content.Context
import com.yuyan.inputmethod.core.HandWriting

class HandwritingEngine {
    var isInitialized = false
        private set

    fun init(context: Context): Boolean {
        if (isInitialized) return true
        val result = HandWriting.initWithDirectory(
            context,
            context.getExternalFilesDir("hw").toString()
        )
        isInitialized = result
        return result
    }

    fun recognize(points: IntArray): List<List<String>> {
        HandWriting.inputHWPoints(points)
        val raw = HandWriting.getCandidates()
        return raw?.mapNotNull { it?.filterNotNull() }?.filter { it.isNotEmpty() } ?: emptyList()
    }

    fun reset() = HandWriting.reset()
    fun release() = HandWriting.release()
}
```

> `HandwritingEngine` 封装了手写 JNI 调用，转换为 `dev.yeying.ime` 自有类型。后续 UI 层只依赖此封装。

- [ ] **Step 3: 实现 HandwritingBoard Composable**

使用 Compose Canvas 绘制手写轨迹（`pointerInput` + `detectDragGestures`），将触摸点收集为 `IntArray`，调用 `HandwritingEngine.recognize()` 获取候选词并展示。

- [ ] **Step 4: 提交**

```bash
git add app/src/main/java/com/yuyan/inputmethod/core/HandWriting.kt
git add app/src/main/java/dev/yeying/ime/engine/HandwritingEngine.kt
git add app/src/main/java/dev/yeying/ime/ui/keyboard/HandwritingBoard.kt
git commit -m "feat: adopt handwriting JNI bridge from yuyan, add HandwritingEngine and board"
```

---
### Task 16: 设置页面

**Files:**
- Create: `app/src/main/java/dev/yeying/ime/ui/settings/SettingsActivity.kt`
- Create: `app/src/main/java/dev/yeying/ime/ui/settings/SettingsScreen.kt`
- Create: `app/src/main/java/dev/yeying/ime/data/Prefs.kt`

- [ ] **Step 1: 创建偏好存储**

基于 DataStore，存储键盘偏好（默认键盘类型、主题色调等）。

- [ ] **Step 2: 实现设置页面**

- 键盘设置：默认键盘类型、按键振动/声音
- 词库管理：导入方案、重新部署、恢复默认
- 主题：浅色/深色、色调选择
- 关于：版本信息

- [ ] **Step 3: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/ui/settings/ app/src/main/java/dev/yeying/ime/data/Prefs.kt
git commit -m "feat: add settings page with dictionary management"
```

---

## Phase 10: 剪贴板

### Task 17: 剪贴板管理

**Files:**
- Create: `app/src/main/java/dev/yeying/ime/data/clipboard/ClipboardDatabase.kt`
- Create: `app/src/main/java/dev/yeying/ime/data/clipboard/ClipboardItem.kt`
- Create: `app/src/main/java/dev/yeying/ime/data/clipboard/ClipboardDao.kt`

- [ ] **Step 1: 定义 Room 数据库**

```kotlin
@Entity
data class ClipboardItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
)

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM ClipboardItem ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ClipboardItem>>

    @Insert
    suspend fun insert(item: ClipboardItem)

    @Query("DELETE FROM ClipboardItem WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM ClipboardItem")
    suspend fun deleteAll()
}

@Database(entities = [ClipboardItem::class], version = 1)
abstract class ClipboardDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao
}
```

- [ ] **Step 2: 实现剪贴板面板 UI**

在符号面板旁增加剪贴板 Tab，展示历史复制内容，支持点击粘贴和删除。

- [ ] **Step 3: 在 InputMethodService 中监听剪贴板**

- [ ] **Step 4: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/data/clipboard/
git commit -m "feat: add clipboard management with Room DB"
```

---

## 实现顺序总结

```
Phase 1  项目脚手架 (Task 1-2)           → 可构建空 APK
Phase 2  引擎层 (Task 3-6)               → Rime JNI 可调用
Phase 3  桥接层 (Task 8)                  → 输入法可激活并显示 Compose UI
Phase 4  Liquid Glass 主题 (Task 9)       → 视觉效果可用
Phase 5  QWERTY 键盘 + 候选词 (Task 10-12) → 基本中文输入可用 ← 核心里程碑
Phase 6  T9 九键键盘 (Task 13)            → 九键输入可用
Phase 7  符号/Emoji 面板 (Task 14)        → 符号输入可用
Phase 8  手写输入 (Task 15)               → 手写可用
Phase 9  设置页 (Task 16)                 → 可管理词库和偏好
Phase 10 剪贴板管理 (Task 16)             → 剪贴板功能可用
```

每个 Phase 完成后都是一个可测试的里程碑。Phase 5 完成后即为核心可用状态。
