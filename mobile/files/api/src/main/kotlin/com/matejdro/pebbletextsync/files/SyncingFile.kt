package com.matejdro.pebbletextsync.files

data class SyncingFile(
   val title: String,
   val contentUri: String,
   val id: Int = 0,
   val slots: Int = 1,
   val orderIndex: Int = 0,
) {
   companion object {
      const val MAX_SLOTS = 20
      const val MAX_TITLE_LENGTH_BYTES = 20
   }
}
