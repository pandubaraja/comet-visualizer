# Comet Visualizer

Comet Visualizer is a real-time web UI for visualizing Kotlin coroutine traces produced by Comet (Coroutine Telemetry). It presents structured concurrency as hierarchical tree views, concurrent Gantt charts, and execution metrics, making coroutine behavior easier to understand and analyze.

## Features

- **Tree View**: Hierarchical display of coroutine traces with parent-child relationships
- **Gantt Chart**: Timeline visualization
- **Source Location**: Display file and line number where coroutines were created
- **Package Names**: Shows the originating package path alongside operation names in tree nodes, Gantt bars, and graph view
- **Real-time Updates**: Live streaming via Server-Sent Events (SSE)
- **Dark/Light Mode**: Toggle between themes

## Demo
https://github.com/user-attachments/assets/900caecc-4b20-4dfb-ba57-ac44d622b9e3

See [comet-demo](https://github.com/pandubaraja/comet-demo) for a full KMP sample app (Android + iOS) demonstrating Comet and comet-visualizer integration with real API calls and some coroutine patterns.

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
    implementation("io.github.pandubaraja:comet:0.3.0")
    implementation("io.github.pandubaraja:comet-visualizer:0.3.0")
}
```

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

> **Note:** If running on an Android emulator or device, forward the port first:
> ```bash
> adb forward tcp:8080 tcp:8080
> ```
> Then open `http://localhost:8080` in your browser.

## License

```
Copyright 2025

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
