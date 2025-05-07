package com.github.ttt374.healthcaretracer.di.modules

import android.content.Context
import com.github.ttt374.csv_backup_lib.ContentResolverWrapper
import com.github.ttt374.csv_backup_lib.ContentResolverWrapperImpl
import com.github.ttt374.csv_backup_lib.CsvExporter
import com.github.ttt374.csv_backup_lib.CsvImporter
import com.github.ttt374.csv_backup_lib.ExportDataUseCase
import com.github.ttt374.csv_backup_lib.ImportDataUseCase
import com.github.ttt374.healthcaretracer.data.backup.ItemCsvSchema
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.data.repository.PreferencesRepository
import com.github.ttt374.healthcaretracer.data.item.ItemDao
import com.github.ttt374.healthcaretracer.data.item.ItemDatabase
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import com.github.ttt374.healthcaretracer.data.repository.ItemRepositoryImpl
import com.github.ttt374.healthcaretracer.shared.AndroidLogger
import com.github.ttt374.healthcaretracer.shared.Logger
import com.github.ttt374.healthcaretracer.data.item.Item
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {
    @Provides
    fun provideCsvImporter(logger: Logger): CsvImporter<Item> = CsvImporter({ Item() }, ItemCsvSchema.fields)
    //fun provideCsvImporter(logger: Logger): CsvImporter<Item, CsvItemPartial> = CsvImporter(logger, { CsvItemPartial() }, ItemCsvSchema.fields )

    @Provides
    fun provideCsvExporter(): CsvExporter<Item> = CsvExporter(ItemCsvSchema.fields)

    @Provides
    fun provideContentResolverWrapper(@ApplicationContext context: Context): ContentResolverWrapper =
        ContentResolverWrapperImpl(context.contentResolver)

    @Provides
    fun provideExportDataUseCase(itemRepository: ItemRepository, csvExporter: CsvExporter<Item>, contentResolverWrapper: ContentResolverWrapper) =
        ExportDataUseCase(
            { itemRepository.getAllItems() },
            csvExporter,
            contentResolverWrapper
        )
//    fun provideExportDataUseCase(itemRepository: ItemRepository, csvExporter: CsvExporter<Item>, contentResolverWrapper: ContentResolverWrapper) =
//    //fun provideExportDataUseCase(itemRepository: ItemRepository, csvExporter: CsvExporter<Item, CsvPartial<Item>>, contentResolverWrapper: ContentResolverWrapper) =
//        ExportDataUseCase(itemRepository, csvExporter, contentResolverWrapper)

    @Provides
    fun provideImportDataUseCase(itemRepository: ItemRepository, csvImporter: CsvImporter<Item>, contentResolverWrapper: ContentResolverWrapper) =
        ImportDataUseCase(
            { itemRepository.replaceAllItems(it) },
            csvImporter,
            contentResolverWrapper
        )
}
@Module
@InstallIn(SingletonComponent::class)
object ItemRepositoryModule {
    @Provides
    @Singleton
    fun provideItemRepository(itemDao: ItemDao): ItemRepository {
        return ItemRepositoryImpl(itemDao)
    }
    @Singleton
    @Provides
    fun provideItemDao(itemDatabase: ItemDatabase): ItemDao = itemDatabase.itemDao()

    @Singleton
    @Provides
    fun provideItemDatabase(@ApplicationContext context: Context): ItemDatabase = ItemDatabase.getDatabase(context)
}

//@Module
//@InstallIn(SingletonComponent::class)
//object ItemModule {
//    @Singleton
//    @Provides
//    fun provideItemRepository(itemDao: ItemDao) =
//        ItemRepositoryImpl(itemDao)
//
//    @Singleton
//    @Provides
//    fun provideItemDao(itemDatabase: ItemDatabase): ItemDao = itemDatabase.itemDao()
//
//    @Singleton
//    @Provides
//    fun provideItemDatabase(@ApplicationContext context: Context): ItemDatabase = ItemDatabase.getDatabase(context)
//
//}

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {
    @Singleton
    @Provides
    fun provideConfigRepository(@ApplicationContext context: Context) = ConfigRepository(context)

    @Singleton
    @Provides
    fun providePreferencesRepository(@ApplicationContext context: Context) = PreferencesRepository(context)

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultMetricCategory

@Module
@InstallIn(SingletonComponent::class)
object MetricCategoryModule {
    @Provides
    @DefaultMetricCategory
    fun provideDefaultMetricCategory(): MetricType {
        return MetricType.BLOOD_PRESSURE // ← ここを切り替えられる！
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggerModule {
    @Binds
    abstract fun bindLogger(
        androidLogger: AndroidLogger
    ): Logger
}
