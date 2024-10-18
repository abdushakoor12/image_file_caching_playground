package com.abdushakoor12.image_file_caching_playground

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class ImageDownloader(
    private val context: Context
) {

    private val dir = File(context.cacheDir, "cachedImages")

    init {
        // ensure the directory exists
        dir.mkdir()
    }

    suspend fun downloadImage(url: String): Either<Exception, File> {
        return withContext(Dispatchers.IO) {
            try {
                val cachedFile = getCachedFile(url)
                if (cachedFile != null) {
                    return@withContext Either.right(cachedFile)
                }
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()
                val inputStream = connection.inputStream
                val imageExtension = getExtension(connection)
                val file = File(dir, "${url.toHash()}.${imageExtension.extension}")
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                return@withContext Either.right(file)
            } catch (e: Exception) {
                return@withContext Either.left(e)
            }
        }
    }

    private fun getExtension(connection: HttpURLConnection): ImageExtension {
        when (connection.contentType) {
            "image/jpeg",
            "image/jpg",
                -> return ImageExtension.JPEG

            "image/png" -> return ImageExtension.PNG
            "image/gif" -> return ImageExtension.GIF
            "image/webp" -> return ImageExtension.WEBP
        }
        return ImageExtension.BIN
    }

    suspend fun getCachedFile(url: String): File? {
        return withContext(Dispatchers.IO) {
            val hashedName = url.toHash().toString()
            val files = dir.listFiles()
            files?.firstOrNull { it.name.contains(hashedName) }
        }
    }
}

enum class ImageExtension {
    JPEG,
    PNG,
    GIF,
    WEBP,
    BIN; // also used for when unknown

    val extension: String
        get() = when (this) {
            JPEG -> "jpg"
            PNG -> "png"
            GIF -> "gif"
            WEBP -> "webp"
            BIN -> "bin"
        }
}

fun String.toHash(): Long {
    var hash = this.hashCode().toLong()
    if (hash < 0) {
        hash *= -1
    }
    return hash
}

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()

    companion object {
        fun <L, R> left(value: L): Either<L, R> = Left(value)
        fun <L, R> right(value: R): Either<L, R> = Right(value)
    }
}