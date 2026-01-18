package io.pandu.comet.visualizer

/**
 * Represents a trace node in the UI with computed properties.
 * Children are stored as IDs only to avoid stale copies.
 */
data class TraceNode(
    val id: String,
    val parentId: String?,
    val operation: String,
    val status: TraceStatus,
    val durationMs: Double,
    val dispatcher: String,
    val startMs: Double,
    val sourceFile: String = "",
    val lineNumber: Int = 0,
    val isUnstructured: Boolean = false,
    val childIds: List<String> = emptyList()
)