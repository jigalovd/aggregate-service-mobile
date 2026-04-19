package com.aggregateservice.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for RoleGuard and RequiredRole.
 */
class RoleGuardTest {

    @Test
    fun `RequiredRole Client has correct string value`() {
        assertEquals("client", RequiredRole.Client.stringValue)
    }

    @Test
    fun `RequiredRole Provider has correct string value`() {
        assertEquals("provider", RequiredRole.Provider.stringValue)
    }

    @Test
    fun `fromString returns Client for client string`() {
        assertEquals(RequiredRole.Client, RequiredRole.fromString("client"))
    }

    @Test
    fun `fromString returns Provider for provider string`() {
        assertEquals(RequiredRole.Provider, RequiredRole.fromString("provider"))
    }

    @Test
    fun `fromString returns null for unknown string`() {
        assertNull(RequiredRole.fromString("admin"))
        assertNull(RequiredRole.fromString(""))
        assertNull(RequiredRole.fromString(null))
    }

    @Test
    fun `canSwitch returns true when role is in available roles`() {
        val availableRoles = listOf("client", "provider")
        assertTrue(RequiredRole.canSwitch("client", availableRoles))
        assertTrue(RequiredRole.canSwitch("provider", availableRoles))
    }

    @Test
    fun `canSwitch returns false when role is not in available roles`() {
        val availableRoles = listOf("client")
        assertFalse(RequiredRole.canSwitch("provider", availableRoles))
        assertFalse(RequiredRole.canSwitch("admin", availableRoles))
    }

    @Test
    fun `isProvider returns true for provider string`() {
        assertTrue("provider".isProvider())
        assertTrue(RequiredRole.Provider.stringValue.isProvider())
    }

    @Test
    fun `isProvider returns false for non-provider string`() {
        assertFalse("client".isProvider())
        assertFalse("admin".isProvider())
        assertFalse("".isProvider())
    }

    @Test
    fun `isClient returns true for client string`() {
        assertTrue("client".isClient())
        assertTrue(RequiredRole.Client.stringValue.isClient())
    }

    @Test
    fun `isClient returns false for non-client string`() {
        assertFalse("provider".isClient())
        assertFalse("admin".isClient())
        assertFalse("".isClient())
    }
}
