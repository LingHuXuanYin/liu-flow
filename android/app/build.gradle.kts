plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.liuflow.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.liuflow.app"
        minSdk = 26
        targetSdk = 34
        // 版本号统一从根 gradle.properties 读取（集中管理）
        versionCode = (project.findProperty("APP_VERSION_CODE") as String? ?: "1").toInt()
        versionName = project.findProperty("APP_VERSION_NAME") as String? ?: "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // ----------------------------------------------------------------
        // Read app/src/main/.env (gitignored) and inject as BuildConfig fields.
        // .env.example is the template; copy to .env and fill in real values.
        // ----------------------------------------------------------------
        val envFile = rootProject.file("app/src/main/.env")
        val envMap: Map<String, String> = if (envFile.exists()) {
            envFile.readLines()
                .map { it.substringBefore("#").trim() }      // strip comments
                .filter { it.isNotEmpty() && "=" in it }
                .associate { line ->
                    val (k, v) = line.split("=", limit = 2)
                    k.trim() to v.trim().trim('"').trim('\'')
                }
        } else emptyMap()

        buildConfigField("String", "TCB_ENV_ID",
            "\"${envMap["TCB_ENV_ID"] ?: ""}\"")
        buildConfigField("String", "TCB_PUBLISHABLE_KEY",
            "\"${envMap["TCB_PUBLISHABLE_KEY"] ?: ""}\"")
        buildConfigField("String", "TCB_REGION",
            "\"${envMap["TCB_REGION"] ?: "ap-shanghai"}\"")
        buildConfigField("boolean", "DEBUG_LOG_NETWORK",
            "${envMap["DEBUG_LOG_NETWORK"]?.toBooleanStrictOrNull() ?: false}")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Default to debug signing for now; user can override.
            signingConfig = signingConfigs.getByName("debug")
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore (settings)
    implementation(libs.androidx.datastore.preferences)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Serialization (data export)
    implementation(libs.kotlinx.serialization.json)

    // CSV export
    implementation(libs.opencsv)

    // auths/ module: HTTP + Security + Image loading
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.androidx.security.crypto)
    implementation(libs.coil.compose)
    implementation(libs.gson)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
