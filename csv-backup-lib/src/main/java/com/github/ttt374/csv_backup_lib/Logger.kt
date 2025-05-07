package com.github.ttt374.csv_backup_lib

import android.util.Log
import jakarta.inject.Inject

interface Logger {
    fun e(tag: String, message: String?)
}

class AndroidLogger @Inject constructor() : Logger {
    override fun e(tag: String, message: String?) {
        Log.e(tag, message.toString())
    }
}
