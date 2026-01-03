package io.pandu.comet.visualizer.ui.stats

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun StatItem(dotColor: String, count: Int, label: String, isPulsing: Boolean) {
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
