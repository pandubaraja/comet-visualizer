package io.pandu.comet.visualizer

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.concurrent.thread

/**
 * Demo main to test the visualizer server.
 * Run with: ./gradlew :server:run
 */
fun main() {
    val server = TraceServer(8080)
    server.start()

    println("Open http://localhost:8080 in your browser")
    println("Sending demo events...")

    val json = Json { prettyPrint = false }

    // Simulate some trace events
    thread {
        Thread.sleep(1000)

        // Start root
        sendEvent(server, json, TraceEvent(
            type = "started",
            id = "span-1",
            parentId = null,
            operation = "api-gateway",
            status = "running",
            dispatcher = "Dispatchers.Default",
            timestamp = System.nanoTime()
        ))

        Thread.sleep(500)

        // Start child
        sendEvent(server, json, TraceEvent(
            type = "started",
            id = "span-2",
            parentId = "span-1",
            operation = "authenticate",
            status = "running",
            dispatcher = "Dispatchers.Default",
            timestamp = System.nanoTime()
        ))

        Thread.sleep(800)

        // Complete child
        sendEvent(server, json, TraceEvent(
            type = "completed",
            id = "span-2",
            parentId = "span-1",
            operation = "authenticate",
            status = "completed",
            durationMs = 800.0,
            dispatcher = "Dispatchers.Default",
            timestamp = System.nanoTime()
        ))

        Thread.sleep(300)

        // Start another child
        sendEvent(server, json, TraceEvent(
            type = "started",
            id = "span-3",
            parentId = "span-1",
            operation = "fetch-data",
            status = "running",
            dispatcher = "Dispatchers.IO",
            timestamp = System.nanoTime()
        ))

        Thread.sleep(600)

        // Complete fetch-data
        sendEvent(server, json, TraceEvent(
            type = "completed",
            id = "span-3",
            parentId = "span-1",
            operation = "fetch-data",
            status = "completed",
            durationMs = 600.0,
            dispatcher = "Dispatchers.IO",
            timestamp = System.nanoTime()
        ))

        Thread.sleep(200)

        // Complete root
        sendEvent(server, json, TraceEvent(
            type = "completed",
            id = "span-1",
            parentId = null,
            operation = "api-gateway",
            status = "completed",
            durationMs = 2400.0,
            dispatcher = "Dispatchers.Default",
            timestamp = System.nanoTime()
        ))

        println("\nDemo events sent! Press Ctrl+C to exit.")
    }

    // Keep running
    Thread.sleep(5000)
    Thread.currentThread().join()
}

private fun sendEvent(server: TraceServer, json: Json, event: TraceEvent) {
    val jsonString = json.encodeToString(event)
    println("Sending: ${event.type} - ${event.operation}")
    server.sendEvent(jsonString)
}
