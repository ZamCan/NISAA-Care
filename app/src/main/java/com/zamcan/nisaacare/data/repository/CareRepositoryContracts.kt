package com.zamcan.nisaacare.data.repository

import com.zamcan.nisaacare.domain.model.AppPreferences
import com.zamcan.nisaacare.domain.model.ContentVersion
import com.zamcan.nisaacare.domain.model.CycleRecord
import com.zamcan.nisaacare.domain.model.FertilityEstimate
import com.zamcan.nisaacare.domain.model.PairingInvitation
import com.zamcan.nisaacare.domain.model.Relationship
import com.zamcan.nisaacare.domain.model.SupportRequest
import com.zamcan.nisaacare.domain.model.UserProfile

interface ProfileRepository {
    fun getLocalProfile(): UserProfile?
    fun saveLocalProfile(profile: UserProfile)
}

interface CycleRepository {
    fun getCycles(userId: String): List<CycleRecord>
    fun saveCycle(record: CycleRecord)
    fun getLatestEstimate(userId: String): FertilityEstimate?
    fun saveEstimate(estimate: FertilityEstimate)
}

interface RelationshipRepository {
    fun getRelationships(userId: String): List<Relationship>
    fun saveRelationship(relationship: Relationship)
    fun saveInvitation(invitation: PairingInvitation)
    fun findInvitation(token: String): PairingInvitation?
    fun addSupportRequest(request: SupportRequest)
}

interface PreferencesRepository {
    fun getPreferences(userId: String): AppPreferences
    fun savePreferences(preferences: AppPreferences)
}

interface ContentStateRepository {
    fun getInstalledVersion(): ContentVersion
    fun saveInstalledVersion(version: ContentVersion)
}
