plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.tradeagently.act_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tradeagently.act_app"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"

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
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.compiler) {
        exclude(group = "com.intellij", module = "annotations")
    }
    implementation(libs.play.services.basement)
    implementation(libs.firebase.common.ktx)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Networking with Retrofit and OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3") {
        exclude(group = "com.intellij", module = "annotations")
    }
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

    // Image loading with Glide
    implementation("com.github.bumptech.glide:glide:4.12.0") {
        exclude(group = "com.intellij", module = "annotations")
    }
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // MPAndroidChart for charting
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    //Stripe
    implementation ("com.stripe:stripe-android:21.2.0")

    // Firebase Authentication
    implementation ("com.google.firebase:firebase-auth-ktx:22.1.1")

    // Google Sign-In
    implementation ("com.google.android.gms:play-services-auth:20.6.0")
}

