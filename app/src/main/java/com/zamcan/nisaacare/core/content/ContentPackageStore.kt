package com.zamcan.nisaacare.core.content

import com.zamcan.nisaacare.domain.content.ContentPackage
import com.zamcan.nisaacare.domain.content.ContentUpdateResult
import com.zamcan.nisaacare.domain.model.ContentVersion

/**
 * Storage boundary for signed content packages. Implementations must validate
 * the manifest and package digest before applying records in one transaction;
 * user health tables are intentionally outside this boundary.
 */
interface ContentPackageStore {
    fun currentVersion(): ContentVersion
    fun validateAndStage(candidate: ContentPackage): ContentUpdateResult
    fun commitStaged(expectedVersion: Int): ContentUpdateResult
    fun discardStaged()
}

class UnconfiguredContentPackageStore(
    private val current: ContentVersion = ContentVersion()
) : ContentPackageStore {
    override fun currentVersion(): ContentVersion = current
    override fun validateAndStage(candidate: ContentPackage): ContentUpdateResult =
        ContentUpdateResult.Rejected("CONTENT_GATEWAY_NOT_CONFIGURED")
    override fun commitStaged(expectedVersion: Int): ContentUpdateResult =
        ContentUpdateResult.Rejected("CONTENT_GATEWAY_NOT_CONFIGURED")
    override fun discardStaged() = Unit
}
