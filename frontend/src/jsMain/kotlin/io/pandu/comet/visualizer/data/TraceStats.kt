package io.pandu.comet.visualizer.data

data class TraceStats(
    val running: Int = 0,
    val completed: Int = 0,
    val failed: Int = 0,
    val cancelled: Int = 0,
    val unstructured: Int = 0,
    val latency: LatencyStats = LatencyStats()
)

data class LatencyStats(
    val min: Double = 0.0,
    val max: Double = 0.0,
    val mean: Double = 0.0,
    val p50: Double = 0.0,
    val p90: Double = 0.0,
    val p99: Double = 0.0,
    val count: Int = 0
)