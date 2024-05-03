
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.udapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.udapp"
        minSdk = 28
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

    buildFeatures{
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        exclude("META-INF/INDEX.LIST")
        exclude("META-INF/DEPENDENCIES")
    }
}

dependencies {

    implementation("io.github.gautamchibde:audiovisualizer:2.2.5")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.android.car.ui:car-ui-lib:2.6.0")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.security:security-identity-credential:1.0.0-alpha03")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("androidx.credentials:credentials:1.3.0-alpha02")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0-alpha02")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation (platform("com.google.firebase:firebase-bom:29.0.0"))
    implementation ("com.google.cloud:google-cloud-speech:2.3.0")
    //implementation ("com.google.auth:google-auth-library-oauth2-http:0.22.2")
    implementation("com.google.android.gms:play-services-drive:17.0.0")
    implementation("com.google.api-client:google-api-client-android:1.31.5")
    //implementation("com.google.api-client:google-api-client:1.31.5")
    //implementation("com.google.api-services:drive:v3-rev20210802-1.32.1")
    //implementation("com.google.apis:google-api-services-drive:v3-rev20230502-2.0.0")

    implementation("com.google.api-client:google-api-client-jackson2:1.31.5")// Or latest version



}