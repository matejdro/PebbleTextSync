package com.matejdro.pebbletextsync.bluetooth.util

import android.content.Context
import android.net.Uri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dispatch.core.withDefault
import okio.buffer
import okio.source
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

@ContributesBinding(AppScope::class)
class FileContentsReaderImpl(
   private val context: Context,
) : FileContentsReader {
   private val utf8Decoder = StandardCharsets.UTF_8.newDecoder()

   override suspend fun read(uri: Uri, limitBytes: Int): String = withDefault {
      val buffer = ByteBuffer.allocate(limitBytes)

      @Suppress("MissingUseCall") // False positive: Use is called below
      val stream = context.contentResolver.openInputStream(uri) ?: error("Invalid file $uri")

      var read = 0
      stream.source().buffer().use { source ->
         while (limitBytes > read && !source.exhausted()) {
            val newRead = source.read(sink = buffer.array(), offset = read, byteCount = limitBytes - read)
            if (newRead == -1) break

            read += newRead
         }
      }

      buffer.limit(read)
      utf8Decoder.decode(buffer).toString()
   }
}

fun interface FileContentsReader {
   suspend fun read(uri: Uri, limitBytes: Int): String
}
