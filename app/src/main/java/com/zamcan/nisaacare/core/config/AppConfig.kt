package com.zamcan.nisaacare.core.config

/** Build-time configuration with no credentials or private URLs committed. */
data class AppConfig(
    val applicationId: String = "com.zamcan.nisaacare",
    val privacyPolicyUrl: String = "",
    val contentUpdateBaseUrl: String = "",
    val syncBaseUrl: String = "",
    val contentPackageName: String = "bundled-content",
    val enableRemoteSync: Boolean = false
) {
    val remoteServicesConfigured: Boolean
        get() = enableRemoteSync && contentUpdateBaseUrl.isNotBlank() && syncBaseUrl.isNotBlank()
}
