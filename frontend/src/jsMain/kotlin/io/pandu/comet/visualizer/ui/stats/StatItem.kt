package io.pandu.comet.visualizer.ui.stats

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun StatItem(dotColor: String, count: Int, label: String) {
    Div({ classes(
        "bg-stone-300/60", "dark:bg-stone-700",
        "rounded-md",
        "shadow-md",
        "text-center"
    ) }) {
        Div ({
            classes(
                "p-2",
                "flex",
                "items-center",
                "justify-center",
                "gap-x-1",
                "text-sm/6",
                "font-semibold",
                "text-neutral-700", "dark:text-neutral-400",
            )
        }) {
            StatIcon(dotColor)
            Text(label)
        }
        P ({
            classes(
                "p-2",
                "flex",
                "justify-center",
                "bg-white", "dark:bg-neutral-600",
                "rounded-b-xl"
            )
        }) {
            Span(attrs = {
                classes(
                    "text-l",
                    "font-normal",
                    "tracking-tight",
                    "text-neutral-900", "dark:text-neutral-100"
                )
            }) {
                Text("$count")
            }
        }
    }
}

@Composable
private fun StatIcon(color: String) {
    Span({
        classes("w-4", "h-4", "block", color)
        ref { element ->
            element.innerHTML = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 640" fill="currentColor" class="w-4 h-4"><path d="M320 576C178.6 576 64 461.4 64 320C64 178.6 178.6 64 320 64C461.4 64 576 178.6 576 320C576 461.4 461.4 576 320 576zM320 112C205.1 112 112 205.1 112 320C112 434.9 205.1 528 320 528C434.9 528 528 434.9 528 320C528 205.1 434.9 112 320 112zM320 416C267 416 224 373 224 320C224 267 267 224 320 224C373 224 416 267 416 320C416 373 373 416 320 416z"/></svg>"""
            onDispose { }
        }
    })
}
