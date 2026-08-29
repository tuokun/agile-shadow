import java.security.MessageDigest
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

val releaseKeystoreFile = rootProject.file("keystore.properties")
val releaseKeystoreProperties = Properties().apply {
    if (releaseKeystoreFile.isFile) {
        releaseKeystoreFile.inputStream().use(::load)
    }
}
val releaseKeystoreKeys = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
val isReleaseKeystoreConfigured = releaseKeystoreFile.isFile &&
    releaseKeystoreKeys.all { !releaseKeystoreProperties.getProperty(it).isNullOrBlank() }

fun requireReleaseKeystore() {
    check(isReleaseKeystoreConfigured) {
        "Release 构建需要根目录 keystore.properties，包含 storeFile、storePassword、keyAlias、keyPassword。"
    }
}

android {
    namespace = "io.github.cgfhsc.agileshadow.ime"
    compileSdk = libs.versions.projectCompileSdk.get().toInt()

    signingConfigs {
        create("release") {
            if (isReleaseKeystoreConfigured) {
                storeFile = rootProject.file(releaseKeystoreProperties.getProperty("storeFile"))
                storePassword = releaseKeystoreProperties.getProperty("storePassword")
                keyAlias = releaseKeystoreProperties.getProperty("keyAlias")
                keyPassword = releaseKeystoreProperties.getProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "io.github.cgfhsc.agileshadow.ime"
        minSdk = libs.versions.projectMinSdk.get().toInt()
        targetSdk = libs.versions.projectTargetSdk.get().toInt()
        versionCode = libs.versions.projectVersionCode.get().toInt()
        versionName = libs.versions.projectVersionName.get()

        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (isReleaseKeystoreConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
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
        buildConfig = true
    }

    dependenciesInfo {
        includeInApk = false
    }
}

tasks.configureEach {
    if (name.contains("Release", ignoreCase = true)) {
        doFirst { requireReleaseKeystore() }
    }
}

val rimeAssetsDirectory = layout.projectDirectory.dir("src/main/assets/rime")
val rimeRuntimeManifest = rimeAssetsDirectory.file("runtime-manifest.txt")

val verifyRimeRuntime by tasks.registering {
    group = "verification"
    description = "Verifies the complete Rime runtime assets used by Android builds."

    inputs.file(rimeRuntimeManifest)
    inputs.dir(rimeAssetsDirectory)

    doLast {
        val assetsRoot = rimeAssetsDirectory.asFile.canonicalFile
        val manifestFile = rimeRuntimeManifest.asFile
        check(manifestFile.isFile) {
            "Rime Runtime 清单不存在：${manifestFile.absolutePath}"
        }

        val entries = manifestFile.readLines()
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapIndexed { index, line ->
                val parts = line.split('|')
                check(parts.size == 3) {
                    "Rime Runtime 清单第 ${index + 1} 条格式错误：$line"
                }

                val relativePath = parts[0]
                val expectedSize = parts[1].toLongOrNull()
                val expectedSha256 = parts[2].lowercase()
                check(expectedSize != null && expectedSize >= 0) {
                    "Rime Runtime 文件长度无效：$line"
                }
                check(expectedSha256.matches(Regex("[0-9a-f]{64}"))) {
                    "Rime Runtime SHA-256 无效：$line"
                }
                Triple(relativePath, expectedSize, expectedSha256)
            }

        check(entries.isNotEmpty()) {
            "Rime Runtime 清单不能为空：${manifestFile.absolutePath}"
        }

        val declaredPaths = entries.map { it.first }
        check(declaredPaths.size == declaredPaths.toSet().size) {
            "Rime Runtime 清单包含重复路径"
        }

        entries.forEach { (relativePath, expectedSize, expectedSha256) ->
            val runtimeFile = assetsRoot.resolve(relativePath).canonicalFile
            check(runtimeFile.toPath().startsWith(assetsRoot.toPath())) {
                "Rime Runtime 路径越出 assets 根目录：$relativePath"
            }
            check(runtimeFile.isFile) {
                "Rime Runtime 文件缺失：$relativePath"
            }
            check(runtimeFile.length() == expectedSize) {
                "Rime Runtime 文件长度不一致：$relativePath，期望 $expectedSize，实际 ${runtimeFile.length()}"
            }

            val digest = MessageDigest.getInstance("SHA-256")
            runtimeFile.inputStream().buffered().use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    digest.update(buffer, 0, count)
                }
            }
            val actualSha256 = digest.digest().joinToString("") { "%02x".format(it) }
            check(actualSha256 == expectedSha256) {
                "Rime Runtime SHA-256 不一致：$relativePath，期望 $expectedSha256，实际 $actualSha256"
            }
        }

        val actualRuntimePaths = assetsRoot
            .walkTopDown()
            .filter { it.isFile }
            .map { it.relativeTo(assetsRoot).invariantSeparatorsPath }
            .filter { it != manifestFile.name }
            .toSet()
        check(actualRuntimePaths == declaredPaths.toSet()) {
            val missing = declaredPaths.toSet() - actualRuntimePaths
            val unexpected = actualRuntimePaths - declaredPaths.toSet()
            "Rime Runtime 文件集合与清单不一致；缺失=$missing，未声明=$unexpected"
        }
    }
}

tasks.named("preBuild") {
    dependsOn(verifyRimeRuntime)
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

    implementation(libs.datastore.preferences)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.emoji.compat)

    implementation(libs.mlkit.digital.ink)
}
