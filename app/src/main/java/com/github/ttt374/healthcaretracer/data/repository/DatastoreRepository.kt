package com.github.ttt374.healthcaretracer.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/////////////////////////////
class GenericSerializer<T : Any>(
    private val serializer: KSerializer<T>,
    default: T,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : Serializer<T> {
    override val defaultValue: T = default

    override suspend fun readFrom(input: InputStream): T {
        return try {
            val jsonString = input.readBytes().decodeToString()
             json.decodeFromString(serializer, jsonString)
        } catch (e: Exception) {
            defaultValue
        }
    }
    override suspend fun writeTo(t: T, output: OutputStream) {
        try {
            val jsonString = json.encodeToString(serializer, t)
            withContext(Dispatchers.IO) {
                output.write(jsonString.encodeToByteArray())
            }
        } catch (e: Exception) {
            Log.e("serialize error", e.message.toString())
        }
    }
}
//////////////////////////////////
interface DataStoreRepository<T> {
    val dataFlow: Flow<T>
    suspend fun updateData(transform: suspend (t: T) -> T): T
}
class DataStoreRepositoryImpl<T>(context: Context, fileName: String, private val serializer: Serializer<T>):
    DataStoreRepository<T> {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val dataStore: DataStore<T> = DataStoreFactory.create(
        serializer = serializer,
        produceFile = { context.dataStoreFile(fileName) },
        scope = scope
    )
    override val dataFlow: Flow<T> = dataStore.data
    override suspend fun updateData(transform: suspend (t: T) -> T): T = dataStore.updateData(transform)
    //override suspend fun clearData() = updateData(serializer.defaultValue)
}
