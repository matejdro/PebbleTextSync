plugins {
   androidLibraryModule
   compose
   navigation
   showkase
}

android {
   namespace = "com.matejdro.textsync.tasker.ui"

   androidResources.enable = true
}

dependencyAnalysis {
   issues {
      onUnusedDependencies {
         exclude(":tasker:api") // https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/1465
      }
   }
}

dependencies {
   api(libs.kotlinova.navigation)

   implementation(projects.commonCompose)
   implementation(projects.tasker.api)
   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.navigation3)
   implementation(libs.kotlinova.navigation.navigation3)
}
