object Versions {
    // App versioning
    const val demoAppId = "org.openmined.syft.demo"
    const val appVersionCode = 1
    const val appVersionName = "1.0"

    const val compileSdk = 29
    const val minSdk = 24
    const val targetSdk = 29

    const val gradle = "3.5.3"
    const val kotlin = "1.3.61"
    const val buildTools = "29.0.2"

    // Android libraries
    const val appCompat = "1.1.0"
    const val coreKtx = "1.2.0"
    const val kotlinSerialization = "0.14.0"

    // Tools
    const val rxJava = "2.2.12"
    const val rxAndroid = "2.1.1"
    const val webrtc = "1.0.30039"
    const val okhttp = "4.3.1"
    const val protobuf = "3.11.0"
    const val syftProto = "0.0.8"

    // release management
    const val netflixPublishing = "14.0.0"
    const val netflixRelease = "13.0.0"
    const val netflixBintray = "3.5.4"

    // Test
    const val junit = "1.1.1"
    const val testRunner = "1.0.2"
    const val espresso = "3.2.0"
    const val mockitoCore = "3.2.4"
    const val mockitoJupiter = "3.2.4"
    const val mockitoKotlin = "2.2.0"
    const val junitJupiter = "5.6.0"

}

object ProjectDependencies {
    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.gradle}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val kotlinSerialization = "org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlin}"
    const val netflixPublishingPlugin = "com.netflix.nebula:nebula-publishing-plugin:${Versions.netflixPublishing}"
    const val netflixReleasePlugin = "com.netflix.nebula:nebula-release-plugin:${Versions.netflixRelease}"
    const val netflixBintrayPlugin = "com.netflix.nebula:nebula-bintray-plugin:${Versions.netflixBintray}"
}

object CommonDependencies {
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val kotlinSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Versions.kotlinSerialization}"
    const val rxJava = "io.reactivex.rxjava2:rxjava:${Versions.rxJava}"
    const val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroid}"
    const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val testExtJunit = "androidx.test.ext:junit:${Versions.junit}"
}

object DemoAppDependencies {
    const val kotlinJDK = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
}

object SyftlibDependencies {
    const val mockitoCore = "org.mockito:mockito-core:${Versions.mockitoCore}"
    const val kotlinJDK = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    const val webrtc = "org.webrtc:google-webrtc:${Versions.webrtc}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val mockitoJupiter = "org.mockito:mockito-junit-jupiter:${Versions.mockitoJupiter}"
    const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}"
    const val syftProto = "org.openmined.kotlinsyft:syft-proto-jvm:${Versions.syftProto}"
    const val protobuf = "com.google.protobuf:protobuf-java:${Versions.protobuf}"
    const val junitJupiter = "org.junit.jupiter:junit-jupiter:${Versions.junitJupiter}"
}
