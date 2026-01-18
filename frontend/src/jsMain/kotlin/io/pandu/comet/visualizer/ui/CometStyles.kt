package io.pandu.comet.visualizer.ui

import org.jetbrains.compose.web.css.StyleSheet

/**
 * Minimal stylesheet for styles that can't be achieved with Tailwind.
 * Most styling is done via Tailwind utility classes in components.
 */
object CometStyles : StyleSheet() {
    init {
        // Gantt bar gradients (can't be easily done with Tailwind)
        ".gantt-bar-running" style {
            property("background", "linear-gradient(90deg, #3b82f6, #60a5fa)")
        }

        ".gantt-bar-completed" style {
            property("background", "linear-gradient(90deg, #10b981, #34d399)")
        }

        ".gantt-bar-failed" style {
            property("background", "linear-gradient(90deg, #ef4444, #f87171)")
        }

        ".gantt-bar-cancelled" style {
            property("background", "linear-gradient(90deg, #f59e0b, #fbbf24)")
        }
        // Tree graph grid pattern (light mode)
        ".tree-graph-grid" style {
            property(
                "background-image",
                """url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='24' height='24'%3E%3Cpath d='M0 0h24v24H0z' fill='none'/%3E%3Cpath d='M24 0v24M0 24h24' stroke='%23c7cbd1' stroke-width='1'/%3E%3C/svg%3E")"""
            )
            property("background-size", "24px 24px")
        }

        // Tree graph grid pattern (dark mode)
        ".dark .tree-graph-grid" style {
            property(
                "background-image",
                """url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='24' height='24'%3E%3Cpath d='M0 0h24v24H0z' fill='none'/%3E%3Cpath d='M24 0v24M0 24h24' stroke='%23333333' stroke-width='1'/%3E%3C/svg%3E")"""
            )
            property("background-size", "24px 24px")
        }
    }
}
