package io.pandu.comet.visualizer.ui.stats

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.data.TraceStats
import io.pandu.comet.visualizer.format
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun StatsBar(
    stats: TraceStats,
    operations: List<String> = emptyList(),
    selectedOperation: String? = null,
    onOperationChange: (String?) -> Unit = {}
) {
    Div({ classes("space-y-2") }) {
        // Row 1: Counts
        Div({
            classes(
                "grid",
                "grid-cols-5",
                "gap-px"
            )
        }) {
            StatItem("text-blue-500", stats.running, "Running")
            StatItem("text-emerald-500", stats.completed, "Completed")
            StatItem("text-amber-500", stats.cancelled, "Cancelled")
            StatItem("text-red-500", stats.failed, "Failed")
            StatItem("text-orange-500", stats.unstructured, "Unstructured")
        }

        // Row 2: Latency stats with filter
        Div({
            classes(
                "flex", "items-center", "gap-3",
                "px-3", "py-2",
                "bg-slate-50", "dark:bg-slate-800/50",
                "rounded-lg"
            )
        }) {
            // Operation filter dropdown
            Div({ classes("flex", "items-center", "gap-2", "flex-shrink-0") }) {
                Span({
                    classes(
                        "text-xs", "font-medium",
                        "text-slate-500", "dark:text-slate-400"
                    )
                }) {
                    Text("Latency")
                }
                Select({
                    classes(
                        "text-xs",
                        "bg-white", "dark:bg-slate-700",
                        "border", "border-slate-200", "dark:border-slate-600",
                        "rounded", "px-2", "py-1",
                        "text-slate-700", "dark:text-slate-200",
                        "focus:outline-none", "focus:ring-1", "focus:ring-blue-500"
                    )
                    onChange { event ->
                        val value = event.target.unsafeCast<org.w3c.dom.HTMLSelectElement>().value
                        onOperationChange(if (value == "") null else value)
                    }
                    attr("value", selectedOperation ?: "")
                }) {
                    Option("") {
                        Text("All Operations")
                    }
                    operations.forEach { op ->
                        Option(op) {
                            Text(op)
                        }
                    }
                }
            }

            // Latency metrics
            Div({
                classes(
                    "flex", "items-center", "gap-4",
                    "overflow-x-auto", "flex-1"
                )
            }) {
                LatencyItem("P50", stats.latency.p50)
                LatencyItem("P90", stats.latency.p90)
                LatencyItem("P99", stats.latency.p99)
                LatencyItem("Min", stats.latency.min)
                LatencyItem("Max", stats.latency.max)
                LatencyItem("Mean", stats.latency.mean)

                // Sample count
                Span({
                    classes(
                        "text-xs",
                        "text-slate-400", "dark:text-slate-500",
                        "ml-auto", "flex-shrink-0"
                    )
                }) {
                    Text("n=${stats.latency.count}")
                }
            }
        }
    }
}

@Composable
private fun LatencyItem(label: String, value: Double) {
    Div({ classes("flex", "items-baseline", "gap-1", "flex-shrink-0") }) {
        Span({
            classes(
                "text-xs", "font-medium",
                "text-slate-500", "dark:text-slate-400"
            )
        }) {
            Text(label)
        }
        Span({
            classes(
                "text-sm", "font-mono", "font-semibold",
                "text-slate-700", "dark:text-slate-200"
            )
        }) {
            Text(if (value > 0) "${value.format(1)}ms" else "-")
        }
    }
}
