package io.pandu.comet.visualizer.ui.tree

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.TraceStatus
import io.pandu.comet.visualizer.format
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.events.MouseEvent

@Composable
fun TreeNodeItem(
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
    val isRunning = status == TraceStatus.RUNNING

    Div({ classes("relative", "my-1", "transition-opacity", "duration-200") }) {
        Div({
            classes(
                "flex", "items-center", "gap-2",
                "px-3", "py-2",
                "rounded-md",
                "border-l-[3px]", borderColor,
                "transition-all", "duration-200",
                "cursor-pointer",
                "hover:bg-slate-200", "dark:hover:bg-white/[0.06]"
            )
            if (isRunning) {
                classes("bg-blue-500/10")
            } else {
                classes("bg-slate-100", "dark:bg-white/[0.03]")
            }
            onMouseMove { event ->
                val mouseEvent = event.nativeEvent as MouseEvent
                onHover(node, mouseEvent.clientX, mouseEvent.clientY)
            }
            onMouseLeave { onLeave() }
        }) {
            TreeStatusIcon(status)
            Span({ classes("font-medium", "text-sm") }) {
                Text(node.operation)
            }
            Div({ classes("ml-auto", "flex", "gap-2", "items-center") }) {
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
                        "text-[0.7rem]",
                        "text-slate-500", "dark:text-slate-400",
                        "bg-slate-200", "dark:bg-white/[0.08]",
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
                    "border-l", "border-dashed",
                    "border-slate-300", "dark:border-white/15"
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
