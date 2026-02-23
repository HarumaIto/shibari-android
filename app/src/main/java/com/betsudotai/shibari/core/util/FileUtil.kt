package com.betsudotai.shibari.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtil {

    private const val MAX_IMAGE_SIZE = 1000

    fun createTempFileFromUri(context: Context, uri: Uri): File? {
        val mimeType = context.contentResolver.getType(uri)
        val isVideo = mimeType?.startsWith("video/") == true

        return if (isVideo) {
            // 動画の場合は圧縮せずにそのまま一時ファイルにコピー（現状維持）
            copyFileToCache(context, uri, ".mp4")
        } else {
            // 画像の場合はリサイズ＆圧縮処理を行う
            compressImage(context, uri)
        }
    }


    private fun compressImage(context: Context, uri: Uri): File? {
        return try {
            // 1. 画像のサイズ情報だけを読み込む（メモリ節約のため）
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            // 2. 縮小率（inSampleSize）を計算する
            var inSampleSize = 1
            if (options.outHeight > MAX_IMAGE_SIZE || options.outWidth > MAX_IMAGE_SIZE) {
                val halfHeight: Int = options.outHeight / 2
                val halfWidth: Int = options.outWidth / 2
                while (halfHeight / inSampleSize >= MAX_IMAGE_SIZE && halfWidth / inSampleSize >= MAX_IMAGE_SIZE) {
                    inSampleSize *= 2
                }
            }

            // 3. 縮小率を適用して実際に画像をメモリに読み込む
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return null

            // 4. 一時ファイルを作成してJPEG形式（品質80%）で書き出す
            val tempFile = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { out ->
                // 画質を80%に落とす（見た目はほぼ変わらず、容量が劇的に下がります）
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            // メモリ解放
            bitmap.recycle()
            tempFile

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun copyFileToCache(context: Context, uri: Uri, extension: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}$extension")
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