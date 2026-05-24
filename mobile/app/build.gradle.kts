plugins {
   androidAppModule
   compose
   navigation
   serialization
   showkase
   sqldelight
   id("androidx.baselineprofile")
}

android {
   namespace = "com.matejdro.pebbletextsync"

   buildFeatures {
      buildConfig = true
   }

   defaultConfig {
      applicationId = "com.matejdro.pebbletextsync"
      targetSdk = 33
      versionCode = 1
      versionName = "1.0.0"

      testInstrumentationRunner = "com.matejdro.pebbletextsync.instrumentation.TestRunner"
      testInstrumentationRunnerArguments += "clearPackageData" to "true"
      // Needed to enable test coverage
      testInstrumentationRunnerArguments += "useTestStorageService" to "true"
   }

   testOptions {
      execution = "ANDROIDX_TEST_ORCHESTRATOR"
   }

   if (providers.gradleProperty("testAppWithProguard").isPresent) {
      testBuildType = "proguardedDebug"
   }

   signingConfigs {
      getByName("debug") {
         // SHA1: B5:36:F6:27:81:AA:BB:26:04:B9:B3:91:1D:CA:6A:CA:74:26:0C:A6
         // SHA256: 28:E8:53:AE:D1:D5:7A:65:44:38:AB:C0:85:99:75:23:63:9B:CA:B8:A9:AC:8B:DD:06:FF:B2:77:6A:C2:AC:FF

         storeFile = File(rootDir, "keys/debug.jks")
         storePassword = "android"
         keyAlias = "androiddebugkey"
         keyPassword = "android"
      }

      create("release") {
         // SHA1: 5E:AE:58:CA:DE:21:52:FD:7C:3D:B3:98:F4:63:84:26:05:AF:B0:39
         // SHA256: 56:1F:15:1B:F5:74:FD:14:7A:44:95:13:DF:4B:33:5D:B9:63:B9:0E:9D:C6:D0:87:83:D1:FF:53:38:B1:40:A2

         storeFile = File(rootDir, "keys/release.jks")
         storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
         keyAlias = "app"
         keyPassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
      }
   }

   buildTypes {
      getByName("debug") {
         // TODO uncomment when above signing config becomes valid
         // signingConfig = signingConfigs.getByName("debug")
      }

      create("proguardedDebug") {
         isMinifyEnabled = true
         isShrinkResources = true

         proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
         )

         testProguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
            "proguard-rules-test.pro"
         )

         matchingFallbacks += "debug"

         signingConfig = signingConfigs.getByName("debug")
      }

      getByName("release") {
         isMinifyEnabled = true
         isShrinkResources = true

         proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
         )

         signingConfig = signingConfigs.getByName("release")
      }
   }
}

custom {
   enableEmulatorTests.set(true)
}

dependencyAnalysis {
   issues {
      onUnusedDependencies {
         // Needed to declare to force newer version to avoid mismatched dependencies error
         exclude("androidx.concurrent:concurrent-futures")
      }
   }
}

sqldelight {
   databases {
      create("Database") {
         packageName.set("com.matejdro.pebbletextsync")
         schemaOutputDirectory.set(file("src/main/sqldelight/databases"))

         dependency(project(projects.files.data.path))
      }
   }
}

afterEvaluate {
   tasks.named("verifyDebugDatabaseMigration") {
      // Workaround for the https://github.com/cashapp/sqldelight/issues/5115
      mustRunAfter("generateDebugDatabaseSchema")
   }
}

dependencies {
   implementation(projects.home.ui)
   implementation(projects.bluetooth.api)
   implementation(projects.bluetooth.data)
   implementation(projects.bluetoothCommon)
   implementation(projects.bucketsync.api)
   implementation(projects.bucketsync.data)
   implementation(projects.common)
   implementation(projects.commonAndroid)
   implementation(projects.commonNavigation)
   implementation(projects.commonCompose)
   implementation(projects.files.api)
   implementation(projects.files.data)
   implementation(projects.files.ui)
   implementation(projects.home.api)
   implementation(projects.home.ui)
   implementation(projects.tools.api)
   implementation(projects.tools.ui)
   implementation(projects.tasker.data)
   runtimeOnly(projects.tasker.ui)
   implementation(projects.logging.api)
   implementation(projects.logging.crashreport)
   implementation(projects.logging.data)
   implementation(projects.navigationImpl)

   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.core)
   implementation(libs.androidx.core.splashscreen)
   implementation(libs.androidx.lifecycle.runtime)
   implementation(libs.androidx.lifecycle.viewModel)
   implementation(libs.androidx.lifecycle.viewModel.compose)
   implementation(libs.androidx.navigation3)
   implementation(libs.androidx.navigation3)
   implementation(libs.androidx.workManager)
   implementation(libs.coil)
   implementation(libs.dispatch)
   implementation(libs.logcat)
   implementation(libs.kermit)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.navigation)
   implementation(libs.kotlinova.navigation.deeplink)
   implementation(libs.kotlinova.navigation.navigation3)
   implementation(libs.pebblekit)
   implementation(libs.sqldelight.android)
   implementation(libs.tinylog.api)

   implementation(libs.androidx.datastore)
   implementation(libs.androidx.datastore.preferences)
}
