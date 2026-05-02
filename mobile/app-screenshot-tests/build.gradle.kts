plugins {
   androidLibraryModule
   compose
   alias(libs.plugins.paparazzi)
}

android {
   namespace = "com.matejdro.pebbletextsync.screenshottests"

   androidResources.enable = true

   testOptions {
      unitTests.all {
         it.useJUnit()
         it.reports.html.required = false

         it.maxParallelForks = minOf(Runtime.getRuntime().availableProcessors(), 2)
         it.systemProperty("maxParallelForks", it.maxParallelForks)
      }
   }
}

plugins.withId("app.cash.paparazzi") {
   // Defer until afterEvaluate so that testImplementation is created by Android plugin.
   afterEvaluate {
      dependencies.constraints {
         add("testImplementation", "com.google.guava:guava") {
            attributes {
               attribute(
                  TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
                  objects.named(TargetJvmEnvironment::class, TargetJvmEnvironment.STANDARD_JVM)
               )
            }
            because(
               "LayoutLib and sdk-common depend on Guava's -jre published variant." +
                  "See https://github.com/cashapp/paparazzi/issues/906."
            )
         }
      }
   }
}

dependencyAnalysis {
   issues {
      onModuleStructure {
         // False positive
         severity("ignore")
      }
   }
}

dependencies {
   implementation(projects.app) {
      // If your app has multiple flavors, this is how you define them:
      //      attributes {
      //         attribute(
      //            ProductFlavorAttr.of("app"),
      //            objects.named(ProductFlavorAttr::class.java, "develop")
      //         )
      //      }
   }
   testImplementation(libs.junit4)
   testImplementation(libs.junit4.parameterinjector)
   testImplementation(libs.showkase)
}
