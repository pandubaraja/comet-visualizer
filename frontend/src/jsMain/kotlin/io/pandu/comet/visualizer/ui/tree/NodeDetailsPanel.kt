package io.pandu.comet.visualizer.ui.tree

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.TraceStatus
import io.pandu.comet.visualizer.format
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun NodeDetailsPanel(
    node: TraceNode,
    onClose: () -> Unit
) {
    Div({ classes("p-4") }) {
        // Header with close button
        Div({ classes("flex", "items-center", "justify-between", "mb-4") }) {
            Span({ classes("font-semibold", "text-lg") }) {
                Text("Details")
            }
            Span({
                classes(
                    "w-6", "h-6",
                    "flex", "items-center", "justify-center",
                    "rounded", "cursor-pointer",
                    "text-slate-400", "hover:text-slate-600",
                    "hover:bg-slate-200", "dark:hover:bg-white/10"
                )
                onClick { onClose() }
            }) {
                Text("\u2715")
            }
        }

        // Status badge
        Div({ classes("mb-4") }) {
            StatusBadge(node.status)
        }

        // Operation name
        DetailRow("Operation", node.operation)

        // Duration
        DetailRow(
            "Duration",
            if (node.durationMs > 0) "${node.durationMs.format(2)}ms" else "Running..."
        )

        // Dispatcher
        DetailRow("Dispatcher", node.dispatcher)

        // ID
        DetailRow("Trace ID", node.id)

        // Parent ID
        node.parentId?.let {
            DetailRow("Parent ID", it)
        }

        // Children count
        if (node.children.isNotEmpty()) {
            DetailRow("Children", "${node.children.size}")
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Div({ classes("mb-3") }) {
        Div({
            classes(
                "text-xs", "text-slate-500", "dark:text-slate-400",
                "uppercase", "tracking-wide", "mb-1"
            )
        }) {
            Text(label)
        }
        Div({
            classes(
                "text-sm", "text-slate-800", "dark:text-slate-200",
                "font-mono", "break-all"
            )
        }) {
            Text(value)
        }
    }
}

@Composable
private fun StatusBadge(status: TraceStatus) {
    val (bgColor, textColor, label) = when (status) {
        TraceStatus.RUNNING -> Triple(
            "bg-blue-100 dark:bg-blue-900/30",
            "text-blue-700 dark:text-blue-300",
            "Running"
        )
        TraceStatus.COMPLETED -> Triple(
            "bg-emerald-100 dark:bg-emerald-900/30",
            "text-emerald-700 dark:text-emerald-300",
            "Completed"
        )
        TraceStatus.FAILED -> Triple(
            "bg-red-100 dark:bg-red-900/30",
            "text-red-700 dark:text-red-300",
            "Failed"
        )
        TraceStatus.CANCELLED -> Triple(
            "bg-amber-100 dark:bg-amber-900/30",
            "text-amber-700 dark:text-amber-300",
            "Cancelled"
        )
    }

    Div({
        classes(
            "inline-flex", "items-center", "gap-2",
            "px-3", "py-1.5", "rounded-full",
            "text-sm", "font-medium"
        )
        // Split classes to avoid space issue
        bgColor.split(" ").forEach { classes(it) }
        textColor.split(" ").forEach { classes(it) }
    }) {
        Span({
            classes("w-2", "h-2", "rounded-full")
            when (status) {
                TraceStatus.RUNNING -> classes("bg-blue-500", "animate-pulse")
                TraceStatus.COMPLETED -> classes("bg-emerald-500")
                TraceStatus.FAILED -> classes("bg-red-500")
                TraceStatus.CANCELLED -> classes("bg-amber-500")
            }
        })
        Text(label)
    }
}
