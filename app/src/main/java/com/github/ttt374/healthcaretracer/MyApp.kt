package com.github.ttt374.healthcaretracer

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale


@HiltAndroidApp
class MyApp : Application(){
    override fun onCreate() {
        super.onCreate()
        val locale = Locale.forLanguageTag("ja")
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
    }
}