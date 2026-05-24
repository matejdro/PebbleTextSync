package com.matejdro.pebbletextsync.files.ui.list

import android.net.Uri
import androidx.compose.runtime.Stable
import com.matejdro.pebble.bluetooth.common.util.LimitingStringEncoder
import com.matejdro.pebbletextsync.bluetooth.WatchSyncer
import com.matejdro.pebbletextsync.common.logging.ActionLogger
import com.matejdro.pebbletextsync.files.SyncingFile
import com.matejdro.pebbletextsync.files.SyncingFileRepository
import com.matejdro.pebbletextsync.files.ui.FileDetailsScreenKey
import com.matejdro.pebbletextsync.files.ui.FileListScreenKey
import com.matejdro.pebbletextsync.files.ui.list.util.FileOpenPreprocessor
import com.matejdro.pebbletextsync.navigation.instructions.OpenScreenOrReplaceExistingType
import dev.zacsweers.metro.Inject
import dispatch.core.withDefault
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel
import kotlin.time.Duration.Companion.seconds

@Stable
@Inject
@ContributesScopedService
class FileListViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val fileOpenPreprocessor: FileOpenPreprocessor,
   private val syncingFileRepository: SyncingFileRepository,
   private val navigator: Navigator,
   private val watchSyncer: WatchSyncer,
) : SingleScreenViewModel<FileListScreenKey>(resources.scope) {
   private val _uiState = MutableStateFlow<Outcome<FileListState>>(Outcome.Progress())
   val uiState: StateFlow<Outcome<FileListState>>
      get() = _uiState

   private val syncing = MutableStateFlow(false)

   override fun onServiceRegistered() {
      actionLogger.logAction { "FileListViewModel.onServiceRegistered()" }

      resources.launchResourceControlTask(_uiState) {
         emitAll(
            syncingFileRepository.getAll().combine(syncing) { outcome, syncing ->
               outcome.mapData { FileListState(it, syncing) }
            }
         )
      }
   }

   fun addFile(uri: Uri) = resources.launchWithExceptionReporting {
      actionLogger.logAction { "FileListViewModel.addFile($uri)" }

      val addedFileId = withDefault {
         val fileName = fileOpenPreprocessor.resolvePermissionsAndGetFileName(uri)
         val limitedFileName = String(
            LimitingStringEncoder()
               .encodeSizeLimited(fileName, SyncingFile.MAX_TITLE_LENGTH_BYTES, ellipsize = false)
               .encodedString
         )
         syncingFileRepository.insert(SyncingFile(title = limitedFileName, contentUri = uri.toString()))
      }

      navigator.navigate(OpenScreenOrReplaceExistingType(FileDetailsScreenKey(addedFileId)))
   }

   fun reorder(id: Int, toIndex: Int = -1) = resources.launchWithExceptionReporting {
      actionLogger.logAction { "FileListViewModel.reorder(id = $id, toIndex = $toIndex)" }
      syncingFileRepository.reorder(id, toIndex)
   }

   fun syncAll() = resources.launchWithExceptionReporting {
      actionLogger.logAction { "FileListViewModel.syncAll()" }

      try {
         syncing.value = true
         watchSyncer.syncAll()
         delay(1.seconds) // Ensure swipe to refresh indicator stays displayed for at least a while
      } finally {
         syncing.value = false
      }
   }
}

data class FileListState(
   val files: List<SyncingFile>,
   val syncing: Boolean,
)
