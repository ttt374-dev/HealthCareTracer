package com.github.ttt374.healthcaretracer.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(private val itemDao: ItemDao) {
    // crud
    suspend fun insertItem(item: Item){
        itemDao.insertItem(item)
    }
    suspend fun updateItem(item: Item){
        itemDao.updateItem(item)
    }

    // query
    fun retrieveItemsFlow(): Flow<List<Item>> =
        itemDao.getAllItemsFlow()
    fun getItemFlow(itemId: Long) = itemDao.getItemFlow(itemId)
}