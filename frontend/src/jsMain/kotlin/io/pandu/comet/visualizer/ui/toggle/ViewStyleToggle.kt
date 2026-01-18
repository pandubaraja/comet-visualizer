package io.pandu.comet.visualizer.ui.toggle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun ViewStyleToggle(current: ViewStyle, onChange: (ViewStyle) -> Unit) {
    var isOpen by remember { mutableStateOf(false) }

    Div({
        classes("relative")
    }) {
        // Selected option button
        Div({
            classes(
                "flex", "items-center", "gap-2",
                "px-3", "py-2.5",
                "bg-stone-300/60", "dark:bg-neutral-700",
                "rounded-lg", "cursor-pointer",
                "text-neutral-700", "dark:text-neutral-200",
                "text-sm", "font-medium",
                "hover:bg-neutral-300", "dark:hover:bg-white/[0.08]",
                "transition-colors"
            )
            onClick { isOpen = !isOpen }
        }) {
            ViewStyleIcon(current)
            Text(current.label)
            ChevronIcon(isOpen)
        }

        // Dropdown menu
        if (isOpen) {
            Div({
                classes(
                    "absolute", "top-full", "left-0", "mt-1",
                    "min-w-full", "z-50",
                    "bg-white", "dark:bg-neutral-800",
                    "border", "border-neutral-300", "dark:border-white/10",
                    "rounded-lg", "shadow-lg",
                    "overflow-hidden"
                )
            }) {
                ViewStyle.entries.forEach { style ->
                    Div({
                        classes(
                            "flex", "items-center", "gap-2",
                            "px-3", "py-2",
                            "text-neutral-700", "dark:text-neutral-200",
                            "text-sm", "cursor-pointer",
                            "hover:bg-neutral-100", "dark:hover:bg-white/[0.06]",
                            "transition-colors"
                        )
                        if (style == current) {
                            classes("bg-neutral-100", "dark:bg-white/[0.06]")
                        }
                        onClick {
                            onChange(style)
                            isOpen = false
                        }
                    }) {
                        ViewStyleIcon(style)
                        Text(style.label)
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewStyleIcon(style: ViewStyle) {
    Span({
        classes("w-4", "h-4", "block")
        ref { element ->
            element.innerHTML = when (style) {
                ViewStyle.TREE -> TREE_ICON_SVG
                ViewStyle.GANTT -> GANTT_ICON_SVG
                ViewStyle.PERFORMANCE -> PERFORMANCE_ICON_SVG
            }
            onDispose { }
        }
    })
}

@Composable
private fun ChevronIcon(isOpen: Boolean) {
    Span({
        classes("w-4", "h-4", "block", "ml-auto", "transition-transform")
        if (isOpen) classes("rotate-180")
        ref { element ->
            element.innerHTML = CHEVRON_ICON_SVG
            onDispose { }
        }
    })
}

private val ViewStyle.label: String
    get() = when (this) {
        ViewStyle.TREE -> "Tree"
        ViewStyle.GANTT -> "Gantt"
        ViewStyle.PERFORMANCE -> "Performance"
    }

private const val TREE_ICON_SVG = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 640" fill="currentColor" class="w-4 h-4"><path d="M320 32C327 32 333.7 35.1 338.3 40.5L474.3 200.5C480.4 207.6 481.7 217.6 477.8 226.1C473.9 234.6 465.4 240 456 240L431.1 240L506.3 328.5C512.4 335.6 513.7 345.6 509.8 354.1C505.9 362.6 497.4 368 488 368L449.5 368L538.3 472.5C544.4 479.6 545.7 489.6 541.8 498.1C537.9 506.6 529.4 512 520 512L352 512L352 576C352 593.7 337.7 608 320 608C302.3 608 288 593.7 288 576L288 512L120 512C110.6 512 102.1 506.6 98.2 498.1C94.3 489.6 95.6 479.6 101.7 472.5L190.5 368L152 368C142.6 368 134.1 362.6 130.2 354.1C126.3 345.6 127.6 335.6 133.7 328.5L208.9 240L184 240C174.6 240 166.1 234.6 162.2 226.1C158.3 217.6 159.6 207.6 165.7 200.5L301.7 40.5C306.3 35.1 313 32 320 32z"/></svg>"""

private const val GANTT_ICON_SVG = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 640" fill="currentColor" class="w-4 h-4"><path d="M128 128C128 110.3 113.7 96 96 96C78.3 96 64 110.3 64 128L64 464C64 508.2 99.8 544 144 544L544 544C561.7 544 576 529.7 576 512C576 494.3 561.7 480 544 480L144 480C135.2 480 128 472.8 128 464L128 128zM224 128C206.3 128 192 142.3 192 160C192 177.7 206.3 192 224 192L320 192C337.7 192 352 177.7 352 160C352 142.3 337.7 128 320 128L224 128zM288 240C270.3 240 256 254.3 256 272C256 289.7 270.3 304 288 304L416 304C433.7 304 448 289.7 448 272C448 254.3 433.7 240 416 240L288 240zM448 352C430.3 352 416 366.3 416 384C416 401.7 430.3 416 448 416L512 416C529.7 416 544 401.7 544 384C544 366.3 529.7 352 512 352L448 352z"/></svg>"""

private const val PERFORMANCE_ICON_SVG = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-4 h-4"><path d="M18.375 2.25c-1.035 0-1.875.84-1.875 1.875v15.75c0 1.035.84 1.875 1.875 1.875h.75c1.035 0 1.875-.84 1.875-1.875V4.125c0-1.036-.84-1.875-1.875-1.875h-.75zM9.75 8.625c0-1.036.84-1.875 1.875-1.875h.75c1.036 0 1.875.84 1.875 1.875v11.25c0 1.035-.84 1.875-1.875 1.875h-.75a1.875 1.875 0 01-1.875-1.875V8.625zM3 13.125c0-1.036.84-1.875 1.875-1.875h.75c1.036 0 1.875.84 1.875 1.875v6.75c0 1.035-.84 1.875-1.875 1.875h-.75A1.875 1.875 0 013 19.875v-6.75z"/></svg>"""

private const val CHEVRON_ICON_SVG = """<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-4 h-4"><path stroke-linecap="round" stroke-linejoin="round" d="m19.5 8.25-7.5 7.5-7.5-7.5" /></svg>"""
