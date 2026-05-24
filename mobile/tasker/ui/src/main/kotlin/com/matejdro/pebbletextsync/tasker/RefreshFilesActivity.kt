package com.matejdro.pebbletextsync.tasker

import android.os.Bundle
import com.matejdro.textsync.tasker.ui.R
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

class RefreshFilesActivity : TaskerConfigurationActivity() {
   override fun getInitialHistory(): List<ScreenKey> {
      return emptyList()
   }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      val bundle = Bundle().apply {
         putString(BundleKeys.ACTION, TaskerAction.REFRESH_FILES.name)
      }

      saveConfiguration(bundle, getString(R.string.refresh_files))
      finish()
   }
}
