package com.matejdro.pebbletextsync.bluetooth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

class FakeWatchSyncer : WatchSyncer {
   val syncedFiles = ArrayList<Int>()
   var syncAllCalled = false

   val blockSyncing = MutableStateFlow(false)

   override suspend fun syncFile(id: Int) {
      syncedFiles += id
   }

   override suspend fun syncAll() {
      blockSyncing.first { !it }

      syncAllCalled = true
   }
}
