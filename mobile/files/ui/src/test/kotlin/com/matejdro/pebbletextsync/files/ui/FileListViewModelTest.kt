package com.matejdro.pebbletextsync.files.ui

import androidx.core.net.toUri
import com.matejdro.pebbletextsync.files.FakeSyncingFileRepository
import com.matejdro.pebbletextsync.files.SyncingFile
import com.matejdro.pebbletextsync.files.ui.list.FileListState
import com.matejdro.pebbletextsync.files.ui.list.FileListViewModel
import com.matejdro.pebbletextsync.files.ui.list.util.FileOpenPreprocessor
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.outcomes.shouldBeSuccessWithData
import si.inova.kotlinova.core.test.outcomes.testCoroutineResourceManager
import si.inova.kotlinova.navigation.test.FakeNavigator

class FileListViewModelTest {
   private val scope = TestScopeWithDispatcherProvider()
   private val navigator = FakeNavigator(FileListScreenKey)

   private val fileOpenPreprocessor = FileOpenPreprocessor { uri ->
      if (uri.toString() == "content://test_file") {
         "File name"
      } else {
         throw UnsupportedOperationException("Unknown file $uri")
      }
   }

   private val syncingFileRepository = FakeSyncingFileRepository()

   private val viewModel = FileListViewModel(
      resources = scope.testCoroutineResourceManager(),
      actionLogger = { },
      fileOpenPreprocessor = fileOpenPreprocessor,
      syncingFileRepository = syncingFileRepository,
      navigator = navigator,
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
   fun `Expose list of files`() = scope.runTest {
      syncingFileRepository.insert(SyncingFile("File A", "content://files/A", slots = 3))
      syncingFileRepository.insert(SyncingFile("File B", "content://files/B"))

      viewModel.onServiceRegistered()
      runCurrent()

      viewModel.uiState.first().shouldBeSuccessWithData(
         FileListState(
            listOf(
               SyncingFile("File A", "content://files/A", orderIndex = 0, id = 1, slots = 3),
               SyncingFile("File B", "content://files/B", orderIndex = 1, id = 2, slots = 1)
            )
         )
      )
   }
}
