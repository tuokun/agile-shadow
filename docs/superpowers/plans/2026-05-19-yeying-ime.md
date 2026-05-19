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

---
### Task 5: T9 拼音映射

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

---

## Phase 3: 桥接层

### Task 6: InputMethodService + ComposeView 桥接

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

### Task 7: Liquid Glass 效果

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

### Task 8: 键盘状态管理

**Files:**
- Create: `app/src/main/java/dev/yeying/ime/ui/keyboard/KeyboardState.kt`

- [ ] **Step 1: 定义键盘状态和类型**

```kotlin
package dev.yeying.ime.ui.keyboard

import dev.yeying.ime.engine.CandidateItem

enum class KeyboardType {
    T9, QWERTY, ENGLISH, SYMBOL, HANDWRITING,
}

enum class CapsState {
    NONE, ONCE, LOCK,
}

data class KeyboardState(
    val activeKeyboard: KeyboardType = KeyboardType.QWERTY,
    val candidates: List<CandidateItem> = emptyList(),
    val composingText: String = "",
    val capsState: CapsState = CapsState.NONE,
    val symbolPage: Int = 0,
    val hasNextPage: Boolean = false,
    val page: Int = 0,
)
```

- [ ] **Step 2: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/ui/keyboard/KeyboardState.kt
git commit -m "feat: add keyboard state types"
```

---

### Task 9: 全键 QWERTY 键盘

**Files:**
- Create: `app/src/main/java/dev/yeying/ime/ui/keyboard/QwertyKeyboard.kt`
- Create: `app/src/main/java/dev/yeying/ime/ui/keyboard/KeyLayout.kt`

- [ ] **Step 1: 定义按键数据**

```kotlin
package dev.yeying.ime.ui.keyboard

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KeyDef(
    val label: String,
    val code: Int,
    val width: Dp = 0.dp, // 0 表示弹性宽度
    val isRepeatable: Boolean = false,
)

object QwertyLayout {

    val row1 = listOf(
        KeyDef("q", KEYCODE_Q), KeyDef("w", KEYCODE_W),
        KeyDef("e", KEYCODE_E), KeyDef("r", KEYCODE_R),
        KeyDef("t", KEYCODE_T), KeyDef("y", KEYCODE_Y),
        KeyDef("u", KEYCODE_U), KeyDef("i", KEYCODE_I),
        KeyDef("o", KEYCODE_O), KeyDef("p", KEYCODE_P),
    )

    val row2 = listOf(
        KeyDef("a", KEYCODE_A), KeyDef("s", KEYCODE_S),
        KeyDef("d", KEYCODE_D), KeyDef("f", KEYCODE_F),
        KeyDef("g", KEYCODE_G), KeyDef("h", KEYCODE_H),
        KeyDef("j", KEYCODE_J), KeyDef("k", KEYCODE_K),
        KeyDef("l", KEYCODE_L),
    )

    val row3 = listOf(
        KeyDef("⇧", KEYCODE_SHIFT),
        KeyDef("z", KEYCODE_Z), KeyDef("x", KEYCODE_X),
        KeyDef("c", KEYCODE_C), KeyDef("v", KEYCODE_V),
        KeyDef("b", KEYCODE_B), KeyDef("n", KEYCODE_N),
        KeyDef("m", KEYCODE_M),
        KeyDef("⌫", KEYCODE_DELETE, isRepeatable = true),
    )

    val row4 = listOf(
        KeyDef("?123", KEYCODE_SYMBOL),
        KeyDef(",", KEYCODE_COMMA, width = 40.dp),
        KeyDef("空格", KEYCODE_SPACE),
        KeyDef(".", KEYCODE_PERIOD, width = 40.dp),
        KeyDef("↵", KEYCODE_ENTER),
    )

    val rows = listOf(row1, row2, row3, row4)
}

