package com.github.ttt374.healthcaretracer.data


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val DATASTORE_NAME = "config_prefs"

val Context.configDataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(name = DATASTORE_NAME)

@Singleton
class ConfigRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.configDataStore

    // 設定キーの定義
    private object Keys {
        val SELECTED_GUIDELINE = stringPreferencesKey("selected_guideline")
        val MORNING_THRESHOLD = intPreferencesKey("morning_threshold")
        val EVENING_THRESHOLD = intPreferencesKey("evening_threshold")
    }

    // 選択された血圧ガイドライン

//    val selectedGuideline: Flow<BloodPressureGuideline> = dataStore.data
//        .map { preferences ->
//            val name = preferences[Keys.SELECTED_GUIDELINE] ?: BloodPressureGuideline.DEFAULT.name
//            BloodPressureGuideline.fromName(name)
//        }

    // 朝の時間閾値
    val morningThreshold: Flow<Int> = dataStore.data
        .map { it[Keys.MORNING_THRESHOLD] ?: 8 }

    // 夜の時間閾値
    val eveningThreshold: Flow<Int> = dataStore.data
        .map { it[Keys.EVENING_THRESHOLD] ?: 19 }

//    // ガイドラインの保存
//    suspend fun saveSelectedGuideline(guideline: BloodPressureGuideline) {
//        dataStore.edit { it[Keys.SELECTED_GUIDELINE] = guideline.name }
//    }

    // 朝の閾値の保存
    suspend fun saveMorningThreshold(hour: Int) {
        dataStore.edit { it[Keys.MORNING_THRESHOLD] = hour }
    }

    // 夜の閾値の保存
    suspend fun saveEveningThreshold(hour: Int) {
        dataStore.edit { it[Keys.EVENING_THRESHOLD] = hour }
    }
}
