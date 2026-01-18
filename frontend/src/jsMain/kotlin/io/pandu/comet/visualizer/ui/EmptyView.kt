package io.pandu.comet.visualizer.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun EmptyView(
  title: String? = null,
) {
    Div({
        classes(
            "flex", "flex-col", "items-center", "justify-center",
            "h-full", "gap-4"
        )
    }) {
        Div({
            classes(
                "w-16", "h-16", "rounded-full",
                "bg-neutral-300", "dark:bg-neutral-700",
                "flex", "items-center", "justify-center"
            )
        }) {
            Span({
                classes("w-8", "h-8", "text-neutral-400", "dark:text-neutral-500")
                ref { element ->
                    element.innerHTML = """<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6A2.25 2.25 0 0 1 6 3.75h2.25A2.25 2.25 0 0 1 10.5 6v2.25a2.25 2.25 0 0 1-2.25 2.25H6a2.25 2.25 0 0 1-2.25-2.25V6ZM3.75 15.75A2.25 2.25 0 0 1 6 13.5h2.25a2.25 2.25 0 0 1 2.25 2.25V18a2.25 2.25 0 0 1-2.25 2.25H6A2.25 2.25 0 0 1 3.75 18v-2.25ZM13.5 6a2.25 2.25 0 0 1 2.25-2.25H18A2.25 2.25 0 0 1 20.25 6v2.25A2.25 2.25 0 0 1 18 10.5h-2.25a2.25 2.25 0 0 1-2.25-2.25V6ZM13.5 15.75a2.25 2.25 0 0 1 2.25-2.25H18a2.25 2.25 0 0 1 2.25 2.25V18A2.25 2.25 0 0 1 18 20.25h-2.25A2.25 2.25 0 0 1 13.5 18v-2.25Z" /></svg>"""
                    onDispose { }
                }
            })
        }

        if(!title.isNullOrEmpty()) {
            Div({ classes("text-center") }) {
                Div({
                    classes(
                        "text-2xl", "font-medium",
                        "text-neutral-500", "dark:text-neutral-300"
                    )
                }) {
                    Text("Waiting for traces")
                }
                Div({
                    classes(
                        "text-md", "mt-1",
                        "text-neutral-500/70", "dark:text-neutral-500"
                    )
                }) {
                    Text(title)
                }
            }
        }
    }
}