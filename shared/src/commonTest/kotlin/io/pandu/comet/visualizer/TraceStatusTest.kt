package io.pandu.comet.visualizer

import kotlin.test.Test
import kotlin.test.assertEquals

class TraceStatusTest {

    // =====================================================================
    // fromString Tests - Standard Cases
    // =====================================================================

    @Test
    fun `fromString parses running correctly`() {
        assertEquals(TraceStatus.RUNNING, TraceStatus.fromString("running"))
    }

    @Test
    fun `fromString parses completed correctly`() {
        assertEquals(TraceStatus.COMPLETED, TraceStatus.fromString("completed"))
    }

    @Test
    fun `fromString parses failed correctly`() {
        assertEquals(TraceStatus.FAILED, TraceStatus.fromString("failed"))
    }

    @Test
    fun `fromString parses cancelled correctly`() {
        assertEquals(TraceStatus.CANCELLED, TraceStatus.fromString("cancelled"))
    }

    // =====================================================================
    // fromString Tests - Case Insensitivity
    // =====================================================================

    @Test
    fun `fromString handles uppercase RUNNING`() {
        assertEquals(TraceStatus.RUNNING, TraceStatus.fromString("RUNNING"))
    }

    @Test
    fun `fromString handles uppercase COMPLETED`() {
        assertEquals(TraceStatus.COMPLETED, TraceStatus.fromString("COMPLETED"))
    }

    @Test
    fun `fromString handles uppercase FAILED`() {
        assertEquals(TraceStatus.FAILED, TraceStatus.fromString("FAILED"))
    }

    @Test
    fun `fromString handles uppercase CANCELLED`() {
        assertEquals(TraceStatus.CANCELLED, TraceStatus.fromString("CANCELLED"))
    }

    @Test
    fun `fromString handles mixed case Running`() {
        assertEquals(TraceStatus.RUNNING, TraceStatus.fromString("Running"))
    }

    @Test
    fun `fromString handles mixed case Completed`() {
        assertEquals(TraceStatus.COMPLETED, TraceStatus.fromString("Completed"))
    }

    @Test
    fun `fromString handles mixed case Failed`() {
        assertEquals(TraceStatus.FAILED, TraceStatus.fromString("Failed"))
    }

    @Test
    fun `fromString handles mixed case Cancelled`() {
        assertEquals(TraceStatus.CANCELLED, TraceStatus.fromString("Cancelled"))
    }

    // =====================================================================
    // fromString Tests - Default Fallback
    // =====================================================================

    @Test
    fun `fromString returns RUNNING for unknown status`() {
        assertEquals(TraceStatus.RUNNING, TraceStatus.fromString("unknown"))
    }

    @Test
    fun `fromString returns RUNNING for empty string`() {
        assertEquals(TraceStatus.RUNNING, TraceStatus.fromString(""))
    }

    @Test
    fun `fromString returns RUNNING for invalid status`() {
        assertEquals(TraceStatus.RUNNING, TraceStatus.fromString("invalid-status"))
    }

    @Test
    fun `fromString returns RUNNING for numeric string`() {
        assertEquals(TraceStatus.RUNNING, TraceStatus.fromString("123"))
    }

    // =====================================================================
    // Enum Values Tests
    // =====================================================================

    @Test
    fun `enum contains all expected values`() {
        val values = TraceStatus.entries
        assertEquals(4, values.size)
        assertEquals(TraceStatus.RUNNING, values[0])
        assertEquals(TraceStatus.COMPLETED, values[1])
        assertEquals(TraceStatus.FAILED, values[2])
        assertEquals(TraceStatus.CANCELLED, values[3])
    }

    @Test
    fun `enum name matches expected string`() {
        assertEquals("RUNNING", TraceStatus.RUNNING.name)
        assertEquals("COMPLETED", TraceStatus.COMPLETED.name)
        assertEquals("FAILED", TraceStatus.FAILED.name)
        assertEquals("CANCELLED", TraceStatus.CANCELLED.name)
    }
}
