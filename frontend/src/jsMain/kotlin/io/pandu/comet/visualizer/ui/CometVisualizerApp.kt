package io.pandu.comet.visualizer.ui

import androidx.compose.runtime.*
import io.pandu.comet.visualizer.SseClient
import io.pandu.comet.visualizer.TraceState
import kotlinx.browser.document
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun CometVisualizerApp() {
    val traceState = remember { TraceState() }
    var currentView by remember { mutableStateOf(ViewType.TREE) }
    var isDarkTheme by remember { mutableStateOf(true) }

    // Connect to SSE on mount
    DisposableEffect(Unit) {
        val client = SseClient(
            onEvent = { event -> traceState.processEvent(event) }
        )
        client.connect()
        onDispose { client.disconnect() }
    }

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
            "bg-slate-100", "dark:bg-comet-bg",
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
                    H1({
                        classes("text-2xl", "font-bold", "title-gradient")
                    }) {
                        Text("Comet Real-time Traces")
                    }
                    StatsBar(traceState.stats.value)
                }
                Div({ classes("flex", "gap-2", "items-center") }) {
                    ViewToggle(currentView) { currentView = it }
                    ThemeToggle(isDarkTheme) { isDarkTheme = it }
                }
            }

            // Content based on view
            when (currentView) {
                ViewType.TREE -> TreeView(traceState)
                ViewType.GANTT -> GanttView(traceState)
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

enum class ViewType { TREE, GANTT }

@Composable
private fun ViewToggle(current: ViewType, onChange: (ViewType) -> Unit) {
    Div({
        classes(
            "flex",
            "bg-slate-200", "dark:bg-white/[0.03]",
            "border", "border-slate-300", "dark:border-white/10",
            "rounded-lg", "overflow-hidden"
        )
    }) {
        Button({
            classes(
                "px-3.5", "py-2", "border-0", "bg-transparent",
                "text-slate-500", "dark:text-slate-400",
                "cursor-pointer", "text-xs", "font-medium",
                "transition-all", "duration-200",
                "hover:bg-slate-300", "dark:hover:bg-white/[0.06]"
            )
            if (current == ViewType.TREE) {
                classes("!bg-blue-500", "!text-white")
            }
            onClick { onChange(ViewType.TREE) }
        }) {
            Text("Tree")
        }
        Button({
            classes(
                "px-3.5", "py-2", "border-0", "bg-transparent",
                "text-slate-500", "dark:text-slate-400",
                "cursor-pointer", "text-xs", "font-medium",
                "transition-all", "duration-200",
                "hover:bg-slate-300", "dark:hover:bg-white/[0.06]"
            )
            if (current == ViewType.GANTT) {
                classes("!bg-blue-500", "!text-white")
            }
            onClick { onChange(ViewType.GANTT) }
        }) {
            Text("Gantt")
        }
    }
}

@Composable
private fun ThemeToggle(isDark: Boolean, onChange: (Boolean) -> Unit) {
    Button({
        classes(
            "bg-white/[0.03]", "border", "border-white/10", "rounded-lg",
            "px-3", "py-2", "cursor-pointer", "text-base",
            "transition-all", "duration-200",
            "flex", "items-center", "gap-1.5",
            "hover:bg-slate-300", "dark:hover:bg-white/[0.06]"
        )
        onClick { onChange(!isDark) }
    }) {
        Span({}) {
            Text(if (isDark) "Light" else "Dark")
        }
    }
}
