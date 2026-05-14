plugins {
   androidLibraryModule
   di
   unmock
}

dependencies {
   api(projects.bluetooth.api)

   implementation(projects.bluetoothCommon)
   implementation(projects.bucketsync.api)
   implementation(projects.files.api)
   implementation(libs.androidx.core)
   implementation(libs.dispatch)
   implementation(libs.logcat)
   implementation(libs.okio)
   implementation(libs.pebblekit)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)

   testImplementation(testFixtures(projects.files.api))
   testImplementation(projects.bucketsync.data)
   testImplementation(projects.bucketsync.test)
   testImplementation(libs.kotlinova.core.test)
}
