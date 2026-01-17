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
    val timestamp: Long,
    val sourceFile: String = "",
    val lineNumber: Int = 0
)
