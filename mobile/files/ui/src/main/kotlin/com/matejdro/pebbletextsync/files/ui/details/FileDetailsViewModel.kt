package com.matejdro.pebbletextsync.files.ui.details

import androidx.compose.runtime.Stable
import com.matejdro.pebbletextsync.common.logging.ActionLogger
import com.matejdro.pebbletextsync.files.SyncingFile
import com.matejdro.pebbletextsync.files.SyncingFileRepository
import com.matejdro.pebbletextsync.files.ui.FileDetailsScreenKey
import com.matejdro.pebbletextsync.files.ui.errors.UnknownFileException
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.data.Debouncer
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.time.TimeProvider
import si.inova.kotlinova.navigation.instructions.goBack
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class FileDetailsViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val syncingFileRepository: SyncingFileRepository,
   private val navigator: Navigator,
   private val timeProvider: TimeProvider,
) : SingleScreenViewModel<FileDetailsScreenKey>(resources.scope) {
   private val _uiState = MutableStateFlow<Outcome<FileDetailsState>>(Outcome.Progress())
   val uiState: StateFlow<Outcome<FileDetailsState>> = _uiState

   // Run using NonCancelable to ensure update sticks even if user leaves the screen
   private val debouncer = Debouncer(resources.scope, timeProvider, targetContext = NonCancellable)

   override fun onServiceRegistered() {
      actionLogger.logAction { "FileDetailsViewModel.onServiceRegistered()" }

      resources.launchResourceControlTask(_uiState) {
         emitAll(
            syncingFileRepository.getSingle(key.id).map { outcome ->
               when (outcome) {
                  is Outcome.Error -> Outcome.Error(outcome.exception, outcome.data?.let { FileDetailsState(it) })
                  is Outcome.Progress -> Outcome.Progress(outcome.data?.let { FileDetailsState(it) })
                  is Outcome.Success -> {
                     val data = outcome.data
                     if (data == null) {
                        Outcome.Error(UnknownFileException())
                     } else {
                        Outcome.Success(FileDetailsState(data))
                     }
                  }
               }
            }
         )
      }
   }

   fun updateName(newName: String) {
      actionLogger.logAction { "FileDetailsViewModel.updateName(newName = $newName)" }
      updateFile { it.copy(title = newName) }
   }

   fun updateSlots(newSlots: Int) {
      actionLogger.logAction { "FileDetailsViewModel.updateSlots(newSlots = $newSlots)" }

      updateFile { it.copy(slots = newSlots) }
   }

   private inline fun updateFile(crossinline transform: (SyncingFile) -> SyncingFile) {
      val existing = _uiState.value.data?.file ?: return

      debouncer.executeDebouncing {
         resources.launchWithExceptionReporting {
            val newFile = transform(existing)
            if (newFile != existing) {
               syncingFileRepository.update(newFile)
            }
         }
      }
   }

   fun delete() = resources.launchWithExceptionReporting {
      actionLogger.logAction { "FileDetailsViewModel.delete()" }

      resources.cancelResource(_uiState)
      syncingFileRepository.delete(key.id)
      navigator.goBack()
   }
}

data class FileDetailsState(
   val file: SyncingFile,
)
