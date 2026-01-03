package io.pandu.comet.visualizer.ui.timeline

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.data.TimelineEvent
import io.pandu.comet.visualizer.format
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.events.MouseEvent

@Composable
fun TimelineItem(
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
            "border-b", "border-slate-200", "dark:border-white/10",
            "animate-fadeIn",
            "cursor-pointer",
            "transition-colors", "duration-150",
            "hover:bg-slate-100", "dark:hover:bg-white/[0.06]"
        )
        onMouseMove { e ->
            val mouseEvent = e.nativeEvent as MouseEvent
            onHover(event, mouseEvent.clientX, mouseEvent.clientY)
        }
        onMouseLeave { onLeave() }
    }) {
        Span({
            classes(
                "font-mono", "text-[0.7rem]",
                "text-slate-500", "dark:text-slate-500",
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
