package io.pandu.comet.visualizer.ui.stats

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.data.TraceStats
import org.jetbrains.compose.web.dom.Div

@Composable
fun StatsBar(stats: TraceStats) {
    Div({ classes("flex", "gap-6", "mt-4") }) {
        StatItem("bg-blue-500", stats.running, "Running", true)
        StatItem("bg-emerald-500", stats.completed, "Completed", false)
        StatItem("bg-red-500", stats.failed, "Failed", false)
    }
}
