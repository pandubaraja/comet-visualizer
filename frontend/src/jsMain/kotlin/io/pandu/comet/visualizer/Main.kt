package io.pandu.comet.visualizer

import io.pandu.comet.visualizer.ui.CometVisualizerApp
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        CometVisualizerApp()
    }
}
