package com.matejdro.pebbletextsync.dummies

import androidx.compose.runtime.Composable
import com.matejdro.pebbletextsync.navigation.keys.ToolsScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class ToolsScreen : Screen<ToolsScreenKey>() {
   @Composable
   override fun Content(key: ToolsScreenKey) {
   }
}
