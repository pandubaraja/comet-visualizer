package io.pandu.comet.visualizer.ui.toggle

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span

@Composable
fun ThemeToggle(isDark: Boolean, onChange: (Boolean) -> Unit) {
    Button({
        classes(
            "relative", "z-0",
            "inline-grid", "grid-cols-2", "gap-2",
            "rounded-full",
            "bg-neutral-200", "dark:bg-neutral-700/50",
            "p-1.5",
            "text-neutral-950", "dark:text-white"
        )
        onClick { onChange(!isDark) }
    }) {
        val selectedClasses = listOf(
            "relative",
            "rounded-full",
            "p-0.5",
            "bg-white", "ring-1", "ring-neutral-950/20",
            "text-neutral-950",
            "dark:bg-neutral-100", "dark:text-neutral-950", "dark:ring-transparent"
        )

        val defaultClasses = listOf(
            "relative",
            "rounded-full",
            "p-0.5",
            "dark:text-white"
        )

        // Moon icon (dark mode)
        Div({
            classes(if (!isDark) selectedClasses else defaultClasses)
        }) {
            MoonIcon()
        }

        // Sun icon (light mode)
        Div({
            classes(if (isDark) selectedClasses else defaultClasses)
        }) {
            SunIcon()
        }
    }
}

@Composable
private fun MoonIcon() {
    Span({
        classes("w-6", "h-6", "block")
        ref { element ->
            element.innerHTML = """<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6"><path stroke-linecap="round" stroke-linejoin="round" d="M21.752 15.002A9.72 9.72 0 0 1 18 15.75c-5.385 0-9.75-4.365-9.75-9.75 0-1.33.266-2.597.748-3.752A9.753 9.753 0 0 0 3 11.25C3 16.635 7.365 21 12.75 21a9.753 9.753 0 0 0 9.002-5.998Z" /></svg>"""
            onDispose { }
        }
    })
}

@Composable
private fun SunIcon() {
    Span({
        classes("w-6", "h-6", "block")
        ref { element ->
            element.innerHTML = """<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-6 h-6"><path stroke-linecap="round" stroke-linejoin="round" d="M12 3v2.25m6.364.386-1.591 1.591M21 12h-2.25m-.386 6.364-1.591-1.591M12 18.75V21m-4.773-4.227-1.591 1.591M5.25 12H3m4.227-4.773L5.636 5.636M15.75 12a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0Z" /></svg>"""
            onDispose { }
        }
    })
}
