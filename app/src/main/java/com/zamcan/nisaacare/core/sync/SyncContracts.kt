package com.zamcan.nisaacare.core.sync

import com.zamcan.nisaacare.domain.content.ContentManifest
import com.zamcan.nisaacare.domain.content.ContentPackage
import com.zamcan.nisaacare.domain.model.AppLanguage
import com.zamcan.nisaacare.domain.model.ContentVersion
import com.zamcan.nisaacare.domain.model.PermissionKey
import com.zamcan.nisaacare.domain.model.Relationship
import java.time.Instant

data class RelationshipSettingsSnapshot(
    val relationshipId: String,
    val permissions: Map<PermissionKey, Boolean>,
    val version: Long,
    val updatedAt: Instant,
    val deviceId: String
)

data class RelationshipSettingsPatch(
    val relationshipId: String,
    val permission: PermissionKey,
    val granted: Boolean,
    val version: Long,
    val updatedAt: Instant,
    val deviceId: String
)

data class PairingRequest(
    val token: String,
    val inviterUserId: String,
    val inviterRole: String,
    val expiresAt: Instant
)

data class AuthSession(
    val userId: String,
    val accessToken: String,
    val expiresAt: Instant
)

sealed class SyncResult<out T> {
    data class Success<T>(val value: T) : SyncResult<T>()
    data object NotConfigured : SyncResult<Nothing>()
    data class Offline<T>(val pending: T? = null) : SyncResult<Nothing>()
    data class Conflict<T>(val local: T, val remote: T) : SyncResult<Nothing>()
    data class Failure(val message: String) : SyncResult<Nothing>()
}

interface RelationshipSettingsGateway {
    fun push(snapshot: RelationshipSettingsSnapshot): SyncResult<RelationshipSettingsSnapshot>
    fun pullSince(relationshipId: String, since: Instant?): SyncResult<List<RelationshipSettingsSnapshot>>
}

interface PairingGateway {
    fun publishInvitation(request: PairingRequest): SyncResult<Unit>
    fun redeemInvitation(token: String): SyncResult<PairingRequest>
}

interface ContentUpdateGateway {
    fun latestManifest(): SyncResult<ContentManifest>
    fun download(manifest: ContentManifest): SyncResult<ContentPackage>
}

interface OptionalEncryptedBackupGateway {
    fun upload(encryptedBlob: ByteArray): SyncResult<Unit>
    fun download(): SyncResult<ByteArray>
}

/** Explicitly unavailable until a real backend is configured. */
class LocalUnconfiguredRelationshipGateway : RelationshipSettingsGateway {
    override fun push(snapshot: RelationshipSettingsSnapshot): SyncResult<RelationshipSettingsSnapshot> =
        SyncResult.NotConfigured

    override fun pullSince(relationshipId: String, since: Instant?): SyncResult<List<RelationshipSettingsSnapshot>> =
        SyncResult.NotConfigured
}

class LocalUnconfiguredContentGateway : ContentUpdateGateway {
    override fun latestManifest(): SyncResult<ContentManifest> = SyncResult.NotConfigured
    override fun download(manifest: ContentManifest): SyncResult<ContentPackage> = SyncResult.NotConfigured
}

/**
 * Resolves only relationship-setting patches. Health records are deliberately
 * absent from this contract so a relationship sync cannot accidentally become
 * a full database sync.
 */
class RelationshipSettingsSyncService(
    private val gateway: RelationshipSettingsGateway
) {
    fun resolveConflict(
        local: RelationshipSettingsSnapshot,
        remote: RelationshipSettingsSnapshot
    ): SyncResult<RelationshipSettingsSnapshot> = when {
        remote.version > local.version -> SyncResult.Conflict(local, remote)
        local.version > remote.version -> SyncResult.Success(local)
        local.updatedAt.isAfter(remote.updatedAt) -> SyncResult.Success(local)
        remote.updatedAt.isAfter(local.updatedAt) -> SyncResult.Conflict(local, remote)
        local.permissions == remote.permissions -> SyncResult.Success(local)
        else -> SyncResult.Conflict(local, remote)
    }

    fun snapshotFrom(
        relationship: Relationship,
        deviceId: String,
        appLanguage: AppLanguage
    ): RelationshipSettingsSnapshot = RelationshipSettingsSnapshot(
        relationshipId = relationship.id,
        permissions = relationship.permissions.permissions,
        version = relationship.permissions.version,
        updatedAt = relationship.permissions.updatedAt,
        deviceId = deviceId
    )
}

data class ContentUpdateState(
    val installed: ContentVersion,
    val latestManifest: ContentManifest? = null,
    val pendingPackage: ContentPackage? = null
)
