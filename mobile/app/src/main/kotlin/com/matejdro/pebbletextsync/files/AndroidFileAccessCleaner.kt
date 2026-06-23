package com.matejdro.pebbletextsync.files

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.matejdro.pebbletextsync.files.util.FileAccessCleaner
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class AndroidFileAccessCleaner(
   private val context: Context,
) : FileAccessCleaner {
   override fun onFileDeleted(uri: String) {
      context.contentResolver.releasePersistableUriPermission(
         uri.toUri(),
         Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
      )
   }
}
