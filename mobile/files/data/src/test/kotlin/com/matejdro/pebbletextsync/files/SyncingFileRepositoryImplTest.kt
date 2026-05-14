package com.matejdro.pebbletextsync.files

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.matejdro.pebbletextsync.bluetooth.FakeWatchSyncer
import com.matejdro.pebbletextsync.files.sqldelight.generated.Database
import com.matejdro.pebbletextsync.files.sqldelight.generated.DbFileQueries
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData

class SyncingFileRepositoryImplTest {
   private val scope = TestScopeWithDispatcherProvider()
   private val watchSyncer = FakeWatchSyncer()

   private val repo = SyncingFileRepositoryImpl(
      createTestFileQueries(),
      lazyOf(watchSyncer),
   )

   @Test
   fun `Insert and get files`() = scope.runTest {
      repo.insert(SyncingFile("File A", "content://files/A", slots = 3)) shouldBe 1
      repo.insert(SyncingFile("File B", "content://files/B")) shouldBe 2

      repo.getAll().first().shouldBeSuccessWithData(
         listOf(
            SyncingFile("File A", "content://files/A", orderIndex = 0, id = 1, slots = 3),
            SyncingFile("File B", "content://files/B", orderIndex = 1, id = 2, slots = 1)
         )
      )

      watchSyncer.syncedFiles.shouldContainExactly(1, 2)
   }

   @Test
   fun `Update existing files`() = scope.runTest {
      repo.insert(SyncingFile("File A", "content://files/A", slots = 3))
      repo.insert(SyncingFile("File B", "content://files/B"))
      runCurrent()
      watchSyncer.syncedFiles.clear()

      repo.update(SyncingFile("File C", "content://files/C", slots = 4, id = 2))
      runCurrent()

      repo.getAll().first().shouldBeSuccessWithData(
         listOf(
            SyncingFile("File A", "content://files/A", orderIndex = 0, id = 1, slots = 3),
            SyncingFile("File C", "content://files/C", orderIndex = 1, id = 2, slots = 4)
         )
      )

      watchSyncer.syncedFiles.shouldContainExactly(2)
   }

   @Test
   fun `Delete files`() = scope.runTest {
      repo.insert(SyncingFile("File A", "content://files/A", slots = 3))
      repo.insert(SyncingFile("File B", "content://files/B"))
      runCurrent()
      watchSyncer.syncedFiles.clear()

      repo.delete(2)
      runCurrent()

      repo.getAll().first().shouldBeSuccessWithData(
         listOf(
            SyncingFile("File A", "content://files/A", orderIndex = 0, id = 1, slots = 3),
         )
      )

      watchSyncer.syncedFiles.shouldContainExactly(2)
   }

   @Test
   fun `Reorder up`() = scope.runTest {
      repo.insert(SyncingFile("File A", "content://files/A"))
      repo.insert(SyncingFile("File B", "content://files/B"))
      repo.insert(SyncingFile("File C", "content://files/C"))
      runCurrent()
      watchSyncer.syncedFiles.clear()

      repo.reorder(3, 1)
      runCurrent()

      repo.getAll().first().shouldBeSuccessWithData(
         listOf(
            SyncingFile("File A", "content://files/A", id = 1, orderIndex = 0),
            SyncingFile("File C", "content://files/C", id = 3, orderIndex = 1),
            SyncingFile("File B", "content://files/B", id = 2, orderIndex = 2),
         )
      )

      watchSyncer.syncedFiles.shouldContainExactly(2, 3)
   }

   @Test
   fun `Reorder down`() = scope.runTest {
      repo.insert(SyncingFile("File A", "content://files/A"))
      repo.insert(SyncingFile("File B", "content://files/B"))
      repo.insert(SyncingFile("File C", "content://files/C"))
      runCurrent()
      watchSyncer.syncedFiles.clear()

      repo.reorder(1, 2)
      runCurrent()

      repo.getAll().first().shouldBeSuccessWithData(
         listOf(
            SyncingFile("File B", "content://files/B", id = 2, orderIndex = 0),
            SyncingFile("File C", "content://files/C", id = 3, orderIndex = 1),
            SyncingFile("File A", "content://files/A", id = 1, orderIndex = 2),
         )
      )

      watchSyncer.syncedFiles.shouldContainExactly(1, 2, 3)
   }

   @Test
   fun `Insert and get single file`() = scope.runTest {
      repo.insert(SyncingFile("File A", "content://files/A", slots = 3))
      repo.insert(SyncingFile("File B", "content://files/B"))

      repo.getSingle(id = 1).first()
         .shouldBeSuccessWithData(SyncingFile("File A", "content://files/A", orderIndex = 0, id = 1, slots = 3))
   }

   @Test
   fun `Return null when single file does not exist`() = scope.runTest {
      repo.insert(SyncingFile("File A", "content://files/A", slots = 3))
      repo.insert(SyncingFile("File B", "content://files/B"))

      repo.getSingle(id = 3).first().shouldBeSuccessWithData(null)
   }
}

internal fun createTestFileQueries(
   driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
      Database.Schema.create(
         this
      )
   },
): DbFileQueries {
   return Database(driver).dbFileQueries
}
