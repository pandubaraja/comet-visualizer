package io.pandu.comet.visualizer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.pandu.comet.visualizer.data.TraceState
import io.pandu.comet.visualizer.data.loadMockData
import io.pandu.comet.visualizer.ui.CometVisualizerContent

/**
 * Mock version of CometVisualizerApp for development.
 * Loads sample data instead of connecting to SSE.
 */
@Composable
fun MockCometVisualizerApp() {
    val traceState = remember { TraceState().apply { loadMockData() } }
    CometVisualizerContent(traceState, badge = "MOCK")
}
