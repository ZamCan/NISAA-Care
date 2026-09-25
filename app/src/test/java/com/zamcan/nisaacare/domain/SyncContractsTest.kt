package com.zamcan.nisaacare.domain

import com.zamcan.nisaacare.core.sync.LocalUnconfiguredRelationshipGateway
import com.zamcan.nisaacare.core.sync.RelationshipSettingsSnapshot
import com.zamcan.nisaacare.core.sync.RelationshipSettingsSyncService
import com.zamcan.nisaacare.core.sync.SyncResult
import com.zamcan.nisaacare.domain.model.PermissionKey
import com.zamcan.nisaacare.domain.model.Relationship
import com.zamcan.nisaacare.domain.model.RelationshipPermissions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SyncContractsTest {
    private val service = RelationshipSettingsSyncService(LocalUnconfiguredRelationshipGateway())

    @Test
    fun newerRemoteVersionWinsConflictReview() {
        val local = snapshot(version = 3, updated = Instant.parse("2025-01-01T00:00:00Z"), fertility = false)
        val remote = snapshot(version = 4, updated = Instant.parse("2025-01-01T00:00:00Z"), fertility = true)
        val result = service.resolveConflict(local, remote)
        assertTrue(result is SyncResult.Conflict<*>)
        val conflict = result as SyncResult.Conflict<RelationshipSettingsSnapshot>
        assertEquals(remote, conflict.remote)
    }

    @Test
    fun localSettingsRemainSeparateFromHealthRecords() {
        val relationship = Relationship(
            id = "relationship",
            husbandUserId = "h",
            wifeUserId = "w",
            permissions = RelationshipPermissions("relationship", mapOf(PermissionKey.FERTILITY_ESTIMATE to true))
        )
        val snapshot = service.snapshotFrom(relationship, "device", com.zamcan.nisaacare.domain.model.AppLanguage.SWAHILI)
        assertEquals(setOf(PermissionKey.FERTILITY_ESTIMATE), snapshot.permissions.filterValues { it }.keys)
        assertTrue(snapshot.permissions[PermissionKey.CYCLE_STATUS] != true)
    }

    private fun snapshot(version: Long, updated: Instant, fertility: Boolean) = RelationshipSettingsSnapshot(
        relationshipId = "relationship",
        permissions = mapOf(PermissionKey.FERTILITY_ESTIMATE to fertility),
        version = version,
        updatedAt = updated,
        deviceId = "device"
    )
}
