package com.matejdro.pebbletextsync.tasker

import android.os.Bundle
import com.matejdro.pebbletextsync.bluetooth.FakeWatchSyncer
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class TaskerActionRunnerTest {
   private val syncer = FakeWatchSyncer()
   private val runner = TaskerActionRunner(syncer)

   @Test
   fun `Refresh files`() = runTest {
      runner.run(
         Bundle().apply {
            putString(BundleKeys.ACTION, TaskerAction.REFRESH_FILES.name)
         }
      )

      syncer.syncAllCalled shouldBe true
   }
}
