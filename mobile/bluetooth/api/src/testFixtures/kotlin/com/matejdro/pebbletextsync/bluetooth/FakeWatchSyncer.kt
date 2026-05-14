package com.matejdro.pebbletextsync.bluetooth

class FakeWatchSyncer : WatchSyncer {
   val syncedFiles = ArrayList<Int>()
   override suspend fun syncFile(id: Int) {
      syncedFiles += id
   }
}
