plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.ikanbot.tv"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.ikanbot.tv"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.leanback)
    implementation(libs.glide)

    implementation("androidx.activity:activity-ktx:1.13.0")
}
