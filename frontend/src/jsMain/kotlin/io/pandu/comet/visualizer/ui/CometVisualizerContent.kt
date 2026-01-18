package io.pandu.comet.visualizer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.data.TraceState
import io.pandu.comet.visualizer.ui.gantt.GanttView
import io.pandu.comet.visualizer.ui.performance.PerformanceView
import io.pandu.comet.visualizer.ui.stats.StatsBar
import io.pandu.comet.visualizer.ui.toggle.ThemeToggle
import io.pandu.comet.visualizer.ui.toggle.ViewStyle
import io.pandu.comet.visualizer.ui.toggle.ViewStyleToggle
import io.pandu.comet.visualizer.ui.tree.HorizontalTreeGraph
import io.pandu.comet.visualizer.ui.tree.NodeDetailsPanel
import io.pandu.comet.visualizer.ui.tree.TreeView
import kotlinx.browser.document
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Header
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun CometVisualizerContent(
    traceState: TraceState,
    badge: String? = null
) {
    var currentViewStyle by remember { mutableStateOf(ViewStyle.GANTT) }
    var isDarkTheme by remember { mutableStateOf(false) }
    var selectedNode by remember { mutableStateOf<TraceNode?>(null) }

    // Toggle dark class on body
    LaunchedEffect(isDarkTheme) {
        if (isDarkTheme) {
            document.body?.classList?.add("dark")
        } else {
            document.body?.classList?.remove("dark")
        }
    }

    Style(CometStyles)

    // Main layout
    Div({
        classes(
            "h-screen", "flex", "flex-col",
            "text-neutral-800", "dark:text-neutral-200",
        )
    }) {
        // Header
        Header({
            classes(
                "pb-6", "px-6", "pt-6", "flex-shrink-0",
                "bg-zinc-100", "dark:bg-neutral-800",
                "flex", "justify-between", "items-start",
                "shadow-md", "dark:shadow-neutral-950/50", "z-20"
            )
        }) {
            Div({}) {
                Div({ classes("flex", "items-center", "gap-3") }) {
                    Img("/icons/comet.png") {
                        classes("w-14", "h-14")
                        attr("alt", "Comet")
                    }
                    H1({
                        classes("text-3xl", "font-bold")
                    }) {
                        Text("Comet Visualizer")
                    }
                    badge?.let {
                        Span({
                            classes(
                                "px-2", "py-0.5", "text-xs", "font-medium",
                                "bg-amber-100", "dark:bg-amber-900/50",
                                "text-amber-700", "dark:text-amber-300",
                                "rounded"
                            )
                        }) {
                            Text(it)
                        }
                    }
                }
                StatsBar(stats = traceState.stats.value)
            }
            Div({ classes("flex", "gap-2", "items-center") }) {
                ViewStyleToggle(currentViewStyle) { currentViewStyle = it }
                ThemeToggle(isDarkTheme) { isDarkTheme = it }
            }
        }

        // Content with sidebar layout
        Div({
            classes("flex", "flex-1", "min-h-0")
        }) {
            // Left Sidebar - Tree View
            Div({
                classes(
                    "w-64", "flex-shrink-0",
                    "bg-zinc-100", "dark:bg-neutral-800",
                    "overflow-y-auto",
                    "shadow-md", "dark:shadow-neutral-950/50", "z-20"
                )
            }) {
                TreeView(
                    traceState = traceState,
                    selectedNodeId = selectedNode?.id,
                    onNodeSelect = { node -> selectedNode = node }
                )
            }

            // Main Content Area - Horizontal Tree Graph
            Div({
                classes("flex-1", "overflow-auto")
                classes("bg-neutral-200/80", "dark:bg-neutral-900")
            }) {
                when (currentViewStyle) {
                    ViewStyle.TREE -> HorizontalTreeGraph(
                        traceState = traceState,
                        selectedNodeId = selectedNode?.id,
                        onNodeSelect = { node -> selectedNode = node }
                    )
                    ViewStyle.GANTT -> GanttView(
                        traceState = traceState,
                        selectedNodeId = selectedNode?.id,
                        onNodeSelect = { node -> selectedNode = node }
                    )
                    ViewStyle.PERFORMANCE -> PerformanceView(traceState = traceState)
                }
            }

            // Right Sidebar - Node Details
            selectedNode?.let { node ->
                Div({
                    classes(
                        "w-80", "flex-shrink-0",
                        "bg-stone-50", "dark:bg-neutral-800",
                        "overflow-y-auto",
                        "shadow-md", "dark:shadow-neutral-950/50", "z-10"
                    )
                }) {
                    NodeDetailsPanel(
                        node = traceState.traces[node.id] ?: node,
                        onClose = { selectedNode = null }
                    )
                }
            }
        }
    }
}
