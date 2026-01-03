package io.pandu.comet.visualizer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.pandu.comet.visualizer.data.TraceState
import io.pandu.comet.visualizer.ui.CometStyles
import io.pandu.comet.visualizer.ui.CometVisualizerContent
import io.pandu.comet.visualizer.ui.gantt.GanttView
import io.pandu.comet.visualizer.ui.stats.StatsBar
import io.pandu.comet.visualizer.ui.timeline.TimelineView
import io.pandu.comet.visualizer.ui.toggle.ThemeToggle
import io.pandu.comet.visualizer.ui.toggle.ViewStyle
import io.pandu.comet.visualizer.ui.toggle.ViewStyleToggle
import io.pandu.comet.visualizer.ui.tree.TreeView
import kotlinx.browser.document
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Header
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

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