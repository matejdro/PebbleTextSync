package com.matejdro.pebbletextsync.network.exceptions

import dev.zacsweers.metro.Inject
import retrofit2.Response
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.retrofit.callfactory.ErrorHandler

@Inject
class DefaultErrorHandler : ErrorHandler {
   override fun generateExceptionFromErrorBody(response: Response<*>, parentException: Exception): CauseException? {
      // TODO
      error("Parse errors from your backend here")
   }
}
