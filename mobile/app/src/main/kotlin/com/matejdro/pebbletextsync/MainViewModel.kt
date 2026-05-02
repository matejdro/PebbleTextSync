package com.matejdro.pebbletextsync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matejdro.pebbletextsync.home.HomeScreenKey
import com.matejdro.pebbletextsync.navigation.keys.FileListScreenKey
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@AssistedInject
class MainViewModel : ViewModel() {
   private val _startingScreens = MutableStateFlow<List<ScreenKey>>(emptyList())
   val startingScreens: StateFlow<List<ScreenKey>> = _startingScreens

   init {
      viewModelScope.launch {
         _startingScreens.value = listOf(HomeScreenKey, FileListScreenKey)
      }
   }

   @AssistedFactory
   fun interface Factory {
      fun create(): MainViewModel
   }
}
