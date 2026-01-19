package io.pandu.comet.visualizer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TraceNodeTest {

    // =====================================================================
    // Construction Tests
    // =====================================================================

    @Test
    fun `constructor sets all required fields`() {
        val node = TraceNode(
            id = "span-123",
            parentId = "parent-456",
            operation = "fetchUser",
            status = TraceStatus.RUNNING,
            durationMs = 0.0,
            dispatcher = "Default",
            startMs = 100.0
        )

        assertEquals("span-123", node.id)
        assertEquals("parent-456", node.parentId)
        assertEquals("fetchUser", node.operation)
        assertEquals(TraceStatus.RUNNING, node.status)
        assertEquals(0.0, node.durationMs)
        assertEquals("Default", node.dispatcher)
        assertEquals(100.0, node.startMs)
    }

    @Test
    fun `constructor uses default values for optional fields`() {
        val node = TraceNode(
            id = "span-123",
            parentId = null,
            operation = "fetchUser",
            status = TraceStatus.RUNNING,
            durationMs = 0.0,
            dispatcher = "Default",
            startMs = 100.0
        )

        assertEquals("", node.sourceFile)
        assertEquals(0, node.lineNumber)
        assertFalse(node.isUnstructured)
        assertTrue(node.childIds.isEmpty())
    }

    @Test
    fun `constructor allows null parentId for root nodes`() {
        val node = TraceNode(
            id = "root-span",
            parentId = null,
            operation = "main",
            status = TraceStatus.RUNNING,
            durationMs = 0.0,
            dispatcher = "Default",
            startMs = 0.0
        )

        assertNull(node.parentId)
    }

    @Test
    fun `constructor sets all optional fields`() {
        val node = TraceNode(
            id = "span-123",
            parentId = "parent-456",
            operation = "fetchUser",
            status = TraceStatus.COMPLETED,
            durationMs = 150.5,
            dispatcher = "IO",
            startMs = 100.0,
            sourceFile = "UserService.kt",
            lineNumber = 42,
            isUnstructured = true,
            childIds = listOf("child-1", "child-2")
        )

        assertEquals("UserService.kt", node.sourceFile)
        assertEquals(42, node.lineNumber)
        assertTrue(node.isUnstructured)
        assertEquals(listOf("child-1", "child-2"), node.childIds)
    }

    // =====================================================================
    // Status Tests
    // =====================================================================

    @Test
    fun `node can have RUNNING status`() {
        val node = createNode(status = TraceStatus.RUNNING)
        assertEquals(TraceStatus.RUNNING, node.status)
    }

    @Test
    fun `node can have COMPLETED status`() {
        val node = createNode(status = TraceStatus.COMPLETED)
        assertEquals(TraceStatus.COMPLETED, node.status)
    }

    @Test
    fun `node can have FAILED status`() {
        val node = createNode(status = TraceStatus.FAILED)
        assertEquals(TraceStatus.FAILED, node.status)
    }

    @Test
    fun `node can have CANCELLED status`() {
        val node = createNode(status = TraceStatus.CANCELLED)
        assertEquals(TraceStatus.CANCELLED, node.status)
    }

    // =====================================================================
    // ChildIds Tests
    // =====================================================================

    @Test
    fun `childIds defaults to empty list`() {
        val node = createNode()
        assertTrue(node.childIds.isEmpty())
    }

    @Test
    fun `childIds can contain single child`() {
        val node = createNode(childIds = listOf("child-1"))
        assertEquals(1, node.childIds.size)
        assertEquals("child-1", node.childIds[0])
    }

    @Test
    fun `childIds can contain multiple children`() {
        val node = createNode(childIds = listOf("child-1", "child-2", "child-3"))
        assertEquals(3, node.childIds.size)
        assertEquals(listOf("child-1", "child-2", "child-3"), node.childIds)
    }

    @Test
    fun `childIds preserves order`() {
        val childIds = listOf("c", "a", "b")
        val node = createNode(childIds = childIds)
        assertEquals(listOf("c", "a", "b"), node.childIds)
    }

    // =====================================================================
    // Data Class Behavior Tests
    // =====================================================================

    @Test
    fun `equals compares all fields`() {
        val node1 = TraceNode(
            id = "span-123",
            parentId = "parent-456",
            operation = "fetchUser",
            status = TraceStatus.RUNNING,
            durationMs = 0.0,
            dispatcher = "Default",
            startMs = 100.0,
            sourceFile = "Test.kt",
            lineNumber = 10,
            isUnstructured = false,
            childIds = listOf("child-1")
        )

        val node2 = TraceNode(
            id = "span-123",
            parentId = "parent-456",
            operation = "fetchUser",
            status = TraceStatus.RUNNING,
            durationMs = 0.0,
            dispatcher = "Default",
            startMs = 100.0,
            sourceFile = "Test.kt",
            lineNumber = 10,
            isUnstructured = false,
            childIds = listOf("child-1")
        )

        assertEquals(node1, node2)
    }

    @Test
    fun `copy creates new instance with modified status`() {
        val original = createNode(status = TraceStatus.RUNNING)
        val modified = original.copy(status = TraceStatus.COMPLETED)

        assertEquals(TraceStatus.COMPLETED, modified.status)
        assertEquals(original.id, modified.id)
        assertEquals(original.operation, modified.operation)
    }

    @Test
    fun `copy creates new instance with modified durationMs`() {
        val original = createNode(durationMs = 0.0)
        val modified = original.copy(durationMs = 150.5)

        assertEquals(150.5, modified.durationMs)
        assertEquals(original.id, modified.id)
    }

    @Test
    fun `copy creates new instance with modified childIds`() {
        val original = createNode(childIds = listOf("child-1"))
        val modified = original.copy(childIds = original.childIds + "child-2")

        assertEquals(listOf("child-1", "child-2"), modified.childIds)
        assertEquals(listOf("child-1"), original.childIds) // Original unchanged
    }

    @Test
    fun `hashCode is consistent`() {
        val node = createNode()
        assertEquals(node.hashCode(), node.hashCode())
    }

    // =====================================================================
    // Helper Functions
    // =====================================================================

    private fun createNode(
        id: String = "span-123",
        parentId: String? = null,
        operation: String = "testOperation",
        status: TraceStatus = TraceStatus.RUNNING,
        durationMs: Double = 0.0,
        dispatcher: String = "Default",
        startMs: Double = 0.0,
        sourceFile: String = "",
        lineNumber: Int = 0,
        isUnstructured: Boolean = false,
        childIds: List<String> = emptyList()
    ) = TraceNode(
        id = id,
        parentId = parentId,
        operation = operation,
        status = status,
        durationMs = durationMs,
        dispatcher = dispatcher,
        startMs = startMs,
        sourceFile = sourceFile,
        lineNumber = lineNumber,
        isUnstructured = isUnstructured,
        childIds = childIds
    )
}
