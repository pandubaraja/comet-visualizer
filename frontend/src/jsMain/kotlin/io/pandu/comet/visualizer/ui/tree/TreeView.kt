package io.pandu.comet.visualizer.ui.tree

import androidx.compose.runtime.Composable
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.data.TraceState
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun TreeView(
    traceState: TraceState,
    selectedNodeId: String? = null,
    onNodeSelect: (TraceNode) -> Unit = {}
) {
    Div({ classes("py-2", "px-2") }) {
        val rootNodes = traceState.getRootNodes()
        if (rootNodes.isEmpty()) {
            Div({ classes("text-center", "py-8", "text-slate-400", "text-sm") }) {
                Text("Waiting for traces...")
            }
        } else {
            rootNodes.forEach { node ->
                TreeNodeItem(
                    node = node,
                    allNodes = traceState.traces,
                    selectedNodeId = selectedNodeId,
                    onNodeSelect = onNodeSelect
                )
            }
        }
    }
}
