package com.matejdro.pebbletextsync.bluetooth

interface WatchSyncer {
   suspend fun syncFile(id: Int)
}
