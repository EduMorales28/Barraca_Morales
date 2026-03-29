plugins {
    id 'com.android.application' version '8.2.0' apply false
    id 'com.android.library' version '8.2.0' apply false
    kotlin("android") version "1.9.20" apply false
}

android {
    compileSdk 34

    defaultConfig {
        applicationId "com.barraca.conductor"
        minSdk 26
        targetSdk 34
        versionCode 1
        versionName "1.0.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary true
        }
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = '11'
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = '1.5.3'
    }

    packaging {
        resources {
            excludes += '/META-INF/{AL2.0,LGPL2.1}'
        }
    }
}

dependencies {
    // Core Android
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'

    // Jetpack Compose
    implementation 'androidx.compose.ui:ui:1.6.0'
    implementation 'androidx.compose.material3:material3:1.2.1'
    implementation 'androidx.compose.ui:ui-graphics:1.6.0'
    implementation 'androidx.compose.ui:ui-tooling-preview:1.6.0'
    implementation 'androidx.activity:activity-compose:1.8.1'
    implementation 'androidx.lifecycle:lifecycle-runtime-compose:2.7.0'

    // Navigation Compose
    implementation 'androidx.navigation:navigation-compose:2.7.6'

    // Hilt - Dependency Injection
    implementation 'com.google.dagger:hilt-android:2.48'
    kapt 'com.google.dagger:hilt-compiler:2.48'
    implementation 'androidx.hilt:hilt-navigation-compose:1.1.0'

    // Retrofit - HTTP Client
    implementation 'com.squareup.retrofit2:retrofit:2.10.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.10.0'
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'

    // GSON - JSON Serialization
    implementation 'com.google.code.gson:gson:2.10.1'

    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'

    // Room - Local Database
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'

    // DataStore - Secure Preferences
    implementation 'androidx.datastore:datastore-preferences:1.0.0'
    implementation 'androidx.security:security-crypto:1.1.0-alpha06'

    // CameraX - Camera library
    implementation 'androidx.camera:camera-core:1.3.0'
    implementation 'androidx.camera:camera-camera2:1.3.0'
    implementation 'androidx.camera:camera-lifecycle:1.3.0'
    implementation 'androidx.camera:camera-view:1.3.0'
    implementation 'androidx.camera:camera-extensions:1.3.0'

    // Google Maps
    implementation 'com.google.maps.android:maps-compose:2.14.1'
    implementation 'com.google.android.gms:play-services-maps:18.2.0'
    implementation 'com.google.android.gms:play-services-location:21.1.0'
Firebase - Messaging (Push Notifications)
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-messaging-ktx'
    implementation 'com.google.firebase:firebase-auth-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'

    // 
    // Image Loading - Coil
    implementation 'io.coil-kt:coil-compose:2.5.0'

    // Logging
    implementation 'timber:timber:5.0.1'

    // Testing
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4:1.6.0'
    debugImplementation 'androidx.compose.ui:ui-tooling:1.6.0'
    debugImplementation 'androidx.compose.ui:ui-test-manifest:1.6.0'
}

// Allow references to generated code
kapt {
    correctErrorTypes true
}
