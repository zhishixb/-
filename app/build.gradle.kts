plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.afinal"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.afinal"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation ("com.google.android.material:material:1.9.0")

    val room_version = "2.6.1"

    //注解使用sqlite
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    testImplementation("androidx.room:room-testing:$room_version")
    implementation("androidx.room:room-paging:$room_version")
    //网络请求处理
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    // 用于解析JSON响应
    implementation ("com.google.code.gson:gson:2.8.6")
    //图片处理
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    //组件库
    implementation ("com.google.android.material:material:<latest_version>")
}