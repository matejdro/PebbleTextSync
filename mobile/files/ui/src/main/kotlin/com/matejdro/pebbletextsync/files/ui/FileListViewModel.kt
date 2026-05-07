package com.matejdro.pebbletextsync.files.ui

import android.net.Uri
import androidx.compose.runtime.Stable
import com.matejdro.pebbletextsync.common.logging.ActionLogger
import com.matejdro.pebbletextsync.files.SyncingFile
import com.matejdro.pebbletextsync.files.SyncingFileRepository
import com.matejdro.pebbletextsync.files.ui.util.FileOpenPreprocessor
import dev.zacsweers.metro.Inject
import dispatch.core.withDefault
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class FileListViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val fileOpenPreprocessor: FileOpenPreprocessor,
   private val syncingFileRepository: SyncingFileRepository,
) : SingleScreenViewModel<FileListScreenKey>(resources.scope) {

   fun addFile(uri: Uri) = resources.launchWithExceptionReporting {
      actionLogger.logAction { "FileListViewModel.addFile($uri)" }

      withDefault {
         val fileName = fileOpenPreprocessor.resolvePermissionsAndGetFileName(uri)
         syncingFileRepository.insert(SyncingFile(title = fileName, contentUri = uri.toString()))
      }
   }
}
