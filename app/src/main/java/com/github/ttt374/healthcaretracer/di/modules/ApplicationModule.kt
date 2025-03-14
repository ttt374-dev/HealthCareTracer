package com.github.ttt374.healthcaretracer.di.modules

import android.content.Context
import com.github.ttt374.healthcaretracer.data.ItemDao
import com.github.ttt374.healthcaretracer.data.ItemDatabase
import com.github.ttt374.healthcaretracer.data.ItemRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Singleton
    @Provides
    fun provideItemRepository(itemDao: ItemDao) =
        ItemRepository(itemDao)

    @Provides
    fun provideItemDao(itemDatabase: ItemDatabase): ItemDao =
        itemDatabase.itemDao()
    @Provides
    fun provideItemDatabase(@ApplicationContext context: Context): ItemDatabase =
        ItemDatabase.getDatabase(context)
}
