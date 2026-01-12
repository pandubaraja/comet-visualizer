package io.pandu.comet.visualizer.ui.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.TraceStatus
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun TreeNodeItem(
    node: TraceNode,
    allNodes: Map<String, TraceNode>,
    selectedNodeId: String? = null,
    onNodeSelect: (TraceNode) -> Unit = {},
    searchQuery: String = "",
    matchingNodeIds: Set<String> = emptySet()
) {
    // Hide non-matching nodes when searching
    if (searchQuery.isNotEmpty() && node.id !in matchingNodeIds) {
        return
    }

    val status = node.status
    val children = node.children
    val hasChildren = children.isNotEmpty()
    var isExpanded by remember { mutableStateOf(true) }
    val isSelected = node.id == selectedNodeId
    val displayName = node.operation.ifEmpty { node.id }
    val isMatch = searchQuery.isNotEmpty() &&
        displayName.lowercase().contains(searchQuery.lowercase())

    val statusColor = when (status) {
        TraceStatus.RUNNING -> "bg-blue-500"
        TraceStatus.COMPLETED -> "bg-emerald-500"
        TraceStatus.FAILED -> "bg-red-500"
        TraceStatus.CANCELLED -> "bg-amber-500"
    }

    Div({ classes("relative") }) {
        Div({
            classes(
                "flex", "items-center", "gap-2",
                "px-2", "py-1",
                "rounded",
                "cursor-pointer"
            )
            if (isSelected) {
                classes("bg-blue-100", "dark:bg-blue-900/30")
            } else if (isMatch) {
                classes("bg-yellow-100", "dark:bg-yellow-900/30")
            } else {
                classes("hover:bg-slate-200", "dark:hover:bg-white/[0.06]")
            }
            onClick { onNodeSelect(node) }
        }) {
            // Expand/collapse icon
            if (hasChildren) {
                Span({
                    classes(
                        "w-3", "text-[10px]",
                        "text-slate-400", "dark:text-slate-500",
                        "transition-transform", "duration-150"
                    )
                    if (isExpanded) {
                        classes("rotate-90")
                    }
                    onClick { e ->
                        e.stopPropagation()
                        isExpanded = !isExpanded
                    }
                }) {
                    Text("\u25B6")
                }
            } else {
                Span({ classes("w-3") })
            }

            // Status dot
            Span({
                classes(
                    "w-2", "h-2", "rounded-full", "flex-shrink-0",
                    statusColor
                )
                if (status == TraceStatus.RUNNING) {
                    classes("animate-pulse")
                }
            })

            // Operation name with highlight
            Span({
                classes(
                    "text-sm", "truncate",
                    "text-slate-700", "dark:text-slate-300"
                )
                if (isSelected || isMatch) {
                    classes("font-medium")
                }
            }) {
                if (isMatch && searchQuery.isNotEmpty()) {
                    HighlightedText(displayName, searchQuery)
                } else {
                    Text(displayName)
                }
            }
        }

        // Children (collapsible)
        if (hasChildren && isExpanded) {
            Div({ classes("ml-4", "border-l", "border-slate-200", "dark:border-white/10") }) {
                children.forEach { child ->
                    val updatedChild = allNodes[child.id] ?: child
                    TreeNodeItem(
                        node = updatedChild,
                        allNodes = allNodes,
                        selectedNodeId = selectedNodeId,
                        onNodeSelect = onNodeSelect,
                        searchQuery = searchQuery,
                        matchingNodeIds = matchingNodeIds
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightedText(text: String, query: String) {
    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    val index = lowerText.indexOf(lowerQuery)

    if (index >= 0) {
        Text(text.substring(0, index))
        Span({
            classes("bg-yellow-300", "dark:bg-yellow-600", "rounded-sm", "px-0.5")
        }) {
            Text(text.substring(index, index + query.length))
        }
        Text(text.substring(index + query.length))
    } else {
        Text(text)
    }
}
