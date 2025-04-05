package com.github.ttt374.healthcaretracer.data.datastore

import android.content.Context
import javax.inject.Singleton

//////////////////////////////////////////////////////////////
/**
 *  [ConfigRepository]
 *
 *  Config - 設定を管理する。データを Config にまとめて serialize して　DataStore に入れている。
 *  datastore の interface に移譲しているが、ConfigHandler でラップしたのを使った方がよい。
 */
@Singleton
class
ConfigRepository(context: Context) :DataStoreRepository<Config> by DataStoreRepositoryImpl (
    context = context,
    fileName = "config4", // AppConst.DataStoreFilename.CONFIG.filename,
    serializer = GenericSerializer(serializer = Config.serializer(), default = Config())
)
