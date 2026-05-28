package com.matejdro.pebbletextsync.bluetooth

import com.matejdro.bucketsync.BucketSyncWatchLoopImpl
import com.matejdro.bucketsync.FakeBucketSyncRepository
import com.matejdro.bucketsync.background.FakeBackgroundSyncNotifier
import com.matejdro.pebble.bluetooth.common.PacketQueue
import com.matejdro.pebble.bluetooth.common.test.FakePebbleSender
import com.matejdro.pebble.bluetooth.common.test.sentData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem
import io.rebble.pebblekit2.common.model.ReceiveResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import io.rebble.pebblekit2.model.ConnectedWatch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import si.inova.kotlinova.core.test.TestScopeWithDispatcherProvider
import si.inova.kotlinova.core.test.time.virtualTimeProvider

class WatchappConnectionImplTest {
   private val scope = TestScopeWithDispatcherProvider()

   private val sender = FakePebbleSender(scope.virtualTimeProvider())
   private val bucketSyncRepository = FakeBucketSyncRepository()

   private val watchappOpenController = FakeWatchappOpenController()

   private val watch = WatchIdentifier("watch")

   private val packetQueue = PacketQueue(sender, watch, WATCHAPP_UUID)

   private val pebbleInfoRetriever = FakePebbleInfoRetriever()

   private val bucketSyncWatchLoop = BucketSyncWatchLoopImpl(
      scope.backgroundScope,
      packetQueue,
      bucketSyncRepository,
      watchappOpenController,
      FakeBackgroundSyncNotifier(),
      watch,
   )

   private val connection = WatchappConnectionImpl(
      scope.backgroundScope,
      packetQueue,
      bucketSyncWatchLoop,
      watchappOpenController,
      pebbleInfoRetriever,
      watch,
   )

   @Test
   fun `Nack unknown packets`() = scope.runTest {
      val result = connection.onPacketReceived(
         mapOf(
            0u to PebbleDictionaryItem.UInt32(255u),
         )
      )
      runCurrent()

      result shouldBe ReceiveResult.Nack
   }

   @Test
   fun `Send only version back when watch packets do not match`() = scope.runTest {
      val result = connection.onPacketReceived(
         mapOf(
            0u to PebbleDictionaryItem.UInt32(0u),
            1u to PebbleDictionaryItem.UInt32(PROTOCOL_VERSION + 1u),
            2u to PebbleDictionaryItem.UInt32(1u),
            3u to PebbleDictionaryItem.UInt32(1000u),
         )
      )
      runCurrent()

      result shouldBe ReceiveResult.Ack

      sender.sentData.shouldContainExactly(
         mapOf(
            0u to PebbleDictionaryItem.UInt8(1u),
            1u to PebbleDictionaryItem.UInt16(PROTOCOL_VERSION),
         )
      )
   }

   @Test
   fun `Send a list of updated buckets`() = scope.runTest {
      bucketSyncRepository.updateBucket(1u, byteArrayOf(1))
      bucketSyncRepository.updateBucket(2u, byteArrayOf(2))

      val result = receiveStandardHelloPacket(bufferSize = 38u)
      runCurrent()

      result shouldBe ReceiveResult.Ack

      sender.sentData.shouldContainExactly(
         mapOf(
            0u to PebbleDictionaryItem.UInt8(1u),
            1u to PebbleDictionaryItem.UInt16(PROTOCOL_VERSION),
            2u to PebbleDictionaryItem.Bytes(
               byteArrayOf(
                  0, // Status
                  0, 2, // Latest version
                  2, // Num of active buckets
                  1, 0, // Metadata for bucket 1
                  2, 0, // Metadata for bucket 2
                  1, 1, 1, // Sync data for bucket 1
               )
            ),
         ),
         mapOf(
            0u to PebbleDictionaryItem.UInt8(3u),
            1u to PebbleDictionaryItem.Bytes(
               byteArrayOf(
                  1, // Status
                  2, 1, 2, // Sync data for bucket 2
               )
            ),
         )
      )
   }

