package com.matejdro.pebbletextsync.files

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import si.inova.kotlinova.core.outcome.Outcome

class FakeSyncingFileRepository : SyncingFileRepository {
   private val files = MutableStateFlow<List<SyncingFile>>(emptyList())
   var numUpdates = 0

   override fun getAll(): Flow<Outcome<List<SyncingFile>>> {
      return files.map { Outcome.Success(it) }
   }

   override fun getSingle(id: Int): Flow<Outcome<SyncingFile?>> {
      return files.map { files -> Outcome.Success(files.find { it.id == id }) }
   }

   override suspend fun insert(file: SyncingFile): Int {
      val orderIndex = files.value.size
      val updatedFile = file.copy(id = orderIndex + 1, orderIndex = orderIndex)

      files.update { list ->
         list + updatedFile
      }

      return updatedFile.id
   }

   override suspend fun update(file: SyncingFile) {
      numUpdates++

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

            for ((index, element) in this.withIndex()) {
               this[index] = element.copy(orderIndex = index)
            }
         }
      }
   }

   override suspend fun delete(id: Int) {
      files.update { fileList ->
         fileList.filter { file -> file.id != id }
      }
   }
}
