package com.matejdro.pebbletextsync.bluetooth

import androidx.core.net.toUri
import com.matejdro.bucketsync.BucketSyncRepository
import com.matejdro.pebble.bluetooth.common.util.LimitingStringEncoder
import com.matejdro.pebble.bluetooth.common.util.fixPebbleIndentation
import com.matejdro.pebbletextsync.bluetooth.util.FileContentsReader
import com.matejdro.pebbletextsync.files.SyncingFile
import com.matejdro.pebbletextsync.files.SyncingFileRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dispatch.core.withDefault
import kotlinx.coroutines.flow.first
import okio.Buffer
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.reporting.ErrorReporter

@ContributesBinding(AppScope::class)
class WatchSyncerImpl(
   private val bucketSyncRepository: BucketSyncRepository,
   private val fileRepository: SyncingFileRepository,
   private val fileReader: FileContentsReader,
   private val errorReporter: ErrorReporter,
) : WatchSyncer {
   private val stringEncoder = LimitingStringEncoder()

   @Suppress("MissingUseCall") // Buffer does not need closing
   override suspend fun syncFile(id: Int) = withDefault {
      val fileMetadataOutcome = fileRepository.getSingle(id).first()
      if (fileMetadataOutcome !is Outcome.Success) {
         errorReporter.report(UnknownCauseException("Got non-success on file syncing: $fileMetadataOutcome"))
         return@withDefault
      }

      val fileMetadata = fileMetadataOutcome.data
      if (fileMetadata != null) {
         val fileContents = fileReader.read(
            fileMetadata.contentUri.toUri(),
            fileMetadata.slots * BucketSyncRepository.MAX_BUCKET_SIZE_BYTES
         ).fixPebbleIndentation()

         val buffer = Buffer()

         val encodedTitle =
            stringEncoder.encodeSizeLimited(fileMetadata.title, SyncingFile.MAX_TITLE_LENGTH_BYTES, ellipsize = false)
               .encodedString

         buffer.write(encodedTitle)
         buffer.writeByte(0)

         val maxBodySize = BucketSyncRepository.MAX_BUCKET_SIZE_BYTES - encodedTitle.size - 1
         val encodedBody = stringEncoder.encodeSizeLimited(fileContents, maxBodySize, ellipsize = false).encodedString

         buffer.write(encodedBody)

         bucketSyncRepository.updateBucketDynamic(
            (id * BUCKET_ID_MULTIPLIER).toString(),
            buffer.readByteArray(),
            sortKey = (fileMetadata.orderIndex * BUCKET_ID_MULTIPLIER).toLong(),
         )
      } else {
         bucketSyncRepository.deleteBucketDynamic((id * BUCKET_ID_MULTIPLIER).toString())
      }
   }

   suspend fun init() {
      val existingDataValid = bucketSyncRepository.init(BUCKET_DATA_VERSION.toInt())

      if (!existingDataValid) {
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
}

// One file can have more than one bucket. To account for this, we give every file a namespace of X buckets by multiplying its id.
private const val BUCKET_ID_MULTIPLIER = 1000
