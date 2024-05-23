plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose)
}

kotlin {
    jvm("desktop")

    sourceSets {
        getByName("desktopMain").dependencies {
            implementation(projects.shared)
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
