plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)

    kotlin("plugin.serialization").version("1.9.21")
    id("com.squareup.sqldelight").version("1.5.5")
}

val coroutinesVersion = "1.7.3"
val ktorVersion = "2.3.5"
val sqlDelightVersion = "1.5.5"
val dateTimeVersion = "0.4.1"

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.0"
        framework {
            baseName = "switchykmmsdk"
            isStatic = true
        }
    }
    
    sourceSets {

        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("com.squareup.sqldelight:runtime:$sqlDelightVersion")
//            implementation("org.jetbrains.kotlinx:kotlinx-datetime:$dateTimeVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")
//            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
        }

        androidMain.dependencies {
            implementation("io.ktor:ktor-client-android:$ktorVersion")
            implementation("com.squareup.sqldelight:android-driver:$sqlDelightVersion")
        }

        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            implementation("com.squareup.sqldelight:native-driver:$sqlDelightVersion")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

sqldelight {
    database("AppDatabase") {
        packageName = "com.switchy.kmm.cache"
    }
}

android {
    namespace = "com.switchy.kmmsdk"
    compileSdk = 34
    defaultConfig {
        minSdk = 29
    }
}