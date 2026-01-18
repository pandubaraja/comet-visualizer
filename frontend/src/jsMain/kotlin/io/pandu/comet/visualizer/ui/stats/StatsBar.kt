package io.pandu.comet.visualizer.ui.stats

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.data.TraceStats
import org.jetbrains.compose.web.dom.Div

@Composable
fun StatsBar(stats: TraceStats) {
    Div({
        classes(
            "mt-4",
            "grid",
            "grid-cols-5",
            "gap-3"
        )
    }) {
        StatItem("text-blue-500", stats.running, "Running")
        StatItem("text-emerald-500", stats.completed, "Completed")
        StatItem("text-amber-500", stats.cancelled, "Cancelled")
        StatItem("text-red-500", stats.failed, "Failed")
        StatItem("text-orange-500", stats.unstructured, "Unstructured")
    }
}
