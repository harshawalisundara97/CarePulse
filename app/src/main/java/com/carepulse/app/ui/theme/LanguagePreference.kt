package com.carepulse.app.ui.theme

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    SINHALA("si"),
    TAMIL("ta");

    companion object {
        fun fromTag(tag: String?): AppLanguage = when (tag?.lowercase()) {
            "si" -> SINHALA
            "ta" -> TAMIL
            else -> ENGLISH
        }
    }
}

object LanguagePreference {
    /** Reads the language currently applied via AppCompat (survives process death). */
    fun current(): AppLanguage {
        val locales = AppCompatDelegate.getApplicationLocales()
        return AppLanguage.fromTag(if (locales.isEmpty) null else locales[0]?.language)
    }

    /** Apply immediately; AppCompat persists the choice per-app. */
    fun set(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }
}
