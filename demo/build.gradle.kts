import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    implementation(project(":library"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
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

// Run demo task (production mode - serves static files)
tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run the visualizer demo"
    mainClass.set("io.pandu.comet.visualizer.demo.DemoKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

// Run demo in dev mode (SSE only - use with frontend dev server for hot reload)
tasks.register<JavaExec>("runDev") {
    group = "application"
    description = "Run demo in dev mode (SSE only, for use with frontend hot reload)"
    mainClass.set("io.pandu.comet.visualizer.demo.DevDemoKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}
