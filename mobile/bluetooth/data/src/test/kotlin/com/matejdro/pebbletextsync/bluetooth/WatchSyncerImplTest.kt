package com.matejdro.pebbletextsync.bluetooth

import com.matejdro.bucketsync.FakeBucketSyncRepository
import com.matejdro.bucketsync.api.Bucket
import com.matejdro.bucketsync.api.BucketUpdate
import com.matejdro.pebbletextsync.bluetooth.util.FileContentsReader
import com.matejdro.pebbletextsync.files.FakeSyncingFileRepository
import com.matejdro.pebbletextsync.files.SyncingFile
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class WatchSyncerImplTest {
   private val scope = TestScope()

   private val fileReader = FileContentsReader { uri, _ ->
      when (uri.toString()) {
         "content://1" -> "Short"
         "content://2" -> "A".repeat(1000)
         else -> "Uri $uri not faked"
      }
   }

   private val fileRepo = FakeSyncingFileRepository()
   private val bucketsyncRepo = FakeBucketSyncRepository(BUCKET_DATA_VERSION.toInt())

   private val watchSyncer = WatchSyncerImpl(
      bucketsyncRepo,
      fileRepo,
      fileReader,
      {},
   )

   @Test
   fun `Sync a file`() = scope.runTest {
      standardInit()

      fileRepo.insert(
         SyncingFile("Title", "content://1"),
      )
      watchSyncer.syncFile(1)
      delay(1.seconds)

      bucketsyncRepo.checkForNextUpdate(0u, emptyList()) shouldBe BucketUpdate(
         1u,
         listOf(1u),
         listOf(
            Bucket(
               1u,
               byteArrayOf(
                  // UTF8 Bytes for the title, followed by null terminator
                  'T'.code.toByte(),
                  'i'.code.toByte(),
                  't'.code.toByte(),
                  'l'.code.toByte(),
                  'e'.code.toByte(),
                  0,

                  0, // ID of the next bucket (null in this case)

                  // UTF8 Bytes for the body, NOT followed by null terminator
                  'S'.code.toByte(),
                  'h'.code.toByte(),
                  'o'.code.toByte(),
                  'r'.code.toByte(),
                  't'.code.toByte(),
               )
            )
         )
      )
   }

   @Test
   fun `Crop a file with the long text`() = scope.runTest {
      standardInit()

      fileRepo.insert(
         SyncingFile("Title", "content://2"),
      )
      watchSyncer.syncFile(1)
      delay(1.seconds)

      bucketsyncRepo.checkForNextUpdate(0u, emptyList()) shouldBe BucketUpdate(
         1u,
         listOf(1u),
         listOf(
            Bucket(
               1u,
               byteArrayOf(
                  // UTF8 Bytes for the title, followed by null terminator
                  'T'.code.toByte(),
                  'i'.code.toByte(),
                  't'.code.toByte(),
                  'l'.code.toByte(),
                  'e'.code.toByte(),
                  0,
                  0, // ID of the next bucket (null in this case)
               ) +
                  // A character, repeated until the end of the bucket
                  ByteArray(255 - 7) { 'A'.code.toByte() },
            )
         )
      )
   }

   @Test
   fun `Limit title size`() = scope.runTest {
      standardInit()

      fileRepo.insert(
         SyncingFile("B".repeat(100), "content://1"),
      )
      watchSyncer.syncFile(1)
      delay(1.seconds)

      bucketsyncRepo.checkForNextUpdate(0u, emptyList()) shouldBe BucketUpdate(
         1u,
         listOf(1u),
         listOf(
            Bucket(
               1u,
               // B character, repeated only 20x
               ByteArray(20) { 'B'.code.toByte() } +
                  byteArrayOf(
                     // Null terminator
                     0,
                     0, // ID of the next bucket (null in this case)
                     // UTF8 Bytes for the body, NOT followed by null terminator
                     'S'.code.toByte(),
                     'h'.code.toByte(),
                     'o'.code.toByte(),
                     'r'.code.toByte(),
                     't'.code.toByte(),
                  ),
            )
         )
      )
   }

   @Test
   fun `Sync a file deletion`() = scope.runTest {
      standardInit()

      fileRepo.insert(SyncingFile("Title", "content://1"))
      watchSyncer.syncFile(1)
      delay(1.seconds)

      fileRepo.delete(1)
      watchSyncer.syncFile(1)
      delay(1.seconds)

      bucketsyncRepo.checkForNextUpdate(1u, emptyList()) shouldBe BucketUpdate(
         2u,
         emptyList(),
         emptyList(),
      )
   }

   @Test
   fun `Do not do anything on init when repo version remains the same`() = scope.runTest {
      fileRepo.insert(SyncingFile("Title", "content://1"))
      fileRepo.insert(SyncingFile("Title 2", "content://1"))
      delay(1.seconds)

      standardInit()

      bucketsyncRepo.checkForNextUpdate(0u, emptyList()) shouldBe null
   }

   @Test
   fun `Resync all files on startup when bucket version changes`() = scope.runTest {
      fileRepo.insert(SyncingFile("Title", "content://1"))
      fileRepo.insert(SyncingFile("Title 2", "content://1"))
      runCurrent()

      val bucketSyncRepositoryWithOldVersion = FakeBucketSyncRepository(0)

      val watchSyncer = WatchSyncerImpl(
         bucketSyncRepositoryWithOldVersion,
         fileRepo,
         fileReader,
         {},
      )

      watchSyncer.init()
      delay(1.seconds)

      bucketSyncRepositoryWithOldVersion.checkForNextUpdate(1u, emptyList())?.emptyBucketData() shouldBe BucketUpdate(
         2u,
         listOf(1u, 2u),
         listOf(Bucket(2u, ByteArray(0)), Bucket(1u, ByteArray(0)))
      )
   }

   @Test
   fun `Sync a file into two buckets`() = scope.runTest {
      standardInit()

      fileRepo.insert(
         SyncingFile("Title", "content://2", slots = 2),
      )
      watchSyncer.syncFile(1)
      delay(1.seconds)

      bucketsyncRepo.checkForNextUpdate(0u, emptyList()) shouldBe BucketUpdate(
         2u,
         listOf(2u, 1u),
         listOf(
            Bucket(
               1u,
               // A character, repeated for the entire bucket
               byteArrayOf(
                  0, // ID of the next bucket (null in this case)
               ) +
                  ByteArray(254) { 'A'.code.toByte() },
            ),
            Bucket(
               2u,
               byteArrayOf(
                  // UTF8 Bytes for the title, followed by null terminator
                  'T'.code.toByte(),
                  'i'.code.toByte(),
                  't'.code.toByte(),
                  'l'.code.toByte(),
                  'e'.code.toByte(),
                  0,
                  1, // ID of the next bucket
               ) +
                  // A character, repeated until the end of the bucket
                  ByteArray(255 - 7) { 'A'.code.toByte() }
            ),
         ),
         activeBucketFlags = listOf(
            0u, // First bucket of the file
            1u // Subsequent bucket of the file
         )
      )
   }

   @Test
   fun `Delete excess buckets after reducing number of slots`() = scope.runTest {
      standardInit()

      fileRepo.insert(
         SyncingFile("Title", "content://2", slots = 2),
      )
      watchSyncer.syncFile(1)
      delay(1.seconds)

      fileRepo.update(
         SyncingFile("Title", "content://2", slots = 1, id = 1),
      )
      watchSyncer.syncFile(1)
      delay(1.seconds)

      bucketsyncRepo.checkForNextUpdate(0u, emptyList()) shouldBe BucketUpdate(
         4u,
         listOf(2u),
         listOf(
            Bucket(
               2u,
               byteArrayOf(
                  // UTF8 Bytes for the title, followed by null terminator
                  'T'.code.toByte(),
                  'i'.code.toByte(),
                  't'.code.toByte(),
                  'l'.code.toByte(),
                  'e'.code.toByte(),
                  0,
                  0, // ID of the next bucket
               ) +
                  // A character, repeated until the end of the bucket
                  ByteArray(255 - 7) { 'A'.code.toByte() }
            ),
         ),
         activeBucketFlags = listOf(
            0u, // First bucket of the file
         )
      )
   }

   @Test
   fun `Do not use multiple buckets if the file is shorter than allowed slots`() = scope.runTest {
      standardInit()

      fileRepo.insert(
         SyncingFile("Title", "content://1", slots = 3),
      )

      watchSyncer.syncFile(1)
      delay(1.seconds)

      bucketsyncRepo.checkForNextUpdate(0u, emptyList()).shouldNotBeNull()
         .activeBuckets
         .shouldContainExactly(1u)
   }

   @Test
   fun `Delete all buckets when deleting a file`() = scope.runTest {
      standardInit()

      fileRepo.insert(SyncingFile("Title", "content://2", slots = 3))
      watchSyncer.syncFile(1)
      delay(1.seconds)

      fileRepo.delete(1)
      watchSyncer.syncFile(1)
      delay(1.seconds)

      bucketsyncRepo.checkForNextUpdate(1u, emptyList()) shouldBe BucketUpdate(
         4u,
         emptyList(),
         emptyList(),
      )
   }

   @Test
   fun `Sync all files on syncAll call`() = scope.runTest {
      fileRepo.insert(SyncingFile("Title", "content://1"))
      fileRepo.insert(SyncingFile("Title 2", "content://1"))
      runCurrent()

      val watchSyncer = WatchSyncerImpl(
         bucketsyncRepo,
         fileRepo,
         fileReader,
         {},
      )

      watchSyncer.init()
      watchSyncer.syncAll()
      delay(1.seconds)

      bucketsyncRepo.checkForNextUpdate(1u, emptyList())?.emptyBucketData() shouldBe BucketUpdate(
         2u,
         listOf(1u, 2u),
         listOf(Bucket(2u, ByteArray(0)), Bucket(1u, ByteArray(0)))
      )
   }

   private suspend fun standardInit() {
      watchSyncer.init()
   }

   private fun BucketUpdate.emptyBucketData(): BucketUpdate {
      return copy(
         bucketsToUpdate = bucketsToUpdate.map {
            it.copy(data = ByteArray(0))
         }
      )
   }
}
