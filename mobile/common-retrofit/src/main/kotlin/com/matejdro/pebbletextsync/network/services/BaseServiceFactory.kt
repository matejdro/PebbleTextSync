package com.matejdro.pebbletextsync.network.services

import com.matejdro.pebbletextsync.network.exceptions.DefaultErrorHandler
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Qualifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import si.inova.kotlinova.core.reporting.ErrorReporter
import si.inova.kotlinova.retrofit.callfactory.ErrorHandlingAdapterFactory
import si.inova.kotlinova.retrofit.callfactory.StaleWhileRevalidateCallAdapterFactory
import si.inova.kotlinova.retrofit.converter.LazyRetrofitConverterFactory

@Inject
open class BaseServiceFactory(
   private val coroutineScope: CoroutineScope,
   private val json: Provider<Json>,
   private val okHttpClient: Provider<OkHttpClient>,
   private val errorReporter: ErrorReporter,
   private val defaultErrorHandler: DefaultErrorHandler,
   @BaseUrl
   private val baseUrl: String,
) : ServiceFactory {
   override fun <S> create(klass: Class<S>, configuration: ServiceFactory.ServiceCreationScope.() -> Unit): S {
      val scope = ServiceFactory.ServiceCreationScope(defaultErrorHandler)
      configuration(scope)

      val updatedClient = lazy {
         okHttpClient().newBuilder()
            .apply {
               if (scope.cache) {
                  @Suppress("MissingUseCall") // Expected in this case, cache is managed by the OKHttp
                  createCache()?.let { cache(it) }
               }
            }
            .apply {
               scope.okHttpCustomizer?.let { it() }
            }
            .build()
      }

      val moshiConverter = lazy {
         json().asConverterFactory(
            "application/json; charset=utf-8".toMediaType()
         )
      }

      return Retrofit.Builder()
         .callFactory { updatedClient.value.newCall(it) }
         .baseUrl(baseUrl)
         .addConverterFactory(LazyRetrofitConverterFactory(moshiConverter))
         .addCallAdapterFactory(StaleWhileRevalidateCallAdapterFactory(scope.errorHandler, errorReporter))
         .addCallAdapterFactory(ErrorHandlingAdapterFactory(coroutineScope, scope.errorHandler))
         .build()
         .create(klass)
   }

   open fun createCache(): Cache? {
      return null
   }

   @Qualifier
   annotation class BaseUrl
}
