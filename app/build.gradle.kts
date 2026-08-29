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
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.emoji.compat)

    implementation(libs.mlkit.digital.ink)
}
