package io.pandu.comet.visualizer

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.MutableSharedFlow

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
 *
 * @param port The port to listen on (default: 8080)
 * @param sseOnly If true, only serve SSE endpoint (for use with external dev server)
 */
class TraceServer(
    private val port: Int = 8080,
    private val sseOnly: Boolean = false
) {
    private val eventFlow = MutableSharedFlow<String>(extraBufferCapacity = 256)
    private lateinit var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>

    /**
     * Starts the HTTP server.
     */
    fun start() {
        server = embeddedServer(CIO, port = port) {
            routing {
                if (!sseOnly) {
                    get("/") {
                        serveResource(call, "/static/index.html", ContentType.Text.Html)
                    }
                    get("/index.html") {
                        serveResource(call, "/static/index.html", ContentType.Text.Html)
                    }
                    get("/comet-visualizer.js") {
                        serveResource(call, "/static/comet-visualizer.js", ContentType.Application.JavaScript)
                    }
                    get("/comet-visualizer.js.map") {
                        serveResource(call, "/static/comet-visualizer.js.map", ContentType.Application.Json)
                    }
                    get("/icons/{path...}") {
                        val path = call.parameters.getAll("path")?.joinToString("/") ?: ""
                        val contentType = when {
                            path.endsWith(".png") -> ContentType.Image.PNG
                            path.endsWith(".ico") -> ContentType("image", "x-icon")
                            path.endsWith(".svg") -> ContentType.Image.SVG
                            else -> ContentType.Application.OctetStream
                        }
                        serveResource(call, "/static/icons/$path", contentType)
                    }
                }

                get("/events") {
                    call.response.header(HttpHeaders.CacheControl, "no-cache")
                    call.response.header(HttpHeaders.Connection, "keep-alive")
                    call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
                    call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                        eventFlow.collect { event ->
                            write("data: $event\n\n")
                            flush()
                        }
                    }
                }
            }
        }

        server.start(wait = false)
        if (sseOnly) {
            println("Comet TraceServer (SSE only) started at http://localhost:$port/events")
        } else {
            println("Comet TraceServer started at http://localhost:$port")
        }
    }

    private suspend fun serveResource(call: io.ktor.server.application.ApplicationCall, resourcePath: String, contentType: ContentType) {
        val inputStream = javaClass.getResourceAsStream(resourcePath)
        if (inputStream != null) {
            val bytes = inputStream.readBytes()
            call.respondBytes(bytes, contentType)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    /**
     * Sends a trace event to all connected clients via SSE.
     * @param event JSON string of the TraceEvent
     */
    fun sendEvent(event: String) {
        eventFlow.tryEmit(event)
    }

    /**
     * Stops the HTTP server and closes all client connections.
     */
    fun stop() {
        server.stop(0, 0)
        println("Comet TraceServer stopped")
    }
}
