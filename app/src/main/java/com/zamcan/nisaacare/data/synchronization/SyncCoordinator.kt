package com.zamcan.nisaacare.data.synchronization

import com.zamcan.nisaacare.core.sync.RelationshipSettingsGateway
import com.zamcan.nisaacare.core.sync.SyncResult
import com.zamcan.nisaacare.domain.model.Relationship
import java.time.Instant
import java.util.concurrent.Executor

/**
 * Coordinates relationship-setting sync only. A production implementation can
 * replace the gateway without allowing health records into the sync payload.
 */
class RelationshipSyncCoordinator(
    private val gateway: RelationshipSettingsGateway,
    private val executor: Executor
) {
    fun enqueue(
        relationship: Relationship,
        onComplete: (SyncResult<com.zamcan.nisaacare.core.sync.RelationshipSettingsSnapshot>) -> Unit
    ) {
        executor.execute {
            val result = gateway.push(
                com.zamcan.nisaacare.core.sync.RelationshipSettingsSnapshot(
                    relationshipId = relationship.id,
                    permissions = relationship.permissions.permissions,
                    version = relationship.permissions.version,
                    updatedAt = relationship.permissions.updatedAt,
                    deviceId = "local-device"
                )
            )
            onComplete(result)
        }
    }

    fun pullRelationshipSettings(
        relationshipId: String,
        since: Instant?,
        onComplete: (SyncResult<List<com.zamcan.nisaacare.core.sync.RelationshipSettingsSnapshot>>) -> Unit
    ) {
        executor.execute { onComplete(gateway.pullSince(relationshipId, since)) }
    }
}
