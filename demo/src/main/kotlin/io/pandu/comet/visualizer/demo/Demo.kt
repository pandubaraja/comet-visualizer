package io.pandu.comet.visualizer.demo

import io.pandu.comet.visualizer.TraceEvent
import io.pandu.comet.visualizer.TraceServer
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private val json = Json { prettyPrint = false }

fun main() = runBlocking {
    val server = TraceServer(8080)
    server.start()

    println("Comet Visualizer Demo")
    println("Open http://localhost:8080 in your browser")
    println("Press Enter to run demo traces...")
    readLine()

    val demo = TraceDemo(server)

    // Demo 1: API Gateway request
    demo.runApiGatewayDemo()

    delay(1000)

    // Demo 2: Background job processing
    println("\n> Starting background jobs...\n")
    demo.runBackgroundJobsDemo()

    println("\nDemo complete! Server running at http://localhost:8080")
    println("Press Enter to run demo again, or Ctrl+C to exit...")

    // Keep server alive and allow re-running demo
    while (true) {
        readLine()
        println("\nRunning demo again...")
        demo.runApiGatewayDemo()
        delay(1000)
        demo.runBackgroundJobsDemo()
        println("\nPress Enter to run demo again, or Ctrl+C to exit...")
    }
}

class TraceDemo(private val server: TraceServer) {
    private fun generateSpanId(): String = Random.nextLong().toULong().toString(16).padStart(16, '0')

    private fun sendEvent(event: TraceEvent) {
        val jsonString = json.encodeToString(event)
        println("  ${event.operation}: ${event.type}")
        server.sendEvent(jsonString)
    }

    private fun started(
        id: String,
        parentId: String?,
        operation: String,
        dispatcher: String = "Dispatchers.Default"
    ) = TraceEvent(
        type = "started",
        id = id,
        parentId = parentId,
        operation = operation,
        status = "running",
        dispatcher = dispatcher,
        timestamp = System.nanoTime()
    )

    private fun completed(
        id: String,
        parentId: String?,
        operation: String,
        durationMs: Double,
        dispatcher: String = "Dispatchers.Default"
    ) = TraceEvent(
        type = "completed",
        id = id,
        parentId = parentId,
        operation = operation,
        status = "completed",
        durationMs = durationMs,
        dispatcher = dispatcher,
        timestamp = System.nanoTime()
    )

    private fun failed(
        id: String,
        parentId: String?,
        operation: String,
        durationMs: Double,
        dispatcher: String = "Dispatchers.Default"
    ) = TraceEvent(
        type = "failed",
        id = id,
        parentId = parentId,
        operation = operation,
        status = "failed",
        durationMs = durationMs,
        dispatcher = dispatcher,
        timestamp = System.nanoTime()
    )

    private fun cancelled(
        id: String,
        parentId: String?,
        operation: String,
        durationMs: Double,
        dispatcher: String = "Dispatchers.Default"
    ) = TraceEvent(
        type = "cancelled",
        id = id,
        parentId = parentId,
        operation = operation,
        status = "cancelled",
        durationMs = durationMs,
        dispatcher = dispatcher,
        timestamp = System.nanoTime()
    )

