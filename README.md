# Comet Visualizer

Comet Visualizer is a real-time web UI for visualizing Kotlin coroutine traces produced by Comet (Coroutine Telemetry). It presents structured concurrency as hierarchical tree views, concurrent Gantt charts, and execution metrics, making coroutine behavior easier to understand and analyze.

## Features

- **Tree View**: Hierarchical display of coroutine traces with parent-child relationships
- **Gantt Chart**: Timeline visualization
- **Source Location**: Display file and line number where coroutines were created
- **Real-time Updates**: Live streaming via Server-Sent Events (SSE)
- **Dark/Light Mode**: Toggle between themes

## Preview
https://github.com/user-attachments/assets/9d9fb746-a588-4b4c-b1ab-81018c221a96

## Project Structure

```
comet-visualizer/
├── shared/      # Kotlin Multiplatform data models
├── frontend/    # Kotlin/JS + Compose for Web UI
└── library/     # Server library (TraceServer + bundled frontend)
```

## Integration

Add both dependencies to your project:

```kotlin
dependencies {
    implementation("io.github.pandubaraja:comet:0.2.0")
    implementation("io.github.pandubaraja:comet-visualizer:0.2.0")
}
```

### Using with Comet

The easiest way to use the visualizer is with `VisualizerJsonExporter` from the Comet library:

```kotlin
import io.pandu.Comet
import io.pandu.comet.visualizer.TraceServer
import io.pandu.core.telemetry.exporters.VisualizerJsonExporter

fun main() = runBlocking {
    // Start the visualizer server
    val server = TraceServer(port = 8080)
    server.start()

    // Configure Comet with the visualizer exporter
    val comet = Comet.create {
        exporter(VisualizerJsonExporter(server::sendEvent))
        includeStackTrace(true)  // Enables source file/line display
    }
    comet.start()

    // Use comet.traced() to instrument your coroutines
    launch(comet.traced("my-operation")) {
        // Your coroutine code here
        launch(CoroutineName("child-task")) {
            delay(100)
        }
    }

    // Open http://localhost:8080 in your browser

    // Cleanup
    comet.shutdown()
    server.stop()
}
```

### Manual Integration

If you're not using the Comet library, you can send trace events directly:

```kotlin
val server = TraceServer(port = 8080)
server.start()

// Send trace events as JSON
val event = TraceEvent(
    type = "started",       // "started", "completed", "failed", or "cancelled"
    id = "span-id",
    parentId = null,
    operation = "my-operation",
    status = "running",
    dispatcher = "Dispatchers.Default",
    timestamp = System.nanoTime(),
    sourceFile = "MyFile.kt",
    lineNumber = 42
)
server.sendEvent(Json.encodeToString(event))

server.stop()
```

## Demo App

See [comet-demo](https://github.com/pandubaraja/comet-demo) for a full KMP sample app (Android + iOS) that integrates both Comet and comet-visualizer with real API calls and various coroutine tracing patterns.

## Controls

- **Tree/Gantt/Performance Toggle**: Switch between visualization modes
- **Theme Toggle**: Switch between dark and light mode
- **Gantt Zoom**: Ctrl + Scroll (or Cmd + Scroll on Mac) - zooms centered on mouse pointer
- **Node Details**: Click on any node to view details including source location

## Tech Stack

- Kotlin Multiplatform
- Compose for Web
- Tailwind CSS
- Server-Sent Events (SSE)

## License

```
Copyright 2025

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
