package io.pandu.comet.visualizer

import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        CometVisualizerApp()
    }
}
