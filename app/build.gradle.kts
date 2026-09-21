plugins { id("com.android.application") }
android {
 namespace = "com.example.suiri"
 compileSdk = 36
 defaultConfig { applicationId = "com.example.suiri"; minSdk = 30; targetSdk = 36; versionCode = 11; versionName = "1.1.5" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_1_8; targetCompatibility = JavaVersion.VERSION_1_8 }
}
