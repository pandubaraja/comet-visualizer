package io.pandu.comet.visualizer.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import io.pandu.comet.visualizer.TraceEvent
import io.pandu.comet.visualizer.TraceNode
import io.pandu.comet.visualizer.TraceStatus

/**
 * Manages the trace visualization state.
 */
class TraceState {
    private var startTime: Long? = null

    val traces = mutableStateMapOf<String, TraceNode>()
    val timelineEvents = mutableStateListOf<TimelineEvent>()
    val stats = mutableStateOf(TraceStats())

    fun processEvent(event: TraceEvent) {
        if (startTime == null) {
            startTime = event.timestamp
        }

        val startMs = (event.timestamp - (startTime ?: event.timestamp)) / 1_000_000.0

        when (event.type) {
            "started" -> {
                val node = TraceNode(
                    id = event.id,
                    parentId = event.parentId,
                    operation = event.operation,
                    status = TraceStatus.RUNNING,
                    durationMs = 0.0,
                    dispatcher = event.dispatcher,
                    startMs = startMs
                )
                traces[event.id] = node

                // Update parent's children - replace parent to trigger recomposition
                event.parentId?.let { parentId ->
                    traces[parentId]?.let { parent ->
                        val newChildren = parent.children.toMutableList()
                        newChildren.add(node)
                        traces[parentId] = parent.copy(children = newChildren)
                    }
                }

                stats.value = stats.value.copy(running = stats.value.running + 1)
            }
            else -> {
                traces[event.id]?.let { node ->
                    val updatedNode = node.copy(
                        status = TraceStatus.Companion.fromString(event.status),
                        durationMs = event.durationMs
                    )
                    traces[event.id] = updatedNode

                    // Update in parent's children list - replace parent to trigger recomposition
                    node.parentId?.let { parentId ->
                        traces[parentId]?.let { parent ->
                            val newChildren = parent.children.map { child ->
                                if (child.id == event.id) updatedNode else child
                            }.toMutableList()
                            traces[parentId] = parent.copy(children = newChildren)
                        }
                    }

                    // Update stats
                    val currentStats = stats.value
                    stats.value = when (event.status) {
                        "completed" -> currentStats.copy(
                            running = currentStats.running - 1,
                            completed = currentStats.completed + 1
                        )
                        "failed", "cancelled" -> currentStats.copy(
                            running = currentStats.running - 1,
                            failed = currentStats.failed + 1
                        )
                        else -> currentStats
                    }
                }
            }
        }

        // Add to timeline
        timelineEvents.add(0, TimelineEvent(
            event = event,
            timeOffset = formatTimeOffset(startMs)
        ))

        // Keep only last 50 events
        while (timelineEvents.size > 50) {
            timelineEvents.removeAt(timelineEvents.lastIndex)
        }
    }

    fun getRootNodes(): List<TraceNode> {
        return traces.values.filter { it.parentId == null || !traces.containsKey(it.parentId) }
    }

    private fun formatTimeOffset(ms: Double): String = "+${ms.toLong()}ms"
}
