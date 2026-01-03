package io.pandu.comet.visualizer.ui.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.data.TraceState
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun TreeView(traceState: TraceState) {
    var hoveredNode by remember { mutableStateOf<TraceNode?>(null) }
    var tooltipPosition by remember { mutableStateOf(Pair(0, 0)) }

    Div({ classes("py-2", "px-6") }) {
        val rootNodes = traceState.getRootNodes()
        if (rootNodes.isEmpty()) {
            Div({ classes("text-center", "py-12", "text-slate-500") }) {
                Div({ classes("text-4xl", "mb-3") }) { Text("") }
                Div({ classes("text-xl", "mb-3") }) { Text("\uD83D\uDC63 Waiting for traces...") }
            }
        } else {
            rootNodes.forEach { node ->
                TreeNodeItem(
                    node = node,
                    allNodes = traceState.traces,
                    onHover = { n, x, y ->
                        hoveredNode = n
                        tooltipPosition = Pair(x, y)
                    },
                    onLeave = { hoveredNode = null }
                )
            }
        }
    }

    // Tooltip
    hoveredNode?.let { node ->
        TreeTooltip(node, tooltipPosition.first, tooltipPosition.second)
    }
}