// Keycode constants
const val KEYCODE_A = 29
const val KEYCODE_B = 30
// ... 完整 A-Z
const val KEYCODE_Q = 45
const val KEYCODE_W = 51
const val KEYCODE_E = 33
const val KEYCODE_R = 46
const val KEYCODE_T = 48
const val KEYCODE_Y = 53
const val KEYCODE_U = 49
const val KEYCODE_I = 37
const val KEYCODE_O = 43
const val KEYCODE_P = 44
const val KEYCODE_S = 47
const val KEYCODE_D = 32
const val KEYCODE_F = 34
const val KEYCODE_G = 35
const val KEYCODE_H = 36
const val KEYCODE_J = 38
const val KEYCODE_K = 39
const val KEYCODE_L = 40
const val KEYCODE_Z = 54
const val KEYCODE_X = 52
const val KEYCODE_C = 31
const val KEYCODE_V = 50
const val KEYCODE_N = 42
const val KEYCODE_M = 41

const val KEYCODE_SHIFT = -1
const val KEYCODE_DELETE = -2
const val KEYCODE_SPACE = 62
const val KEYCODE_ENTER = 66
const val KEYCODE_COMMA = 55
const val KEYCODE_PERIOD = 56
const val KEYCODE_SYMBOL = -3
```

> **注意:** Android KeyEvent keycode 值来自 `android.view.KeyEvent.KEYCODE_*`。此处使用 int 常量以便传递给 Rime 引擎。完整常量列表参照 `android.view.KeyEvent`。

- [ ] **Step 2: 实现 QWERTY 键盘 Composable**

```kotlin
package dev.yeying.ime.ui.keyboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QwertyKeyboard(
    onKey: (KeyDef) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                        onClick = { onKey(key) },
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
            .then(if (key.width > 0.dp) Modifier.width(key.width) else Modifier)
            // 点击和外观实现
            ,
        contentAlignment = Alignment.Center,
    ) {
        Text(text = key.label, fontSize = 18.sp)
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/ui/keyboard/
git commit -m "feat: add QWERTY keyboard layout and composable"
```

---

### Task 10: 候选词栏

**Files:**
- Create: `app/src/main/java/dev/yeying/ime/ui/candidate/CandidateBar.kt`

- [ ] **Step 1: 实现候选词栏**

```kotlin
package dev.yeying.ime.ui.candidate

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.yeying.ime.engine.CandidateItem
import dev.yeying.ime.ui.theme.liquidGlass

@Composable
fun CandidateBar(
    composingText: String,
    candidates: List<CandidateItem>,
    onSelect: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .liquidGlass(cornerRadius = 0.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (composingText.isNotEmpty()) {
            Text(
                text = composingText,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp),
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(candidates) { index, candidate ->
                Text(
                    text = candidate.text,
                    fontSize = 16.sp,
                    modifier = Modifier
                        // clickable { onSelect(index) }
                        ,
                )
            }
        }
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/ui/candidate/CandidateBar.kt
git commit -m "feat: add candidate bar with Liquid Glass"
```

---

## Phase 6: T9 九键键盘

### Task 11: T9 键盘 UI

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

const val KEYCODE_T9_2 = 200
const val KEYCODE_T9_3 = 201
const val KEYCODE_T9_4 = 202
const val KEYCODE_T9_5 = 203
const val KEYCODE_T9_6 = 204
const val KEYCODE_T9_7 = 205
const val KEYCODE_T9_8 = 206
const val KEYCODE_T9_9 = 207
const val KEYCODE_QUOTE = 208
const val KEYCODE_CLEAR = 209
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

### Task 12: 符号面板

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

### Task 13: 手写输入（采纳 yuyan + 搜狗 .so 链）

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
### Task 14: 设置页面

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

### Task 15: 剪贴板管理

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
Phase 1  项目脚手架           → 可构建空 APK
Phase 2  引擎层               → Rime JNI 可调用
Phase 3  桥接层               → 输入法可激活并显示 Compose UI
Phase 4  Liquid Glass 主题    → 视觉效果可用
Phase 5  QWERTY 键盘 + 候选词 → 基本中文输入可用
Phase 6  T9 九键键盘          → 九键输入可用
Phase 7  符号/Emoji 面板      → 符号输入可用
Phase 8  手写输入             → 手写可用
Phase 9  设置页               → 可管理词库和偏好
Phase 10 剪贴板管理           → 剪贴板功能可用
```

每个 Phase 完成后都是一个可测试的里程碑。Phase 5 完成后即为核心可用状态。
