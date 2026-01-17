# Comet Visualizer

Real-time trace visualization for Kotlin coroutines. A web-based UI that displays coroutine execution traces with tree and Gantt chart views.

## Features

- **Tree View**: Hierarchical display of coroutine traces with parent-child relationships
- **Gantt Chart**: Timeline visualization with mouse-centered zoom (Ctrl+Scroll)
- **Source Location**: Display file and line number where coroutines were created
- **Real-time Updates**: Live streaming via Server-Sent Events (SSE)
- **Dark/Light Mode**: Toggle between themes
- **Hot Reload**: Development mode with live frontend updates

## Project Structure

```
comet-visualizer/
├── shared/      # Kotlin Multiplatform data models (JVM + JS)
├── frontend/    # Kotlin/JS + Compose for Web UI
└── library/     # JVM server library (TraceServer + bundled frontend)
```

## Integration

Add to your project:

```kotlin
dependencies {
    implementation("io.pandu.comet:comet-visualizer:0.1.0")
}
```

Usage:

```kotlin
val server = TraceServer(port = 8080)
server.start()

// Send trace events
val event = TraceEvent(
    type = "started",
    id = "span-id",
    parentId = null,
    operation = "my-operation",
    status = "running",
    dispatcher = "Dispatchers.Default",
    timestamp = System.nanoTime(),
    sourceFile = "MyFile.kt",    // Optional: source file name
    lineNumber = 42              // Optional: line number
)
server.sendEvent(Json.encodeToString(event))

// When done
server.stop()
```

## Controls

- **Tree/Gantt Toggle**: Switch between visualization modes
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
