package com.matejdro.pebbletextsync.bluetooth

import com.matejdro.bucketsync.FakeBucketSyncRepository
import com.matejdro.bucketsync.api.Bucket
import com.matejdro.bucketsync.api.BucketUpdate
import com.matejdro.pebbletextsync.bluetooth.util.FileContentsReader
import com.matejdro.pebbletextsync.files.FakeSyncingFileRepository
import com.matejdro.pebbletextsync.files.SyncingFile
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

/**
 * Android's CharsetEncoder behaves differently to the one in the JVM: If it runs out of space, it writes partial UTF-8 glyphs
 * which breaks text rendering on the watch.
 *
 * As a workaround, we had to  create a hack to trim these partial glyphs away.
 * This test tests that workaround in the Instrumented test (since it only affects Android, we cannot test on JVM).
 */
class TextEncodingAndroidTest {
   private val scope = TestScope()

   private val fileReader = FileContentsReader { uri, _ ->
      when (uri.toString()) {
         "content://1" -> "A".repeat(247) + "ž"
         "content://2" -> "A".repeat(246) + "ž"
         "content://3" -> "A".repeat(247 + 254) + "ž"
         "content://blank" -> ""
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
   fun doNotLeaveDanglingUtf8CharactersWhenSplittingTexts() = scope.runTest {
      standardInit()

      fileRepo.insert(
         SyncingFile("Title", "content://1", slots = 2),
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
                  0, // ID of the next bucket (null in this case),
                  -59, // UTF-8 glyphs for the 'ž' character
                  -66
               )
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
                  1, // ID of the next bucket,
               ) +
                  // A character, repeated until the end of the bucket
                  ByteArray(255 - 7 - 1) { 'A'.code.toByte() }
            ),
         ),
         activeBucketFlags = listOf(
            0u, // First bucket of the file
            1u // Subsequent bucket of the file
         )
      )
   }

   @Test
   fun doNotAffectEncodingWhenUtfCharacterCompletesNormally() = scope.runTest {
      standardInit()

      fileRepo.insert(
         SyncingFile("Title", "content://2", slots = 2),
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
                  0, // ID of the next bucket,
               ) +
                  // A character, repeated until the end of the bucket + 'ž' character encoded
                  ByteArray(255 - 7 - 2) { 'A'.code.toByte() } + byteArrayOf(-59, -66)
            ),
         ),
         activeBucketFlags = listOf(
            0u, // First bucket of the file
         )
      )
   }

   @Test
   fun doNotLeaveDanglingUtf8CharactersWhenCroppingTitle() = scope.runTest {
      standardInit()

      fileRepo.insert(
         SyncingFile("aaaaaaaaaaaaaaaaaaaž", "content://blank", slots = 1),
      )
      watchSyncer.syncFile(1)
      delay(1.seconds)

      bucketsyncRepo.checkForNextUpdate(0u, emptyList()) shouldBe BucketUpdate(
         1u,
         listOf(1u),
         listOf(
            Bucket(
               1u,
               // UTF8 Bytes for the title - 19x 'a', followed by null characters (ž should be cropped completely)
               ByteArray(19) { 'a'.code.toByte() } +
                  byteArrayOf(
                     0,
                     0, // ID of the next bucket,
                     // Nothing here, there is no text
                  )
            ),
         ),
         activeBucketFlags = listOf(
            0u, // First bucket of the file
         )
      )
   }

   @Test
   fun doNotLeaveDanglingUtf8CharactersWhenSplittingTextsInto3Buckets() = scope.runTest {
      standardInit()

      fileRepo.insert(
         SyncingFile("Title", "content://3", slots = 3),
      )
      watchSyncer.syncFile(1)
      delay(1.seconds)

      bucketsyncRepo.checkForNextUpdate(0u, emptyList())
         .shouldNotBeNull()
         .bucketsToUpdate
         .shouldHaveSize(3)
         .get(1)
         .data
         .takeLast(5) shouldBe listOf(
         'A'.code.toByte(),
         'A'.code.toByte(),
         'A'.code.toByte(),
         'A'.code.toByte(),
         'A'.code.toByte(),
      )
   }

   private suspend fun standardInit() {
      watchSyncer.init()
   }
}
