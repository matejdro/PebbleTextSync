package com.matejdro.pebbletextsync.files.ui.list.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class FileOpenPreprocessorImpl(
   private val context: Context,
) : FileOpenPreprocessor {
   override fun resolvePermissionsAndGetFileName(uri: Uri): FileProperties {
      val contentResolver = context.contentResolver
      contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

      val projection = arrayOf<String?>(MediaStore.MediaColumns.DISPLAY_NAME)
      return contentResolver.query(uri, projection, null, null, null).use { cursor ->
         if (cursor?.moveToFirst() != true) error("Unknown URI: $uri")

         val name = cursor.getString(0).substringBeforeLast(".")

         FileProperties(
            name,
            uri.host != "com.android.externalstorage.documents"
         )
      }
   }
}

fun interface FileOpenPreprocessor {
   fun resolvePermissionsAndGetFileName(uri: Uri): FileProperties
}

data class FileProperties(
   val name: String,
   val isFromUnreliableStorage: Boolean,
)
