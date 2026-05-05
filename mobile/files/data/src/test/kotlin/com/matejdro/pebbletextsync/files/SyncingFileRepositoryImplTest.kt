package com.matejdro.pebbletextsync.files

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.matejdro.pebbletextsync.files.sqldelight.generated.Database
import com.matejdro.pebbletextsync.files.sqldelight.generated.DbFileQueries
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData

class SyncingFileRepositoryImplTest {
   private val scope = TestScopeWithDispatcherProvider()
   private val repo = SyncingFileRepositoryImpl(
      createTestFileQueries()
   )

   @Test
   fun `Insert and get files`() = scope.runTest {
      repo.insert(SyncingFile("File A", "content://files/A", slots = 3))
      repo.insert(SyncingFile("File B", "content://files/B"))

      repo.getAll().first().shouldBeSuccessWithData(
         listOf(
            SyncingFile("File A", "content://files/A", orderIndex = 0, id = 1, slots = 3),
            SyncingFile("File B", "content://files/B", orderIndex = 1, id = 2, slots = 1)
         )
      )
   }

   @Test
   fun `Update existing files`() = scope.runTest {
      repo.insert(SyncingFile("File A", "content://files/A", slots = 3))
      repo.insert(SyncingFile("File B", "content://files/B"))
      runCurrent()

      repo.update(SyncingFile("File C", "content://files/C", slots = 4, id = 2))
      runCurrent()

      repo.getAll().first().shouldBeSuccessWithData(
         listOf(
            SyncingFile("File A", "content://files/A", orderIndex = 0, id = 1, slots = 3),
            SyncingFile("File C", "content://files/C", orderIndex = 1, id = 2, slots = 4)
         )
      )
   }

   @Test
   fun `Delete files`() = scope.runTest {
      repo.insert(SyncingFile("File A", "content://files/A", slots = 3))
      repo.insert(SyncingFile("File B", "content://files/B"))
      runCurrent()

      repo.delete(2)
      runCurrent()

      repo.getAll().first().shouldBeSuccessWithData(
         listOf(
            SyncingFile("File A", "content://files/A", orderIndex = 0, id = 1, slots = 3),
         )
      )
   }

   @Test
   fun `Reorder up`() = scope.runTest {
      repo.insert(SyncingFile("File A", "content://files/A"))
      repo.insert(SyncingFile("File B", "content://files/B"))
      repo.insert(SyncingFile("File C", "content://files/C"))
      runCurrent()

      repo.reorder(3, 0)
      runCurrent()

      repo.getAll().first().shouldBeSuccessWithData(
         listOf(
            SyncingFile("File C", "content://files/C", id = 3, orderIndex = 0),
            SyncingFile("File A", "content://files/A", id = 1, orderIndex = 1),
            SyncingFile("File B", "content://files/B", id = 2, orderIndex = 2),
         )
      )
   }

   @Test
   fun `Reorder down`() = scope.runTest {
      repo.insert(SyncingFile("File A", "content://files/A"))
      repo.insert(SyncingFile("File B", "content://files/B"))
      repo.insert(SyncingFile("File C", "content://files/C"))
      runCurrent()

      repo.reorder(1, 2)
      runCurrent()

      repo.getAll().first().shouldBeSuccessWithData(
         listOf(
            SyncingFile("File B", "content://files/B", id = 2, orderIndex = 0),
            SyncingFile("File C", "content://files/C", id = 3, orderIndex = 1),
            SyncingFile("File A", "content://files/A", id = 1, orderIndex = 2),
         )
      )
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
