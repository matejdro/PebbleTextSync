package com.matejdro.pebbletextsync.network.services

import com.matejdro.pebbletextsync.network.test.serviceFactory
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Test
import retrofit2.http.GET
import si.inova.kotlinova.retrofit.createJsonMockResponse
import si.inova.kotlinova.retrofit.mockWebServer

class BaseServiceFactoryTest {
   @Test
   internal fun `Create basic service that returns data`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = serviceFactory(this@runTest).create()

         mockResponse("/data") {
            createJsonMockResponse("\"Hello\"")
         }

         service.getResult() shouldBe "Hello"
      }
   }

   @Test
   internal fun `Use modified okHttp client when okHttp clause is used`() = runTest {
      mockWebServer {
         val service: TestRetrofitService = serviceFactory(this@runTest).create {
            okHttp {
               addNetworkInterceptor {
                  it.proceed(it.request()).newBuilder().body("\"World\"".toResponseBody()).build()
               }
            }
         }

         mockResponse("/data") {
            createJsonMockResponse("\"Hello\"")
         }

         service.getResult() shouldBe "World"
      }
   }

   interface TestRetrofitService {
      @GET("/data")
      suspend fun getResult(): String
   }
}
