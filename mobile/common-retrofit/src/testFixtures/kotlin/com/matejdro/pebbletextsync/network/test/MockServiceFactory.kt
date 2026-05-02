package com.matejdro.pebbletextsync.network.test

import com.matejdro.pebbletextsync.network.di.NetworkProviders
import com.matejdro.pebbletextsync.network.exceptions.DefaultErrorHandler
import com.matejdro.pebbletextsync.network.services.BaseServiceFactory
import kotlinx.coroutines.test.TestScope
import si.inova.kotlinova.core.test.outcomes.ThrowingErrorReporter
import si.inova.kotlinova.retrofit.MockWebServerScope

fun MockWebServerScope.serviceFactory(testScope: TestScope): BaseServiceFactory {
   val json = NetworkProviders.createJson(emptyMap())

   return BaseServiceFactory(
      testScope,
      { json },
      { NetworkProviders.prepareDefaultOkHttpClient().build() },
      ThrowingErrorReporter(testScope),
      DefaultErrorHandler(),
      baseUrl
   )
}
