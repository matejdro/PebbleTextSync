package com.matejdro.pebbletextsync.files

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import si.inova.kotlinova.core.outcome.Outcome

class FakeSyncingFileRepository : SyncingFileRepository {
   private val files = MutableStateFlow<List<SyncingFile>>(emptyList())

   override fun getAll(): Flow<Outcome<List<SyncingFile>>> {
      return files.map { Outcome.Success(it) }
   }

   override suspend fun insert(file: SyncingFile) {
      files.update { list ->
         val orderIndex = list.size
         list + file.copy(id = orderIndex + 1, orderIndex = orderIndex)
      }
   }

   override suspend fun update(file: SyncingFile) {
      files.update { fileList ->
         fileList.map { fileToCheck ->
            if (fileToCheck.id == file.id) {
               file
            } else {
               fileToCheck
            }
         }
      }
   }

   override suspend fun reorder(id: Int, toIndex: Int) {
      files.update { list ->
         val existing = list.first { it.id == id }
         list.toMutableList().apply {
            remove(existing)
            add(toIndex, existing)
         }
      }
   }

   override suspend fun delete(id: Int) {
      files.update { fileList ->
         fileList.filter { file -> file.id != id }
      }
   }
}