    suspend fun runApiGatewayDemo() = coroutineScope {
        val apiGatewayId = generateSpanId()

        // Start api-gateway
        sendEvent(started(apiGatewayId, null, "api-gateway"))
        delay(200)

        // Authentication flow
        val authId = generateSpanId()
        sendEvent(started(authId, apiGatewayId, "authenticate"))
        delay(150)

        // JWT validation
        val jwtId = generateSpanId()
        sendEvent(started(jwtId, authId, "validate-jwt"))
        delay(800)
        sendEvent(completed(jwtId, authId, "validate-jwt", 800.0))

        delay(100)

        // Load user session
        val sessionId = generateSpanId()
        sendEvent(started(sessionId, authId, "load-user-session"))
        delay(600)
        sendEvent(completed(sessionId, authId, "load-user-session", 600.0))

        sendEvent(completed(authId, apiGatewayId, "authenticate", 1650.0))
        delay(100)

        // Parallel data fetches
        val profileId = generateSpanId()
        val notificationsId = generateSpanId()
        val recommendationsId = generateSpanId()

        // Start all three in parallel
        sendEvent(started(profileId, apiGatewayId, "fetch-user-profile"))
        delay(50)
        sendEvent(started(notificationsId, apiGatewayId, "fetch-notifications"))
        delay(50)
        sendEvent(started(recommendationsId, apiGatewayId, "fetch-recommendations"))

        // Profile: db queries
        delay(100)
        val dbUserId = generateSpanId()
        sendEvent(started(dbUserId, profileId, "db-query-user", "Dispatchers.IO"))

        // Notifications: redis lookup
        delay(50)
        val redisId = generateSpanId()
        sendEvent(started(redisId, notificationsId, "redis-cache-lookup", "Dispatchers.IO"))

        // Recommendations: ML inference
        delay(50)
        val mlId = generateSpanId()
        sendEvent(started(mlId, recommendationsId, "ml-inference"))

        // Complete redis first
        delay(500)
        sendEvent(completed(redisId, notificationsId, "redis-cache-lookup", 550.0, "Dispatchers.IO"))
        sendEvent(completed(notificationsId, apiGatewayId, "fetch-notifications", 700.0))

        // Complete db user query
        delay(400)
        sendEvent(completed(dbUserId, profileId, "db-query-user", 900.0, "Dispatchers.IO"))

        // Start preferences query
        val dbPrefsId = generateSpanId()
        sendEvent(started(dbPrefsId, profileId, "db-query-preferences", "Dispatchers.IO"))

        delay(600)
        sendEvent(completed(dbPrefsId, profileId, "db-query-preferences", 600.0, "Dispatchers.IO"))
        sendEvent(completed(profileId, apiGatewayId, "fetch-user-profile", 2000.0))

        // Complete ML inference
        delay(300)
        sendEvent(completed(mlId, recommendationsId, "ml-inference", 1800.0))
        sendEvent(completed(recommendationsId, apiGatewayId, "fetch-recommendations", 2100.0))

        delay(100)

        // Build response
        val responseId = generateSpanId()
        sendEvent(started(responseId, apiGatewayId, "build-response"))
        delay(400)
        sendEvent(completed(responseId, apiGatewayId, "build-response", 400.0))

        // Complete api-gateway
        sendEvent(completed(apiGatewayId, null, "api-gateway", 4500.0))
    }

    suspend fun runBackgroundJobsDemo() = coroutineScope {
        val schedulerId = generateSpanId()
        sendEvent(started(schedulerId, null, "job-scheduler"))
        delay(200)

        // Start batch jobs
        val jobs = mutableListOf<Pair<String, String>>() // id to name

        for (i in 1..3) {
            delay(200)
            val jobId = generateSpanId()
            val jobName = "batch-job-$i"
            jobs.add(jobId to jobName)
            sendEvent(started(jobId, schedulerId, jobName))
        }

        // Process each job with subtasks
        for ((index, job) in jobs.withIndex()) {
            val (jobId, jobName) = job
            delay(300 + (index * 200).toLong())

            // Subtask A - will be cancelled
            val subtaskAId = generateSpanId()
            sendEvent(started(subtaskAId, jobId, "job-${index + 1}-subtask-a", "Dispatchers.IO"))
            delay(200)
            sendEvent(cancelled(subtaskAId, jobId, "job-${index + 1}-subtask-a", 200.0, "Dispatchers.IO"))

            delay(100)

            // Subtask B - completes successfully
            val subtaskBId = generateSpanId()
            sendEvent(started(subtaskBId, jobId, "job-${index + 1}-subtask-b"))
            delay(400)
            sendEvent(completed(subtaskBId, jobId, "job-${index + 1}-subtask-b", 400.0))

            // Complete job
            val duration = 900.0 + (index * 300)
            sendEvent(completed(jobId, schedulerId, jobName, duration))
        }

        // Complete scheduler
        delay(200)
        sendEvent(completed(schedulerId, null, "job-scheduler", 3500.0))
    }
}
