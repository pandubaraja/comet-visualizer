plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "comet-visualizer.js"
            }
            runTask {
                devServerProperty.set(
                    org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer(
                        port = 3000,
                        proxy = mutableListOf(
                            org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer.Proxy(
                                mutableListOf("/events"),
                                "http://localhost:8080"
                            )
                        )
                    )
                )
            }
        }
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":shared"))
            implementation(compose.runtime)
            implementation(compose.html.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
