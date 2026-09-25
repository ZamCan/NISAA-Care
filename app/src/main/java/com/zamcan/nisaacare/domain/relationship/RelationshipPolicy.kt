package com.zamcan.nisaacare.domain.relationship

import com.zamcan.nisaacare.domain.model.AppLanguage
import com.zamcan.nisaacare.domain.model.DomainError
import com.zamcan.nisaacare.domain.model.DomainResult
import com.zamcan.nisaacare.domain.model.PairingInvitation
import com.zamcan.nisaacare.domain.model.PermissionKey
import com.zamcan.nisaacare.domain.model.Relationship
import com.zamcan.nisaacare.domain.model.RelationshipStatus
import com.zamcan.nisaacare.domain.model.UserRole
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64

class RelationshipPolicy(private val random: SecureRandom = SecureRandom()) {

    fun validateNewRelationship(
        candidate: Relationship,
        existing: List<Relationship>,
        currentUserId: String
    ): DomainResult<Relationship> {
        if (candidate.husbandUserId == null && candidate.wifeUserId == null) {
            return failure("RELATIONSHIP_PARTICIPANTS_REQUIRED", "Both relationship participants are required")
        }
        if (candidate.husbandUserId == candidate.wifeUserId) {
            return failure("SELF_RELATIONSHIP", "A relationship needs two different people")
        }
        if (currentUserId !in setOfNotNull(candidate.husbandUserId, candidate.wifeUserId)) {
            return failure("RELATIONSHIP_ACCESS_DENIED", "The current user is not part of this relationship")
        }

        val liveExisting = existing.filter { it.status != RelationshipStatus.REVOKED && it.status != RelationshipStatus.EXPIRED }
        if (candidate.wifeUserId != null && liveExisting.any { it.wifeUserId == candidate.wifeUserId }) {
            return failure(
                "WIFE_SINGLE_RELATIONSHIP",
                "A wife profile can have only one active wife relationship on this device"
            )
        }
        if (liveExisting.any {
                it.husbandUserId == candidate.husbandUserId &&
                    it.wifeUserId == candidate.wifeUserId
            }) {
            return failure("DUPLICATE_RELATIONSHIP", "This relationship already exists")
        }
        return DomainResult.Success(candidate)
    }

    fun canRead(
        relationship: Relationship,
        viewerUserId: String,
        permission: PermissionKey,
        now: Instant = Instant.now()
    ): Boolean {
        if (relationship.status != RelationshipStatus.ACTIVE) return false
        if (viewerUserId !in setOfNotNull(relationship.husbandUserId, relationship.wifeUserId)) return false
        if (relationship.revokedAt != null && now.isAfter(relationship.revokedAt)) return false
        return relationship.permissions.permissions[permission] == true
    }

    fun revoke(relationship: Relationship, now: Instant = Instant.now()): Relationship = relationship.copy(
        status = RelationshipStatus.REVOKED,
        revokedAt = relationship.revokedAt ?: now,
        syncVersion = relationship.syncVersion + 1
    )

    fun grantPermission(
        relationship: Relationship,
        permission: PermissionKey,
        granted: Boolean,
        now: Instant = Instant.now()
    ): Relationship {
        val updated = relationship.permissions.permissions.toMutableMap().apply {
            put(permission, granted)
        }
        return relationship.copy(
            permissions = relationship.permissions.copy(
                permissions = updated,
                version = relationship.permissions.version + 1,
                updatedAt = now
            ),
            syncVersion = relationship.syncVersion + 1
        )
    }

    fun createInvitation(
        inviterUserId: String,
        inviterRole: UserRole,
        now: Instant = Instant.now(),
        lifetime: Duration = Duration.ofMinutes(15)
    ): PairingInvitation {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        return PairingInvitation(
            token = token,
            inviterUserId = inviterUserId,
            inviterRole = inviterRole,
            createdAt = now,
            expiresAt = now.plus(lifetime)
        )
    }

    fun acceptInvitation(
        invitation: PairingInvitation,
        accepterId: String,
        accepterRole: UserRole,
        relationship: Relationship,
        now: Instant = Instant.now()
    ): DomainResult<Relationship> {
        if (!invitation.isUsable(now)) {
            return failure("PAIRING_TOKEN_EXPIRED", "This pairing invitation is expired or already used")
        }
        if (invitation.inviterUserId == accepterId) {
            return failure("SELF_PAIRING", "A person cannot pair with their own invitation")
        }
        if (invitation.inviterRole == accepterRole) {
            return failure("ROLE_MISMATCH", "Pairing requires one woman and one husband profile")
        }
        val accepted = relationship.copy(
            husbandUserId = if (invitation.inviterRole == UserRole.HUSBAND) invitation.inviterUserId else accepterId,
            wifeUserId = if (invitation.inviterRole == UserRole.WOMAN) invitation.inviterUserId else accepterId,
            status = RelationshipStatus.ACTIVE,
            connectionToken = null,
            syncVersion = relationship.syncVersion + 1
        )
        return validateNewRelationship(accepted, emptyList(), accepterId).let { result ->
            when (result) {
                is DomainResult.Success -> DomainResult.Success(accepted)
                is DomainResult.Failure -> result
            }
        }
    }

    fun onlyForRelationship(
        relationships: List<Relationship>,
        relationshipId: String,
        viewerUserId: String
    ): List<Relationship> = relationships.filter {
        it.id == relationshipId && viewerUserId in setOfNotNull(it.husbandUserId, it.wifeUserId)
    }

    fun withPermissionDefaults(relationshipId: String): Map<PermissionKey, Boolean> =
        PermissionKey.entries.associateWith { false }

    private fun <T> failure(code: String, message: String): DomainResult<T> =
        DomainResult.Failure(DomainError(code, message))
}
