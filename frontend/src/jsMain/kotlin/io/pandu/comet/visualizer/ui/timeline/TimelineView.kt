package io.pandu.comet.visualizer.ui.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.pandu.comet.visualizer.data.TimelineEvent
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun TimelineView(events: List<TimelineEvent>) {
    var hoveredEvent by remember { mutableStateOf<TimelineEvent?>(null) }
    var tooltipPosition by remember { mutableStateOf(Pair(0, 0)) }

    Div({}) {
        if (events.isEmpty()) {
            Div({ classes("text-center", "py-8", "text-slate-500") }) {
                Text("No events yet...")
            }
        } else {
            events.forEach { event ->
                TimelineItem(
                    event = event,
                    onHover = { e, x, y ->
                        hoveredEvent = e
                        tooltipPosition = Pair(x, y)
                    },
                    onLeave = { hoveredEvent = null }
                )
            }
        }
    }

    // Tooltip
    hoveredEvent?.let { event ->
        TimelineTooltip(event, tooltipPosition.first, tooltipPosition.second)
    }
}