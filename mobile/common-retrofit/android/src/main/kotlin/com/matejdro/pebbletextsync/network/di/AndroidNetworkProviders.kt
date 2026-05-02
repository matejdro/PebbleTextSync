package com.matejdro.pebbletextsync.network.di

import android.content.Context
import com.matejdro.pebbletextsync.network.services.AndroidServiceFactory
import com.matejdro.pebbletextsync.network.services.ServiceFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.retrofit.caching.GlobalOkHttpDiskCacheManager

@ContributesTo(AppScope::class)
interface AndroidNetworkProviders {
   @Provides
   fun bindToServiceFactory(androidServiceFactory: AndroidServiceFactory): ServiceFactory = androidServiceFactory

   @Provides
   fun provideDiskCacheManager(
      context: Context,
      errorReporter: ErrorReporter,
   ): GlobalOkHttpDiskCacheManager {
      return GlobalOkHttpDiskCacheManager(context, errorReporter)
   }
}
