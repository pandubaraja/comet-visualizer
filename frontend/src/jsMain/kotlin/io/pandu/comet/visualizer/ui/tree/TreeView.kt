package io.pandu.comet.visualizer.ui.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.data.TraceState
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput

@Composable
fun TreeView(
    traceState: TraceState,
    selectedNodeId: String? = null,
    onNodeSelect: (TraceNode) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Div({ classes("flex", "flex-col", "h-full") }) {
        // Search input
        Div({
            classes(
                "p-2", "border-b", "border-slate-200", "dark:border-white/10"
            )
        }) {
            Div({
                classes(
                    "flex", "items-center", "gap-2",
                    "px-2", "py-1.5",
                    "bg-slate-100", "dark:bg-white/5",
                    "rounded", "text-sm"
                )
            }) {
                Span({ classes("text-slate-400", "text-xs") }) {
                    Text("\uD83D\uDD0D")
                }
                TextInput(searchQuery) {
                    classes(
                        "flex-1", "bg-transparent", "outline-none",
                        "text-slate-700", "dark:text-slate-200",
                        "placeholder:text-slate-400"
                    )
                    placeholder("Search...")
                    onInput { event ->
                        searchQuery = event.value
                    }
                }
                if (searchQuery.isNotEmpty()) {
                    Span({
                        classes(
                            "text-slate-400", "cursor-pointer",
                            "hover:text-slate-600", "dark:hover:text-slate-200",
                            "text-xs"
                        )
                        onClick { searchQuery = "" }
                    }) {
                        Text("\u2715")
                    }
                }
            }
        }

        // Tree content
        Div({ classes("flex-1", "overflow-y-auto", "py-2", "px-2") }) {
            val rootNodes = traceState.getRootNodes()
            if (rootNodes.isEmpty()) {
                Div({ classes("text-center", "py-8", "text-slate-400", "text-sm") }) {
                    Text("Waiting for traces...")
                }
            } else {
                val matchingNodeIds = if (searchQuery.isNotEmpty()) {
                    findMatchingNodes(rootNodes, traceState.traces, searchQuery.lowercase())
                } else {
                    emptySet()
                }

                rootNodes.forEach { node ->
                    TreeNodeItem(
                        node = node,
                        allNodes = traceState.traces,
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

private fun findMatchingNodes(
    nodes: List<TraceNode>,
    allNodes: Map<String, TraceNode>,
    query: String
): Set<String> {
    val matches = mutableSetOf<String>()

    fun searchNode(node: TraceNode): Boolean {
        val nodeMatches = node.operation.lowercase().contains(query)
        var hasMatchingChild = false

        node.children.forEach { child ->
            val updatedChild = allNodes[child.id] ?: child
            if (searchNode(updatedChild)) {
                hasMatchingChild = true
            }
        }

        if (nodeMatches || hasMatchingChild) {
            matches.add(node.id)
        }

        return nodeMatches || hasMatchingChild
    }

    nodes.forEach { searchNode(it) }
    return matches
}
