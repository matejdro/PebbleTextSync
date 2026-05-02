package com.matejdro.pebbletextsync.network.services

import com.matejdro.pebbletextsync.network.exceptions.DefaultErrorHandler
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dispatch.core.DefaultCoroutineScope
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.retrofit.caching.GlobalOkHttpDiskCacheManager

@Inject
class AndroidServiceFactory(
   moshi: Provider<Json>,
   errorReporter: ErrorReporter,
   okHttpClient: Provider<OkHttpClient>,
   defaultCoroutineScope: DefaultCoroutineScope,
   defaultErrorHandler: DefaultErrorHandler,
   @BaseUrl
   baseUrl: String,
   private val cacheManager: GlobalOkHttpDiskCacheManager,
) : BaseServiceFactory(defaultCoroutineScope, moshi, okHttpClient, errorReporter, defaultErrorHandler, baseUrl) {
   override fun createCache(): Cache {
      return cacheManager.cache
   }
}
