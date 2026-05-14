plugins {
   androidLibraryModule
   di
}

dependencies {
   api(projects.bluetooth.api)

   implementation(projects.bluetoothCommon)
   implementation(projects.bucketsync.api)
   implementation(libs.logcat)
   implementation(libs.pebblekit)
   implementation(libs.kotlin.coroutines)
}
