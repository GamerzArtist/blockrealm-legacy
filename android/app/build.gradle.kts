plugins {
    id("com.android.application")
}

android {
    namespace = "com.gamerzartist.blockrealm"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gamerzartist.blockr4"
        minSdk = 24
        targetSdk = 36
        versionCode = 40000
        versionName = "4.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.webkit:webkit:1.16.0")
}
