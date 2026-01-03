package io.pandu.comet.visualizer.data

data class TraceStats(
    val running: Int = 0,
    val completed: Int = 0,
    val failed: Int = 0
)