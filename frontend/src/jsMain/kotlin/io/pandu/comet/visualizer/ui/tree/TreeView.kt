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
                "px-2"
            )
        }) {
            Div({
                classes(
                    "flex", "items-center", "gap-2",
                    "px-2", "py-1.5",
                    "bg-slate-100", "dark:bg-white/5",
                    "rounded-full", "text-sm"
                )
            }) {
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
                Div({
                    classes(
                        "flex", "flex-col", "items-center", "justify-center",
                        "py-8", "gap-3"
                    )
                }) {
                    Div({
                        classes(
                            "w-10", "h-10", "rounded-full",
                            "bg-slate-200", "dark:bg-neutral-700",
                            "flex", "items-center", "justify-center",
                            "animate-pulse"
                        )
                    }) {
                        Span({
                            classes("w-5", "h-5", "text-slate-400", "dark:text-neutral-500")
                            ref { element ->
                                element.innerHTML = """<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6A2.25 2.25 0 0 1 6 3.75h2.25A2.25 2.25 0 0 1 10.5 6v2.25a2.25 2.25 0 0 1-2.25 2.25H6a2.25 2.25 0 0 1-2.25-2.25V6ZM3.75 15.75A2.25 2.25 0 0 1 6 13.5h2.25a2.25 2.25 0 0 1 2.25 2.25V18a2.25 2.25 0 0 1-2.25 2.25H6A2.25 2.25 0 0 1 3.75 18v-2.25ZM13.5 6a2.25 2.25 0 0 1 2.25-2.25H18A2.25 2.25 0 0 1 20.25 6v2.25A2.25 2.25 0 0 1 18 10.5h-2.25a2.25 2.25 0 0 1-2.25-2.25V6ZM13.5 15.75a2.25 2.25 0 0 1 2.25-2.25H18a2.25 2.25 0 0 1 2.25 2.25V18A2.25 2.25 0 0 1 18 20.25h-2.25A2.25 2.25 0 0 1 13.5 18v-2.25Z" /></svg>"""
                                onDispose { }
                            }
                        })
                    }
                    Div({
                        classes(
                            "text-xs", "text-center",
                            "text-slate-400", "dark:text-neutral-500"
                        )
                    }) {
                        Text("Waiting for traces")
                    }
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

        // Look up children by ID from the map
        node.childIds.forEach { childId ->
            allNodes[childId]?.let { child ->
                if (searchNode(child)) {
                    hasMatchingChild = true
                }
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
