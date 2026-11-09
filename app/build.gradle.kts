import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun quotedApiBaseUrl(propertyKey: String, defaultUrl: String): String {
    val override = localProperties.getProperty(propertyKey)
        ?: providers.gradleProperty(propertyKey).orNull
    val raw = override?.trim()?.takeIf { it.isNotEmpty() } ?: defaultUrl
    val withSlash = if (raw.endsWith("/")) raw else "$raw/"
    val escaped = withSlash.replace("\\", "\\\\").replace("\"", "\\\"")
    return "\"$escaped\""
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.logix.optiflow"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.logix.optiflow"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    flavorDimensions += "environment"
    productFlavors {
        create("local") {
            dimension = "environment"
            buildConfigField(
                "String",
                "API_BASE_URL",
                quotedApiBaseUrl("api.base.url.local", "http://10.0.2.2:8080/"),
            )
        }
        create("prod") {
            dimension = "environment"
            buildConfigField(
                "String",
                "API_BASE_URL",
                quotedApiBaseUrl(
                    "api.base.url.prod",
                    "https://logix-optiflow-back-end.onrender.com/",
                ),
            )
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
}
