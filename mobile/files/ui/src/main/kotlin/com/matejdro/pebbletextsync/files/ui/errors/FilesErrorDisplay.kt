package com.matejdro.pebbletextsync.files.ui.errors

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.matejdro.pebbletextsync.files.ui.R
import com.matejdro.pebbletextsync.ui.errors.commonUserFriendlyMessage
import si.inova.kotlinova.core.outcome.CauseException

@Composable
fun CauseException.filesUserFriendlyMessage(
   hasExistingData: Boolean = false,
): String {
   return if (this is UnknownFileException) {
      stringResource(R.string.missing_text_file)
   } else {
      commonUserFriendlyMessage(hasExistingData)
   }
}
