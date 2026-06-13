package com.matejdro.pebbletextsync.bluetooth

import androidx.core.net.toUri
import com.matejdro.bucketsync.BucketSyncRepository
import com.matejdro.pebble.bluetooth.common.util.LimitingStringEncoder
import com.matejdro.pebble.bluetooth.common.util.fixPebbleIndentation
import com.matejdro.pebble.bluetooth.common.util.trimLastInvalidUtf8Character
import com.matejdro.pebbletextsync.bluetooth.util.FileContentsReader
import com.matejdro.pebbletextsync.files.SyncingFile
import com.matejdro.pebbletextsync.files.SyncingFileRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dispatch.core.withDefault
import kotlinx.coroutines.flow.first
import logcat.logcat
import okio.Buffer
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.reporting.ErrorReporter
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CoderResult
import java.nio.charset.StandardCharsets

@ContributesBinding(AppScope::class)
class WatchSyncerImpl(
   private val bucketSyncRepository: BucketSyncRepository,
   private val fileRepository: SyncingFileRepository,
   private val fileReader: FileContentsReader,
   private val errorReporter: ErrorReporter,
) : WatchSyncer {
   private val stringEncoder = LimitingStringEncoder()
   private val utf8Encoder = StandardCharsets.UTF_8.newEncoder()

   override suspend fun syncFile(id: Int) = withDefault {
      logcat { "Syncing file $id" }
      val fileMetadataOutcome = fileRepository.getSingle(id).first()
      if (fileMetadataOutcome !is Outcome.Success) {
         errorReporter.report(UnknownCauseException("Got non-success on file syncing: $fileMetadataOutcome"))
         return@withDefault
      }

      val fileMetadata = fileMetadataOutcome.data
      if (fileMetadata == null) {
         logcat { "File $id not found, deleting..." }
         bucketSyncRepository.deleteGroup(id.toString())
         return@withDefault
      }
      val fileIdString = fileMetadata.id.toString()

      val fileContents = fileReader.read(
         fileMetadata.contentUri.toUri(),
         fileMetadata.slots * BucketSyncRepository.MAX_BUCKET_SIZE_BYTES
      ).fixPebbleIndentation()

      val encodedTitle =
         stringEncoder.encodeSizeLimited(fileMetadata.title, SyncingFile.MAX_TITLE_LENGTH_BYTES, ellipsize = false)
            .encodedString

      val contentBuffer = CharBuffer.wrap(fileContents)
      val byteBuffer = ByteBuffer.allocate(BucketSyncRepository.MAX_BUCKET_SIZE_BYTES - 1)

      byteBuffer.position(encodedTitle.size + 1)
      val firstBucketResult = utf8Encoder.encode(contentBuffer, byteBuffer, true)
      byteBuffer.trimLastInvalidUtf8Character(contentBuffer)

      val firstBucketTextBody = byteBuffer.array().copyOfRange(fromIndex = encodedTitle.size + 1, toIndex = byteBuffer.position())

      val extraTextBodies = getExtraTextBodies(firstBucketResult, byteBuffer, fileMetadata, contentBuffer)

      val savedBuckets = saveBuckets(fileMetadata, extraTextBodies, fileIdString, encodedTitle, firstBucketTextBody)

      logcat { "Written buckets $savedBuckets" }
      bucketSyncRepository.deleteGroup(fileIdString, except = savedBuckets)
   }

   @Suppress("MissingUseCall") // Buffer does not need closing
   private suspend fun saveBuckets(
      fileMetadata: SyncingFile,
      extraTextBodies: List<ByteArray>,
      fileIdString: String,
      encodedTitle: ByteArray,
      firstBucketTextBody: ByteArray,
   ): MutableList<String> {
      // Update buckets in reverse order to get the ids of the next bucket in the sequence
      var nextBucketId = 0

      val savedBuckets = mutableListOf((fileMetadata.id * BUCKET_ID_MULTIPLIER).toString())

      for ((index, body) in extraTextBodies.withIndex().reversed()) {
         val buffer = Buffer()

         buffer.writeByte(nextBucketId)
         buffer.write(body)

         val indexIncludingFirst = index + 1

         val bucketId = (fileMetadata.id * BUCKET_ID_MULTIPLIER + indexIncludingFirst).toString()
         nextBucketId = bucketSyncRepository.updateBucketDynamic(
            bucketId,
            buffer.readByteArray(),
            sortKey = (fileMetadata.orderIndex * BUCKET_ID_MULTIPLIER + indexIncludingFirst).toLong(),
            flags = 1u,
            groupId = fileIdString,
         )
         savedBuckets.add(bucketId)
      }

      val buffer = Buffer()

      buffer.write(encodedTitle)
      buffer.writeByte(0)
      buffer.writeByte(nextBucketId)
      buffer.write(firstBucketTextBody)

      bucketSyncRepository.updateBucketDynamic(
         (fileMetadata.id * BUCKET_ID_MULTIPLIER).toString(),
         buffer.readByteArray(),
         sortKey = (fileMetadata.orderIndex * BUCKET_ID_MULTIPLIER).toLong(),
         groupId = fileIdString,
         flags = 0u,
      )
      return savedBuckets
   }

   private fun getExtraTextBodies(
      firstBucketResult: CoderResult,
      byteBuffer: ByteBuffer,
      fileMetadata: SyncingFile,
      contentBuffer: CharBuffer,
   ): List<ByteArray> = buildList {
      if (!firstBucketResult.isUnderflow) {
         byteBuffer.rewind()

         var bucketsLeft = fileMetadata.slots
         while (--bucketsLeft > 0) {
            utf8Encoder.reset()
            val result = utf8Encoder.encode(contentBuffer, byteBuffer, true)
            byteBuffer.trimLastInvalidUtf8Character(contentBuffer)
            add(byteBuffer.array().copyOfRange(fromIndex = 0, toIndex = byteBuffer.position()))
            if (result.isUnderflow) {
               break
            }

            byteBuffer.rewind()
         }
      }
   }

   suspend fun init() {
      val existingDataValid = bucketSyncRepository.init(BUCKET_DATA_VERSION.toInt())

      if (!existingDataValid) {
         syncAll()
      }
   }

   override suspend fun syncAll() {
      val existingBucketsOutcome = fileRepository.getAll().first()
      if (existingBucketsOutcome !is Outcome.Success) {
         errorReporter.report(UnknownCauseException("Got non-success on file syncing: $existingBucketsOutcome"))
         return
      }

      for (bucket in existingBucketsOutcome.data) {
         syncFile(bucket.id)
      }
   }
}

// One file can have more than one bucket. To account for this, we give every file a namespace of X buckets by multiplying its id.
private const val BUCKET_ID_MULTIPLIER = 1000
