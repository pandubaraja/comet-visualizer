package io.pandu.comet.visualizer.ui.gantt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.data.TraceState
import kotlinx.browser.document
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

    // Scroll to selected node when it changes
    LaunchedEffect(selectedNodeId, scale) {
        selectedNodeId?.let { id ->
            val node = traceState.traces[id]
            if (node != null) {
                // Scroll horizontally to node's start position with left margin
                timelineElement?.let { element ->
                    element.scrollLeft = (node.startMs * scale - 20).coerceAtLeast(0.0)
                }
                // Scroll vertically to center the bar
                document.getElementById("gantt-bar-$id")?.let { element ->
                    element.asDynamic().scrollIntoView(
                        js("{ behavior: 'smooth', block: 'center' }")
                    )
                }
            }
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

                    timelineElement?.let { timeline ->
                        // Get mouse X position relative to timeline viewport
                        val rect = timeline.getBoundingClientRect()
                        val mouseX = wheelEvent.clientX - rect.left

                        // Calculate the time value under the mouse pointer
                        val timeAtMouse = (timeline.scrollLeft + mouseX) / scale

                        // Apply zoom
                        val delta = if (wheelEvent.deltaY < 0) 1.05 else 0.95
                        val newScale = min(100.0, max(0.1, scale * delta))

                        // Adjust scroll so the same time stays under the mouse
                        val newScrollLeft = timeAtMouse * newScale - mouseX

                        scale = newScale
                        timeline.scrollLeft = newScrollLeft.coerceAtLeast(0.0)
                    }
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
                Div({
                    classes(
                        "flex", "flex-col", "items-center", "justify-center",
                        "py-16", "gap-4"
                    )
                }) {
                    Div({
                        classes(
                            "w-14", "h-14", "rounded-full",
                            "bg-slate-200", "dark:bg-neutral-700",
                            "flex", "items-center", "justify-center",
                            "animate-pulse"
                        )
                    }) {
                        Span({
                            classes("w-7", "h-7", "text-slate-400", "dark:text-neutral-500")
                            ref { element ->
                                element.innerHTML = """<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 0 1 3 19.875v-6.75ZM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V8.625ZM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 0 1-1.125-1.125V4.125Z" /></svg>"""
                                onDispose { }
                            }
                        })
                    }
                    Div({ classes("text-center") }) {
                        Div({
                            classes(
                                "text-base", "font-medium",
                                "text-slate-600", "dark:text-neutral-300"
                            )
                        }) {
                            Text("Waiting for traces")
                        }
                        Div({
                            classes(
                                "text-sm", "mt-1",
                                "text-slate-400", "dark:text-neutral-500"
                            )
                        }) {
                            Text("Timeline will appear here")
                        }
                    }
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
        // Look up children by ID from the map and sort by startMs
        node.childIds
            .mapNotNull { traceState.traces[it] }
            .sortedBy { it.startMs }
            .forEach { child -> addNodeWithChildren(child, depth + 1) }
    }

    traceState.getRootNodes().forEach { addNodeWithChildren(it, 0) }
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
