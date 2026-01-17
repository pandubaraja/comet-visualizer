package io.pandu.comet.visualizer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import io.pandu.comet.visualizer.data.TraceState
import io.pandu.comet.visualizer.ui.CometVisualizerContent

@Composable
fun CometVisualizerApp() {
    val traceState = remember { TraceState() }

    // Connect to SSE on mount
    DisposableEffect(Unit) {
        val client = SseClient(
            onEvent = { event -> traceState.processEvent(event) }
        )
        client.connect()
        onDispose { client.disconnect() }
    }

    CometVisualizerContent(traceState)
}