package io.pandu.comet.visualizer.ui

import androidx.compose.runtime.*
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.TraceState
import io.pandu.comet.visualizer.TraceStatus
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import kotlin.math.max
import kotlin.math.min

@Composable
fun GanttView(traceState: TraceState) {
    var hoveredNode by remember { mutableStateOf<TraceNode?>(null) }
    var tooltipPosition by remember { mutableStateOf(Pair(0, 0)) }
    var scale by remember { mutableStateOf(10.0) } // pixels per ms

    val allNodes = traceState.traces.values.toList()
    val maxTime = allNodes.maxOfOrNull { node ->
        if (node.durationMs > 0) node.startMs + node.durationMs else node.startMs + 100
    } ?: 0.0

    val orderedNodes = remember(allNodes) { orderNodes(traceState) }

    Div({
        classes(
            "flex", "flex-col",
            "h-[calc(100vh-180px)]",
            "overflow-hidden"
        )
        ref { element ->
            val handler: (org.w3c.dom.events.Event) -> Unit = { event ->
                val wheelEvent = event as WheelEvent
                if (wheelEvent.ctrlKey || wheelEvent.metaKey) {
                    event.preventDefault()
                    event.stopPropagation()
                    val delta = if (wheelEvent.deltaY < 0) 1.2 else 0.8
                    scale = min(50.0, max(5.0, scale * delta))
                }
            }
            element.addEventListener("wheel", handler, js("{ passive: false }"))
            onDispose {
                element.removeEventListener("wheel", handler)
            }
        }
    }) {
        // Scale info
        Div({
            classes(
                "px-3", "py-2",
                "text-xs", "text-slate-500",
                "border-b", "border-white/10",
                "flex", "justify-between", "items-center"
            )
        }) {
            Span({}) { Text("Timeline: 0ms - ${maxTime.toInt()}ms") }
            Span({ classes("text-[0.7rem]", "opacity-60") }) {
                Text("Ctrl + Scroll to zoom")
            }
        }

        // Content
        Div({ classes("flex-1", "flex", "overflow-hidden") }) {
            // Labels column
            Div({
                classes(
                    "min-w-[200px]", "max-w-[200px]",
                    "overflow-y-auto", "overflow-x-hidden",
                    "border-r", "border-white/10",
                    "bg-comet-bg", "flex-shrink-0"
                )
            }) {
                Div({
                    classes(
                        "px-3", "py-2",
                        "text-xs", "font-semibold", "text-slate-400",
                        "border-b", "border-white/10",
                        "bg-white/[0.02]",
                        "min-h-[28px]"
                    )
                }) { Text("Operation") }
                Div({}) {
                    orderedNodes.forEach { (node, depth) ->
                        GanttLabelRow(node, depth)
                    }
                }
            }

            // Timeline column
            Div({ classes("flex-1", "overflow-auto") }) {
                // Time header
                Div({
                    classes(
                        "border-b", "border-white/10",
                        "bg-white/[0.02]",
                        "sticky", "top-0", "z-10",
                        "min-h-[28px]"
                    )
                }) {
                    Div({
                        classes("relative", "min-h-[28px]")
                        style { property("width", "${(maxTime + 50) * scale}px") }
                    }) {
                        val step = getTimeStep(maxTime, scale)
                        var t = 0.0
                        while (t <= maxTime + 50) {
                            Div({
                                classes(
                                    "absolute", "top-0", "bottom-0",
                                    "border-l", "border-white/10",
                                    "px-2", "py-1",
                                    "text-[0.65rem]", "text-slate-500",
                                    "font-mono"
                                )
                                style { property("left", "${t * scale}px") }
                            }) {
                                Text("${t.toInt()}ms")
                            }
                            t += step
                        }
                    }
                }

                // Bars
                Div({
                    style { property("width", "${(maxTime + 50) * scale}px") }
                }) {
                    orderedNodes.forEach { (node, depth) ->
                        GanttBarRow(
                            node = node,
                            depth = depth,
                            scale = scale,
                            maxTime = maxTime,
                            onHover = { n, x, y ->
                                hoveredNode = n
                                tooltipPosition = Pair(x, y)
                            },
                            onLeave = { hoveredNode = null }
                        )
                    }
                }
            }
        }
    }

    // Tooltip
    hoveredNode?.let { node ->
        GanttTooltip(node, tooltipPosition.first, tooltipPosition.second)
    }
}

