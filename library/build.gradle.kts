import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    `java-library`
    `maven-publish`
}

group = "io.pandu.comet"
version = "0.1.0"

dependencies {
    // Use 'api' so TraceEvent is exposed transitively to consumers
    api(project(":shared"))
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

// Custom configuration for publishable dependencies (excludes project deps)
val publishableDeps by configurations.creating {
    extendsFrom(configurations.implementation.get())
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.pandu.comet"
            artifactId = "comet-visualizer"
            version = project.version.toString()

            // Add JAR artifact manually
            artifact(tasks.named("jar"))

            pom {
                name.set("comet-visualizer")
                description.set("Visualization tools for Comet coroutine telemetry - real-time web UI with Kotlin/JS.")
                url.set("https://github.com/pandubaraja/comet/")

                withXml {
                    val deps = asNode().appendNode("dependencies")

                    // Add shared module dependency
                    val sharedDep = deps.appendNode("dependency")
                    sharedDep.appendNode("groupId", "io.pandu.comet")
                    sharedDep.appendNode("artifactId", "comet-visualizer-shared-jvm")
                    sharedDep.appendNode("version", "0.1.0")
                    sharedDep.appendNode("scope", "compile")

                    // Add coroutines
                    val coroutinesDep = deps.appendNode("dependency")
                    coroutinesDep.appendNode("groupId", "org.jetbrains.kotlinx")
                    coroutinesDep.appendNode("artifactId", "kotlinx-coroutines-core")
                    coroutinesDep.appendNode("version", "1.9.0")
                    coroutinesDep.appendNode("scope", "runtime")

                    // Add serialization
                    val serializationDep = deps.appendNode("dependency")
                    serializationDep.appendNode("groupId", "org.jetbrains.kotlinx")
                    serializationDep.appendNode("artifactId", "kotlinx-serialization-json")
                    serializationDep.appendNode("version", "1.7.3")
                    serializationDep.appendNode("scope", "runtime")
                }

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("pandubaraja")
                        name.set("Pandu Baraja")
                    }
                }
                scm {
                    url.set("https://github.com/pandubaraja/comet/")
                }
            }
        }
    }
}
