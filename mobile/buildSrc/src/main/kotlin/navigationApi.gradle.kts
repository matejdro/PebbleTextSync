import com.autonomousapps.DependencyAnalysisExtension
import com.autonomousapps.DependencyAnalysisSubExtension
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
   id("serialization")
}

dependencies {
   if (name != "common-navigation") {
      add("implementation", project(":common-navigation"))
   }
}
