package io.pandu.comet.visualizer.demo

import io.pandu.comet.visualizer.TraceServer
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds

/**
 * Development mode demo that runs the TraceServer in SSE-only mode.
 * Use this with `./gradlew :frontend:jsBrowserDevelopmentRun` for hot reload.
 *
 * Terminal 1: ./gradlew :demo:runDev
 * Terminal 2: ./gradlew :frontend:jsBrowserDevelopmentRun --continuous
 * Browser: http://localhost:3000
 */
fun main() = runBlocking {
    val server = TraceServer(port = 8080, sseOnly = true)
    server.start()

    println()
    println("=".repeat(60))
    println("Development Mode - Hot Reload Enabled")
    println("=".repeat(60))
    println()
    println("1. In another terminal, run:")
    println("   ./gradlew :frontend:jsBrowserDevelopmentRun --continuous")
    println()
    println("2. Open http://localhost:3000 in your browser")
    println()
    println("3. Edit frontend code - changes will auto-reload!")
    println()
    println("=".repeat(60))
    println()
    println("Press Enter to run demo traces...")
    readLine()

    val demo = TraceDemo(server)

    // Run demo
    demo.runApiGatewayDemo()
    delay(1000)
    demo.runBackgroundJobsDemo()

    println("\nPress Enter to run demo again, or Ctrl+C to exit...")

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
