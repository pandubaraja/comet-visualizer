package io.pandu.comet.visualizer.ui.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.TraceStatus
import io.pandu.comet.visualizer.data.TraceState
import io.pandu.comet.visualizer.format
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.math.roundToInt

private const val NODE_WIDTH = 180
private const val NODE_HEIGHT = 60
private const val HORIZONTAL_GAP = 100
private const val VERTICAL_GAP = 20

private data class NodePosition(
    val node: TraceNode,
    val x: Int,
    val y: Int,
    val level: Int
)

private data class Connection(
    val fromX: Int,
    val fromY: Int,
    val toX: Int,
    val toY: Int
)

private fun calculateTreeLayout(
    rootNodes: List<TraceNode>,
    allNodes: Map<String, TraceNode>
): Pair<List<NodePosition>, List<Connection>> {
    val positions = mutableListOf<NodePosition>()
    val connections = mutableListOf<Connection>()
    val visited = mutableSetOf<String>()
    val nodeYPositions = mutableMapOf<String, Int>() // Track actual Y position of each node

    fun layoutNode(node: TraceNode, level: Int, startY: Int): Int {
        // Prevent infinite recursion from circular references
        if (node.id in visited || level > 20) {
            return NODE_HEIGHT
        }
        visited.add(node.id)

        // Look up children by ID from the map and sort by startMs for stable ordering
        val children = node.childIds
            .mapNotNull { allNodes[it] }
            .sortedBy { it.startMs }
        val x = level * (NODE_WIDTH + HORIZONTAL_GAP)

        if (children.isEmpty()) {
            positions.add(NodePosition(node, x, startY, level))
            nodeYPositions[node.id] = startY + NODE_HEIGHT / 2
            return NODE_HEIGHT
        }

        var currentY = startY
        children.forEach { child ->
            val childHeight = layoutNode(child, level + 1, currentY)
            currentY += childHeight + VERTICAL_GAP
        }

        // Get actual child center positions after they've been laid out
        val childCenters = children.mapNotNull { nodeYPositions[it.id] }

        // Position parent at center of children
        val centerY = if (childCenters.isNotEmpty()) {
            (childCenters.first() + childCenters.last()) / 2 - NODE_HEIGHT / 2
        } else {
            startY
        }
        positions.add(NodePosition(node, x, centerY, level))
        nodeYPositions[node.id] = centerY + NODE_HEIGHT / 2

        // Create connections from parent to actual child positions
        childCenters.forEach { childCenterY ->
            connections.add(Connection(
                fromX = x + NODE_WIDTH,
                fromY = centerY + NODE_HEIGHT / 2,
                toX = x + NODE_WIDTH + HORIZONTAL_GAP,
                toY = childCenterY
            ))
        }

        return maxOf(NODE_HEIGHT, currentY - startY - VERTICAL_GAP)
    }

    var currentY = 40
    rootNodes.forEach { root ->
        visited.clear()
        val height = layoutNode(root, 0, currentY)
        currentY += height + VERTICAL_GAP * 2
    }

    return Pair(positions, connections)
}

private const val MIN_ZOOM = 0.25
private const val MAX_ZOOM = 2.0
private const val ZOOM_STEP = 0.02

