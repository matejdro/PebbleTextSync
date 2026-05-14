package com.matejdro.pebbletextsync.files.ui.details

import com.matejdro.pebbletextsync.files.FakeSyncingFileRepository
import com.matejdro.pebbletextsync.files.SyncingFile
import com.matejdro.pebbletextsync.files.ui.FileDetailsScreenKey
import com.matejdro.pebbletextsync.files.ui.FileListScreenKey
import com.matejdro.pebbletextsync.files.ui.errors.UnknownFileException
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.test.outcomes.shouldBeErrorWith
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.outcomes.testCoroutineResourceManager
import si.inova.kotlinova.core.test.time.virtualTimeProvider
import si.inova.kotlinova.navigation.test.FakeNavigator
import kotlin.time.Duration.Companion.seconds

class FileDetailsViewModelTest {
   private val scope = TestScope()

   private val repo = FakeSyncingFileRepository()

   private val navigator = FakeNavigator()

   private val viewModel = FileDetailsViewModel(
      scope.testCoroutineResourceManager(),
      {},
      repo,
      navigator,
      scope.virtualTimeProvider()
   )

   @Test
   fun `Load data`() = scope.runTest {
      insertSampleData()

      viewModel.onServiceRegistered()
      runCurrent()

      viewModel.uiState.value shouldBeSuccessWithData FileDetailsState(
         SyncingFile("File B", "content://b", 2, 5, 1)
      )
   }

   @Test
   fun `Emit error outcome when file stops existing`() = scope.runTest {
      insertSampleData()

      viewModel.onServiceRegistered()
      runCurrent()

      repo.delete(2)
      runCurrent()

      viewModel.uiState.value.shouldBeErrorWith(exceptionType = UnknownFileException::class, expectedData = null)
   }

   @Test
   fun `Update name`() = scope.runTest {
      insertSampleData()

      viewModel.onServiceRegistered()
      runCurrent()

      viewModel.updateName("File C")
      delay(1.seconds)

      repo.getSingle(2).first() shouldBeSuccessWithData
         SyncingFile("File C", "content://b", 2, 5, 1)
   }

   @Test
   fun `Wait there are many name updates in succession`() = scope.runTest {
      insertSampleData()

      viewModel.onServiceRegistered()
      runCurrent()

      repeat(10) {
         viewModel.updateName("File C")
      }
      delay(1.seconds)

      repo.getSingle(2).first() shouldBeSuccessWithData
         SyncingFile("File C", "content://b", 2, 5, 1)
      repo.numUpdates shouldBe 1
   }

   @Test
   fun `Update slots`() = scope.runTest {
      insertSampleData()

      viewModel.onServiceRegistered()
      runCurrent()

      viewModel.updateSlots(3)
      delay(1.seconds)

      repo.getSingle(2).first() shouldBeSuccessWithData
         SyncingFile("File B", "content://b", 2, 3, 1)
   }

   @Test
   fun `Wait when there are many slots updates in succession`() = scope.runTest {
      insertSampleData()

      viewModel.onServiceRegistered()
      runCurrent()

      repeat(10) {
         viewModel.updateSlots(3)
      }
      delay(1.seconds)

      repo.getSingle(2).first() shouldBeSuccessWithData
         SyncingFile("File B", "content://b", 2, 3, 1)
      repo.numUpdates shouldBe 1
   }

   @Test
   fun `Delete item`() = scope.runTest {
      insertSampleData()

      viewModel.onServiceRegistered()
      runCurrent()

      viewModel.delete()
      runCurrent()

      repo.getSingle(2).first() shouldBeSuccessWithData null
      viewModel.uiState.value.shouldBeInstanceOf<Outcome.Success<*>>() // Keep the data loaded in UI to not break the animation
      navigator.backstack.shouldContainExactly(FileListScreenKey)
   }

   private suspend fun insertSampleData() {
      navigator.backstack = listOf(FileListScreenKey, FileDetailsScreenKey(2))
      viewModel.key = FileDetailsScreenKey(2)

      repo.insert(SyncingFile("File A", "content://a", 1, 2, 0))
      repo.insert(SyncingFile("File B", "content://b", 2, 5, 1))
   }
}
