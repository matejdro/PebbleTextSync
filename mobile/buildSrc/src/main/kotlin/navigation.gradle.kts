import com.autonomousapps.DependencyAnalysisSubExtension
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
   id("com.google.devtools.ksp")
}

dependencies {
   if (name != "common-navigation") {
      add("implementation", project(":common-navigation"))
   }

   ksp(libs.kotlinova.navigation.compiler)

   add("testImplementation", libs.kotlinova.navigation.test)
   add("androidTestImplementation", libs.kotlinova.navigation.test)
}

configure<DependencyAnalysisSubExtension> {
   issues {
      onUnusedDependencies {
         // Every navigation project has to include common-navigation, either directly or transitively,
         // So it's not a big deal if some projects have it while not directly using it
         exclude(":common-navigation")
      }
   }
}
