package io.pandu.comet.visualizer.ui

import androidx.compose.runtime.*
import io.pandu.comet.visualizer.TimelineEvent
import io.pandu.comet.visualizer.TraceStatus
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.events.MouseEvent

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

@Composable
private fun TimelineItem(
    event: TimelineEvent,
    onHover: (TimelineEvent, Int, Int) -> Unit,
    onLeave: () -> Unit
) {
    val traceEvent = event.event
    val type = if (traceEvent.type == "started") "started" else traceEvent.status
    val detail = if (traceEvent.type == "started") "Started" else {
        if (traceEvent.durationMs > 0) "${traceEvent.durationMs.format(1)}ms" else traceEvent.status
    }

    val dotColor = when (type) {
        "started" -> "bg-blue-500"
        "completed" -> "bg-emerald-500"
        "failed" -> "bg-red-500"
        "cancelled" -> "bg-amber-500"
        else -> "bg-slate-500"
    }

    Div({
        classes(
            "flex", "items-start", "gap-3",
            "py-2",
            "border-b", "border-white/10",
            "animate-fadeIn",
            "cursor-pointer",
            "transition-colors", "duration-150",
            "hover:bg-white/[0.06]"
        )
        onMouseMove { e ->
            val mouseEvent = e.nativeEvent as MouseEvent
            onHover(event, mouseEvent.clientX, mouseEvent.clientY)
        }
        onMouseLeave { onLeave() }
    }) {
        Span({
            classes(
                "font-mono", "text-[0.7rem]", "text-slate-500",
                "min-w-[70px]"
            )
        }) {
            Text(event.timeOffset)
        }
        Span({
            classes(
                "w-2", "h-2",
                "rounded-full",
                "mt-1", "flex-shrink-0",
                dotColor
            )
        })
        Div({ classes("flex-1", "min-w-0") }) {
            Div({
                classes(
                    "text-sm", "font-medium",
                    "whitespace-nowrap", "overflow-hidden", "text-ellipsis"
                )
            }) {
                Text(traceEvent.operation)
            }
            Div({ classes("text-xs", "text-slate-500", "mt-0.5") }) {
                Text(detail)
            }
        }
    }
}

@Composable
private fun TimelineTooltip(event: TimelineEvent, x: Int, y: Int) {
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

@Composable
private fun TimelineTooltipRow(label: String, value: String) {
    Div({
        classes(
            "flex", "justify-between", "gap-4",
            "mt-1", "text-slate-400"
        )
    }) {
        Span({}) { Text(label) }
        Span({ classes("font-mono", "text-slate-200") }) { Text(value) }
    }
}

private fun getStatusIcon(status: TraceStatus): String = when (status) {
    TraceStatus.RUNNING -> "●"
    TraceStatus.COMPLETED -> "✓"
    TraceStatus.FAILED -> "✗"
    TraceStatus.CANCELLED -> "○"
}

private fun Double.format(decimals: Int): String {
    return this.asDynamic().toFixed(decimals) as String
}
