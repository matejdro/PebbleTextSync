package com.matejdro.pebbletextsync.bluetooth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.matejdro.bucketsync.BucketSyncRepository.Companion.MAX_BUCKETS_CORE_WATCHES
import com.matejdro.bucketsync.BucketSyncRepository.Companion.MAX_BUCKETS_LEGACY_WATCHES
import com.matejdro.bucketsync.BucketSyncWatchLoop
import com.matejdro.bucketsync.BucketSyncWatchappOpenController
import com.matejdro.pebble.bluetooth.common.PacketQueue
import com.matejdro.pebble.bluetooth.common.WatchAppConnection
import com.matejdro.pebble.bluetooth.common.di.WatchappConnectionGraph
import com.matejdro.pebble.bluetooth.common.di.WatchappConnectionScope
import com.matejdro.pebble.bluetooth.common.util.requireUint
import com.matejdro.tools.PebbleFont
import com.matejdro.tools.PreferenceKeys
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.rebble.pebblekit2.client.PebbleInfoRetriever
import io.rebble.pebblekit2.common.model.PebbleDictionary
import io.rebble.pebblekit2.common.model.PebbleDictionaryItem
import io.rebble.pebblekit2.common.model.ReceiveResult
import io.rebble.pebblekit2.common.model.WatchIdentifier
import io.rebble.pebblekit2.model.ConnectedWatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import logcat.logcat

@Inject
@ContributesBinding(WatchappConnectionScope::class)
@Suppress("MagicNumber") // Packet processing involves a lot of numbers, it would be less readable to make consts
class WatchappConnectionImpl(
   private val coroutineScope: CoroutineScope,
   private val packetQueue: PacketQueue,
   private val bucketSyncWatchLoop: BucketSyncWatchLoop,
   private val openController: BucketSyncWatchappOpenController,
   private val pebbleInfoRetriever: PebbleInfoRetriever,
   private val watch: WatchIdentifier,
   private val preferenceDatastore: DataStore<Preferences>,
) : WatchAppConnection {

   init {
      coroutineScope.launch {
         packetQueue.runQueue()
      }
   }

   override suspend fun onPacketReceived(data: PebbleDictionary): ReceiveResult {
      val id = (data.get(0u) as PebbleDictionaryItem.UInt32?)?.value
      logcat { "Received packet ${id ?: "null"}" }

      return if (id == 0u) {
         processWatchWelcomePacket(data)
      } else {
         logcat { "Unknown packet ID. Nacking..." }
         ReceiveResult.Nack
      }
   }

   private suspend fun processWatchWelcomePacket(data: PebbleDictionary): ReceiveResult {
      val watchInfo = pebbleInfoRetriever.getConnectedWatches().first().firstOrNull { it.id == watch }

      val watchProtocolVersion = data.requireUint(1u)
      if (watchProtocolVersion != PROTOCOL_VERSION.toUInt()) {
         logcat { "Mismatch protocol version $watchProtocolVersion" }
         packetQueue.sendPacket(
            mapOf(
               0u to PebbleDictionaryItem.UInt8(1u),
               1u to PebbleDictionaryItem.UInt16(PROTOCOL_VERSION)
            )
         )
         return ReceiveResult.Ack
      }

      val watchVersion = data.requireUint(2u).toUShort()
      val watchBufferSize = data.requireUint(3u).toInt()

      val activeBuckets = data[5u]
         ?.let { it as? PebbleDictionaryItem.Bytes }
         ?.value
         ?.map { it.toUByte() }
         .orEmpty()

      logcat { "Watch data: version=$watchVersion, buffer size=$watchBufferSize" }

      val selectedFont = preferenceDatastore.data.first()[PreferenceKeys.TEXT_FONT]?.let { enumValueOf<PebbleFont>(it) }
         ?: PreferenceKeys.TEXT_FONT_DEFAULT

      bucketSyncWatchLoop.sendFirstPacketAndStartLoop(
         mapOfNotNull(
            0u to PebbleDictionaryItem.UInt8(1u),
            1u to PebbleDictionaryItem.UInt16(PROTOCOL_VERSION),
            (3u to PebbleDictionaryItem.UInt8(1u)).takeIf { openController.isNextWatchappOpenForAutoSync() },
            4u to PebbleDictionaryItem.UInt8(selectedFont.ordinal)
         ),
         watchVersion,
         watchBufferSize,
         currentlyActiveBuckets = activeBuckets,
         maxActiveBuckets = if (watchInfo?.supportsLargeStorage() == true) {
            MAX_BUCKETS_CORE_WATCHES
         } else {
            MAX_BUCKETS_LEGACY_WATCHES
         },
      )

      return ReceiveResult.Ack
   }

   @Inject
   @ContributesBinding(AppScope::class)
   class Factory(
      private val subgraphFactory: WatchappConnectionGraph.Factory,
   ) : WatchAppConnection.Factory {
      override fun create(watch: WatchIdentifier, scope: CoroutineScope): WatchAppConnection {
         return subgraphFactory.create(scope, watch).createWatchappConnection()
      }
   }
}

private fun <K, V> mapOfNotNull(vararg pairs: Pair<K, V>?): Map<K, V> =
   pairs.filterNotNull().toMap()

@Suppress("MagicNumber") // Just a hardcoded firmware version
private fun ConnectedWatch.supportsLargeStorage(): Boolean {
   return firmwareVersionMajor > 4 || firmwareVersionMinor > 9 || firmwareVersionPatch >= 171
}
