package io.pandu.comet.visualizer.ui

import androidx.compose.runtime.*
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.TraceState
import io.pandu.comet.visualizer.TraceStatus
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.events.MouseEvent

@Composable
fun TreeView(traceState: TraceState) {
    var hoveredNode by remember { mutableStateOf<TraceNode?>(null) }
    var tooltipPosition by remember { mutableStateOf(Pair(0, 0)) }

    Div({ classes("py-2") }) {
        val rootNodes = traceState.getRootNodes()
        if (rootNodes.isEmpty()) {
            Div({ classes("text-center", "py-12", "text-slate-500") }) {
                Div({ classes("text-4xl", "mb-3") }) { Text("") }
                Div({}) { Text("Waiting for traces...") }
            }
        } else {
            rootNodes.forEach { node ->
                TreeNodeItem(
                    node = node,
                    allNodes = traceState.traces,
                    onHover = { n, x, y ->
                        hoveredNode = n
                        tooltipPosition = Pair(x, y)
                    },
                    onLeave = { hoveredNode = null }
                )
            }
        }
    }

    // Tooltip
    hoveredNode?.let { node ->
        TreeTooltip(node, tooltipPosition.first, tooltipPosition.second)
    }
}

@Composable
private fun TreeNodeItem(
    node: TraceNode,
    allNodes: Map<String, TraceNode>,
    onHover: (TraceNode, Int, Int) -> Unit,
    onLeave: () -> Unit
) {
    val status = node.status
    val borderColor = when (status) {
        TraceStatus.RUNNING -> "border-l-blue-500"
        TraceStatus.COMPLETED -> "border-l-emerald-500"
        TraceStatus.FAILED -> "border-l-red-500"
        TraceStatus.CANCELLED -> "border-l-amber-500"
    }
    val bgClass = if (status == TraceStatus.RUNNING) "bg-blue-500/10" else "bg-white/[0.03]"

    Div({ classes("relative", "my-1", "transition-opacity", "duration-200") }) {
        Div({
            classes(
                "flex", "items-center", "gap-2",
                "px-3", "py-2",
                bgClass,
                "rounded-md",
                "border-l-[3px]", borderColor,
                "transition-all", "duration-200",
                "cursor-pointer",
                "hover:bg-white/[0.06]"
            )
            onMouseMove { event ->
                val mouseEvent = event.nativeEvent as MouseEvent
                onHover(node, mouseEvent.clientX, mouseEvent.clientY)
            }
            onMouseLeave { onLeave() }
        }) {
            StatusIcon(status)
            Span({ classes("font-medium", "text-sm") }) {
                Text(node.operation)
            }
            Div({ classes("ml-auto", "flex", "gap-2", "items-center") }) {
                Span({
                    classes(
                        "font-mono", "text-xs",
                        "text-blue-400", "bg-blue-500/15",
                        "px-1.5", "py-0.5", "rounded"
                    )
                }) {
                    Text(if (node.durationMs > 0) "${node.durationMs.format(1)}ms" else "...")
                }
                Span({
                    classes(
                        "text-[0.7rem]", "text-slate-400",
                        "bg-white/[0.08]",
                        "px-1.5", "py-0.5", "rounded"
                    )
                }) {
                    Text(node.dispatcher.substringAfterLast("."))
                }
            }
        }

        // Children
        val children = node.children
        if (children.isNotEmpty()) {
            Div({
                classes(
                    "ml-6", "pl-3",
                    "border-l", "border-dashed", "border-white/15"
                )
            }) {
                children.forEach { child ->
                    val updatedChild = allNodes[child.id] ?: child
                    TreeNodeItem(updatedChild, allNodes, onHover, onLeave)
                }
            }
        }
    }
}

@Composable
private fun StatusIcon(status: TraceStatus) {
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
        if (status == TraceStatus.RUNNING) {
            classes("animate-pulse")
        }
    }) {
        Text(icon)
    }
}

@Composable
private fun TreeTooltip(node: TraceNode, x: Int, y: Int) {
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
            StatusIcon(node.status)
            Text(node.operation)
        }
        TooltipRow("Status", node.status.name.lowercase().replaceFirstChar { it.uppercase() })
        TooltipRow("Duration", if (node.durationMs > 0) "${node.durationMs.format(1)}ms" else "running...")
        TooltipRow("Start", "+${node.startMs.format(1)}ms")
        TooltipRow("Dispatcher", node.dispatcher)
        TooltipRow("Span ID", node.id.take(16) + "...")
        node.parentId?.let { parentId ->
            TooltipRow("Parent ID", parentId.take(16) + "...")
        }
    }
}

@Composable
private fun TooltipRow(label: String, value: String) {
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

private fun Double.format(decimals: Int): String {
    return this.asDynamic().toFixed(decimals) as String
}
