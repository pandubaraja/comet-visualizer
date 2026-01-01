import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import kotlin.text.set

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.pandu.comet"
version = "0.1.0"

dependencies {
    implementation(libs.kotlinx.coroutine.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlin.test)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

mavenPublishing {
    publishToMavenCentral()

    if (System.getenv("CI") != null) {
        signAllPublications()
    }

    coordinates(group.toString(), "comet-visualizer", version.toString())

    pom {
        name = "comet-visualizer"
        description = "Visualization tools for Comet coroutine telemetry - HTML export and real-time web UI."
        inceptionYear = "2025"
        url = "https://github.com/pandubaraja/comet/"
        licenses {
            license {
                name = "XXX"
                url = "YYY"
                distribution = "ZZZ"
            }
        }
        developers {
            developer {
                id = "XXX"
                name = "YYY"
                url = "ZZZ"
            }
        }
        scm {
            url = "XXX"
            connection = "YYY"
            developerConnection = "ZZZ"
        }
    }
}
