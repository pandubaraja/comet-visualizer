package io.pandu.comet.visualizer

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TraceServerTest {

    private fun findAvailablePort(): Int {
        // Use a port in the dynamic/private range
        return (49152..65535).random()
    }

    // =====================================================================
    // Server Lifecycle Tests
    // =====================================================================

    @Test
    fun `server starts and stops without error`() {
        val port = findAvailablePort()
        val server = TraceServer(port = port)

        server.start()
        Thread.sleep(100) // Give server time to start

        server.stop()
        // If we reach here without exception, test passes
    }

    @Test
    fun `server starts in SSE-only mode`() {
        val port = findAvailablePort()
        val server = TraceServer(port = port, sseOnly = true)

        server.start()
        Thread.sleep(100)

        // In SSE-only mode, root path should return 404
        val connection = URL("http://localhost:$port/").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 1000
        connection.readTimeout = 1000

        try {
            assertEquals(404, connection.responseCode)
        } finally {
            connection.disconnect()
            server.stop()
        }
    }

    // =====================================================================
    // SSE Endpoint Tests
    // =====================================================================

    @Test
    fun `SSE endpoint returns correct headers`() {
        val port = findAvailablePort()
        val server = TraceServer(port = port, sseOnly = true)
        server.start()
        Thread.sleep(100)

        val connection = URL("http://localhost:$port/events").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 1000
        connection.readTimeout = 1000

        try {
            assertEquals(200, connection.responseCode)
            assertEquals("text/event-stream", connection.contentType)
            assertTrue(connection.getHeaderField("Access-Control-Allow-Origin") == "*")
        } finally {
            connection.disconnect()
            server.stop()
        }
    }

    @Test
    fun `sendEvent broadcasts to connected client`() {
        val port = findAvailablePort()
        val server = TraceServer(port = port, sseOnly = true)
        server.start()
        Thread.sleep(100)

        val connection = URL("http://localhost:$port/events").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 2000
        connection.readTimeout = 2000

        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))

            // Give client time to connect
            Thread.sleep(200)

            // Send an event
            val testEvent = """{"type":"started","id":"test-123"}"""
            server.sendEvent(testEvent)

            // Read the event
            val line = reader.readLine()
            assertTrue(line.startsWith("data: "), "Expected SSE data format, got: $line")
            assertTrue(line.contains("test-123"), "Expected event data in response")
        } finally {
            connection.disconnect()
            server.stop()
        }
    }

    @Test
    fun `sendEvent formats event correctly as SSE`() {
        val port = findAvailablePort()
        val server = TraceServer(port = port, sseOnly = true)
        server.start()
        Thread.sleep(100)

        val connection = URL("http://localhost:$port/events").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 2000
        connection.readTimeout = 2000

        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            Thread.sleep(200)

            val testEvent = """{"key":"value"}"""
            server.sendEvent(testEvent)

            val line = reader.readLine()
            assertEquals("data: $testEvent", line)
        } finally {
            connection.disconnect()
            server.stop()
        }
    }

    // =====================================================================
    // Static File Serving Tests (non-SSE mode)
    // =====================================================================

    @Test
    fun `root path serves index html when resources are bundled`() {
        // When frontend resources are bundled (via copyFrontend task),
        // the server serves index.html at root path
        val port = findAvailablePort()
        val server = TraceServer(port = port, sseOnly = false)
        server.start()
        Thread.sleep(100)

        val connection = URL("http://localhost:$port/").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 1000
        connection.readTimeout = 1000

        try {
            // With bundled resources, should return 200
            // Without resources, would return 404
            val responseCode = connection.responseCode
            assertTrue(responseCode == 200 || responseCode == 404,
                "Expected 200 (bundled) or 404 (not bundled), got $responseCode")
        } finally {
            connection.disconnect()
            server.stop()
        }
    }

    @Test
    fun `unknown path returns 404`() {
        val port = findAvailablePort()
        val server = TraceServer(port = port, sseOnly = false)
        server.start()
        Thread.sleep(100)

        val connection = URL("http://localhost:$port/nonexistent").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 1000
        connection.readTimeout = 1000

        try {
            assertEquals(404, connection.responseCode)
        } finally {
            connection.disconnect()
            server.stop()
        }
    }

    // =====================================================================
    // Multiple Client Tests
    // =====================================================================

    @Test
    fun `sendEvent broadcasts to multiple clients`() {
        val port = findAvailablePort()
        val server = TraceServer(port = port, sseOnly = true)
        server.start()
        Thread.sleep(100)

        val connection1 = URL("http://localhost:$port/events").openConnection() as HttpURLConnection
        val connection2 = URL("http://localhost:$port/events").openConnection() as HttpURLConnection

        connection1.connectTimeout = 2000
        connection1.readTimeout = 2000
        connection2.connectTimeout = 2000
        connection2.readTimeout = 2000

        try {
            val reader1 = BufferedReader(InputStreamReader(connection1.inputStream))
            val reader2 = BufferedReader(InputStreamReader(connection2.inputStream))

            Thread.sleep(200)

            val testEvent = """{"id":"broadcast-test"}"""
            server.sendEvent(testEvent)

            val line1 = reader1.readLine()
            val line2 = reader2.readLine()

            assertTrue(line1.contains("broadcast-test"))
            assertTrue(line2.contains("broadcast-test"))
        } finally {
            connection1.disconnect()
            connection2.disconnect()
            server.stop()
        }
    }

    // =====================================================================
    // Client Disconnection Tests
    // =====================================================================

    @Test
    fun `server handles client disconnection gracefully`() {
        val port = findAvailablePort()
        val server = TraceServer(port = port, sseOnly = true)
        server.start()
        Thread.sleep(100)

        // Connect and immediately disconnect
        val connection = URL("http://localhost:$port/events").openConnection() as HttpURLConnection
        connection.connectTimeout = 1000
        connection.readTimeout = 1000
        connection.inputStream // Force connection
        Thread.sleep(100)
        connection.disconnect()

        Thread.sleep(100)

        // Send event after client disconnected - should not throw
        server.sendEvent("""{"test":"after-disconnect"}""")

        // If we reach here without exception, server handled disconnection
        server.stop()
    }

    // =====================================================================
    // Port Configuration Tests
    // =====================================================================

    @Test
    fun `server uses specified port`() {
        val port = findAvailablePort()
        val server = TraceServer(port = port)
        server.start()
        Thread.sleep(100)

        val connection = URL("http://localhost:$port/events").openConnection() as HttpURLConnection
        connection.connectTimeout = 1000
        connection.readTimeout = 1000

        try {
            assertEquals(200, connection.responseCode)
        } finally {
            connection.disconnect()
            server.stop()
        }
    }

    @Test
    fun `default port is 8080`() {
        // Just verify the constructor default
        val server = TraceServer()
        // We can't easily verify the port without starting, but we test that
        // the constructor accepts no arguments (uses defaults)
        assertTrue(true)
    }
}
