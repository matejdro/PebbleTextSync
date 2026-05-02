plugins {
   androidLibraryModule
   compose
   showkase
}

android {
   namespace = "com.matejdro.pebbletextsync.ui"

   androidResources.enable = true
}

dependencies {
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.compose)
   implementation(libs.kotlin.coroutines)
   implementation(libs.coil)
   implementation(libs.coil.okhttp)
}
