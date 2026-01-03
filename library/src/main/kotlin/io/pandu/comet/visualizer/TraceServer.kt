package io.pandu.comet.visualizer

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.InputStream
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList

/**
 * HTTP server for real-time trace visualization.
 * Serves a Kotlin/JS web UI and streams trace events via Server-Sent Events (SSE).
 *
 * Usage:
 * ```kotlin
 * val server = TraceServer(port = 8080)
 * server.start()
 *
 * val exporter = CallbackCoroutineTelemetryExporter { event ->
 *     val traceEvent = TraceEvent(...)
 *     server.sendEvent(json.encodeToString(traceEvent))
 * }
 *
 * val comet = Comet.create {
 *     exporter(exporter)
 * }
 * comet.start()
 *
 * // Open http://localhost:8080 in browser
 * // ...
 *
 * comet.shutdown()
 * server.stop()
 * ```
 */
class TraceServer(private val port: Int = 8080) {
    private val clients = CopyOnWriteArrayList<HttpExchange>()
    private lateinit var server: HttpServer

    /**
     * Starts the HTTP server.
     */
    fun start() {
        server = HttpServer.create(InetSocketAddress(port), 0)

        // Serve index.html
        server.createContext("/") { exchange ->
            if (exchange.requestURI.path == "/" || exchange.requestURI.path == "/index.html") {
                serveResource(exchange, "/static/index.html", "text/html")
            } else {
                exchange.sendResponseHeaders(404, -1)
            }
        }

        // Serve JS bundle
        server.createContext("/comet-visualizer.js") { exchange ->
            serveResource(exchange, "/static/comet-visualizer.js", "application/javascript")
        }

        // Serve source maps if available
        server.createContext("/comet-visualizer.js.map") { exchange ->
            serveResource(exchange, "/static/comet-visualizer.js.map", "application/json")
        }

        // SSE endpoint
        server.createContext("/events") { exchange ->
            exchange.responseHeaders.add("Content-Type", "text/event-stream")
            exchange.responseHeaders.add("Cache-Control", "no-cache")
            exchange.responseHeaders.add("Connection", "keep-alive")
            exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
            exchange.sendResponseHeaders(200, 0)
            clients.add(exchange)
        }

        server.executor = null
        server.start()
        println("Comet TraceServer started at http://localhost:$port")
    }

    private fun serveResource(exchange: HttpExchange, resourcePath: String, contentType: String) {
        val inputStream: InputStream? = javaClass.getResourceAsStream(resourcePath)
        if (inputStream != null) {
            val response = inputStream.readBytes()
            exchange.responseHeaders.add("Content-Type", contentType)
            exchange.sendResponseHeaders(200, response.size.toLong())
            exchange.responseBody.use { it.write(response) }
        } else {
            exchange.sendResponseHeaders(404, -1)
        }
    }

    /**
     * Sends a trace event to all connected clients via SSE.
     * @param event JSON string of the TraceEvent
     */
    fun sendEvent(event: String) {
        val data = "data: $event\n\n"
        val deadClients = mutableListOf<HttpExchange>()

        clients.forEach { client ->
            try {
                client.responseBody.write(data.toByteArray())
                client.responseBody.flush()
            } catch (e: Exception) {
                deadClients.add(client)
            }
        }

        clients.removeAll(deadClients)
    }

    /**
     * Stops the HTTP server and closes all client connections.
     */
    fun stop() {
        clients.forEach {
            try { it.responseBody.close() } catch (_: Exception) {}
        }
        server.stop(0)
        println("Comet TraceServer stopped")
    }
}
