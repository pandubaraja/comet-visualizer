package io.pandu.comet.visualizer.ui.toggle

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun ThemeToggle(isDark: Boolean, onChange: (Boolean) -> Unit) {
    Button({
        classes(
            "bg-slate-200", "dark:bg-white/[0.03]",
            "border", "border-slate-300", "dark:border-white/10",
            "rounded-lg",
            "px-3", "py-2",
            "cursor-pointer", "text-sm", "font-medium",
            "transition-all", "duration-200",
            "flex", "items-center", "gap-1.5",
            "text-slate-700", "dark:text-slate-200",
            "hover:bg-slate-300", "dark:hover:bg-white/[0.1]"
        )
        onClick { onChange(!isDark) }
    }) {
        Span({}) {
            Text(if (isDark) "☀️ Light" else "🌙 Dark")
        }
    }
}
