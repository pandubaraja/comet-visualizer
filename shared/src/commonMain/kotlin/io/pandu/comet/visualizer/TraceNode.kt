package io.pandu.comet.visualizer

/**
 * Represents a trace node in the UI with computed properties.
 */
data class TraceNode(
    val id: String,
    val parentId: String?,
    val operation: String,
    val status: TraceStatus,
    val durationMs: Double,
    val dispatcher: String,
    val startMs: Double,
    val children: MutableList<TraceNode> = mutableListOf()
)