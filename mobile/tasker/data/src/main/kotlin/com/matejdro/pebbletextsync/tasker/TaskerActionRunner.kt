package com.matejdro.pebbletextsync.tasker

import android.os.Bundle
import com.matejdro.pebbletextsync.bluetooth.WatchSyncer
import dev.zacsweers.metro.Inject
import logcat.logcat
import si.inova.kotlinova.core.state.toMap

@Inject
class TaskerActionRunner(
   private val watchSyncer: WatchSyncer,
) {
   suspend fun run(bundle: Bundle) {
      val actionName = bundle.getString(BundleKeys.ACTION) ?: error("Missing action from bundle")
      val action = enumValueOf<TaskerAction>(actionName)

      logcat { "Got tasker action ${bundle.toMap()}" }

      when (action) {
         TaskerAction.REFRESH_FILES -> watchSyncer.syncAll()
      }
   }
}
