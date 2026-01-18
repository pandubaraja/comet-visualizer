package io.pandu.comet.visualizer.ui.performance

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.data.LatencyStats
import io.pandu.comet.visualizer.data.TraceState
import io.pandu.comet.visualizer.format
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Table
import org.jetbrains.compose.web.dom.Tbody
import org.jetbrains.compose.web.dom.Td
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Th
import org.jetbrains.compose.web.dom.Thead
import org.jetbrains.compose.web.dom.Tr

@Composable
fun PerformanceView(traceState: TraceState) {
    val overallStats = traceState.getOverallLatencyStats()
    val operationStats = traceState.getOperationStats()

    Div({
        classes("p-6", "space-y-6", "overflow-auto", "h-full")
    }) {
        // Overall Latency Stats Card
        OverallLatencyCard(overallStats)

        // Per-Operation Breakdown Table
        if (operationStats.isNotEmpty()) {
            OperationBreakdownTable(operationStats)
        }
    }
}

@Composable
private fun OverallLatencyCard(stats: LatencyStats) {
    Div({
        classes(
            "bg-white", "dark:bg-neutral-800",
            "rounded-xl", "p-6",
            "border", "border-slate-200", "dark:border-white/10"
        )
    }) {
        Div({
            classes(
                "text-lg", "font-semibold", "mb-4",
                "text-slate-800", "dark:text-slate-200"
            )
        }) {
            Text("Overall Latency Stats")
        }

        Div({
            classes("grid", "grid-cols-6", "gap-4")
        }) {
            LatencyStatCell("P50", stats.p50)
            LatencyStatCell("P90", stats.p90)
            LatencyStatCell("P99", stats.p99)
            LatencyStatCell("Min", stats.min)
            LatencyStatCell("Max", stats.max)
            LatencyStatCell("Mean", stats.mean)
        }

        // Sample count
        Div({
            classes(
                "mt-4", "text-sm",
                "text-slate-500", "dark:text-slate-400"
            )
        }) {
            Text("Total samples: ${stats.count}")
        }
    }
}

@Composable
private fun LatencyStatCell(label: String, value: Double) {
    Div({
        classes(
            "bg-slate-50", "dark:bg-neutral-700/50",
            "rounded-lg", "p-4", "text-center"
        )
    }) {
        Div({
            classes(
                "text-xs", "font-medium", "uppercase", "tracking-wide",
                "text-slate-500", "dark:text-slate-400", "mb-1"
            )
        }) {
            Text(label)
        }
        Div({
            classes(
                "text-xl", "font-mono", "font-semibold",
                "text-slate-800", "dark:text-slate-200"
            )
        }) {
            Text(if (value > 0) "${value.format(1)}ms" else "-")
        }
    }
}

@Composable
private fun OperationBreakdownTable(operationStats: Map<String, LatencyStats>) {
    Div({
        classes(
            "bg-white", "dark:bg-neutral-800",
            "rounded-xl", "overflow-hidden",
            "border", "border-slate-200", "dark:border-white/10"
        )
    }) {
        Div({
            classes(
                "text-lg", "font-semibold", "p-6", "pb-4",
                "text-slate-800", "dark:text-slate-200"
            )
        }) {
            Text("Per-Operation Breakdown")
        }

        Div({
            classes("overflow-x-auto")
        }) {
            Table({
                classes("w-full")
            }) {
                Thead({
                    classes(
                        "bg-slate-50", "dark:bg-neutral-700/50",
                        "border-y", "border-slate-200", "dark:border-white/10"
                    )
                }) {
                    Tr {
                        TableHeader("Operation", "text-left")
                        TableHeader("P50")
                        TableHeader("P90")
                        TableHeader("P99")
                        TableHeader("Min")
                        TableHeader("Max")
                        TableHeader("n")
                    }
                }
                Tbody {
                    operationStats.entries.sortedByDescending { it.value.count }.forEach { (operation, stats) ->
                        Tr({
                            classes(
                                "border-b", "border-slate-100", "dark:border-white/5",
                                "hover:bg-slate-50", "dark:hover:bg-white/[0.02]",
                                "transition-colors"
                            )
                        }) {
                            // Operation name
                            Td({
                                classes(
                                    "px-6", "py-3",
                                    "text-sm", "font-medium",
                                    "text-slate-800", "dark:text-slate-200"
                                )
                            }) {
                                Text(operation)
                            }
                            TableCell(stats.p50)
                            TableCell(stats.p90)
                            TableCell(stats.p99)
                            TableCell(stats.min)
                            TableCell(stats.max)
                            // Count cell
                            Td({
                                classes(
                                    "px-6", "py-3", "text-center",
                                    "text-sm", "font-mono",
                                    "text-slate-600", "dark:text-slate-300"
                                )
                            }) {
                                Text(stats.count.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeader(label: String, align: String = "text-center") {
    Th({
        classes(
            "px-6", "py-3",
            "text-xs", "font-semibold", "uppercase", "tracking-wide",
            "text-slate-500", "dark:text-slate-400",
            align
        )
    }) {
        Text(label)
    }
}

@Composable
private fun TableCell(value: Double) {
    Td({
        classes(
            "px-6", "py-3", "text-center",
            "text-sm", "font-mono",
            "text-slate-700", "dark:text-slate-300"
        )
    }) {
        Text(if (value > 0) "${value.format(1)}ms" else "-")
    }
}