@Composable
private fun GanttLabelRow(node: TraceNode, depth: Int) {
    Div({
        classes(
            "min-h-[32px]",
            "px-3", "py-1.5",
            "flex", "items-center", "gap-2",
            "border-b", "border-white/10",
            "overflow-hidden",
            "transition-colors", "duration-150",
            "hover:bg-white/[0.06]"
        )
        if (depth == 0) {
            classes("bg-white/[0.03]")
        }
    }) {
        Span({
            style { property("width", "${depth * 16}px") }
        })
        GanttStatusIcon(node.status)
        Span({
            classes(
                "text-xs",
                "whitespace-nowrap", "overflow-hidden", "text-ellipsis"
            )
        }) {
            Text(node.operation)
        }
    }
}

@Composable
private fun GanttBarRow(
    node: TraceNode,
    depth: Int,
    scale: Double,
    maxTime: Double,
    onHover: (TraceNode, Int, Int) -> Unit,
    onLeave: () -> Unit
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
            "border-b", "border-white/10",
            "transition-colors", "duration-150",
            "hover:bg-white/[0.06]"
        )
        if (depth == 0) {
            classes("bg-white/[0.03]")
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

@Composable
private fun GanttStatusIcon(status: TraceStatus) {
    val (bgColor, icon) = when (status) {
        TraceStatus.RUNNING -> "bg-blue-500" to "●"
        TraceStatus.COMPLETED -> "bg-emerald-500" to "✓"
        TraceStatus.FAILED -> "bg-red-500" to "✗"
        TraceStatus.CANCELLED -> "bg-amber-500" to "○"
    }

    Span({
        classes(
            "w-[18px]", "h-[18px]",
            "rounded-full",
            "flex", "items-center", "justify-center",
            "text-[10px]", "text-white",
            "flex-shrink-0",
            bgColor
        )
    }) {
        Text(icon)
    }
}

@Composable
private fun GanttTooltip(node: TraceNode, x: Int, y: Int) {
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
            GanttStatusIcon(node.status)
            Text(node.operation)
        }
        GanttTooltipRow("Status", node.status.name.lowercase().replaceFirstChar { it.uppercase() })
        GanttTooltipRow("Duration", if (node.durationMs > 0) "${node.durationMs.format(1)}ms" else "...")
        GanttTooltipRow("Start", "+${node.startMs.format(1)}ms")
        GanttTooltipRow("Dispatcher", node.dispatcher)
    }
}

@Composable
private fun GanttTooltipRow(label: String, value: String) {
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

private fun orderNodes(traceState: TraceState): List<Pair<TraceNode, Int>> {
    val result = mutableListOf<Pair<TraceNode, Int>>()
    val processed = mutableSetOf<String>()

    fun addNodeWithChildren(node: TraceNode, depth: Int) {
        if (node.id in processed) return
        processed.add(node.id)
        result.add(node to depth)
        node.children.sortedBy { it.startMs }.forEach { child ->
            traceState.traces[child.id]?.let { addNodeWithChildren(it, depth + 1) }
        }
    }

    traceState.getRootNodes().sortedBy { it.startMs }.forEach { addNodeWithChildren(it, 0) }
    return result
}

private fun getTimeStep(maxTime: Double, scale: Double): Double {
    val minStep = 60 / scale
    return when {
        minStep < 10 -> 10.0
        minStep < 25 -> 25.0
        minStep < 50 -> 50.0
        minStep < 100 -> 100.0
        minStep < 250 -> 250.0
        minStep < 500 -> 500.0
        minStep < 1000 -> 1000.0
        else -> 5000.0
    }
}

private fun Double.format(decimals: Int): String {
    return this.asDynamic().toFixed(decimals) as String
}
