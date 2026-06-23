package com.matejdro.pebbletextsync.files.ui

import androidx.core.net.toUri
import com.matejdro.pebbletextsync.bluetooth.FakeWatchSyncer
import com.matejdro.pebbletextsync.files.FakeSyncingFileRepository
import com.matejdro.pebbletextsync.files.SyncingFile
import com.matejdro.pebbletextsync.files.ui.errors.UnreliableStorageWarningScreenKey
import com.matejdro.pebbletextsync.files.ui.list.FileListState
import com.matejdro.pebbletextsync.files.ui.list.FileListViewModel
import com.matejdro.pebbletextsync.files.ui.list.util.FileOpenPreprocessor
import com.matejdro.pebbletextsync.files.ui.list.util.FileProperties
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.outcomes.testCoroutineResourceManager
import si.inova.kotlinova.navigation.test.FakeNavigator
import kotlin.time.Duration.Companion.seconds

class FileListViewModelTest {
   private val scope = TestScopeWithDispatcherProvider()
   private val navigator = FakeNavigator(FileListScreenKey)

   private val fileOpenPreprocessor = FileOpenPreprocessor { uri ->
      val uriString = uri.toString()

      if (uriString == "content://test_file") {
         FileProperties("File name", false)
      } else if (uriString == "content://test_unreliable_file") {
         FileProperties("File name", true)
      } else if (uriString == "content://long_file") {
         FileProperties("Looooooooooooooooooong file name", false)
      } else {
         throw UnsupportedOperationException("Unknown file $uri")
      }
   }

   private val syncingFileRepository = FakeSyncingFileRepository()

   private val syncer = FakeWatchSyncer()

   private val viewModel = FileListViewModel(
      resources = scope.testCoroutineResourceManager(),
      actionLogger = { },
      fileOpenPreprocessor = fileOpenPreprocessor,
      syncingFileRepository = syncingFileRepository,
      navigator = navigator,
      watchSyncer = syncer,
   )

   @Test
   fun `Add a file`() = scope.runTest {
      viewModel.addFile("content://test_file".toUri())
      runCurrent()

      syncingFileRepository.getAll().first().shouldBeSuccessWithData(
         listOf(
            SyncingFile("File name", "content://test_file", id = 1, orderIndex = 0)
         )
      )

      navigator.backstack.shouldContainExactly(FileListScreenKey, FileDetailsScreenKey(1))
   }

   @Test
   fun `Add an unreliable file`() = scope.runTest {
      viewModel.addFile("content://test_unreliable_file".toUri())
      runCurrent()

      syncingFileRepository.getAll().first().shouldBeSuccessWithData(
         listOf(
            SyncingFile("File name", "content://test_unreliable_file", id = 1, orderIndex = 0)
         )
      )

      navigator.backstack.shouldContainExactly(
         FileListScreenKey,
         FileDetailsScreenKey(1),
         UnreliableStorageWarningScreenKey
      )
   }

   @Test
   fun `Expose list of files`() = scope.runTest {
      syncingFileRepository.insert(SyncingFile("File A", "content://files/A", slots = 3))
      syncingFileRepository.insert(SyncingFile("File B", "content://files/B"))

      viewModel.onServiceRegistered()
      runCurrent()

      viewModel.uiState.first().shouldBeSuccessWithData(
         FileListState(
            listOf(
               SyncingFile("File A", "content://files/A", orderIndex = 0, id = 1, slots = 3),
               SyncingFile("File B", "content://files/B", orderIndex = 1, id = 2, slots = 5)
            ),
            syncing = false
         )
      )
   }

   @Test
   fun `Trim long file names`() = scope.runTest {
      viewModel.addFile("content://long_file".toUri())
      runCurrent()

      syncingFileRepository.getAll().first().shouldBeSuccessWithData(
         listOf(
            SyncingFile("Looooooooooooooooooo", "content://long_file", id = 1, orderIndex = 0)
         )
      )

      navigator.backstack.shouldContainExactly(FileListScreenKey, FileDetailsScreenKey(1))
   }

   @Test
   fun `Reorder files`() = scope.runTest {
      syncingFileRepository.insert(SyncingFile("File A", "content://files/A", slots = 3))
      syncingFileRepository.insert(SyncingFile("File B", "content://files/B"))

      viewModel.onServiceRegistered()
      runCurrent()

      viewModel.reorder(2, toIndex = 0)
      runCurrent()

      viewModel.uiState.first().shouldBeSuccessWithData(
         FileListState(
            listOf(
               SyncingFile("File B", "content://files/B", orderIndex = 0, id = 2, slots = 5),
               SyncingFile("File A", "content://files/A", orderIndex = 1, id = 1, slots = 3),
            ),
            syncing = false,
         )
      )
   }

   @Test
   fun `Sync all`() = scope.runTest {
      syncingFileRepository.insert(SyncingFile("File A", "content://files/A", slots = 3))
      syncingFileRepository.insert(SyncingFile("File B", "content://files/B"))

      viewModel.onServiceRegistered()
      runCurrent()

      viewModel.syncAll()
      runCurrent()

      syncer.syncAllCalled shouldBe true
   }

   @Test
   fun `Syncing should be set to true until sync completes`() = scope.runTest {
      syncingFileRepository.insert(SyncingFile("File A", "content://files/A", slots = 3))
      syncingFileRepository.insert(SyncingFile("File B", "content://files/B"))

      viewModel.onServiceRegistered()
      runCurrent()

      syncer.blockSyncing.value = true
      viewModel.syncAll()
      runCurrent()

      viewModel.uiState.value.data!!.syncing shouldBe true

      syncer.blockSyncing.value = false
      delay(2.seconds)

      viewModel.uiState.value.data!!.syncing shouldBe false
   }
}
