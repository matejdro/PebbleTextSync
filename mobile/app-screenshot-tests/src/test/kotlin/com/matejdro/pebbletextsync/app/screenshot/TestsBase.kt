package com.matejdro.pebbletextsync.app.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_5
import app.cash.paparazzi.Paparazzi
import com.airbnb.android.showkase.models.ShowkaseBrowserComponent
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@Suppress("JUnitMalformedDeclaration")
@RunWith(TestParameterInjector::class)
open class TestsBase {
   @get:Rule
   val paparazzi = Paparazzi(
      deviceConfig = PIXEL_5,
      theme = "android:Theme.Material.Light.NoActionBar",
      showSystemUi = false,
      renderingMode = SessionParams.RenderingMode.SHRINK,
      snapshotHandler = determinedHandlerWithRenaming(maxPercentDifference = 0.0)
   )

   object PreviewProvider : TestParameterValuesProvider() {
      override fun provideValues(context: Context): List<*> {
         //         TODO uncomment this when you have at least one preview marked with @ShowkaseComposable
         // val splitIndex = context.getOtherAnnotation(SplitIndex::class.java).index
         // val totalSplits = System.getProperty("maxParallelForks")?.toInt() ?: error("Missing maxParallelForks property")
         //
         // val allComponents = Showkase.getMetadata().componentList
         // val perSplit = allComponents.size / totalSplits
         //
         // val start = splitIndex * perSplit
         // val end = if (splitIndex == totalSplits - 1) {
         //    allComponents.size
         // } else {
         //    start + perSplit
         // }
         //
         // val components = allComponents
         //    .subList(start, end)
         //    .map { TestKey(it) }

         val components = emptyList<TestKey>()

         for (i in components.indices) {
            for (j in components.indices) {
               if (i != j && components[i].key == components[j].key) {
                  throw AssertionError("Duplicate @Preview: '${components[i].key}'")
               }
            }
         }

         return components
      }
   }

   data class TestKey(val showkaseBrowserComponent: ShowkaseBrowserComponent) {
      val key = with(showkaseBrowserComponent) {
         componentName + (styleName?.let { "-$it" }.orEmpty())
      }

      override fun toString(): String = key
   }

   @Before
   fun setUp() {
      // Note: if you have lottie in your project, uncomment this
      // Workaround for the https://github.com/cashapp/paparazzi/issues/630
      // LottieTask.EXECUTOR = Executor(Runnable::run)
   }

   protected open fun test(

      testKey: TestKey,
   ) {
      val composable = @Composable {
         CompositionLocalProvider(LocalInspectionMode provides true) {
            testKey.showkaseBrowserComponent.component()
         }
      }

      val previewName = testKey.toString()
      require(previewName.isNotBlank()) { "Test name should not be blank for ${testKey.key}" }

      fun snapshot(name: String) {
         val tags = testKey.showkaseBrowserComponent.tags
         if (tags.contains("animated")) {
            val duration = tags.firstOrNull { it.startsWith("duration-") }?.removePrefix("duration-")?.toInt()
               ?: DEFAULT_DURATION_MS

            paparazzi.gif(
               name = name,
               view = ComposeView(paparazzi.context).apply {
                  setContent {
                     composable()
                  }
               },
               end = duration.toLong(),
               fps = 20
            )
         } else {
            paparazzi.snapshot(name = name) {
               composable()
            }
         }
      }

      snapshot(previewName)

      paparazzi.unsafeUpdateConfig(
         PIXEL_5.copy(
            nightMode = NightMode.NIGHT
         )
      )
      snapshot("${previewName}_night")

      paparazzi.unsafeUpdateConfig(
         PIXEL_5.copy(
            ydpi = 600,
            xdpi = 300,
            screenWidth = 300 * 440 / 160,
            screenHeight = 600 * 440 / 160,
            nightMode = NightMode.NOTNIGHT
         )
      )
      snapshot("${previewName}_small")

      paparazzi.unsafeUpdateConfig(
         PIXEL_5.copy(
            fontScale = 1.5f
         )
      )
      snapshot("${previewName}_largefont")
   }

   annotation class SplitIndex(val index: Int)
}

private const val DEFAULT_DURATION_MS = 1000
