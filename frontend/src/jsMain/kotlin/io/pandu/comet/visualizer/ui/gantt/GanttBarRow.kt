package io.pandu.comet.visualizer.ui.gantt

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.TraceStatus
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.events.MouseEvent
import kotlin.math.max

@Composable
fun GanttBarRow(
    node: TraceNode,
    scale: Double,
    maxTime: Double,
    isSelected: Boolean = false,
    onHover: (TraceNode, Int, Int) -> Unit,
    onLeave: () -> Unit,
    onClick: () -> Unit = {}
) {
    val status = node.status
    val leftPos = node.startMs * scale
    val barWidth = if (node.durationMs > 0) {
        max(node.durationMs * scale, 4.0)
    } else {
        max((maxTime - node.startMs) * scale * 0.3, 20.0)
    }

    val barGradientClass = when (status) {
        TraceStatus.RUNNING -> "gantt-bar-running"
        TraceStatus.COMPLETED -> "gantt-bar-completed"
        TraceStatus.FAILED -> "gantt-bar-failed"
        TraceStatus.CANCELLED -> "gantt-bar-cancelled"
    }

    Div({
        classes(
            "min-h-[32px]", "relative",
            "border-b", "border-slate-200", "dark:border-white/10",
            "transition-colors", "duration-150"
        )
        if (isSelected) {
            classes("bg-blue-100", "dark:bg-blue-900/30")
        } else {
            classes("hover:bg-slate-100", "dark:hover:bg-white/[0.06]")
        }
    }) {
        Div({
            classes(
                "absolute", "top-1",
                "h-6", "rounded",
                "min-w-1",
                "transition-all", "duration-300",
                "shadow-md",
                "cursor-pointer",
                "flex", "items-center",
                "px-1.5", "overflow-hidden",
                "hover:brightness-110", "hover:z-[4]",
                barGradientClass
            )
            if (isSelected) {
                classes("ring-2", "ring-blue-500", "ring-offset-1", "z-[5]")
            }
            if (status == TraceStatus.RUNNING) {
                classes("animate-pulse")
            }
            style {
                property("left", "${leftPos}px")
                property("width", "${barWidth}px")
            }
            onMouseMove { event ->
                val mouseEvent = event.nativeEvent as MouseEvent
                onHover(node, mouseEvent.clientX, mouseEvent.clientY)
            }
            onMouseLeave { onLeave() }
            onClick { onClick() }
        }) {
            Span({
                classes(
                    "text-[0.7rem]", "text-white", "font-medium",
                    "whitespace-nowrap", "overflow-hidden", "text-ellipsis"
                )
            }) {
                Text(node.operation)
            }
        }
    }
}