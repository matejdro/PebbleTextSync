package com.matejdro.pebbletextsync.tools.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.matejdro.pebbletextsync.home.OnboardingScreenKey
import com.matejdro.pebbletextsync.ui.components.ErrorAlertDialog
import com.matejdro.pebbletextsync.ui.components.ProgressErrorSuccessScaffold
import com.matejdro.pebbletextsync.ui.debugging.FullScreenPreviews
import com.matejdro.pebbletextsync.ui.debugging.PreviewTheme
import com.matejdro.tools.PebbleFont
import com.matejdro.tools.PreferenceKeys
import com.matejdro.tools.ui.ToolsScreenKey
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ListPreferenceType
import me.zhanghai.compose.preference.LocalPreferenceTheme
import me.zhanghai.compose.preference.preferenceTheme
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.di.ContributesScreenBinding
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import kotlin.enums.enumEntries

@InjectNavigationScreen
@ContributesScreenBinding
class ToolsScreen(
   private val navigator: Navigator,
   private val viewModel: ToolsViewModel,
) : Screen<ToolsScreenKey>() {
   @Composable
   override fun Content(key: ToolsScreenKey) {
      val stateOutcome = viewModel.appVersion.collectAsStateWithLifecycle()
      val logSaveStatus = viewModel.logSave.collectAsStateWithLifecycleAndBlinkingPrevention().value

      ProgressErrorSuccessScaffold(
         stateOutcome::value,
         Modifier
            .fillMaxSize()
            .safeDrawingPadding()
      ) { state ->
         ToolsScreenContent(
            state = state,
            loggingTransmissionState = logSaveStatus,
            openPermissions = {
               navigator.navigateTo(OnboardingScreenKey)
            },
            startLogSaving = viewModel::getLogs,
            notifyLogIntentSent = viewModel::resetLog,
            updatePreference = { prefKey, value ->
               @Suppress("UNCHECKED_CAST")
               viewModel.updatePreference(prefKey as Preferences.Key<Any>, value)
            }
         )
      }
   }
}

@Composable
private fun ToolsScreenContent(
   state: ToolsState,
   loggingTransmissionState: Outcome<Uri?>?,
   openPermissions: () -> Unit,
   startLogSaving: () -> Unit,
   notifyLogIntentSent: () -> Unit,
   updatePreference: (key: Preferences.Key<*>, value: Any) -> Unit,
) {
   CompositionLocalProvider(LocalPreferenceTheme provides preferenceTheme()) {
      LazyVerticalGrid(
         GridCells.Adaptive(160.dp),
         horizontalArrangement = Arrangement.spacedBy(16.dp),
         verticalArrangement = Arrangement.spacedBy(16.dp),
         contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
         modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
      ) {
         item {
            ToolButton(onClick = openPermissions, icon = R.drawable.permissions, text = R.string.permissions)
         }

         item {
            ErrorAlertDialog(loggingTransmissionState)

            if (loggingTransmissionState is Outcome.Progress<*>) {
               CircularProgressIndicator(
                  Modifier
                     .size(60.dp)
                     .wrapContentWidth()
               )
            } else {
               ToolButton(onClick = startLogSaving, icon = R.drawable.logs, text = R.string.save_logs)

               val context = LocalContext.current
               SideEffect {
                  loggingTransmissionState?.data?.let { targetUri ->
                     val activityIntent = Intent(Intent.ACTION_SEND)
                     activityIntent.putExtra(Intent.EXTRA_STREAM, targetUri)
                     activityIntent.setType("application/zip")

                     activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                     activityIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                     context.startActivity(Intent.createChooser(activityIntent, null))
                     notifyLogIntentSent()
                  }
               }
            }
         }

         item(span = { GridItemSpan(maxLineSpan) }) {
            FontPreference(state, updatePreference)
         }

         item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
               stringResource(R.string.version, state.versionName),
               Modifier
                  .fillMaxWidth()
                  .wrapContentWidth(Alignment.CenterHorizontally)
            )
         }
      }
   }
}

@Composable
private fun FontPreference(
   state: ToolsState,
   updatePreference: (Preferences.Key<*>, Any) -> Unit,
) {
   val fontNames = remember {
      PebbleFont.entries.map { font ->
         font.name.split("_").joinToString(" ") { name -> name.lowercase().replaceFirstChar { it.uppercase() } }
      }
   }

   val enumEntries = enumEntries<PebbleFont>()
   val selectedValue = state.preferences[PreferenceKeys.TEXT_FONT]?.let { enumValueOf<PebbleFont>(it) }
      ?: PreferenceKeys.TEXT_FONT_DEFAULT

   ListPreference(
      selectedValue.name,
      {
         updatePreference(PreferenceKeys.TEXT_FONT, it)
      },
      enumEntries.map { it.name },
      { Text(stringResource(R.string.text_font)) },
      summary = { Text(fontNames.elementAt(selectedValue.ordinal)) },
      valueToText = { enumName ->
         AnnotatedString(
            fontNames.elementAt(
               enumValueOf<PebbleFont>(enumName).ordinal
            )
         )
      },
      type = ListPreferenceType.ALERT_DIALOG
   )
}

@Composable
private fun ToolButton(onClick: () -> Unit, icon: Int, text: Int) {
   Button(onClick = onClick, Modifier.sizeIn(minHeight = 60.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
         Icon(
            painterResource(icon),
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
         )

         Text(stringResource(text))
      }
   }
}

@FullScreenPreviews
@Composable
@ShowkaseComposable(group = "test")
internal fun ToolsScreenPreview() {
   PreviewTheme {
      ToolsScreenContent(
         state = ToolsState("1.0.0-alpha07", emptyPreferences()),
         loggingTransmissionState = Outcome.Success(null),
         openPermissions = {},
         startLogSaving = {},
         notifyLogIntentSent = {},
         updatePreference = { _, _ -> }
      )
   }
}
