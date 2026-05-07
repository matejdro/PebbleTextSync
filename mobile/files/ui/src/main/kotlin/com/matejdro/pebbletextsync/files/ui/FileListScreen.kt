package com.matejdro.pebbletextsync.files.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.pebbletextsync.ui.debugging.FullScreenPreviews
import com.matejdro.pebbletextsync.ui.debugging.PreviewTheme
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

      FileListScreenContent(
         addNewFile = {
            openFileLauncher.launch(arrayOf("text/*"))
         }
      )
   }
}

@Composable
private fun FileListScreenContent(
   addNewFile: () -> Unit,
) {
   Scaffold(
      floatingActionButton = {
         FloatingActionButton(onClick = addNewFile) {
            Text(stringResource(R.string.plus))
         }
      }
   ) { _ ->
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "test")
internal fun FileListScreenContentPreview() {
   PreviewTheme {
      FileListScreenContent(
         addNewFile = {},
      )
   }
}
