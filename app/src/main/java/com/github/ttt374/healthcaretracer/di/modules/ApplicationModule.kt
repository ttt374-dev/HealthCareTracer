package com.github.ttt374.healthcaretracer.di.modules

import android.content.Context
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.data.repository.PreferencesRepository
import com.github.ttt374.healthcaretracer.data.item.ItemDao
import com.github.ttt374.healthcaretracer.data.item.ItemDatabase
import com.github.ttt374.healthcaretracer.data.metric.MetricCategory
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import com.github.ttt374.healthcaretracer.usecase.ExportDataUseCase
import com.github.ttt374.healthcaretracer.usecase.ImportDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Singleton
    @Provides
    fun provideItemRepository(itemDao: ItemDao) =
        ItemRepository(itemDao)

    @Singleton
    @Provides
    fun provideConfigRepository(@ApplicationContext context: Context) = ConfigRepository(context)

    @Singleton
    @Provides
    fun providePreferencesRepository(@ApplicationContext context: Context) = PreferencesRepository(context)

//    @Provides
//    fun provideChartRepository(itemRepository: ItemRepository, configRepository: ConfigRepository) =
//        ChartRepository(itemRepository, configRepository)

    @Provides
    fun provideItemDao(itemDatabase: ItemDatabase): ItemDao = itemDatabase.itemDao()

    @Provides
    fun provideItemDatabase(@ApplicationContext context: Context): ItemDatabase = ItemDatabase.getDatabase(context)

    @Provides
    fun provideExportDataUseCase(itemRepository: ItemRepository) = ExportDataUseCase(itemRepository)

    @Provides
    fun provideImportDataUseCase(itemRepository: ItemRepository) = ImportDataUseCase(itemRepository)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultMetricCategory

@Module
@InstallIn(SingletonComponent::class)
object MetricCategoryModule {

    @Provides
    @DefaultMetricCategory
    fun provideDefaultMetricCategory(): MetricCategory {
        return MetricCategory.BLOOD_PRESSURE // ← ここを切り替えられる！
    }
}

