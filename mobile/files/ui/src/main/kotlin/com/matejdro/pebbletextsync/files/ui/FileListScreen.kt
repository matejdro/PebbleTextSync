package com.matejdro.pebbletextsync.files.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.pebbletextsync.files.SyncingFile
import com.matejdro.pebbletextsync.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.pebbletextsync.ui.debugging.FullScreenPreviews
import com.matejdro.pebbletextsync.ui.debugging.PreviewTheme
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class FileListScreen(
   private val viewModel: FileListViewModel,
) : Screen<FileListScreenKey>() {
   @Composable
   override fun Content(key: FileListScreenKey) {
      val openFileLauncher = rememberLauncherForActivityResult(OpenDocumentPersistable()) { file ->
         if (file != null) {
            viewModel.addFile(file)
         }
      }

      val stateOutcome = viewModel.uiState.collectAsStateWithLifecycleAndBlinkingPrevention()

      ProgressErrorSuccessScaffold(
         stateOutcome::value,
         errorProgressModifier = Modifier.safeDrawingPadding()
      ) { state ->
         FileListScreenContent(
            state,
            addNewFile = {
               openFileLauncher.launch(arrayOf("text/*"))
            }
         )
      }
   }
}

@Composable
private fun FileListScreenContent(
   state: FileListState,
   addNewFile: () -> Unit,
) {
   Scaffold(
      floatingActionButton = {
         FloatingActionButton(onClick = addNewFile) {
            Text(stringResource(R.string.plus))
         }
      }
   ) { scaffoldPadding ->
      LazyColumn(contentPadding = scaffoldPadding) {
         itemsWithDivider(state.files) { file ->
            Text(
               file.title,
               Modifier
                  .padding(32.dp)
                  .fillMaxWidth()
                  .animateItem()
            )
         }
      }
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "test")
internal fun FileListScreenContentPreview() {
   PreviewTheme {
      FileListScreenContent(
         state = FileListState(
            listOf(
               SyncingFile("File A", ""),
               SyncingFile("File B", ""),
            )
         ),
         addNewFile = {},
      )
   }
}
