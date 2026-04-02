plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    kotlin("native.cocoapods")
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        getByName("iosX64Main") {
            dependencies {
                implementation(project(":shared"))
            }
        }
        getByName("iosArm64Main") {
            dependencies {
                implementation(project(":shared"))
            }
        }
        getByName("iosSimulatorArm64Main") {
            dependencies {
                implementation(project(":shared"))
            }
        }
    }

    cocoapods {
        version = "1.0.0"
        name = "iosApp"
        ios.deploymentTarget = "14.0"
        framework {
            baseName = "iosApp"
            isStatic = true
            transitiveExport = true
        }
        podfile = project.file("Podfile")
    }
}