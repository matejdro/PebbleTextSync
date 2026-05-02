package com.matejdro.pebbletextsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.matejdro.pebbletextsync.navigation.scenes.ListDetailScene
import com.matejdro.pebbletextsync.navigation.scenes.rememberListDetailSceneStrategy
import com.matejdro.pebbletextsync.navigation.scenes.rememberTabListSceneDecoratorStrategy
import com.matejdro.pebbletextsync.ui.theme.TextSyncTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import logcat.logcat
import si.inova.kotlinova.compose.result.LocalResultPassingStore
import si.inova.kotlinova.compose.result.ResultPassingStore
import si.inova.kotlinova.compose.time.ComposeAndroidDateTimeFormatter
import si.inova.kotlinova.compose.time.LocalDateFormatter
import si.inova.kotlinova.core.time.AndroidDateTimeFormatter
import si.inova.kotlinova.navigation.backstack.Backstack
import si.inova.kotlinova.navigation.deeplink.HandleNewIntentDeepLinks
import si.inova.kotlinova.navigation.deeplink.MainDeepLinkHandler
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.navigation3.NavDisplay
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

class MainActivity : ComponentActivity() {
   private lateinit var navigationInjectionFactory: NavigationInjection.Factory
   private lateinit var mainDeepLinkHandler: MainDeepLinkHandler
   private lateinit var navigationContext: NavigationContext
   private lateinit var dateFormatter: AndroidDateTimeFormatter
   private lateinit var mainViewModelFactory: MainViewModel.Factory
   private lateinit var listDetailSceneFactory: ListDetailScene.Factory

   private val viewModel by viewModels<MainViewModel>() { ViewModelFactory() }
   private var initComplete = false

   override fun onCreate(savedInstanceState: Bundle?) {
      val appGraph = (requireNotNull(application) as TextSyncApplication).applicationGraph

      navigationInjectionFactory = appGraph.getNavigationInjectionFactory()
      mainDeepLinkHandler = appGraph.getMainDeepLinkHandler()
      navigationContext = appGraph.getNavigationContext()
      dateFormatter = appGraph.getDateFormatter()
      mainViewModelFactory = appGraph.getMainViewModelFactory()
      listDetailSceneFactory = appGraph.getListDetailSceneFactory()

      super.onCreate(savedInstanceState)
      enableEdgeToEdge()

      val splashScreen = installSplashScreen()
      splashScreen.setKeepOnScreenCondition { !initComplete }

      beginInitialisation(savedInstanceState == null)
   }

   private fun beginInitialisation(startup: Boolean) {
      lifecycleScope.launch {
         val initialHistory: List<ScreenKey> = viewModel.startingScreens.first { it.isNotEmpty() }

         val deepLinkTarget = if (startup) {
            intent?.data?.let { mainDeepLinkHandler.handleDeepLink(it, startup = true) }
         } else {
            null
         }

         val overridenInitialHistoryFromDeepLink = if (deepLinkTarget != null) {
            deepLinkTarget.performNavigation(initialHistory, navigationContext).newBackstack
         } else {
            initialHistory
         }

         setContent {
            NavigationRoot(overridenInitialHistoryFromDeepLink)
         }

         initComplete = true
      }
   }

   @Composable
   private fun NavigationRoot(initialHistory: List<ScreenKey>) {
      TextSyncTheme {
         // A surface container using the 'background' color from the theme
         Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
         ) {
            val resultPassingStore = rememberSaveable { ResultPassingStore() }
            CompositionLocalProvider(
               LocalDateFormatter provides ComposeAndroidDateTimeFormatter(dateFormatter),
               LocalResultPassingStore provides resultPassingStore
            ) {
               val backstack = navigationInjectionFactory.NavDisplay(
                  initialHistory = { initialHistory },
                  entryDecorators = listOf(
                     rememberSaveableStateHolderNavEntryDecorator(),
                     NavEntryDecorator<ScreenKey>(
                        decorate = { targetNavEntry ->
                           Surface {
                              targetNavEntry.Content()
                           }
                        }
                     )
                  ),
                  sceneStrategies = listOf(rememberListDetailSceneStrategy(listDetailSceneFactory)),
                  sceneDecoratorStrategies = listOf(rememberTabListSceneDecoratorStrategy())
               )

               LogCurrentScreen(backstack)

               mainDeepLinkHandler.HandleNewIntentDeepLinks(this@MainActivity, backstack)
            }
         }
      }
   }

   private inner class ViewModelFactory : ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
         @Suppress("UNCHECKED_CAST")
         return mainViewModelFactory.create() as T
      }
   }
}

@Composable
private fun LogCurrentScreen(backstack: Backstack) {
   val topScreenFlow = remember(backstack) {
      backstack.backstack
         .map { list -> list.lastOrNull() }
   }

   val newTopKey = topScreenFlow.collectAsStateWithLifecycle(null).value

   SideEffect {
      logcat("MainActivity") { "Switched to ${newTopKey ?: "null"}" }
   }
}
