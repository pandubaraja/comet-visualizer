package io.pandu.comet.visualizer.ui.gantt

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.format
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun GanttTooltip(node: TraceNode, x: Int, y: Int) {
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
            Text(if (node.operation.isEmpty() || node.operation == "coroutine") node.id else node.operation)
        }
        GanttTooltipRow("Status", node.status.name.lowercase().replaceFirstChar { it.uppercase() })
        GanttTooltipRow("Duration", if (node.durationMs > 0) "${node.durationMs.format(1)}ms" else "...")
        GanttTooltipRow("Start", "+${node.startMs.format(1)}ms")
        GanttTooltipRow("Dispatcher", node.dispatcher)
        if (node.sourceFile.isNotEmpty()) {
            GanttTooltipRow("Source", if (node.lineNumber > 0) "${node.sourceFile}:${node.lineNumber}" else node.sourceFile)
        }
    }
}