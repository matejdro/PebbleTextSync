plugins {
   androidLibraryModule
   compose
   di
   navigation
   showkase
}

android {
   namespace = "com.matejdro.pebbletextsync.tools.ui"

   androidResources.enable = true
}


dependencies {
   api(projects.tools.api)

   implementation(projects.common)
   implementation(projects.commonCompose)
   implementation(projects.home.api)
   implementation(projects.logging.api)
   implementation(projects.tools.api)
   implementation(libs.androidx.core)
   implementation(libs.dispatch)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.navigation)
}
