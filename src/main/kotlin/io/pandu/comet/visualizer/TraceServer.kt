package io.pandu.comet.visualizer

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList

/**
 * HTTP server for real-time trace visualization.
 * Serves a web UI and streams trace events via Server-Sent Events (SSE).
 *
 * Usage:
 * ```kotlin
 * val server = TraceServer(port = 8080)
 * server.start()
 *
 * val exporter = RealtimeExporter { event ->
 *     server.sendEvent(event)
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

        server.createContext("/") { exchange ->
            val response = HTML_PAGE.toByteArray()
            exchange.responseHeaders.add("Content-Type", "text/html")
            exchange.sendResponseHeaders(200, response.size.toLong())
            exchange.responseBody.use { it.write(response) }
        }

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

    /**
     * Sends a trace event to all connected clients via SSE.
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

val HTML_PAGE = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Comet - Real-time Trace Visualization</title>
    <style>
        :root {
            --bg-primary: #0a0a0f;
            --bg-secondary: rgba(255,255,255,0.02);
            --bg-card: rgba(255,255,255,0.03);
            --bg-card-hover: rgba(255,255,255,0.06);
            --bg-running: rgba(59,130,246,0.08);
            --text-primary: #e2e8f0;
            --text-secondary: #94a3b8;
            --text-muted: #64748b;
            --border-color: rgba(255,255,255,0.1);
            --border-dashed: rgba(255,255,255,0.15);
            --duration-bg: rgba(96,165,250,0.15);
            --dispatcher-bg: rgba(255,255,255,0.08);
        }
        [data-theme="light"] {
            --bg-primary: #f8fafc;
            --bg-secondary: #f1f5f9;
            --bg-card: #ffffff;
            --bg-card-hover: #f1f5f9;
            --bg-running: rgba(59,130,246,0.1);
            --text-primary: #1e293b;
            --text-secondary: #475569;
            --text-muted: #64748b;
            --border-color: #e2e8f0;
            --border-dashed: #cbd5e1;
            --duration-bg: rgba(59,130,246,0.12);
            --dispatcher-bg: #e2e8f0;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: var(--bg-primary);
            min-height: 100vh;
            color: var(--text-primary);
            transition: background 0.3s, color 0.3s;
        }
        .layout {
            display: grid;
            grid-template-columns: 1fr 400px;
            height: 100vh;
        }
        .main-panel {
            padding: 1.5rem;
            overflow-y: auto;
            border-right: 1px solid var(--border-color);
        }
        .timeline-panel {
            background: var(--bg-secondary);
            padding: 1.5rem;
            overflow-y: auto;
        }
        header {
            margin-bottom: 1.5rem;
            padding-bottom: 1rem;
            border-bottom: 1px solid var(--border-color);
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
        }
        h1 {
            font-size: 1.5rem;
            background: linear-gradient(90deg, #60a5fa, #a78bfa);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        h1::before { content: "☄️"; -webkit-text-fill-color: initial; }
        .theme-toggle {
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 8px 12px;
            cursor: pointer;
            font-size: 1rem;
            transition: all 0.2s;
            display: flex;
            align-items: center;
            gap: 6px;
        }
        .theme-toggle:hover { background: var(--bg-card-hover); }
        .theme-toggle .icon { font-size: 1.1rem; }
        .theme-toggle .label { font-size: 0.8rem; color: var(--text-secondary); }
        .stats {
            display: flex;
            gap: 1.5rem;
            margin-top: 1rem;
        }
        .stat {
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        .stat-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
        }
        .stat-dot.running { background: #3b82f6; animation: pulse 1.5s infinite; }
        .stat-dot.completed { background: #10b981; }
        .stat-dot.failed { background: #ef4444; }
        .stat-dot.cancelled { background: #f59e0b; }
        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
        }
        .stat-value { font-weight: 600; font-size: 1.1rem; }
        .stat-label { color: var(--text-muted); font-size: 0.8rem; }

        .tree { padding: 0.5rem 0; }
        .tree-node {
            position: relative;
            margin: 4px 0;
            transition: opacity 0.2s;
        }
        .node-row {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            background: var(--bg-card);
            border-radius: 6px;
            border-left: 3px solid transparent;
            transition: all 0.2s ease;
        }
        [data-theme="light"] .node-row { box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
        .node-row:hover { background: var(--bg-card-hover); }
        .node-row.running { border-left-color: #3b82f6; background: var(--bg-running); }
        .node-row.completed { border-left-color: #10b981; }
        .node-row.failed { border-left-color: #ef4444; }
        .node-row.cancelled { border-left-color: #f59e0b; }

        .status-icon {
            width: 18px;
            height: 18px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 10px;
            color: white;
            flex-shrink: 0;
        }
        .status-icon.running { background: #3b82f6; }
        .status-icon.completed { background: #10b981; }
        .status-icon.failed { background: #ef4444; }
        .status-icon.cancelled { background: #f59e0b; }

        .operation { font-weight: 500; font-size: 0.9rem; }
        .meta {
            margin-left: auto;
            display: flex;
            gap: 8px;
            align-items: center;
        }
        .duration {
            font-family: 'SF Mono', Monaco, monospace;
            font-size: 0.75rem;
            color: #3b82f6;
            background: var(--duration-bg);
            padding: 2px 6px;
            border-radius: 4px;
        }
        .dispatcher {
            font-size: 0.7rem;
            color: var(--text-secondary);
            background: var(--dispatcher-bg);
            padding: 2px 6px;
            border-radius: 4px;
        }
        .children {
            margin-left: 24px;
            padding-left: 12px;
            border-left: 1px dashed var(--border-dashed);
        }

        .timeline-header {
            font-size: 0.9rem;
            font-weight: 600;
            margin-bottom: 1rem;
            color: var(--text-secondary);
        }
        .timeline-item {
            display: flex;
            align-items: flex-start;
            gap: 12px;
            padding: 8px 0;
            border-bottom: 1px solid var(--border-color);
            animation: fadeIn 0.3s ease;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-5px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .timeline-time {
            font-family: 'SF Mono', Monaco, monospace;
            font-size: 0.7rem;
            color: var(--text-muted);
            min-width: 70px;
        }
        .timeline-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            margin-top: 4px;
            flex-shrink: 0;
        }
        .timeline-dot.started { background: #3b82f6; }
        .timeline-dot.completed { background: #10b981; }
        .timeline-dot.failed { background: #ef4444; }
        .timeline-dot.cancelled { background: #f59e0b; }
        .timeline-content {
            flex: 1;
            min-width: 0;
        }
        .timeline-op {
            font-size: 0.85rem;
            font-weight: 500;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .timeline-detail {
            font-size: 0.75rem;
            color: var(--text-muted);
            margin-top: 2px;
        }

        .empty-state {
            text-align: center;
            padding: 3rem;
            color: var(--text-muted);
        }
        .empty-state .icon { font-size: 2.5rem; margin-bottom: 0.75rem; }

        .header-controls {
            display: flex;
            gap: 8px;
            align-items: center;
        }
        .view-toggle {
            display: flex;
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            overflow: hidden;
        }
        .view-btn {
            padding: 8px 14px;
            border: none;
            background: transparent;
            color: var(--text-secondary);
            cursor: pointer;
            font-size: 0.8rem;
            font-weight: 500;
            transition: all 0.2s;
            display: flex;
            align-items: center;
            gap: 6px;
        }
        .view-btn:hover { background: var(--bg-card-hover); }
        .view-btn.active {
            background: #3b82f6;
            color: white;
        }
        .view-btn + .view-btn { border-left: 1px solid var(--border-color); }

        .gantt-container {
            display: none;
            flex-direction: column;
            height: calc(100vh - 180px);
            overflow: hidden;
        }
        .gantt-container.active { display: flex; }
        .tree.active { display: block; }
        .tree:not(.active) { display: none; }

        .gantt-header {
            border-bottom: 1px solid var(--border-color);
            background: var(--bg-secondary);
            position: sticky;
            top: 0;
            z-index: 10;
            min-height: 28px;
        }
        .gantt-labels-header {
            padding: 8px 12px;
            font-size: 0.75rem;
            font-weight: 600;
            color: var(--text-secondary);
            border-bottom: 1px solid var(--border-color);
            background: var(--bg-secondary);
            min-height: 28px;
            display: flex;
            align-items: center;
        }
        .gantt-timeline-header {
            position: relative;
            min-height: 28px;
        }
        .gantt-time-marker {
            position: absolute;
            top: 0;
            bottom: 0;
            border-left: 1px solid var(--border-color);
            padding: 4px 8px;
            font-size: 0.65rem;
            color: var(--text-muted);
            font-family: 'SF Mono', Monaco, monospace;
        }

        .gantt-content {
            flex: 1;
            display: flex;
            overflow: hidden;
        }
        .gantt-labels-column {
            min-width: 200px;
            max-width: 200px;
            overflow-y: auto;
            overflow-x: hidden;
            border-right: 1px solid var(--border-color);
            background: var(--bg-primary);
            flex-shrink: 0;
        }
        .gantt-labels-column::-webkit-scrollbar { display: none; }
        .gantt-timeline-column {
            flex: 1;
            overflow: auto;
        }
        .gantt-label-row {
            min-height: 32px;
            padding: 6px 12px;
            display: flex;
            align-items: center;
            gap: 8px;
            border-bottom: 1px solid var(--border-color);
            overflow: hidden;
            transition: background 0.15s;
        }
        .gantt-label-row:hover { background: var(--bg-card-hover); }
        .gantt-label-row.depth-0 { background: var(--bg-card); }
        .gantt-label-text {
            font-size: 0.8rem;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .gantt-indent { display: inline-block; }

        .gantt-bars-row {
            min-height: 32px;
            position: relative;
            border-bottom: 1px solid var(--border-color);
            transition: background 0.15s;
        }
        .gantt-bars-row:hover { background: var(--bg-card-hover); }
        .gantt-bars-row.depth-0 { background: var(--bg-card); }
        .gantt-bar {
            position: absolute;
            top: 4px;
            height: 24px;
            border-radius: 4px;
            min-width: 4px;
            transition: width 0.3s ease, background 0.2s;
            box-shadow: 0 1px 3px rgba(0,0,0,0.2);
            cursor: pointer;
            display: flex;
            align-items: center;
            padding: 0 6px;
            overflow: hidden;
        }
        .gantt-bar:hover {
            filter: brightness(1.1);
            z-index: 4;
        }
        .gantt-bar-text {
            font-size: 0.7rem;
            color: white;
            font-weight: 500;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .gantt-bar.running {
            background: linear-gradient(90deg, #3b82f6, #60a5fa);
            animation: gantt-pulse 1.5s infinite;
        }
        .gantt-bar.completed { background: linear-gradient(90deg, #10b981, #34d399); }
        .gantt-bar.failed { background: linear-gradient(90deg, #ef4444, #f87171); }
        .gantt-bar.cancelled { background: linear-gradient(90deg, #f59e0b, #fbbf24); }

        @keyframes gantt-pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.7; }
        }

        .gantt-tooltip {
            position: fixed;
            background: var(--bg-primary);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 10px 14px;
            font-size: 0.8rem;
            box-shadow: 0 4px 12px rgba(0,0,0,0.3);
            z-index: 1000;
            pointer-events: none;
            opacity: 0;
            transition: opacity 0.15s;
            max-width: 280px;
        }
        .gantt-tooltip.visible { opacity: 1; }
        .gantt-tooltip-title {
            font-weight: 600;
            margin-bottom: 6px;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .gantt-tooltip-row {
            display: flex;
            justify-content: space-between;
            gap: 16px;
            margin-top: 4px;
            color: var(--text-secondary);
        }
        .gantt-tooltip-row span:last-child {
            font-family: 'SF Mono', Monaco, monospace;
            color: var(--text-primary);
        }

        .gantt-scale-info {
            padding: 8px 12px;
            font-size: 0.75rem;
            color: var(--text-muted);
            border-bottom: 1px solid var(--border-color);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
    </style>
</head>
<body>
    <div class="layout">
        <div class="main-panel">
            <header>
                <div>
                    <h1>Comet Real-time Traces</h1>
                    <div class="stats">
                        <div class="stat">
                            <span class="stat-dot running"></span>
                            <span class="stat-value" id="running">0</span>
                            <span class="stat-label">running</span>
                        </div>
                        <div class="stat">
                            <span class="stat-dot completed"></span>
                            <span class="stat-value" id="completed">0</span>
                            <span class="stat-label">completed</span>
                        </div>
                        <div class="stat">
                            <span class="stat-dot failed"></span>
                            <span class="stat-value" id="failed">0</span>
                            <span class="stat-label">failed</span>
                        </div>
                    </div>
                </div>
                <div class="header-controls">
                    <div class="view-toggle">
                        <button class="view-btn active" id="tree-btn" onclick="setView('tree')">
                            <span>🌲</span> Tree
                        </button>
                        <button class="view-btn" id="gantt-btn" onclick="setView('gantt')">
                            <span>📊</span> Gantt
                        </button>
                    </div>
                    <button class="theme-toggle" onclick="toggleTheme()">
                        <span class="icon" id="theme-icon">☀️</span>
                        <span class="label" id="theme-label">Light</span>
                    </button>
                </div>
            </header>
            <div class="tree active" id="tree">
                <div class="empty-state">
                    <div class="icon">☄️</div>
                    <div>Waiting for traces...</div>
                </div>
            </div>
            <div class="gantt-container" id="gantt">
                <div class="gantt-scale-info">
                    <span id="gantt-range">Timeline: 0ms - 0ms</span>
                    <span style="font-size:0.7rem;opacity:0.6">Ctrl + Scroll to zoom</span>
                </div>
                <div class="gantt-content">
                    <div class="gantt-labels-column">
                        <div class="gantt-labels-header">Operation</div>
                        <div id="gantt-labels"></div>
                    </div>
                    <div class="gantt-timeline-column" id="gantt-timeline-column">
                        <div class="gantt-header">
                            <div class="gantt-timeline-header" id="gantt-time-header"></div>
                        </div>
                        <div id="gantt-bars-container"></div>
                    </div>
                </div>
            </div>
            <div class="gantt-tooltip" id="gantt-tooltip"></div>
        </div>
        <div class="timeline-panel">
            <div class="timeline-header">Event Timeline</div>
            <div id="timeline"></div>
        </div>
    </div>
    <script>
        const traces = new Map();
        const domNodes = new Map();
        const ganttRows = new Map();
        let stats = { running: 0, completed: 0, failed: 0 };
        let startTime = null;
        let currentView = 'tree';
        let ganttScale = 10;
        let ganttMinTime = Infinity;
        let ganttMaxTime = 0;
        let ganttUpdatePending = false;

        function updateStats() {
            document.getElementById('running').textContent = stats.running;
            document.getElementById('completed').textContent = stats.completed;
            document.getElementById('failed').textContent = stats.failed;
        }

        function getIcon(status) {
            return { running: '◉', completed: '✓', failed: '✗', cancelled: '⊘' }[status] || '?';
        }

        function formatTime(ts) {
            if (!startTime) startTime = ts;
            const ms = (ts - startTime) / 1_000_000;
            return '+' + ms.toFixed(0) + 'ms';
        }

        function getTimeMs(ts) {
            if (!startTime) startTime = ts;
            return (ts - startTime) / 1_000_000;
        }

        function setView(view) {
            currentView = view;
            document.getElementById('tree').classList.toggle('active', view === 'tree');
            document.getElementById('gantt').classList.toggle('active', view === 'gantt');
            document.getElementById('tree-btn').classList.toggle('active', view === 'tree');
            document.getElementById('gantt-btn').classList.toggle('active', view === 'gantt');
            localStorage.setItem('comet-view', view);
            if (view === 'gantt') renderGantt();
        }

        function getDepth(nodeId, depth = 0) {
            const node = traces.get(nodeId);
            if (!node || !node.parentId) return depth;
            return getDepth(node.parentId, depth + 1);
        }

        function renderGantt() {
            if (traces.size === 0) return;
            const labelsContainer = document.getElementById('gantt-labels');
            const barsContainer = document.getElementById('gantt-bars-container');
            const header = document.getElementById('gantt-time-header');
            const timelineColumn = document.getElementById('gantt-timeline-column');

            let minT = Infinity, maxT = 0;
            traces.forEach(node => {
                if (node.startMs !== undefined) {
                    minT = Math.min(minT, node.startMs);
                    const endMs = node.durationMs > 0 ? node.startMs + node.durationMs : node.startMs + 100;
                    maxT = Math.max(maxT, endMs);
                }
            });
            if (minT === Infinity) return;

            ganttMinTime = 0;
            ganttMaxTime = maxT + 50;
            const duration = ganttMaxTime - ganttMinTime;
            const containerWidth = timelineColumn.offsetWidth;
            const timelineWidth = Math.max(containerWidth, duration * ganttScale);

            document.getElementById('gantt-range').textContent = 'Timeline: 0ms - ' + ganttMaxTime.toFixed(0) + 'ms';

            header.innerHTML = '';
            header.style.width = timelineWidth + 'px';
            header.parentElement.style.width = timelineWidth + 'px';
            barsContainer.style.width = timelineWidth + 'px';
            const step = getTimeStep(duration);
            for (let t = 0; t <= ganttMaxTime; t += step) {
                const marker = document.createElement('div');
                marker.className = 'gantt-time-marker';
                marker.style.left = (t * ganttScale) + 'px';
                marker.textContent = t + 'ms';
                header.appendChild(marker);
            }

            const allNodes = Array.from(traces.values()).filter(n => n.startMs !== undefined);
            const orderedNodes = [];
            const processed = new Set();

            function addNodeWithChildren(node) {
                if (processed.has(node.id)) return;
                processed.add(node.id);
                orderedNodes.push(node);
                allNodes.filter(n => n.parentId === node.id).sort((a, b) => a.startMs - b.startMs).forEach(child => addNodeWithChildren(child));
            }

            allNodes.filter(n => !n.parentId || !traces.has(n.parentId)).sort((a, b) => a.startMs - b.startMs).forEach(node => addNodeWithChildren(node));

            labelsContainer.innerHTML = '';
            barsContainer.innerHTML = '';

            orderedNodes.forEach(node => {
                const depth = getDepth(node.id);
                const labelRow = document.createElement('div');
                labelRow.className = 'gantt-label-row' + (depth === 0 ? ' depth-0' : '');
                labelRow.dataset.id = node.id;
                labelRow.innerHTML = '<span class="gantt-indent" style="width:' + (depth * 16) + 'px"></span><span class="status-icon ' + node.status + '" style="width:14px;height:14px;font-size:8px">' + getIcon(node.status) + '</span><span class="gantt-label-text">' + node.operation + '</span>';
                labelsContainer.appendChild(labelRow);

                const barsRow = document.createElement('div');
                barsRow.className = 'gantt-bars-row' + (depth === 0 ? ' depth-0' : '');
                barsRow.dataset.id = node.id;
                const leftPos = node.startMs * ganttScale;
                const barWidth = node.durationMs > 0 ? Math.max(node.durationMs * ganttScale, 4) : Math.max((ganttMaxTime - node.startMs) * ganttScale * 0.3, 20);
                const bar = document.createElement('div');
                bar.className = 'gantt-bar ' + node.status;
                bar.style.left = leftPos + 'px';
                bar.style.width = barWidth + 'px';
                bar.dataset.id = node.id;
                bar.dataset.operation = node.operation;
                bar.dataset.status = node.status;
                bar.dataset.duration = node.durationMs > 0 ? node.durationMs.toFixed(1) : '...';
                bar.dataset.dispatcher = node.dispatcher || 'Default';
                bar.dataset.startMs = node.startMs.toFixed(1);
                bar.innerHTML = '<span class="gantt-bar-text">' + node.operation + '</span>';
                barsRow.appendChild(bar);
                barsContainer.appendChild(barsRow);
                ganttRows.set(node.id, { labelRow, barsRow });
            });

            barsContainer.querySelectorAll('.gantt-bar').forEach(bar => {
                bar.addEventListener('mouseenter', showGanttTooltip);
                bar.addEventListener('mousemove', moveGanttTooltip);
                bar.addEventListener('mouseleave', hideGanttTooltip);
            });
        }

        function showGanttTooltip(e) {
            const bar = e.target;
            const tooltip = document.getElementById('gantt-tooltip');
            const statusLabel = { running: 'Running', completed: 'Completed', failed: 'Failed', cancelled: 'Cancelled' }[bar.dataset.status] || bar.dataset.status;
            tooltip.innerHTML = '<div class="gantt-tooltip-title"><span class="status-icon ' + bar.dataset.status + '" style="width:14px;height:14px;font-size:8px">' + getIcon(bar.dataset.status) + '</span>' + bar.dataset.operation + '</div><div class="gantt-tooltip-row"><span>Status</span><span>' + statusLabel + '</span></div><div class="gantt-tooltip-row"><span>Duration</span><span>' + bar.dataset.duration + 'ms</span></div><div class="gantt-tooltip-row"><span>Start</span><span>+' + bar.dataset.startMs + 'ms</span></div><div class="gantt-tooltip-row"><span>Dispatcher</span><span>' + bar.dataset.dispatcher + '</span></div>';
            tooltip.classList.add('visible');
            moveGanttTooltip(e);
        }

        function moveGanttTooltip(e) {
            const tooltip = document.getElementById('gantt-tooltip');
            const x = e.clientX + 12;
            const y = e.clientY + 12;
            const rect = tooltip.getBoundingClientRect();
            tooltip.style.left = Math.min(x, window.innerWidth - rect.width - 20) + 'px';
            tooltip.style.top = Math.min(y, window.innerHeight - rect.height - 20) + 'px';
        }

        function hideGanttTooltip() {
            document.getElementById('gantt-tooltip').classList.remove('visible');
        }

        function getTimeStep(duration) {
            const pixelsPerMs = ganttScale;
            const minStep = 60 / pixelsPerMs;
            if (minStep < 10) return 10;
            if (minStep < 25) return 25;
            if (minStep < 50) return 50;
            if (minStep < 100) return 100;
            if (minStep < 250) return 250;
            if (minStep < 500) return 500;
            if (minStep < 1000) return 1000;
            return 5000;
        }

        function ganttZoom(delta) {
            const factor = delta > 0 ? 1.2 : 0.8;
            ganttScale = Math.max(0.02, Math.min(50, ganttScale * factor));
            renderGantt();
        }

        function scheduleGanttUpdate() {
            if (!ganttUpdatePending && currentView === 'gantt') {
                ganttUpdatePending = true;
                requestAnimationFrame(() => { ganttUpdatePending = false; renderGantt(); });
            }
        }

        function createNodeElement(node) {
            const div = document.createElement('div');
            div.className = 'tree-node';
            div.dataset.id = node.id;
            div.innerHTML = '<div class="node-row ' + node.status + '"><span class="status-icon ' + node.status + '">' + getIcon(node.status) + '</span><span class="operation">' + node.operation + '</span><div class="meta"><span class="duration">' + (node.durationMs > 0 ? node.durationMs.toFixed(1) + 'ms' : '...') + '</span><span class="dispatcher">' + node.dispatcher + '</span></div></div><div class="children"></div>';
            return div;
        }

        function updateNodeElement(el, node) {
            const row = el.querySelector('.node-row');
            row.className = 'node-row ' + node.status;
            row.querySelector('.status-icon').className = 'status-icon ' + node.status;
            row.querySelector('.status-icon').textContent = getIcon(node.status);
            row.querySelector('.duration').textContent = node.durationMs > 0 ? node.durationMs.toFixed(1) + 'ms' : '...';
        }

        function addTimelineEvent(data) {
            const timeline = document.getElementById('timeline');
            const item = document.createElement('div');
            item.className = 'timeline-item';
            const type = data.type === 'started' ? 'started' : data.status;
            const detail = data.type === 'started' ? 'Started' : (data.durationMs > 0 ? data.durationMs.toFixed(1) + 'ms' : data.status);
            item.innerHTML = '<span class="timeline-time">' + formatTime(data.timestamp) + '</span><span class="timeline-dot ' + type + '"></span><div class="timeline-content"><div class="timeline-op">' + data.operation + '</div><div class="timeline-detail">' + detail + '</div></div>';
            timeline.insertBefore(item, timeline.firstChild);
            while (timeline.children.length > 50) timeline.removeChild(timeline.lastChild);
        }

        function handleEvent(event) {
            const data = JSON.parse(event.data);
            if (data.type === 'started') {
                const node = { id: data.id, parentId: data.parentId, operation: data.operation, status: 'running', durationMs: 0, dispatcher: data.dispatcher, startMs: getTimeMs(data.timestamp) };
                traces.set(data.id, node);
                stats.running++;
                const el = createNodeElement(node);
                domNodes.set(data.id, el);
                const tree = document.getElementById('tree');
                if (node.parentId && domNodes.has(node.parentId)) {
                    domNodes.get(node.parentId).querySelector('.children').appendChild(el);
                } else {
                    if (tree.querySelector('.empty-state')) tree.innerHTML = '';
                    tree.appendChild(el);
                }
                scheduleGanttUpdate();
            } else {
                const node = traces.get(data.id);
                if (node) {
                    if (node.status === 'running') stats.running--;
                    node.status = data.status;
                    node.durationMs = data.durationMs;
                    if (data.status === 'completed') stats.completed++;
                    else if (data.status === 'failed' || data.status === 'cancelled') stats.failed++;
                    const el = domNodes.get(data.id);
                    if (el) updateNodeElement(el, node);
                    scheduleGanttUpdate();
                }
            }
            updateStats();
            addTimelineEvent(data);
        }

        const eventSource = new EventSource('/events');
        eventSource.onmessage = handleEvent;
        eventSource.onerror = () => console.log('SSE reconnecting...');

        (function() {
            const savedView = localStorage.getItem('comet-view');
            if (savedView === 'gantt') setView('gantt');
        })();

        (function() {
            const labelsColumn = document.querySelector('.gantt-labels-column');
            const timelineColumn = document.getElementById('gantt-timeline-column');
            timelineColumn.addEventListener('scroll', () => { labelsColumn.scrollTop = timelineColumn.scrollTop; });
            labelsColumn.addEventListener('scroll', () => { timelineColumn.scrollTop = labelsColumn.scrollTop; });
        })();

        (function() {
            const gantt = document.getElementById('gantt');
            gantt.addEventListener('wheel', (e) => {
                if (e.ctrlKey || e.metaKey) { e.preventDefault(); ganttZoom(e.deltaY > 0 ? -1 : 1); }
            }, { passive: false });
        })();

        function toggleTheme() {
            const body = document.body;
            const icon = document.getElementById('theme-icon');
            const label = document.getElementById('theme-label');
            if (body.dataset.theme === 'light') {
                body.removeAttribute('data-theme');
                icon.textContent = '☀️';
                label.textContent = 'Light';
                localStorage.setItem('comet-theme', 'dark');
            } else {
                body.dataset.theme = 'light';
                icon.textContent = '🌙';
                label.textContent = 'Dark';
                localStorage.setItem('comet-theme', 'light');
            }
        }

        (function() {
            const saved = localStorage.getItem('comet-theme');
            if (saved === 'light') {
                document.body.dataset.theme = 'light';
                document.getElementById('theme-icon').textContent = '🌙';
                document.getElementById('theme-label').textContent = 'Dark';
            }
        })();
    </script>
</body>
</html>
""".trimIndent()
