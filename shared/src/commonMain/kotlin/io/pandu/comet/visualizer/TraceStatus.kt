package io.pandu.comet.visualizer

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