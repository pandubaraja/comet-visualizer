package io.pandu.comet.visualizer.data

import io.pandu.comet.visualizer.TraceEvent

data class TimelineEvent(
    val event: TraceEvent,
    val timeOffset: String
)