package com.matejdro.pebbletextsync.files

import app.cash.sqldelight.coroutines.asFlow
import com.matejdro.pebbletextsync.bluetooth.WatchSyncer
import com.matejdro.pebbletextsync.files.sqldelight.generated.DbFile
import com.matejdro.pebbletextsync.files.sqldelight.generated.DbFileQueries
import com.matejdro.pebbletextsync.files.util.FileAccessCleaner
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dispatch.core.flowOnDefault
import dispatch.core.withDefault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.Outcome

@ContributesBinding(AppScope::class)
class SyncingFileRepositoryImpl(
   private val fileQueries: DbFileQueries,
   private val watchSyncer: Lazy<WatchSyncer>,
   private val fileAccessCleaner: FileAccessCleaner,
) : SyncingFileRepository {
   override fun getAll(): Flow<Outcome<List<SyncingFile>>> {
      return fileQueries.getAll().asFlow().map { query ->
         Outcome.Success(query.executeAsList().map { it.toSyncingFile() })
      }.flowOnDefault()
   }

   override fun getSingle(id: Int): Flow<Outcome<SyncingFile?>> {
      return fileQueries.getSingle(id.toLong()).asFlow().map { query ->
         Outcome.Success(query.executeAsOneOrNull()?.toSyncingFile())
      }.flowOnDefault()
   }

   override suspend fun insert(file: SyncingFile): Int = withDefault {
      fileQueries.insert(title = file.title, uri = file.contentUri, slots = file.slots.toLong())
      val insertedId = fileQueries.lastInsertRowId().executeAsOne().toInt()
      watchSyncer.value.syncFile(insertedId)

      insertedId
   }

   override suspend fun update(file: SyncingFile) = withDefault<Unit> {
      fileQueries.update(title = file.title, uri = file.contentUri, slots = file.slots.toLong(), id = file.id.toLong())
      watchSyncer.value.syncFile(file.id)
   }

   override suspend fun reorder(id: Int, toIndex: Int) = withDefault<Unit> {
      val existingFile = fileQueries.getSingle(id.toLong()).executeAsOne()

      if (existingFile.orderIndex < toIndex) {
         val affectedIds =
            fileQueries.getReorderUpwardsAffectedIds(fromIndex = existingFile.orderIndex, toIndex = toIndex.toLong())
               .executeAsList()

         fileQueries.reorderUpwards(fromIndex = existingFile.orderIndex, toIndex = toIndex.toLong(), id = id.toLong())

         for (affectedId in affectedIds) {
            watchSyncer.value.syncFile(affectedId.toInt())
         }
      } else {
         val affectedIds =
            fileQueries.getReorderDownwardsAffectedIds(toIndex = toIndex.toLong(), fromIndex = existingFile.orderIndex)
               .executeAsList()

         fileQueries.reorderDownwards(toIndex = toIndex.toLong(), fromIndex = existingFile.orderIndex, id = id.toLong())

         for (affectedId in affectedIds) {
            watchSyncer.value.syncFile(affectedId.toInt())
         }
      }
   }

   override suspend fun delete(id: Int) = withDefault<Unit> {
      val file = fileQueries.getSingle(id.toLong()).executeAsOneOrNull()

      fileQueries.delete(id.toLong())
      watchSyncer.value.syncFile(id)
      file?.let {
         fileAccessCleaner.onFileDeleted(it.uri)
      }
   }
}

private fun DbFile.toSyncingFile(): SyncingFile {
   return SyncingFile(title = title, contentUri = uri, id = id.toInt(), slots = slots.toInt(), orderIndex = orderIndex.toInt())
}
