package com.zamcan.nisaacare

import android.app.Application
import com.zamcan.nisaacare.core.notifications.LocalNotificationScheduler
import com.zamcan.nisaacare.core.notifications.NotificationScheduler
import com.zamcan.nisaacare.data.content.SeedContent
import com.zamcan.nisaacare.data.local.LocalCareRepository
import com.zamcan.nisaacare.domain.content.ContentEngine
import com.zamcan.nisaacare.domain.cycle.BiologicalEngine
import com.zamcan.nisaacare.domain.relationship.RelationshipPolicy
import com.zamcan.nisaacare.domain.worship.WorshipContextEngine

class NisaaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    val careRepository = LocalCareRepository(application)
    val biologicalEngine = BiologicalEngine()
    val contentEngine = ContentEngine()
    val relationshipPolicy = RelationshipPolicy()
    val worshipContextEngine = WorshipContextEngine()
    val notificationScheduler: NotificationScheduler = LocalNotificationScheduler(application)
    val seedContent = SeedContent
}
