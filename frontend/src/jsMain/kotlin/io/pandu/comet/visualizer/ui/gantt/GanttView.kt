package io.pandu.comet.visualizer.ui.gantt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.data.TraceState
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import org.w3c.dom.events.WheelEvent
import kotlin.math.max
import kotlin.math.min

@Composable
fun GanttView(
    traceState: TraceState,
    selectedNodeId: String? = null,
    onNodeSelect: (TraceNode) -> Unit = {}
) {
    var hoveredNode by remember { mutableStateOf<TraceNode?>(null) }
    var tooltipPosition by remember { mutableStateOf(Pair(0, 0)) }
    var scale by remember { mutableStateOf(10.0) } // pixels per ms
    var timelineElement by remember { mutableStateOf<Element?>(null) }

    val allNodes = traceState.traces.values.toList()
    val maxTime = allNodes.maxOfOrNull { node ->
        if (node.durationMs > 0) node.startMs + node.durationMs else node.startMs + 100
    } ?: 0.0

    val orderedNodes = remember(allNodes) { orderNodes(traceState) }

    fun selectNode(node: TraceNode) {
        onNodeSelect(node)
        timelineElement?.let { element ->
            val scrollLeft = (node.startMs * scale - element.clientWidth / 3).coerceAtLeast(0.0)
            element.scrollLeft = scrollLeft
        }
    }

    Div({
        classes(
            "flex", "flex-col",
            "h-[calc(100vh-140px)]",
            "overflow-hidden"
        )
        ref { element ->
            val handler: (Event) -> Unit = { event ->
                val wheelEvent = event as WheelEvent
                if (wheelEvent.ctrlKey || wheelEvent.metaKey) {
                    event.preventDefault()
                    event.stopPropagation()
                    val delta = if (wheelEvent.deltaY < 0) 1.2 else 0.8
                    scale = min(100.0, max(0.1, scale * delta))
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
                "border-b", "border-slate-200", "dark:border-white/10",
                "flex", "justify-between", "items-center"
            )
        }) {
            Span({}) { Text("Timeline: 0ms - ${maxTime.toInt()}ms") }
            Span({ classes("text-[0.7rem]", "opacity-60") }) {
                Text("Ctrl + Scroll to zoom")
            }
        }

        // Content - Timeline only (labels are in sidebar)
        Div({
            classes("flex-1", "overflow-auto")
            ref { el ->
                timelineElement = el
                onDispose { }
            }
        }) {
            // Time header
            Div({
                classes(
                    "border-b", "border-slate-200", "dark:border-white/10",
                    "bg-slate-50", "dark:bg-white/[0.02]",
                    "sticky", "top-0", "z-10",
                    "min-h-[32px]", "relative"
                )
                style { property("width", "${(maxTime + 50) * scale}px") }
            }) {
                val step = getTimeStep(maxTime, scale)
                var t = 0.0
                while (t <= maxTime + 50) {
                    Div({
                        classes(
                            "absolute", "top-0", "bottom-0",
                            "border-l", "border-slate-200", "dark:border-white/10",
                            "px-3", "py-2",
                            "text-xs", "font-semibold", "text-slate-500",
                            "font-mono", "whitespace-nowrap"
                        )
                        style { property("left", "${t * scale}px") }
                    }) {
                        Text("${t.toInt()}ms")
                    }
                    t += step
                }
            }

            // Bars
            if (orderedNodes.isEmpty()) {
                Div({ classes("text-center", "py-12", "text-slate-500") }) {
                    Div({ classes("text-4xl", "mb-3") }) { Text("") }
                    Div({ classes("text-xl", "mb-3") }) { Text("\uD83D\uDC63 Waiting for traces...") }
                }
            } else {
                Div({
                    classes("relative")
                    style { property("width", "${(maxTime + 50) * scale}px") }
                }) {
                    orderedNodes.forEach { (node, _) ->
                        GanttBarRow(
                            node = node,
                            scale = scale,
                            maxTime = maxTime,
                            isSelected = node.id == selectedNodeId,
                            onHover = { n, x, y ->
                                hoveredNode = n
                                tooltipPosition = Pair(x, y)
                            },
                            onLeave = { hoveredNode = null },
                            onClick = { selectNode(node) }
                        )
                    }

                    // Vertical grid lines
                    Div({
                        classes("absolute", "inset-0", "pointer-events-none", "z-[1]")
                    }) {
                        val step = getTimeStep(maxTime, scale)
                        var t = 0.0
                        while (t <= maxTime + 50) {
                            Div({
                                classes(
                                    "absolute", "top-0", "bottom-0",
                                    "border-l", "border-slate-300/50", "dark:border-white/5"
                                )
                                style { property("left", "${t * scale}px") }
                            })
                            t += step
                        }
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
