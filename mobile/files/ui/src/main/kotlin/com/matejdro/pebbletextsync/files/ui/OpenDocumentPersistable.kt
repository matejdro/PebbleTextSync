package com.matejdro.pebbletextsync.files.ui

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts

class OpenDocumentPersistable : ActivityResultContracts.OpenDocument() {
   override fun createIntent(context: Context, input: Array<String>): Intent {
      return super.createIntent(context, input)
         .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
   }
}
