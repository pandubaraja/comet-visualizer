package io.pandu.comet.visualizer.ui.stats

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun StatItem(dotColor: String, count: Int, label: String) {
    Div({ classes(
        "p-2",
        "bg-neutral-100", "dark:bg-neutral-700",
        "rounded-xl"
    ) }) {
        Div ({
            classes(
                "flex",
                "items-center",
                "gap-x-2",
                "text-sm/6",
                "font-medium",
                "text-neutral-500", "dark:text-neutral-200"
            )
        }) {
            StatIcon(dotColor)
            Text(label)
        }
        P ({
            classes(
                "mt-1",
                "flex",
                "items-baseline",
                "gap-x-2"
            )
        }) {
            Span(attrs = {
                classes(
                    "text-xl",
                    "font-normal",
                    "tracking-tight",
                    "text-neutral-900", "dark:text-neutral-200"
                )
            }) {
                Text( "$count")
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