   @Test
   fun `Send auto-close flag when watchapp was started by auto sync`() = scope.runTest {
      watchappOpenController.setNextWatchappOpenForAutoSync()

      bucketSyncRepository.updateBucket(1u, byteArrayOf(1))
      bucketSyncRepository.updateBucket(2u, byteArrayOf(2))

      receiveStandardHelloPacket(bufferSize = 61u)
      runCurrent()

      sender.sentData.first().shouldContainKey(3u)
   }

   @Test
   fun `Report max buckets as 15 on legacy watches`() = scope.runTest {
      pebbleInfoRetriever.setConnectedWatches(
         listOf(
            ConnectedWatch(
               id = watch,
               name = "",
               platform = "",
               revision = "",
               firmwareVersionMajor = 4,
               firmwareVersionMinor = 4,
               firmwareVersionPatch = 3,
               firmwareVersionTag = ""
            ),
            ConnectedWatch(
               id = WatchIdentifier("Another watch"),
               name = "",
               platform = "",
               revision = "",
               firmwareVersionMajor = 10,
               firmwareVersionMinor = 0,
               firmwareVersionPatch = 0,
               firmwareVersionTag = ""
            ),
         )
      )

      receiveStandardHelloPacket(bufferSize = 38u)
      runCurrent()

      bucketSyncWatchLoop.lastMaxActiveBuckets shouldBe 15
   }

   @Test
   fun `Report max buckets as 15 on core watches with older firmware`() = scope.runTest {
      pebbleInfoRetriever.setConnectedWatches(
         listOf(
            ConnectedWatch(
               id = watch,
               name = "",
               platform = "",
               revision = "",
               firmwareVersionMajor = 4,
               firmwareVersionMinor = 9,
               firmwareVersionPatch = 163,
               firmwareVersionTag = ""
            ),
            ConnectedWatch(
               id = WatchIdentifier("Another watch"),
               name = "",
               platform = "",
               revision = "",
               firmwareVersionMajor = 10,
               firmwareVersionMinor = 0,
               firmwareVersionPatch = 0,
               firmwareVersionTag = ""
            ),
         )
      )

      receiveStandardHelloPacket(bufferSize = 38u)
      runCurrent()

      bucketSyncWatchLoop.lastMaxActiveBuckets shouldBe 15
   }

   @Test
   fun `Report max buckets as 255 on core watches with the new firmware`() = scope.runTest {
      pebbleInfoRetriever.setConnectedWatches(
         listOf(
            ConnectedWatch(
               id = watch,
               name = "",
               platform = "",
               revision = "",
               firmwareVersionMajor = 4,
               firmwareVersionMinor = 9,
               firmwareVersionPatch = 171,
               firmwareVersionTag = ""
            ),
            ConnectedWatch(
               id = WatchIdentifier("Another watch"),
               name = "",
               platform = "",
               revision = "",
               firmwareVersionMajor = 10,
               firmwareVersionMinor = 0,
               firmwareVersionPatch = 0,
               firmwareVersionTag = ""
            ),
         )
      )

      receiveStandardHelloPacket(bufferSize = 38u)
      runCurrent()

      bucketSyncWatchLoop.lastMaxActiveBuckets shouldBe 255
   }

   private suspend fun receiveStandardHelloPacket(
      version: UInt = 0u,
      bufferSize: UInt = 1000u,
      flags: UInt = 0u,
      currentlyActiveBuckets: ByteArray = byteArrayOf(),
   ): ReceiveResult =
      connection.onPacketReceived(
         mapOf(
            0u to PebbleDictionaryItem.UInt32(0u),
            1u to PebbleDictionaryItem.UInt32(PROTOCOL_VERSION.toUInt()),
            2u to PebbleDictionaryItem.UInt32(version),
            3u to PebbleDictionaryItem.UInt32(bufferSize),
            4u to PebbleDictionaryItem.UInt32(flags),
            5u to PebbleDictionaryItem.Bytes(currentlyActiveBuckets),
         )
      )
}
