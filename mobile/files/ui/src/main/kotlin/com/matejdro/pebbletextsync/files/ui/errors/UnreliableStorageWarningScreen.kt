package com.matejdro.pebbletextsync.files.ui.errors

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.matejdro.pebbletextsync.files.ui.R
import com.matejdro.pebbletextsync.ui.debugging.PreviewTheme
import kotlinx.serialization.Serializable
import si.inova.kotlinova.navigation.instructions.goBack
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screenkeys.ScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class UnreliableStorageWarningScreen(
   private val navigator: Navigator,
) : Screen<UnreliableStorageWarningScreenKey>() {
   @Composable
   override fun Content(key: UnreliableStorageWarningScreenKey) {
      UnreliableStorageWarningScreenContent(navigator::goBack)
   }
}

@Composable
private fun UnreliableStorageWarningScreenContent(dismiss: () -> Unit) {
   AlertDialog(
      onDismissRequest = dismiss,
      confirmButton = { TextButton(onClick = dismiss) { Text(stringResource(android.R.string.ok)) } },
      title = { Text(stringResource(R.string.warning)) },
      text = { Text(stringResource(R.string.unreliable_storage_warning_description)) },
   )
}

@Serializable
data object UnreliableStorageWarningScreenKey : ScreenKey()

@Preview
@Composable
internal fun UnreliableStorageWarningScreenPreview() {
   PreviewTheme {
      UnreliableStorageWarningScreenContent { }
   }
}
