package com.matejdro.pebbletextsync.files.ui.details

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.pebbletextsync.files.SyncingFile
import com.matejdro.pebbletextsync.files.ui.FileDetailsScreenKey
import com.matejdro.pebbletextsync.files.ui.R
import com.matejdro.pebbletextsync.files.ui.details.util.MaxStringSizeBytesInputTransformation
import com.matejdro.pebbletextsync.files.ui.errors.filesUserFriendlyMessage
import com.matejdro.pebbletextsync.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.pebbletextsync.ui.debugging.FullScreenPreviews
import com.matejdro.pebbletextsync.ui.debugging.PreviewTheme
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import kotlin.math.roundToInt

@InjectNavigationScreen
class FileDetailsScreen(
   private val viewModel: FileDetailsViewModel,
) : Screen<FileDetailsScreenKey>() {
   @Composable
   override fun Content(key: FileDetailsScreenKey) {
      val stateOutcome = viewModel.uiState.collectAsStateWithLifecycleAndBlinkingPrevention()
      val context = LocalContext.current

      ProgressErrorSuccessScaffold(
         stateOutcome::value,
         errorProgressModifier = Modifier.safeDrawingPadding(),
         errorText = { it.filesUserFriendlyMessage() }
      ) { state ->
         FileDetailsScreenContent(
            state,
            openSlotsInfo = {
               context.startActivity(
                  Intent(
                     Intent.ACTION_VIEW,
                     "https://github.com/matejdro/PebbleTextSync#slots".toUri()
                  )
               )
            },
            editName = viewModel::updateName,
            editSlots = viewModel::updateSlots,
            delete = viewModel::delete,
         )
      }
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileDetailsScreenContent(
   state: FileDetailsState,
   openSlotsInfo: () -> Unit,
   editName: (String) -> Unit,
   editSlots: (Int) -> Unit,
   delete: () -> Unit,
) {
   Column(
      Modifier
         .fillMaxWidth()
         .verticalScroll(state = rememberScrollState())
         .safeDrawingPadding()
         .padding(8.dp)
   ) {
      val titleState = rememberTextFieldState(state.file.title)
      val inputTransformation = remember { MaxStringSizeBytesInputTransformation(SyncingFile.MAX_TITLE_LENGTH_BYTES) }
      TextField(
         titleState,
         label = { Text(stringResource(R.string.name)) },
         modifier = Modifier.fillMaxWidth(),
         inputTransformation = inputTransformation
      )

      LaunchedEffect(titleState.text) {
         editName(titleState.text.toString())
      }

      Row(Modifier.padding(top = 16.dp), verticalAlignment = Alignment.Bottom) {
         Text(
            stringResource(R.string.slots),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
         )

         Button(onClick = openSlotsInfo) {
            Icon(painterResource(R.drawable.ic_question), contentDescription = stringResource(R.string.what_is_that))
         }
      }

      var currentSlots by remember { mutableFloatStateOf(state.file.slots.toFloat()) }
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
         Text(currentSlots.roundToInt().toString(), Modifier.defaultMinSize(minWidth = 30.dp))

         Slider(
            currentSlots,
            onValueChange = { newValue ->
               currentSlots = newValue
               editSlots(newValue.roundToInt())
            },
            valueRange = 1f..SyncingFile.MAX_SLOTS.toFloat(),
            steps = SyncingFile.MAX_SLOTS,
            modifier = Modifier.weight(1f)
         )
      }

      Button(
         onClick = delete,
         colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
         modifier = Modifier
            .padding(top = 32.dp, end = 16.dp)
            .fillMaxWidth()
            .wrapContentWidth(align = Alignment.End)
      ) {
         Text(stringResource(R.string.delete))
      }
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "test")
internal fun FileDetailsScreenPreview() {
   PreviewTheme {
      FileDetailsScreenContent(
         FileDetailsState(
            SyncingFile(
               "Shopping list",
               "content://",
               id = 1,
               slots = 7,
               orderIndex = 0
            )
         ),
         {},
         {},
         {},
         {}
      )
   }
}
