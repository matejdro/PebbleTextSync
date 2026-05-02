package com.matejdro.pebbletextsync.reporting

import com.matejdro.pebbletextsync.common.logging.ActionLogger
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import logcat.logcat

@ContributesBinding(AppScope::class)
@Inject
class DemoActionLogger : ActionLogger {
   override fun logAction(text: () -> String) {
      // TODO ideally here log actions somewhere where it can be useful (such as Firebase's Crashlytics)
      logcat(message = text)
   }
}
