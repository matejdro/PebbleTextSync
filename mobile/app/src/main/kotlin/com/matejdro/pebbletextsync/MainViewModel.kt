package com.matejdro.pebbletextsync

import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matejdro.pebbletextsync.home.HomeScreenKey
import com.matejdro.pebbletextsync.home.OnboardingScreenKey
import com.matejdro.pebbletextsync.navigation.keys.FileListScreenKey
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@AssistedInject
class MainViewModel(
   private val preferences: DataStore<Preferences>,
) : ViewModel() {
   private val _startingScreens = MutableStateFlow<List<ScreenKey>>(emptyList())
   val startingScreens: StateFlow<List<ScreenKey>> = _startingScreens

   init {
      viewModelScope.launch {
         _startingScreens.value = if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            preferences.data.first()[onboardingShownVersion] == LATEST_VERSION
         ) {
            listOf(HomeScreenKey, FileListScreenKey)
         } else {
            preferences.edit {
               it[onboardingShownVersion] = LATEST_VERSION
            }

            listOf(OnboardingScreenKey)
         }
      }
   }

   @AssistedFactory
   fun interface Factory {
      fun create(): MainViewModel
   }
}

private const val LATEST_VERSION = 1

private val onboardingShownVersion = intPreferencesKey("onboarding_shown_version")
