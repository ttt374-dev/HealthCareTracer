package com.github.ttt374.csv_backup_lib

import android.content.ContentResolver
import android.net.Uri
import jakarta.inject.Inject
import java.io.InputStream
import java.io.OutputStream


interface ContentResolverWrapper {
    fun openOutputStream(uri: Uri): OutputStream?
    fun openInputStream(uri: Uri): InputStream?
}
class ContentResolverWrapperImpl @Inject constructor(private val contentResolver: ContentResolver) :
    ContentResolverWrapper {
    override fun openOutputStream(uri: Uri): OutputStream? {
        return contentResolver.openOutputStream(uri)
    }
    override fun openInputStream(uri: Uri): InputStream? {
        return contentResolver.openInputStream(uri)
    }
}
