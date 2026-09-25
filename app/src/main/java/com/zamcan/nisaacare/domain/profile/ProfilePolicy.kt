package com.zamcan.nisaacare.domain.profile

import com.zamcan.nisaacare.domain.model.AppLanguage
import com.zamcan.nisaacare.domain.model.MaritalStatus
import com.zamcan.nisaacare.domain.model.UserProfile
import com.zamcan.nisaacare.domain.model.UserRole

object ProfilePolicy {
    fun sanitizeAverageCycleLength(value: Int?): Int? = value?.takeIf { it in 21..45 }

    fun withExplicitRole(profile: UserProfile, role: UserRole): UserProfile =
        profile.copy(role = role)

    fun withMaritalStatus(profile: UserProfile, status: MaritalStatus): UserProfile =
        profile.copy(maritalStatus = status)

    fun withLanguage(profile: UserProfile, language: AppLanguage): UserProfile =
        profile.copy(language = language)
}
