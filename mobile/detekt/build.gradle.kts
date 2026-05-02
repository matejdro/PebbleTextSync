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


dependencies {
   compileOnly(libs.detekt.api)

   testImplementation(libs.detekt.api)
   testImplementation(libs.detekt.test)
}
