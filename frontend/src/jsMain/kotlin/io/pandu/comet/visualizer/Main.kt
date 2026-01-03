package io.pandu.comet.visualizer

import kotlinx.browser.window
import org.jetbrains.compose.web.renderComposable

fun main() {
    val useMockData = window.location.search.contains("mock=true")

    renderComposable(rootElementId = "root") {
        if (useMockData) {
            MockCometVisualizerApp()
        } else {
            CometVisualizerApp()
        }
    }
}
