import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    `java-library`
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.pandubaraja"
version = "0.2.0"

dependencies {
    // Use 'api' so TraceEvent is exposed transitively to consumers
    api(project(":shared"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.kotlinx.coroutines.core)
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

// Task to copy frontend build to resources
tasks.register<Copy>("copyFrontend") {
    dependsOn(":frontend:jsBrowserDistribution")
    from(project(":frontend").layout.buildDirectory.dir("dist/js/productionExecutable"))
    into(layout.buildDirectory.dir("resources/main/static"))
}

// Make sure jar includes the frontend files
tasks.named<Jar>("jar") {
    dependsOn("copyFrontend")
    from(layout.buildDirectory.dir("resources/main"))
}

// Run task for testing
tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run the visualizer server for testing"
    mainClass.set("io.pandu.comet.visualizer.MainKt")
    classpath = sourceSets["main"].runtimeClasspath
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("io.github.pandubaraja", "comet-visualizer", version.toString())

    pom {
        name = "Comet Visualizer"
        description = "Real-time web UI for visualizing Coroutine traces via structured Tree Views, concurrent Gantt charts, and execution metrics."
        inceptionYear = "2026"
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
