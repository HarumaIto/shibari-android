package com.betsudotai.shibari.core.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtil {
    fun createTempFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val mimeType = context.contentResolver.getType(uri)

            val ext = if (mimeType?.startsWith("video/") == true) ".mp4" else ".jpg"

            val tempFile = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}$ext")
            val outputStream = FileOutputStream(tempFile)

            inputStream.copyTo(outputStream)

            inputStream.close()
            outputStream.close()

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}