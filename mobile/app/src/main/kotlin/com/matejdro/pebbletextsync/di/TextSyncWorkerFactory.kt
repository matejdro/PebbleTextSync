package com.matejdro.pebbletextsync.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlin.reflect.KClass

@ContributesBinding(AppScope::class)
class TextSyncWorkerFactory(
   private val factories: Map<KClass<out ListenableWorker>, (WorkerParameters) -> ListenableWorker>,
) : WorkerFactory() {
   override fun createWorker(
      appContext: Context,
      workerClassName: String,
      workerParameters: WorkerParameters,
   ): ListenableWorker? {
      val factory = factories[Class.forName(workerClassName).kotlin] ?: return null
      return factory(workerParameters)
   }
}
