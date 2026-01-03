plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    `maven-publish`
}

group = "io.pandu.comet"
version = "0.1.0"

kotlin {
    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        artifactId = "comet-visualizer-shared" + artifactId.removePrefix("shared")
    }
}
