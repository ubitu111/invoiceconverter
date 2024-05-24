plugins {
    alias(libs.plugins.kotlinMultiplatform)
//    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
    alias(libs.plugins.moko.res)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    jvm()

//    iosX64()
//    iosArm64()
//    iosSimulatorArm64()

//    cocoapods {
//        summary = "Some description for the Shared Module"
//        homepage = "Link to the Shared Module homepage"
//        version = "1.0"
//        ios.deploymentTarget = "16.0"
//        podfile = project.file("../iosApp/Podfile")
//        framework {
//            baseName = "shared"
//            isStatic = true
//        }
//    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)

                api(libs.resources.core)
                api(libs.resources.compose)

                api(libs.kotlin.file.picker)
            }
        }

//        commonTest.dependencies {
//            implementation(libs.kotlin.test)
//        }

        jvmMain {
            dependsOn(commonMain)
            dependencies {
                api(compose.desktop.currentOs)
                implementation(libs.apache.poi)
                implementation(libs.apache.poi.ooxml)
            }
        }

        androidMain {
            dependsOn(commonMain)
        }

//        val iosArm64Main by getting
//        val iosX64Main by getting
//        val iosSimulatorArm64Main by getting
//
//        iosMain {
//            dependsOn(commonMain)
//            iosArm64Main.dependsOn(this)
//            iosX64Main.dependsOn(this)
//            iosSimulatorArm64Main.dependsOn(this)
//        }
    }

    task("testClasses")
}

android {
    namespace = "com.mamontov.invoice_converter"
    compileSdk = 34
    defaultConfig {
        minSdk = 28
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "com.mamontov.invoice_converter"
}
