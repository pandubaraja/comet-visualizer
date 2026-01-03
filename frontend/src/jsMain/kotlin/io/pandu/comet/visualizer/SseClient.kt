package io.pandu.comet.visualizer

import kotlinx.serialization.json.Json
import org.w3c.dom.EventSource

/**
 * SSE client for receiving trace events from the server.
 */
class SseClient(
    private val url: String = "/events",
    private val onEvent: (TraceEvent) -> Unit,
    private val onError: (String) -> Unit = {}
) {
    private var eventSource: EventSource? = null
    private val json = Json { ignoreUnknownKeys = true }

    fun connect() {
        eventSource = EventSource(url).apply {
            onmessage = { event ->
                val data = event.data as? String
                if (data != null) {
                    try {
                        val traceEvent = json.decodeFromString<TraceEvent>(data)
                        onEvent(traceEvent)
                    } catch (e: Exception) {
                        console.error("Failed to parse event: $data", e)
                    }
                }
            }
            onerror = {
                console.log("SSE connection error, reconnecting...")
                onError("Connection error")
            }
        }
    }

    fun disconnect() {
        eventSource?.close()
        eventSource = null
    }
}

private external val console: Console

private external interface Console {
    fun log(vararg args: Any?)
    fun error(vararg args: Any?)
}
