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
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
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
    val retrofitVersion = "2.11.0"
    val okhttpVersion = "4.12.0"
    val moshiVersion = "1.15.2"

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.navigation:navigation-compose:2.9.4")

    val composeBom = platform("androidx.compose:compose-bom:2025.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.10.1")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
}
