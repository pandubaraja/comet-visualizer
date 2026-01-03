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

        // Title gradient
        ".title-gradient" style {
            property("background", "linear-gradient(90deg, #60a5fa, #a78bfa)")
            property("-webkit-background-clip", "text")
            property("-webkit-text-fill-color", "transparent")
            property("background-clip", "text")
        }
    }
}
