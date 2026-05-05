package com.matejdro.pebbletextsync.files

import app.cash.sqldelight.coroutines.asFlow
import com.matejdro.pebbletextsync.files.sqldelight.generated.DbFile
import com.matejdro.pebbletextsync.files.sqldelight.generated.DbFileQueries
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
) : SyncingFileRepository {
   override fun getAll(): Flow<Outcome<List<SyncingFile>>> {
      return fileQueries.getAll().asFlow().map { query ->
         Outcome.Success(query.executeAsList().map { it.toSyncingFile() })
      }.flowOnDefault()
   }

   override suspend fun insert(file: SyncingFile) = withDefault<Unit> {
      fileQueries.insert(title = file.title, uri = file.contentUri, slots = file.slots.toLong())
   }

   override suspend fun update(file: SyncingFile) = withDefault<Unit> {
      fileQueries.update(title = file.title, uri = file.contentUri, slots = file.slots.toLong(), id = file.id.toLong())
   }

   override suspend fun reorder(id: Int, toIndex: Int) = withDefault<Unit> {
      val existingFile = fileQueries.getSingle(id.toLong()).executeAsOne()

      if (existingFile.orderIndex < toIndex) {
         fileQueries.reorderUpwards(fromIndex = existingFile.orderIndex, toIndex = toIndex.toLong(), id = id.toLong())
      } else {
         fileQueries.reorderDownwards(toIndex = toIndex.toLong(), fromIndex = existingFile.orderIndex, id = id.toLong())
      }
   }

   override suspend fun delete(id: Int) = withDefault<Unit> {
      fileQueries.delete(id.toLong())
   }
}

private fun DbFile.toSyncingFile(): SyncingFile {
   return SyncingFile(title = title, contentUri = uri, id = id.toInt(), slots = slots.toInt(), orderIndex = orderIndex.toInt())
}
