package io.pandu.comet.visualizer.data

import io.pandu.comet.visualizer.TraceEvent
import kotlin.js.Date

/**
 * Mock data for development and testing.
 * Simulates an API gateway request with authentication, data fetching, and response building.
 */
object MockData {

    private var baseTime = 0L

    /**
     * Generates mock trace events simulating an API gateway request flow.
     */
    fun generateMockEvents(): List<TraceEvent> {
        baseTime = (Date.now() * 1_000_000).toLong() // Convert to nanoseconds

        return buildList {
            // Root: API Gateway Request
            add(started("api-gateway", null, "api-gateway", "Dispatchers.IO"))

            // Authentication phase
            add(started("auth", "api-gateway", "authenticate", "Dispatchers.Default", 50))
            add(started("jwt", "auth", "validate-jwt", "Dispatchers.Default", 100))
            add(completed("jwt", 800, 900))
            add(started("session", "auth", "load-user-session", "Dispatchers.IO", 920))
            add(completed("session", 600, 1520))
            add(completed("auth", 1500, 1550))

            // Parallel data fetching phase
            add(started("user-profile", "api-gateway", "fetch-user-profile", "Dispatchers.IO", 1600))
            add(started("notifications", "api-gateway", "fetch-notifications", "Dispatchers.IO", 1620))
            add(started("recommendations", "api-gateway", "fetch-recommendations", "Dispatchers.Default", 1640))

            // User profile sub-tasks
            add(started("db-user", "user-profile", "db-query-user", "Dispatchers.IO", 1700))
            add(completed("db-user", 900, 2600))
            add(started("db-prefs", "user-profile", "db-query-preferences", "Dispatchers.IO", 2650))
            add(completed("db-prefs", 600, 3250))
            add(completed("user-profile", 1700, 3300))

            // Notifications with cache
            add(started("redis-cache", "notifications", "redis-cache-lookup", "Dispatchers.IO", 1700))
            add(completed("redis-cache", 550, 2250))
            add(completed("notifications", 700, 2320))

            // Recommendations with ML inference
            add(started("ml-model", "recommendations", "ml-inference", "Dispatchers.Default", 1750))
            add(started("feature-extract", "ml-model", "feature-extraction", "Dispatchers.Default", 1800))
            add(completed("feature-extract", 400, 2200))
            add(started("model-predict", "ml-model", "model-prediction", "Dispatchers.Default", 2250))
            add(completed("model-predict", 1200, 3450))
            add(completed("ml-model", 1750, 3500))
            add(completed("recommendations", 1900, 3550))

            // Response building
            add(started("build-response", "api-gateway", "build-response", "Dispatchers.Default", 3600))
            add(started("serialize", "build-response", "serialize-json", "Dispatchers.Default", 3650))
            add(completed("serialize", 150, 3800))
            add(started("compress", "build-response", "compress-response", "Dispatchers.Default", 3820))
            add(completed("compress", 80, 3900))
            add(completed("build-response", 350, 3950))

            // Complete API gateway
            add(completed("api-gateway", 4000, 4000))

            // Second request: Background job with some failures
            add(started("job-scheduler", null, "job-scheduler", "Dispatchers.Default", 4500))

            add(started("job-1", "job-scheduler", "process-batch-1", "Dispatchers.IO", 4600))
            add(started("job-1-a", "job-1", "validate-input", "Dispatchers.Default", 4650))
            add(completed("job-1-a", 200, 4850))
            add(started("job-1-b", "job-1", "transform-data", "Dispatchers.Default", 4900))
            add(completed("job-1-b", 350, 5250))
            add(completed("job-1", 700, 5300))

            add(started("job-2", "job-scheduler", "process-batch-2", "Dispatchers.IO", 5350))
            add(started("job-2-a", "job-2", "validate-input", "Dispatchers.Default", 5400))
            add(failed("job-2-a", 150, 5550)) // This one fails
            add(failed("job-2", 250, 5600))

            add(started("job-3", "job-scheduler", "process-batch-3", "Dispatchers.IO", 5650))
            add(started("job-3-a", "job-3", "validate-input", "Dispatchers.Default", 5700))
            add(cancelled("job-3-a", 100, 5800)) // This one is cancelled
            add(cancelled("job-3", 200, 5850))

            add(started("job-4", "job-scheduler", "process-batch-4", "Dispatchers.IO", 5650))
            add(started("job-4-a", "job-4", "validate-input", "Dispatchers.Default", 5700))
            add(cancelled("job-4-a", 100, 5800)) // This one is cancelled
            add(cancelled("job-4", 200, 5850))

            add(completed("job-scheduler", 1400, 5900))
        }
    }

    private fun started(
        id: String,
        parentId: String?,
        operation: String,
        dispatcher: String,
        offsetMs: Long = 0
    ) = TraceEvent(
        type = "started",
        id = id,
        parentId = parentId,
        operation = operation,
        status = "running",
        durationMs = 0.0,
        dispatcher = dispatcher,
        timestamp = baseTime + (offsetMs * 1_000_000)
    )

    private fun completed(id: String, durationMs: Long, offsetMs: Long) = TraceEvent(
        type = "completed",
        id = id,
        parentId = null,
        operation = "",
        status = "completed",
        durationMs = durationMs.toDouble(),
        dispatcher = "",
        timestamp = baseTime + (offsetMs * 1_000_000)
    )

    private fun failed(id: String, durationMs: Long, offsetMs: Long) = TraceEvent(
        type = "failed",
        id = id,
        parentId = null,
        operation = "",
        status = "failed",
        durationMs = durationMs.toDouble(),
        dispatcher = "",
        timestamp = baseTime + (offsetMs * 1_000_000)
    )

    private fun cancelled(id: String, durationMs: Long, offsetMs: Long) = TraceEvent(
        type = "cancelled",
        id = id,
        parentId = null,
        operation = "",
        status = "cancelled",
        durationMs = durationMs.toDouble(),
        dispatcher = "",
        timestamp = baseTime + (offsetMs * 1_000_000)
    )
}

/**
 * Load mock data into the trace state.
 */
fun TraceState.loadMockData() {
    MockData.generateMockEvents().forEach { event ->
        processEvent(event)
    }
}
