package io.pandu.comet.visualizer

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TraceEventTest {

    private val json = Json { ignoreUnknownKeys = true }

    // =====================================================================
    // Construction Tests
    // =====================================================================

    @Test
    fun `constructor sets all required fields`() {
        val event = TraceEvent(
            type = "started",
            id = "span-123",
            parentId = "parent-456",
            operation = "fetchUser",
            status = "running",
            timestamp = 1234567890L
        )

        assertEquals("started", event.type)
        assertEquals("span-123", event.id)
        assertEquals("parent-456", event.parentId)
        assertEquals("fetchUser", event.operation)
        assertEquals("running", event.status)
        assertEquals(1234567890L, event.timestamp)
    }

    @Test
    fun `constructor uses default values for optional fields`() {
        val event = TraceEvent(
            type = "started",
            id = "span-123",
            parentId = null,
            operation = "fetchUser",
            status = "running",
            timestamp = 1234567890L
        )

        assertEquals(0.0, event.durationMs)
        assertEquals("", event.dispatcher)
        assertEquals("", event.sourceFile)
        assertEquals(0, event.lineNumber)
        assertFalse(event.isUnstructured)
    }

    @Test
    fun `constructor allows null parentId`() {
        val event = TraceEvent(
            type = "started",
            id = "root-span",
            parentId = null,
            operation = "main",
            status = "running",
            timestamp = 1234567890L
        )

        assertNull(event.parentId)
    }

    @Test
    fun `constructor sets all optional fields`() {
        val event = TraceEvent(
            type = "completed",
            id = "span-123",
            parentId = "parent-456",
            operation = "fetchUser",
            status = "completed",
            durationMs = 150.5,
            dispatcher = "IO",
            timestamp = 1234567890L,
            sourceFile = "UserService.kt",
            lineNumber = 42,
            isUnstructured = true
        )

        assertEquals(150.5, event.durationMs)
        assertEquals("IO", event.dispatcher)
        assertEquals("UserService.kt", event.sourceFile)
        assertEquals(42, event.lineNumber)
        assertTrue(event.isUnstructured)
    }

    // =====================================================================
    // Serialization Tests
    // =====================================================================

    @Test
    fun `serializes to JSON correctly`() {
        val event = TraceEvent(
            type = "started",
            id = "span-123",
            parentId = "parent-456",
            operation = "fetchUser",
            status = "running",
            durationMs = 100.0,
            dispatcher = "Default",
            timestamp = 1234567890L,
            sourceFile = "Test.kt",
            lineNumber = 10,
            isUnstructured = false
        )

        val jsonString = json.encodeToString(event)

        assertTrue(jsonString.contains("\"type\":\"started\""))
        assertTrue(jsonString.contains("\"id\":\"span-123\""))
        assertTrue(jsonString.contains("\"parentId\":\"parent-456\""))
        assertTrue(jsonString.contains("\"operation\":\"fetchUser\""))
        assertTrue(jsonString.contains("\"status\":\"running\""))
    }

    @Test
    fun `deserializes from JSON correctly`() {
        val jsonString = """
            {
                "type": "completed",
                "id": "span-123",
                "parentId": "parent-456",
                "operation": "fetchUser",
                "status": "completed",
                "durationMs": 150.5,
                "dispatcher": "IO",
                "timestamp": 1234567890,
                "sourceFile": "UserService.kt",
                "lineNumber": 42,
                "isUnstructured": true
            }
        """.trimIndent()

        val event = json.decodeFromString<TraceEvent>(jsonString)

        assertEquals("completed", event.type)
        assertEquals("span-123", event.id)
        assertEquals("parent-456", event.parentId)
        assertEquals("fetchUser", event.operation)
        assertEquals("completed", event.status)
        assertEquals(150.5, event.durationMs)
        assertEquals("IO", event.dispatcher)
        assertEquals(1234567890L, event.timestamp)
        assertEquals("UserService.kt", event.sourceFile)
        assertEquals(42, event.lineNumber)
        assertTrue(event.isUnstructured)
    }

    @Test
    fun `deserializes with null parentId`() {
        val jsonString = """
            {
                "type": "started",
                "id": "root-span",
                "parentId": null,
                "operation": "main",
                "status": "running",
                "timestamp": 1234567890
            }
        """.trimIndent()

        val event = json.decodeFromString<TraceEvent>(jsonString)

        assertNull(event.parentId)
    }

    @Test
    fun `deserializes with missing optional fields`() {
        val jsonString = """
            {
                "type": "started",
                "id": "span-123",
                "parentId": null,
                "operation": "fetchUser",
                "status": "running",
                "timestamp": 1234567890
            }
        """.trimIndent()

        val event = json.decodeFromString<TraceEvent>(jsonString)

        assertEquals(0.0, event.durationMs)
        assertEquals("", event.dispatcher)
        assertEquals("", event.sourceFile)
        assertEquals(0, event.lineNumber)
        assertFalse(event.isUnstructured)
    }

    @Test
    fun `serialization roundtrip preserves all fields`() {
        val original = TraceEvent(
            type = "completed",
            id = "span-123",
            parentId = "parent-456",
            operation = "fetchUser",
            status = "completed",
            durationMs = 150.5,
            dispatcher = "IO",
            timestamp = 1234567890L,
            sourceFile = "UserService.kt",
            lineNumber = 42,
            isUnstructured = true
        )

        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<TraceEvent>(serialized)

        assertEquals(original, deserialized)
    }

    // =====================================================================
    // Data Class Behavior Tests
    // =====================================================================

    @Test
    fun `equals compares all fields`() {
        val event1 = TraceEvent(
            type = "started",
            id = "span-123",
            parentId = null,
            operation = "fetchUser",
            status = "running",
            timestamp = 1234567890L
        )

        val event2 = TraceEvent(
            type = "started",
            id = "span-123",
            parentId = null,
            operation = "fetchUser",
            status = "running",
            timestamp = 1234567890L
        )

        assertEquals(event1, event2)
    }

    @Test
    fun `copy creates new instance with modified fields`() {
        val original = TraceEvent(
            type = "started",
            id = "span-123",
            parentId = null,
            operation = "fetchUser",
            status = "running",
            timestamp = 1234567890L
        )

        val modified = original.copy(status = "completed", durationMs = 150.0)

        assertEquals("completed", modified.status)
        assertEquals(150.0, modified.durationMs)
        assertEquals(original.id, modified.id)
        assertEquals(original.operation, modified.operation)
    }

    @Test
    fun `hashCode is consistent`() {
        val event = TraceEvent(
            type = "started",
            id = "span-123",
            parentId = null,
            operation = "fetchUser",
            status = "running",
            timestamp = 1234567890L
        )

        assertEquals(event.hashCode(), event.hashCode())
    }
}
