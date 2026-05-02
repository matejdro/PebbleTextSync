pluginManagement {
   repositories {
      google()
      mavenCentral()
      gradlePluginPortal()
   }
}

dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

   repositories {
      mavenLocal()
      google()
      mavenCentral()
      maven("https://jitpack.io")
   }

   versionCatalogs {
      create("libs") {
         from(files("config/libs.toml"))
      }
   }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "PebbleTextSync"

include(":app")
include(":app-screenshot-tests")
include(":common")
include(":common-android")
include(":common-compose")
include(":common-navigation")
include(":common-retrofit")
include(":common-retrofit:android")
include(":detekt")
include(":shared-resources")
include(":home:api")
include(":home:ui")
include(":navigation-impl")
