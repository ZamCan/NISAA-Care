package com.zamcan.nisaacare.domain

import com.zamcan.nisaacare.domain.model.DomainResult
import com.zamcan.nisaacare.domain.model.PairingInvitation
import com.zamcan.nisaacare.domain.model.PermissionKey
import com.zamcan.nisaacare.domain.model.Relationship
import com.zamcan.nisaacare.domain.model.RelationshipPermissions
import com.zamcan.nisaacare.domain.model.RelationshipStatus
import com.zamcan.nisaacare.domain.model.UserRole
import com.zamcan.nisaacare.domain.relationship.RelationshipPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class RelationshipPolicyTest {
    private val policy = RelationshipPolicy()

    @Test
    fun husbandMayHaveIndependentWifeRelationships() {
        val husband = "h"
        val first = relationship("r1", husband, "w1")
        val second = relationship("r2", husband, "w2")
        val result = policy.validateNewRelationship(second, listOf(first), husband)
        assertTrue(result is DomainResult.Success)
    }

    @Test
    fun wifeCannotCreateSecondActiveRelationship() {
        val wife = "w1"
        val existing = relationship("r1", "h1", wife)
        val result = policy.validateNewRelationship(relationship("r2", "h2", wife), listOf(existing), wife)
        assertTrue(result is DomainResult.Failure)
        assertEquals("WIFE_SINGLE_RELATIONSHIP", (result as DomainResult.Failure).error.code)
    }

    @Test
    fun permissionGrantAndRevokeAreExplicit() {
        val relationship = relationship("r1", "h1", "w1").copy(status = RelationshipStatus.ACTIVE)
        assertFalse(policy.canRead(relationship, "h1", PermissionKey.FERTILITY_ESTIMATE))
        val granted = policy.grantPermission(relationship, PermissionKey.FERTILITY_ESTIMATE, true)
        assertTrue(policy.canRead(granted, "h1", PermissionKey.FERTILITY_ESTIMATE))
        val revokedRelationship = policy.revoke(granted)
        assertFalse(policy.canRead(revokedRelationship, "h1", PermissionKey.FERTILITY_ESTIMATE))
    }

    @Test
    fun relationshipIsolationRejectsOtherUsers() {
        val relationship = relationship("r1", "h1", "w1").copy(
            status = RelationshipStatus.ACTIVE,
            permissions = RelationshipPermissions("r1", mapOf(PermissionKey.CYCLE_STATUS to true))
        )
        assertFalse(policy.canRead(relationship, "h2", PermissionKey.CYCLE_STATUS))
        assertTrue(policy.canRead(relationship, "w1", PermissionKey.CYCLE_STATUS))
    }

    @Test
    fun expiredPairingTokenCannotBeAccepted() {
        val now = Instant.parse("2025-01-01T00:00:00Z")
        val invitation = PairingInvitation(
            token = "expired",
            inviterUserId = "w1",
            inviterRole = UserRole.WOMAN,
            createdAt = now.minus(Duration.ofMinutes(20)),
            expiresAt = now.minus(Duration.ofMinutes(5))
        )
        val result = policy.acceptInvitation(
            invitation,
            "h1",
            UserRole.HUSBAND,
            relationship("r1", "h1", "w1"),
            now
        )
        assertTrue(result is DomainResult.Failure)
        assertEquals("PAIRING_TOKEN_EXPIRED", (result as DomainResult.Failure).error.code)
    }

    private fun relationship(id: String, husband: String, wife: String) = Relationship(
        id = id,
        husbandUserId = husband,
        wifeUserId = wife,
        status = RelationshipStatus.ACTIVE,
        permissions = RelationshipPermissions(id)
    )
}
