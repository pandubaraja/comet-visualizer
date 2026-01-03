package io.pandu.comet.visualizer.ui.tree

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.TraceStatus
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun TreeStatusIcon(status: TraceStatus) {
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