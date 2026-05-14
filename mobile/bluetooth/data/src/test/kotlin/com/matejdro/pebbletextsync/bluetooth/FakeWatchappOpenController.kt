package com.matejdro.pebbletextsync.bluetooth

import com.matejdro.bucketsync.BucketSyncWatchappOpenController

class FakeWatchappOpenController : BucketSyncWatchappOpenController {
   private var nextWatchappOpenForAutoSync: Boolean = false

   override fun isNextWatchappOpenForAutoSync(): Boolean {
      return nextWatchappOpenForAutoSync
   }

   override fun setNextWatchappOpenForAutoSync() {
      nextWatchappOpenForAutoSync = true
   }

   override fun resetNextWatchappOpen() {
      nextWatchappOpenForAutoSync = false
   }
}
