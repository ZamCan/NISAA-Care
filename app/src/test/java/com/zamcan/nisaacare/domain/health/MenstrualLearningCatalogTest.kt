package com.zamcan.nisaacare.domain.health

import com.zamcan.nisaacare.domain.model.AppLanguage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MenstrualLearningCatalogTest {
    @Test
    fun everyLessonHasAllThreeLanguagesAndSources() {
        MenstrualLearningCatalog.lessons.forEach { lesson ->
            assertFalse(lesson.titleFor(AppLanguage.ENGLISH).isBlank())
            assertFalse(lesson.titleFor(AppLanguage.SWAHILI).isBlank())
            assertFalse(lesson.titleFor(AppLanguage.ARABIC).isBlank())
            assertFalse(lesson.bodyFor(AppLanguage.ENGLISH).isBlank())
            assertFalse(lesson.bodyFor(AppLanguage.SWAHILI).isBlank())
            assertFalse(lesson.bodyFor(AppLanguage.ARABIC).isBlank())
            assertTrue(lesson.sourceIds.isNotEmpty())
        }
    }
}
