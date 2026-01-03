package io.pandu.comet.visualizer

import kotlinx.serialization.Serializable

/**
 * Serializable trace event for real-time streaming via SSE.
 */
@Serializable
data class TraceEvent(
    val type: String,
    val id: String,
    val parentId: String?,
    val operation: String,
    val status: String,
    val durationMs: Double = 0.0,
    val dispatcher: String = "",
    val timestamp: Long
)

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

enum class TraceStatus {
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED;

    companion object {
        fun fromString(value: String): TraceStatus = when (value.lowercase()) {
            "running" -> RUNNING
            "completed" -> COMPLETED
            "failed" -> FAILED
            "cancelled" -> CANCELLED
            else -> RUNNING
        }
    }
}
