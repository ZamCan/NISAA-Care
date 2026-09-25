package com.zamcan.nisaacare.core.localization

import android.content.Context
import android.content.res.Configuration
import com.zamcan.nisaacare.domain.model.AppLanguage
import java.util.Locale

object LocaleManager {
    private const val PREFERENCES = "nisaa-care-preferences"
    private const val KEY_LANGUAGE = "app-language"

    fun wrap(base: Context): Context {
        val language = savedLanguage(base)
        val configuration = Configuration(base.resources.configuration).apply {
            setLocale(language.toLocale())
            setLayoutDirection(language.toLocale())
        }
        return base.createConfigurationContext(configuration)
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.code)
            .apply()
    }

    fun savedLanguage(context: Context): AppLanguage = AppLanguage.fromCode(
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, AppLanguage.SWAHILI.code)
    )

    private fun AppLanguage.toLocale(): Locale = when (this) {
        AppLanguage.SWAHILI -> Locale("sw")
        AppLanguage.ENGLISH -> Locale.ENGLISH
        AppLanguage.ARABIC -> Locale("ar")
    }
}
