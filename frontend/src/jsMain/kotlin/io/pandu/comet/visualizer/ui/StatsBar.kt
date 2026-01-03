package io.pandu.comet.visualizer.ui

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.TraceStats
import org.jetbrains.compose.web.dom.*

@Composable
fun StatsBar(stats: TraceStats) {
    Div({ classes("flex", "gap-6", "mt-4") }) {
        StatItem("bg-blue-500", stats.running, "running", true)
        StatItem("bg-emerald-500", stats.completed, "completed", false)
        StatItem("bg-red-500", stats.failed, "failed", false)
    }
}

@Composable
private fun StatItem(dotColor: String, count: Int, label: String, isPulsing: Boolean) {
    Div({ classes("flex", "items-center", "gap-2") }) {
        Span({
            classes(
                "w-2", "h-2",
                "rounded-full",
                dotColor
            )
            if (isPulsing && count > 0) {
                classes("animate-pulse")
            }
        })
        Span({ classes("font-semibold", "text-lg") }) { Text("$count") }
        Span({ classes("text-slate-500", "text-xs") }) { Text(label) }
    }
}
