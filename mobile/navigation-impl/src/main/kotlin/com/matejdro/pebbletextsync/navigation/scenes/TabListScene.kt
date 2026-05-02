package com.matejdro.pebbletextsync.navigation.scenes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import com.matejdro.pebbletextsync.navigation.keys.base.LocalSelectedTabContent
import com.matejdro.pebbletextsync.navigation.keys.base.SelectedTabContent
import com.matejdro.pebbletextsync.navigation.keys.base.TabContainerKey
import si.inova.kotlinova.navigation.navigation3.key
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

class TabListScene(
   override val key: Any,
   override val entries: List<NavEntry<ScreenKey>>,
   override val previousEntries: List<NavEntry<ScreenKey>>,
   private val innerScene: Scene<ScreenKey>,
) : Scene<ScreenKey> {
   override val content: @Composable (() -> Unit) = {
      val tabContainerEntry = entries.first()

      val selectedTabContent = SelectedTabContent(innerScene.content, innerScene.entries.first().key())
      CompositionLocalProvider(LocalSelectedTabContent provides selectedTabContent) {
         tabContainerEntry.Content()
      }
   }
}

@Composable
fun rememberTabListSceneDecoratorStrategy(): SceneDecoratorStrategy<ScreenKey> {
   return remember() {
      TabListDetailSceneDecoratorStrategy()
   }
}

private class TabListDetailSceneDecoratorStrategy : SceneDecoratorStrategy<ScreenKey> {
   override fun SceneDecoratorStrategyScope<ScreenKey>.decorateScene(scene: Scene<ScreenKey>): Scene<ScreenKey> {
      val lastKey = scene.previousEntries.lastOrNull()
      if (lastKey?.key() !is TabContainerKey) {
         return scene
      }

      return TabListScene(
         key = lastKey,
         entries = listOf(lastKey),
         previousEntries = scene.previousEntries.dropLast(1),
         innerScene = scene
      )
   }
}
