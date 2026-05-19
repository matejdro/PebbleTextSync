plugins {
   androidLibraryModule
   compose
   di
   navigation
   showkase
   unmock
}

android {
   namespace = "com.matejdro.pebbletextsync.files.ui"

   androidResources.enable = true
}

dependencies {
   api(projects.files.api)

   implementation(projects.bluetoothCommon)
   implementation(projects.common)
   implementation(projects.commonCompose)
   implementation(libs.androidx.activity.compose)
   implementation(libs.androidx.core)
   implementation(libs.dispatch)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.navigation)

   testImplementation(libs.kotlin.coroutines.test)
   testImplementation(libs.kotlinova.core.test)
   testImplementation(libs.kotlinova.navigation.test)
   testImplementation(testFixtures(projects.files.api))
}
