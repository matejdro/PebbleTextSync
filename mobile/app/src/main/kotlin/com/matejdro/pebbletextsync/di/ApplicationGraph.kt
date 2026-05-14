package com.matejdro.pebbletextsync.di

import android.app.Application
import androidx.work.WorkerFactory
import com.matejdro.pebble.common.logging.FileLoggingController
import com.matejdro.pebble.common.logging.TinyLogLoggingThread
import com.matejdro.pebbletextsync.MainViewModel
import com.matejdro.pebbletextsync.bluetooth.WatchSyncerImpl
import com.matejdro.pebbletextsync.navigation.scenes.ListDetailScene
import com.matejdro.pebbletextsync.receiving.PebbleListenerService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dispatch.core.DefaultCoroutineScope
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.core.time.AndroidDateTimeFormatter
import si.inova.kotlinova.navigation.deeplink.MainDeepLinkHandler
import si.inova.kotlinova.navigation.di.NavigationContext
import si.inova.kotlinova.navigation.di.NavigationInjection
import si.inova.kotlinova.navigation.di.OuterNavigationScope

@DependencyGraph(AppScope::class, additionalScopes = [OuterNavigationScope::class])
interface MainApplicationGraph : ApplicationGraph {
   @DependencyGraph.Factory
   interface Factory {
      fun create(
         @Provides
         application: Application,
      ): MainApplicationGraph
   }
}

@Suppress("ComplexInterface") // DI
interface ApplicationGraph {
   fun getErrorReporter(): ErrorReporter
   fun getDefaultCoroutineScope(): DefaultCoroutineScope
   fun getNavigationInjectionFactory(): NavigationInjection.Factory
   fun getMainDeepLinkHandler(): MainDeepLinkHandler
   fun getNavigationContext(): NavigationContext
   fun getDateFormatter(): AndroidDateTimeFormatter
   fun getMainViewModelFactory(): MainViewModel.Factory
   fun getListDetailSceneFactory(): ListDetailScene.Factory
   fun getFileLoggingController(): FileLoggingController
   fun getTinyLogLoggingThread(): TinyLogLoggingThread
   fun getWorkerFactory(): WorkerFactory
   fun getWatchSyncer(): WatchSyncerImpl
   fun inject(service: PebbleListenerService)
}
