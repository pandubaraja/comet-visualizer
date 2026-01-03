package io.pandu.comet.visualizer.ui.toggle

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun ViewStyleToggle(current: ViewStyle, onChange: (ViewStyle) -> Unit) {
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
                "text-slate-600", "dark:text-slate-400",
                "cursor-pointer", "text-sm", "font-medium",
                "transition-all", "duration-200",
                "hover:bg-slate-300", "dark:hover:bg-white/[0.06]"
            )
            if (current == ViewStyle.TREE) {
                classes("!bg-blue-500", "!text-white")
            }
            onClick { onChange(ViewStyle.TREE) }
        }) {
            Text("Tree")
        }
        Button({
            classes(
                "px-3.5", "py-2", "border-0", "bg-transparent",
                "text-slate-600", "dark:text-slate-400",
                "cursor-pointer", "text-sm", "font-medium",
                "transition-all", "duration-200",
                "hover:bg-slate-300", "dark:hover:bg-white/[0.06]"
            )
            if (current == ViewStyle.GANTT) {
                classes("!bg-blue-500", "!text-white")
            }
            onClick { onChange(ViewStyle.GANTT) }
        }) {
            Text("Gantt")
        }
    }
}
