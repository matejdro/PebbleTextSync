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
include(":bluetooth:api")
include(":bluetooth:data")
include(":common")
include(":common-android")
include(":common-compose")
include(":common-navigation")
include(":detekt")
include(":files:api")
include(":files:data")
include(":files:ui")
include(":shared-resources")
include(":home:api")
include(":home:ui")
include(":tools:api")
include(":tools:ui")
include(":navigation-impl")

include(":bluetooth-common")
project(":bluetooth-common").projectDir = file("../PebbleCommons/mobile/bluetooth-common")

include(":bucketsync:api")
project(":bucketsync:api").projectDir = file("../PebbleCommons/mobile/bucketsync/api")
include(":bucketsync:data")
project(":bucketsync:data").projectDir = file("../PebbleCommons/mobile/bucketsync/data")
include(":bucketsync:test")
project(":bucketsync:test").projectDir = file("../PebbleCommons/mobile/bucketsync/test")
project(":bucketsync").projectDir = file("../PebbleCommons/mobile/bucketsync")

include(":logging:api")
project(":logging:api").projectDir = file("../PebbleCommons/mobile/logging/api")
include(":logging:data")
project(":logging:data").projectDir = file("../PebbleCommons/mobile/logging/data")
include(":logging:crashreport")
project(":logging:crashreport").projectDir = file("../PebbleCommons/mobile/logging/crashreport")
project(":logging").projectDir = file("../PebbleCommons/mobile/logging")
