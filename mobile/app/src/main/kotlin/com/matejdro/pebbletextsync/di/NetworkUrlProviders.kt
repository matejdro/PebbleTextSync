package com.matejdro.pebbletextsync.di

import com.matejdro.pebbletextsync.network.services.BaseServiceFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
interface NetworkUrlProviders {
   @Provides
   @BaseServiceFactory.BaseUrl
   fun provideBaseUrl(): String {
      return "https://dummyjson.com/"
   }
}
