package com.matejdro.pebbletextsync.files

import kotlinx.coroutines.flow.Flow
import si.inova.kotlinova.core.outcome.Outcome

interface SyncingFileRepository {
   fun getAll(): Flow<Outcome<List<SyncingFile>>>
   suspend fun insert(file: SyncingFile)
   suspend fun update(file: SyncingFile)
   suspend fun reorder(id: Int, toIndex: Int)
   suspend fun delete(id: Int)
}
