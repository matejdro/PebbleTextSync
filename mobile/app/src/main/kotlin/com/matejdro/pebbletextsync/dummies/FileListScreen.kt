package com.matejdro.pebbletextsync.dummies

import androidx.compose.runtime.Composable
import com.matejdro.pebbletextsync.navigation.keys.FileListScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class FileListScreen : Screen<FileListScreenKey>() {
   @Composable
   override fun Content(key: FileListScreenKey) {
   }
}
