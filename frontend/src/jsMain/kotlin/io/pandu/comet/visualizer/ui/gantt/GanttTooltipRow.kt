package io.pandu.comet.visualizer.ui.gantt

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun GanttTooltipRow(label: String, value: String, valueColor: String = "text-slate-200") {
    Div({
        classes(
            "flex", "justify-between", "gap-4",
            "mt-1", "text-slate-400"
        )
    }) {
        Span({}) { Text(label) }
        Span({ classes("font-mono", valueColor) }) { Text(value) }
    }
}