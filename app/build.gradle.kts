plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.0-1.0.24"
}

android {
    namespace = "com.logex.fleteros"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.logex.fleteros"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Agregar dentro del bloque dependencies { ... } de app/build.gradle.kts
    // (junto a las que ya trae la plantilla Empty Activity con Compose)

    // Almacenamiento cifrado del token
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Retrofit + JSON
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ViewModel en Compose (probablemente ya venga en la plantilla, revisar si tira error de duplicado)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Almacenamiento cifrado del token
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Retrofit + JSON
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ViewModel en Compose (probablemente ya venga en la plantilla, revisar si tira error de duplicado)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Icono de hamburguesa (Icons.Filled.Menu) para el drawer del menu
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    // Agregar dentro del bloque dependencies { ... } de app/build.gradle.kts
    // (junto a las que ya trae la plantilla Empty Activity con Compose)

    // Almacenamiento cifrado del token
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Retrofit + JSON
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ViewModel en Compose (probablemente ya venga en la plantilla, revisar si tira error de duplicado)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Icono de hamburguesa (Icons.Filled.Menu) para el drawer del menu
    implementation("androidx.compose.material:material-icons-core")

    // Set extendido de iconos (incluye Image, y varios otros que no estan en el core)
    implementation("androidx.compose.material:material-icons-extended")

    // Ubicacion en segundo plano (para el recorrido en el mapa)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.compose.material3:material3:1.3.1")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}