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
    val stats = mutableStateOf(TraceStats())

    // Duration tracking per operation name
    private val durationsByOperation = mutableMapOf<String, MutableList<Double>>()
    private val allDurations = mutableListOf<Double>()

    // Available operations for filtering
    val operations = mutableStateListOf<String>()

    // Selected operation filter (null = all operations)
    val selectedOperation = mutableStateOf<String?>(null)

    fun setOperationFilter(operation: String?) {
        selectedOperation.value = operation
        recalculateLatencyStats()
    }

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
                    startMs = startMs,
                    sourceFile = event.sourceFile,
                    lineNumber = event.lineNumber,
                    isUnstructured = event.isUnstructured
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

                val currentStats = stats.value
                stats.value = if (event.isUnstructured) {
                    currentStats.copy(
                        running = currentStats.running + 1,
                        unstructured = currentStats.unstructured + 1
                    )
                } else {
                    currentStats.copy(running = currentStats.running + 1)
                }
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
                        "completed" -> {
                            // Track duration for latency stats
                            trackDuration(event.operation, event.durationMs)
                            currentStats.copy(
                                running = currentStats.running - 1,
                                completed = currentStats.completed + 1,
                                latency = calculateLatencyStats()
                            )
                        }
                        "failed" -> currentStats.copy(
                            running = currentStats.running - 1,
                            failed = currentStats.failed + 1
                        )
                        "cancelled" -> currentStats.copy(
                            running = currentStats.running - 1,
                            cancelled = currentStats.cancelled + 1
                        )
                        else -> currentStats
                    }
                }
            }
        }
    }

    fun getRootNodes(): List<TraceNode> {
        return traces.values.filter { it.parentId == null || !traces.containsKey(it.parentId) }
    }

    fun getOperationStats(): Map<String, LatencyStats> {
        return durationsByOperation.mapValues { (_, durations) ->
            if (durations.isEmpty()) {
                LatencyStats()
            } else {
                val sorted = durations.sorted()
                LatencyStats(
                    min = sorted.first(),
                    max = sorted.last(),
                    mean = sorted.average(),
                    p50 = percentile(sorted, 50.0),
                    p90 = percentile(sorted, 90.0),
                    p99 = percentile(sorted, 99.0),
                    count = sorted.size
                )
            }
        }
    }

    fun getOverallLatencyStats(): LatencyStats {
        if (allDurations.isEmpty()) {
            return LatencyStats()
        }
        val sorted = allDurations.sorted()
        return LatencyStats(
            min = sorted.first(),
            max = sorted.last(),
            mean = sorted.average(),
            p50 = percentile(sorted, 50.0),
            p90 = percentile(sorted, 90.0),
            p99 = percentile(sorted, 99.0),
            count = sorted.size
        )
    }

    private fun trackDuration(operation: String, durationMs: Double) {
        // Add to all durations
        allDurations.add(durationMs)

        // Add to operation-specific durations
        val operationDurations = durationsByOperation.getOrPut(operation) { mutableListOf() }
        operationDurations.add(durationMs)

        // Track unique operations
        if (operation !in operations) {
            operations.add(operation)
        }
    }

    private fun recalculateLatencyStats() {
        val currentStats = stats.value
        stats.value = currentStats.copy(latency = calculateLatencyStats())
    }

    private fun calculateLatencyStats(): LatencyStats {
        val durations = if (selectedOperation.value != null) {
            durationsByOperation[selectedOperation.value] ?: emptyList()
        } else {
            allDurations
        }

        if (durations.isEmpty()) {
            return LatencyStats()
        }

        val sorted = durations.sorted()
        val count = sorted.size

        return LatencyStats(
            min = sorted.first(),
            max = sorted.last(),
            mean = sorted.average(),
            p50 = percentile(sorted, 50.0),
            p90 = percentile(sorted, 90.0),
            p99 = percentile(sorted, 99.0),
            count = count
        )
    }

    private fun percentile(sortedData: List<Double>, percentile: Double): Double {
        if (sortedData.isEmpty()) return 0.0
        if (sortedData.size == 1) return sortedData[0]

        val index = (percentile / 100.0) * (sortedData.size - 1)
        val lower = sortedData[index.toInt()]
        val upper = sortedData[minOf(index.toInt() + 1, sortedData.size - 1)]
        val fraction = index - index.toInt()

        return lower + (upper - lower) * fraction
    }
}
