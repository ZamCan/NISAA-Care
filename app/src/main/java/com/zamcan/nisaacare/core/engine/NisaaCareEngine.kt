package com.zamcan.nisaacare.core.engine

import android.app.Application
import com.zamcan.nisaacare.core.notifications.LocalNotificationScheduler
import com.zamcan.nisaacare.core.notifications.NotificationScheduler
import com.zamcan.nisaacare.data.content.SeedContent
import com.zamcan.nisaacare.data.local.LocalCareRepository
import com.zamcan.nisaacare.domain.content.ContentEngine
import com.zamcan.nisaacare.domain.cycle.BiologicalEngine
import com.zamcan.nisaacare.domain.relationship.RelationshipPolicy
import com.zamcan.nisaacare.domain.worship.WorshipContextEngine

/**
 * The single application-engine composition root.
 *
 * Feature code should depend on these shared, app-scoped services instead of
 * constructing parallel repositories or rule engines inside screens. The
 * implementation remains in focused packages so each engine can be tested
 * independently while a clone/build produces one complete Android app.
 */
class NisaaCareEngine(application: Application) {
    /** Local SQLite-backed source of truth for personal care records. */
    val careRepository = LocalCareRepository(application)

    /** Deterministic cycle estimates with explicit uncertainty. */
    val biologicalEngine = BiologicalEngine()

    /** Reviewed content selection and deterministic quotation rotation. */
    val contentEngine = ContentEngine()

    /** Relationship, pairing, and record-sharing policy rules. */
    val relationshipPolicy = RelationshipPolicy()

    /** Keeps worship-context education separate from biological predictions. */
    val worshipContextEngine = WorshipContextEngine()

    /** Device-local notifications; no remote service is required. */
    val notificationScheduler: NotificationScheduler =
        LocalNotificationScheduler(application)

    /** Bundled, deterministic initial content for offline use. */
    val seedContent = SeedContent
}
