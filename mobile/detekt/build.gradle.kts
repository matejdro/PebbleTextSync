plugins {
   pureKotlinModule
}

dependencyAnalysis {
   issues {
      onUsedTransitiveDependencies {
         // We don't really want to include entire kotlin here.
         // Detekt can manage its own dependencies.
         exclude("org.jetbrains.kotlin:kotlin-compiler")
         exclude("dev.detekt:detekt-kotlin-analysis-api")
      }

      onDuplicateClassWarnings {
         // Caused by detekt shading analysis API
         // This is unavoidable until https://youtrack.jetbrains.com/issue/KT-56203 is solved
         excludeRegex("kotlin/.*")
         excludeRegex("org/jetbrains/.*")
      }
   }
}

// Workaround from https://github.com/lunaynx/SkyHanni/commit/2acc2ffe6909f6bbfb9ebb0b383dff1e774d248a
abstract class DetektTestMetadataRule : ComponentMetadataRule {
   override fun execute(context: ComponentMetadataContext) {
      val version = context.details.id.version
      if (version != "2.0.0-alhpa.4" && version != "2.0.0-alpha.5") return

      context.details.withVariant("runtimeElements") {
         withDependencies {
            // detekt-test 2.0.0-alpha.4 and 2.0.0-alpha.5 request detekt-api test fixtures, but
            // detekt-api only publishes fixture sources.
            removeAll { it.group == "dev.detekt" && it.name == "detekt-api" }
            add("dev.detekt:detekt-api:$version")
         }
      }
   }
}


dependencies {
   compileOnly(libs.detekt.api)

   testImplementation(libs.detekt.api)
   testImplementation(libs.detekt.test)

   // TODO remove this once Detekt 2.0.0-alpha.6 is out
   // https://github.com/detekt/detekt/issues/9409
   components {
      withModule<DetektTestMetadataRule>("dev.detekt:detekt-test")
   }
}
