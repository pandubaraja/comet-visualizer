package io.pandu.comet.visualizer.ui.timeline

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.data.TimelineEvent
import io.pandu.comet.visualizer.TraceStatus
import io.pandu.comet.visualizer.format
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.text.ifEmpty


@Composable
fun TimelineTooltip(event: TimelineEvent, x: Int, y: Int) {
    val traceEvent = event.event
    val status = if (traceEvent.type == "started") TraceStatus.RUNNING else TraceStatus.fromString(traceEvent.status)
    val eventType = if (traceEvent.type == "started") "Started" else "Updated"

    val dotColor = when (status) {
        TraceStatus.RUNNING -> "bg-blue-500"
        TraceStatus.COMPLETED -> "bg-emerald-500"
        TraceStatus.FAILED -> "bg-red-500"
        TraceStatus.CANCELLED -> "bg-amber-500"
    }

    Div({
        classes(
            "fixed", "z-[1000]",
            "bg-slate-900", "border", "border-white/10",
            "rounded-lg", "px-3.5", "py-2.5",
            "text-xs", "shadow-xl",
            "pointer-events-none",
            "max-w-[300px]"
        )
        style {
            property("left", "${x + 12}px")
            property("top", "${y + 12}px")
        }
    }) {
        Div({ classes("font-semibold", "mb-1.5", "flex", "items-center", "gap-2") }) {
            Span({
                classes(
                    "w-[18px]", "h-[18px]",
                    "rounded-full",
                    "flex", "items-center", "justify-center",
                    "text-[10px]", "text-white",
                    dotColor
                )
            }) {
                Text(getStatusIcon(status))
            }
            Text(traceEvent.operation)
        }
        TimelineTooltipRow("Event", eventType)
        TimelineTooltipRow("Status", status.name.lowercase().replaceFirstChar { it.uppercase() })
        TimelineTooltipRow("Time", event.timeOffset)
        if (traceEvent.type != "started" && traceEvent.durationMs > 0) {
            TimelineTooltipRow("Duration", "${traceEvent.durationMs.format(1)}ms")
        }
        TimelineTooltipRow("Dispatcher", traceEvent.dispatcher.ifEmpty { "Default" })
        if (traceEvent.id.isNotEmpty()) {
            Div({
                classes(
                    "flex", "justify-between", "gap-4",
                    "mt-1", "text-slate-400"
                )
            }) {
                Span({}) { Text("Span ID") }
                Span({ classes("font-mono", "text-slate-200", "text-[0.7rem]") }) {
                    Text(traceEvent.id.take(12) + "...")
                }
            }
        }
    }
}

private fun getStatusIcon(status: TraceStatus): String = when (status) {
    TraceStatus.RUNNING -> "●"
    TraceStatus.COMPLETED -> "✓"
    TraceStatus.FAILED -> "✗"
    TraceStatus.CANCELLED -> "○"
}
