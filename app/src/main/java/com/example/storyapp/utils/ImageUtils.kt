package com.example.storyapp.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlin.math.max

private const val MAXIMAL_SIZE = 1_000_000
private const val MAX_DIMENSION = 1600
private const val QUALITY_STEP = 5
private const val IMAGE_DIRECTORY = "images"

private fun createTempImageFile(context: Context): File {
    val directory = File(context.cacheDir, IMAGE_DIRECTORY).apply { mkdirs() }
    return File.createTempFile("STORY_${System.currentTimeMillis()}_", ".jpg", directory)
}

fun createCameraImageUri(context: Context): Uri = FileProvider.getUriForFile(
    context,
    "${context.packageName}.fileprovider",
    createTempImageFile(context)
)

suspend fun reduceImageFile(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

    val options = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
    }
    val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        ?: throw IOException("Unable to decode image: $uri")
    val rotatedBitmap = bitmap.rotate(resolver.readRotationDegrees(uri))

    var quality = 100
    var stream: ByteArrayOutputStream
    do {
        stream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        quality -= QUALITY_STEP
    } while (stream.size() > MAXIMAL_SIZE && quality > QUALITY_STEP)

    createTempImageFile(context).apply { writeBytes(stream.toByteArray()) }
}

private fun calculateInSampleSize(width: Int, height: Int): Int {
    var sampleSize = 1
    while (max(width, height) / (sampleSize * 2) >= MAX_DIMENSION) {
        sampleSize *= 2
    }
    return sampleSize
}

private fun ContentResolver.readRotationDegrees(uri: Uri): Float {
    val orientation = openInputStream(uri)?.use {
        ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } ?: ExifInterface.ORIENTATION_NORMAL
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
}

private fun Bitmap.rotate(degrees: Float): Bitmap {
    if (degrees == 0f) return this
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
