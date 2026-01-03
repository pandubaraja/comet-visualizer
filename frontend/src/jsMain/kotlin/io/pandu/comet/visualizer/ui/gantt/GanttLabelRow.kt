package io.pandu.comet.visualizer.ui.gantt

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.TraceNode
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun GanttLabelRow(
    node: TraceNode,
    depth: Int,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Div({
        classes(
            "min-h-[32px]",
            "px-3", "py-1.5",
            "flex", "items-center", "gap-2",
            "border-b", "border-slate-200", "dark:border-white/10",
            "overflow-hidden",
            "transition-colors", "duration-150",
            "cursor-pointer"
        )
        if (isSelected) {
            classes("bg-blue-100", "dark:bg-blue-900/30", "border-l-2", "border-l-blue-500")
        } else {
            classes("hover:bg-slate-100", "dark:hover:bg-white/[0.06]")
            if (depth == 0) {
                classes("bg-slate-50", "dark:bg-white/[0.03]")
            }
        }
        onClick { onClick() }
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