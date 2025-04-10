package com.github.ttt374.healthcaretracer

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.collectAsState
import androidx.core.os.LocaleListCompat
import com.github.ttt374.healthcaretracer.data.datastore.ConfigRepository
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


@HiltAndroidApp
class MyApp : Application(){
    @Inject
    lateinit var configRepository: ConfigRepository
    private val applicationScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate() {
        super.onCreate()
        //collectLocaleConfig()
//        val locale = Locale.forLanguageTag("ja")
//        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
    }
//    private fun collectLocaleConfig() {
//        // configRepository の StateFlow を収集し、ロケール設定を行う
//        applicationScope.launch {
//            configRepository.dataFlow
//                .collect { config ->
//                    val locale = Locale.forLanguageTag( config.localeTag)
//                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
//
//                    val localesChangedToDefault = AppCompatDelegate.getApplicationLocales()
//                    val currentLocale = LocaleListCompat.getDefault()
//                    println("Current Locale: $currentLocale")
//
//
//                }
//        }
//    }
}