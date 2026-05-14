package com.matejdro.pebbletextsync.files

import kotlinx.coroutines.flow.Flow
import si.inova.kotlinova.core.outcome.Outcome

interface SyncingFileRepository {
   fun getAll(): Flow<Outcome<List<SyncingFile>>>
   fun getSingle(id: Int): Flow<Outcome<SyncingFile?>>

   /**
    * @return id of the inserted file
    */
   suspend fun insert(file: SyncingFile): Int
   suspend fun update(file: SyncingFile)
   suspend fun reorder(id: Int, toIndex: Int)
   suspend fun delete(id: Int)
}
