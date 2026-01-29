plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.pandubaraja"
version = "0.2.0"

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
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("io.github.pandubaraja", "comet-visualizer-shared", version.toString())

    pom {
        name = "Comet Visualizer Shared"
        description = "Shared models and utilities for Comet Visualizer (JVM + JS)."
        inceptionYear = "2025"
        url = "https://github.com/pandubaraja/comet-visualizer/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "pandubaraja"
                name = "Pandu Baraja"
                url = "https://github.com/pandubaraja"
            }
        }
        scm {
            url = "https://github.com/pandubaraja/comet-visualizer"
            connection = "scm:git:git://github.com/pandubaraja/comet-visualizer.git"
            developerConnection = "scm:git:ssh://git@github.com/pandubaraja/comet-visualizer.git"
        }
    }
}
