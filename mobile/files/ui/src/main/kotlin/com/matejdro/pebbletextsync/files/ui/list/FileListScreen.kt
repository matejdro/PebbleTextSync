package com.matejdro.pebbletextsync.files.ui.list

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.pebbletextsync.files.SyncingFile
import com.matejdro.pebbletextsync.files.ui.FileDetailsScreenKey
import com.matejdro.pebbletextsync.files.ui.FileListScreenKey
import com.matejdro.pebbletextsync.files.ui.R
import com.matejdro.pebbletextsync.files.ui.list.util.ReorderableListContainer
import com.matejdro.pebbletextsync.navigation.instructions.OpenScreenOrReplaceExistingType
import com.matejdro.pebbletextsync.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.pebbletextsync.ui.debugging.FullScreenPreviews
import com.matejdro.pebbletextsync.ui.debugging.PreviewTheme
import si.inova.kotlinova.compose.components.itemsWithDivider
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class FileListScreen(
   private val viewModel: FileListViewModel,
   private val navigator: Navigator,
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
         val context = LocalContext.current
         val resources = LocalResources.current

         FileListScreenContent(
            state,
            addNewFile = {
               openFileLauncher.launch(arrayOf("text/*"))
            },
            reorder = viewModel::reorder,
            openDetails = {
               navigator.navigate(OpenScreenOrReplaceExistingType(FileDetailsScreenKey(it)))
            },
            sync = {
               viewModel.syncAll()

               Toast.makeText(
                  context,
                  resources.getString(R.string.refreshing_file_contents),
                  Toast.LENGTH_SHORT
               ).show()
            },
         )
      }
   }
}

@Composable
private fun FileListScreenContent(
   state: FileListState,
   addNewFile: () -> Unit,
   openDetails: (id: Int) -> Unit,
   reorder: (id: Int, toIndex: Int) -> Unit,
   sync: () -> Unit,
) {
   Scaffold(
      floatingActionButton = {
         FloatingActionButton(onClick = addNewFile) {
            Text(stringResource(R.string.plus))
         }
      }
   ) { scaffoldPadding ->
      val listState = rememberLazyListState()
      ReorderableListContainer(state.files, listState) { items ->
         val topWindowOffset = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
         val refreshState = rememberPullToRefreshState()

         Box(
            Modifier.pullToRefresh(
               isRefreshing = state.syncing,
               onRefresh = sync,
               threshold = topWindowOffset + 48.dp,
               state = refreshState,
            )
         ) {
            LazyColumn(contentPadding = scaffoldPadding, state = listState, modifier = Modifier.fillMaxSize()) {
               itemsWithDivider(items, key = { it.id }) { file ->
                  ReorderableListItem(file.id, file, setOrder = reorder) { modifier, _ ->
                     Text(
                        file.title,
                        modifier
                           .padding(32.dp)
                           .fillMaxWidth()
                           .animateItem()
                           .clickable(onClick = { openDetails(file.id) })
                     )
                  }
               }
            }

            PullToRefreshDefaults.Indicator(
               state = refreshState,
               modifier = Modifier.align(Alignment.TopCenter),
               isRefreshing = state.syncing,
               maxDistance = topWindowOffset + 48.dp,
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
            ),
            syncing = false,
         ),
         addNewFile = {},
         openDetails = {},
         reorder = { _, _ -> },
         sync = {},
      )
   }
}