@Composable
fun HorizontalTreeGraph(
    traceState: TraceState,
    selectedNodeId: String? = null,
    onNodeSelect: (TraceNode) -> Unit = {}
) {
    var zoom by remember { mutableStateOf(1.0) }

    // Create stable snapshot sorted by startMs to prevent layout jumping
    val traces = traceState.traces.toMap()
    val rootNodes = traces.values
        .filter { it.parentId == null || !traces.containsKey(it.parentId) }
        .sortedBy { it.startMs }

    if (rootNodes.isEmpty()) {
        Div({
            classes(
                "flex", "flex-col", "items-center", "justify-center",
                "h-full", "gap-4"
            )
        }) {
            Div({
                classes(
                    "w-16", "h-16", "rounded-full",
                    "bg-slate-200", "dark:bg-neutral-700",
                    "flex", "items-center", "justify-center",
                    "animate-pulse"
                )
            }) {
                Span({
                    classes("w-8", "h-8", "text-slate-400", "dark:text-neutral-500")
                    ref { element ->
                        element.innerHTML = """<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6A2.25 2.25 0 0 1 6 3.75h2.25A2.25 2.25 0 0 1 10.5 6v2.25a2.25 2.25 0 0 1-2.25 2.25H6a2.25 2.25 0 0 1-2.25-2.25V6ZM3.75 15.75A2.25 2.25 0 0 1 6 13.5h2.25a2.25 2.25 0 0 1 2.25 2.25V18a2.25 2.25 0 0 1-2.25 2.25H6A2.25 2.25 0 0 1 3.75 18v-2.25ZM13.5 6a2.25 2.25 0 0 1 2.25-2.25H18A2.25 2.25 0 0 1 20.25 6v2.25A2.25 2.25 0 0 1 18 10.5h-2.25a2.25 2.25 0 0 1-2.25-2.25V6ZM13.5 15.75a2.25 2.25 0 0 1 2.25-2.25H18a2.25 2.25 0 0 1 2.25 2.25V18A2.25 2.25 0 0 1 18 20.25h-2.25A2.25 2.25 0 0 1 13.5 18v-2.25Z" /></svg>"""
                        onDispose { }
                    }
                })
            }
            Div({ classes("text-center") }) {
                Div({
                    classes(
                        "text-lg", "font-medium",
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
                    Text("Coroutine traces will appear here")
                }
            }
        }
        return
    }

    val (positions, connections) = calculateTreeLayout(rootNodes, traces)

    val maxX = (positions.maxOfOrNull { it.x + NODE_WIDTH } ?: 0) + 50
    val maxY = (positions.maxOfOrNull { it.y + NODE_HEIGHT } ?: 0) + 50

    // Scroll to selected node when it changes
    LaunchedEffect(selectedNodeId) {
        selectedNodeId?.let { id ->
            document.getElementById("graph-node-$id")?.let { element ->
                element.asDynamic().scrollIntoView(
                    js("{ behavior: 'smooth', block: 'center', inline: 'center' }")
                )
            }
        }
    }

    Div({
        classes("relative", "h-full", "overflow-auto", "p-6")
        ref { element ->
            // Wheel event for Ctrl+Scroll (Windows/Linux) and pinch-to-zoom (Mac trackpad)
            element.addEventListener("wheel", { event ->
                val wheelEvent = event.asDynamic()
                // Pinch-to-zoom on Mac trackpad sets ctrlKey = true
                if (wheelEvent.ctrlKey == true || wheelEvent.metaKey == true) {
                    event.preventDefault()

                    val oldZoom = zoom
                    val delta = if (wheelEvent.deltaY < 0) ZOOM_STEP else -ZOOM_STEP
                    val newZoom = (zoom + delta).coerceIn(MIN_ZOOM, MAX_ZOOM)

                    if (newZoom != oldZoom) {
                        // Get mouse position relative to container
                        val rect = element.getBoundingClientRect()
                        val mouseX = (wheelEvent.clientX as Double) - rect.left
                        val mouseY = (wheelEvent.clientY as Double) - rect.top

                        // Calculate content position under mouse before zoom
                        val contentX = (element.scrollLeft + mouseX) / oldZoom
                        val contentY = (element.scrollTop + mouseY) / oldZoom

                        // Apply new zoom
                        zoom = newZoom

                        // Adjust scroll to keep same content point under mouse
                        element.scrollLeft = contentX * newZoom - mouseX
                        element.scrollTop = contentY * newZoom - mouseY
                    }
                }
            }, js("{ passive: false }"))

            // Gesture events for Safari on Mac
            element.addEventListener("gesturestart", { event ->
                event.preventDefault()
            }, js("{ passive: false }"))

            element.addEventListener("gesturechange", { event ->
                event.preventDefault()
                val gestureEvent = event.asDynamic()
                val scale = gestureEvent.scale as? Double ?: 1.0
                // Dampen the scale change for smoother zooming
                val dampedScale = 1.0 + (scale - 1.0) * 0.3
                val newZoom = (zoom * dampedScale).coerceIn(MIN_ZOOM, MAX_ZOOM)
                zoom = newZoom
            }, js("{ passive: false }"))

            onDispose { }
        }
    }) {
        // Zoom indicator
        Div({
            classes(
                "fixed", "bottom-4", "right-4", "z-50",
                "px-3", "py-1.5", "rounded-full",
                "bg-black/70", "text-white", "text-xs", "font-mono",
                "pointer-events-none"
            )
        }) {
            Text("${(zoom * 100).roundToInt()}% (Pinch or Ctrl+Scroll)")
        }

        Div({
            classes("relative", "tree-graph-grid")
            style {
                property("min-width", "${(maxX * zoom).roundToInt()}px")
                property("min-height", "${(maxY * zoom).roundToInt()}px")
                property("transform", "scale($zoom)")
                property("transform-origin", "top left")
            }
        }) {
            // Draw connections using CSS
            connections.forEach { conn ->
                ConnectionLine(conn)
            }

            // Draw nodes
            positions.forEach { pos ->
                TreeGraphNode(
                    node = pos.node,
                    x = pos.x,
                    y = pos.y,
                    isSelected = pos.node.id == selectedNodeId,
                    onClick = { onNodeSelect(pos.node) }
                )
            }
        }
    }
}

@Composable
private fun ConnectionLine(conn: Connection) {
    val midX = (conn.fromX + conn.toX) / 2

    // Horizontal line from node to midpoint
    Div({
        classes("absolute", "bg-slate-400", "dark:bg-slate-500")
        style {
            property("left", "${conn.fromX}px")
            property("top", "${conn.fromY}px")
            property("width", "${midX - conn.fromX}px")
            property("height", "2px")
        }
    })

    // Vertical line at midpoint
    val minY = minOf(conn.fromY, conn.toY)
    val maxY = maxOf(conn.fromY, conn.toY)
    if (conn.fromY != conn.toY) {
        Div({
            classes("absolute", "bg-slate-400", "dark:bg-slate-500")
            style {
                property("left", "${midX}px")
                property("top", "${minY}px")
                property("width", "2px")
                property("height", "${maxY - minY}px")
            }
        })
    }

    // Horizontal line from midpoint to child
    Div({
        classes("absolute", "bg-slate-400", "dark:bg-slate-500")
        style {
            property("left", "${midX}px")
            property("top", "${conn.toY}px")
            property("width", "${conn.toX - midX}px")
            property("height", "2px")
        }
    })
}

@Composable
private fun TreeGraphNode(
    node: TraceNode,
    x: Int,
    y: Int,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val status = node.status

    val borderColor = if (isSelected) {
        "border-blue-500"
    } else {
        when (status) {
            TraceStatus.RUNNING -> "border-blue-400"
            TraceStatus.COMPLETED -> "border-emerald-400"
            TraceStatus.FAILED -> "border-red-400"
            TraceStatus.CANCELLED -> "border-amber-400"
        }
    }

    val (bgLight, bgDark) = when (status) {
        TraceStatus.RUNNING -> "bg-blue-50" to "dark:bg-blue-900/20"
        TraceStatus.COMPLETED -> "bg-white" to "dark:bg-neutral-800"
        TraceStatus.FAILED -> "bg-red-50" to "dark:bg-red-900/20"
        TraceStatus.CANCELLED -> "bg-amber-50" to "dark:bg-amber-900/20"
    }

    Div({
        id("graph-node-${node.id}")
        classes(
            "absolute",
            "rounded-lg",
            "border-2", borderColor,
            bgLight, bgDark,
            "px-3", "py-2",
            "cursor-pointer",
            "transition-all", "duration-200",
            "hover:shadow-lg"
        )
        if (isSelected) {
            classes("shadow-lg", "ring-2", "ring-blue-500", "ring-offset-2", "dark:ring-offset-neutral-900")
        } else {
            classes("shadow-md", "dark:shadow-neutral-950/50")
        }
        style {
            property("left", "${x}px")
            property("top", "${y}px")
            property("width", "${NODE_WIDTH}px")
            property("height", "${NODE_HEIGHT}px")
            property("z-index", if (isSelected) "20" else "10")
        }
        if (status == TraceStatus.RUNNING) {
            classes("animate-pulse")
        }
        onClick { onClick() }
    }) {
        // Header row with status and operation
        Div({ classes("flex", "items-center", "gap-2", "mb-1") }) {
            TreeStatusIcon(status)
            Span({
                classes(
                    "font-medium", "text-sm",
                    "text-slate-800", "dark:text-slate-200",
                    "truncate", "flex-1"
                )
            }) {
                Text(if (node.operation.isEmpty() || node.operation == "coroutine") node.id else node.operation)
            }
        }

        // Footer row with duration and dispatcher
        Div({ classes("flex", "items-center", "gap-2", "justify-between") }) {
            Span({
                classes(
                    "font-mono", "text-xs",
                    "text-blue-600", "dark:text-blue-400",
                    "bg-blue-100", "dark:bg-blue-500/15",
                    "px-1.5", "py-0.5", "rounded"
                )
            }) {
                Text(if (node.durationMs > 0) "${node.durationMs.format(1)}ms" else "...")
            }
            Span({
                classes(
                    "text-[0.65rem]",
                    "text-slate-500", "dark:text-slate-400",
                    "truncate"
                )
            }) {
                Text(node.dispatcher.substringAfterLast("."))
            }
        }
    }
}
