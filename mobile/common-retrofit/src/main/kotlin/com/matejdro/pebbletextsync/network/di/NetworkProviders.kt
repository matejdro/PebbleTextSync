package com.matejdro.pebbletextsync.network.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import okhttp3.OkHttpClient
import si.inova.kotlinova.retrofit.interceptors.BypassCacheInterceptor
import java.time.Duration
import kotlin.reflect.KClass

@ContributesTo(AppScope::class)
interface NetworkProviders {
   // Uncomment when adding adapters
   @Multibinds(allowEmpty = true)
   val moshiAdapters: Map<KClass<*>, KSerializer<*>>

   @Provides
   @SingleIn(AppScope::class)
   fun provideJson(
      adapters: Map<KClass<*>, KSerializer<*>>,
   ): Json {
      if (Thread.currentThread().name == "main") {
         error("Kotlinx serialization should not be initialized on the main thread")
      }

      return createJson(adapters)
   }

   @Provides
   @SingleIn(AppScope::class)
   fun provideOkHttpClient(): OkHttpClient {
      if (Thread.currentThread().name == "main") {
         error("OkHttp should not be initialized on the main thread")
      }

      return prepareDefaultOkHttpClient().build()
   }

   companion object {
      fun prepareDefaultOkHttpClient(): OkHttpClient.Builder {
         return OkHttpClient.Builder()
            .addInterceptor(BypassCacheInterceptor())
            .callTimeout(DEFAULT_TIMEOUT)
            .readTimeout(DEFAULT_TIMEOUT)
            .writeTimeout(DEFAULT_TIMEOUT)
            .connectTimeout(DEFAULT_TIMEOUT)
      }

      fun createJson(
         adapters: Map<KClass<*>, KSerializer<*>>,
      ): Json {
         if (Thread.currentThread().name == "main") {
            error("Kotlinx serialization should not be initialized on the main thread")
         }

         return Json {
            serializersModule = SerializersModule {
               for ((klas, adapter) in adapters) {
                  @Suppress("UNCHECKED_CAST")
                  contextual(klas as KClass<Any>, adapter as KSerializer<Any>)
               }
            }

            ignoreUnknownKeys = true
         }
      }
   }
}

private val DEFAULT_TIMEOUT = Duration.ofSeconds(10)
