package io.pandu.comet.visualizer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.pandu.comet.visualizer.data.TraceState
import io.pandu.comet.visualizer.ui.gantt.GanttView
import io.pandu.comet.visualizer.ui.stats.StatsBar
import io.pandu.comet.visualizer.ui.timeline.TimelineView
import io.pandu.comet.visualizer.ui.toggle.ThemeToggle
import io.pandu.comet.visualizer.ui.toggle.ViewStyle
import io.pandu.comet.visualizer.ui.toggle.ViewStyleToggle
import io.pandu.comet.visualizer.ui.tree.TreeView
import kotlinx.browser.document
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Header
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun CometVisualizerContent(
    traceState: TraceState,
    badge: String? = null
) {
    var currentViewStyle by remember { mutableStateOf(ViewStyle.GANTT) }
    var isDarkTheme by remember { mutableStateOf(false) }

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
            "grid", "grid-cols-1", "lg:grid-cols-[1fr_400px]", "h-screen",
            "bg-slate-200", "dark:bg-neutral-900",
            "text-slate-800", "dark:text-slate-200"
        )
    }) {
        // Main panel
        Div({
            classes(
                "p-6", "overflow-y-auto",
                "border-r", "border-slate-200", "dark:border-white/10"
            )
        }) {
            // Header
            Header({
                classes(
                    "mb-6", "pb-4",
                    "border-b", "border-slate-200", "dark:border-white/10",
                    "flex", "justify-between", "items-start"
                )
            }) {
                Div({}) {
                    Div({ classes("flex", "items-center", "gap-3") }) {
                        H1({
                            classes("text-2xl", "font-bold", "title-gradient")
                        }) {
                            Text("Comet Real-time Traces")
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
                    StatsBar(traceState.stats.value)
                }
                Div({ classes("flex", "gap-2", "items-center") }) {
                    ViewStyleToggle(currentViewStyle) { currentViewStyle = it }
                    ThemeToggle(isDarkTheme) { isDarkTheme = it }
                }
            }

            // Content based on view
            when (currentViewStyle) {
                ViewStyle.TREE -> TreeView(traceState)
                ViewStyle.GANTT -> GanttView(traceState)
            }
        }

        // Timeline panel
        Div({
            classes(
                "bg-slate-50", "dark:bg-white/[0.02]",
                "p-6", "overflow-y-auto",
                "hidden", "lg:block"
            )
        }) {
            Div({
                classes("text-sm", "font-semibold", "mb-4", "text-slate-500", "dark:text-slate-400")
            }) {
                Text("Event Timeline")
            }
            TimelineView(traceState.timelineEvents)
        }
    }
}
