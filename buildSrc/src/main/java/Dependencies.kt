object Versions {
    // App versioning
    const val demoAppId = "org.openmined.syft.demo"
    const val appVersionCode = 1
    const val appVersionName = "1.0"

    const val compileSdk = 29
    const val minSdk = 24
    const val targetSdk = 29

    const val gradle = "4.0.1"
    const val kotlin = "1.3.61"
    const val buildTools = "29.0.2"

    // Android libraries
    const val appCompat = "1.1.0-alpha04"
    const val constraintLayout = "1.1.3"
    const val coreKtx = "1.2.0"
    const val kotlinSerialization = "0.14.0"
    const val material="1.1.0"
    const val workManagerVersion = "2.3.4"
    const val lifecycleVersion = "2.2.0"
    // Tools
    const val rxJava = "2.2.12"
    const val rxAndroid = "2.1.1"
    const val webrtc = "1.0.30039"
    const val okhttp = "4.3.1"
    const val protobuf = "3.11.4"
    const val syftProto = "0.5.0"
    const val retrofit = "2.7.1"
    const val kotlinConverter = "0.4.0"
    const val pytorchAndroid = "1.4.0"
    const val mpAndroidChart = "v3.1.0"
    const val coroutines = "1.3.7"

    // release management
    const val netflixPublishing = "17.3.2"
    const val netflixRelease = "15.2.0"
    const val netflixBintray = "3.5.4"
    const val jacocoVersion = "0.8.5"
    const val dokkaVersion = "0.10.1"

    // Test
    const val extJunit = "1.1.1"
    const val testRunner = "1.0.2"
    const val espresso = "3.2.0"
    const val mockitoCore = "3.2.4"
    const val mockitoKotlin = "2.2.0"
    const val junit = "4.12"
    const val adxCore = "1.1.0"
    const val adxRunner = "1.1.1"
    const val adxExtTruth = "1.1.0"
    const val robolectric = "4.4-SNAPSHOT"
    const val mockk = "1.10.0"

}

object ProjectDependencies {
    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.gradle}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val kotlinSerialization = "org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlin}"
    const val netflixPublishingPlugin = "com.netflix.nebula:nebula-publishing-plugin:${Versions.netflixPublishing}"
    const val netflixReleasePlugin = "com.netflix.nebula:nebula-release-plugin:${Versions.netflixRelease}"
    const val netflixBintrayPlugin = "com.netflix.nebula:nebula-bintray-plugin:${Versions.netflixBintray}"
    const val jacoco = "org.jacoco:org.jacoco.core:${Versions.jacocoVersion}"
    const val dokka = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokkaVersion}"
}

object CommonDependencies {
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val kotlinSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Versions.kotlinSerialization}"
    const val kotlinSerializationFactory = "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:${Versions.kotlinConverter}"
    const val rxJava = "io.reactivex.rxjava2:rxjava:${Versions.rxJava}"
    const val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroid}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"

    const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val adxTest = "androidx.test:core:${Versions.adxCore}"
    const val adxExtJunit = "androidx.test.ext:junit:${Versions.extJunit}"
    const val adxRunner = "androidx.test:runner:${Versions.adxRunner}"
    const val adxExtTruth = "androidx.test.ext:truth:${Versions.adxExtTruth}"
    const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}"
    const val junit = "junit:junit:${Versions.junit}"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    const val mockitoCore = "org.mockito:mockito-core:${Versions.mockitoCore}"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
}

object DemoAppDependencies {
    const val kotlinJDK = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val materialAndroid = "com.google.android.material:material:${Versions.material}"
    const val mpAndroidChart = "com.github.PhilJay:MPAndroidChart:${Versions.mpAndroidChart}"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val lifecycle = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycleVersion}"
    const val lifeCycleProcess = "androidx.lifecycle:lifecycle-process:${Versions.lifecycleVersion}"

    const val workKtx = "androidx.work:work-runtime-ktx:${Versions.workManagerVersion}"
    const val workRX = "androidx.work:work-rxjava2:${Versions.workManagerVersion}"
    const val workGCM = "androidx.work:work-gcm:${Versions.workManagerVersion}"

    const val workTest = "androidx.work:work-testing:${Versions.workManagerVersion}"
}

object SyftlibDependencies {
    const val kotlinJDK = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    const val webrtc = "org.webrtc:google-webrtc:${Versions.webrtc}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val syftProto = "org.openmined.kotlinsyft:syft-proto-jvm:${Versions.syftProto}"
    const val protobuf = "com.google.protobuf:protobuf-java:${Versions.protobuf}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofitAdapter = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
    const val pytorchAndroid = "org.pytorch:pytorch_android:${Versions.pytorchAndroid}"
    const val torchVisionAndroid = "org.pytorch:pytorch_android_torchvision:${Versions.pytorchAndroid}"
}
