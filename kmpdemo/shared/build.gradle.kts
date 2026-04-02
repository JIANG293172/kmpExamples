plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    kotlin("native.cocoapods")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    // iOS targets
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // Desktop target
    jvm("desktop") {
        jvmToolchain(17)
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.material3)
                implementation("androidx.activity:activity-compose:1.8.2")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }

        val iosX64Main by getting {
            dependencies {
                implementation(compose.ui)
            }
        }
        val iosArm64Main by getting {
            dependencies {
                implementation(compose.ui)
            }
        }
        val iosSimulatorArm64Main by getting {
            dependencies {
                implementation(compose.ui)
            }
        }
    }

    cocoapods {
        version = "1.0.0"
        name = "shared"
        summary = "KMP shared module for iOS and Android"
        homepage = "https://example.com"
        ios.deploymentTarget = "14.0"
        framework {
            baseName = "shared"
            isStatic = true
            transitiveExport = true
        }
        podfile = project.file("../ios/Podfile")
    }
}

android {
    namespace = "com.example.kmpdemo"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}