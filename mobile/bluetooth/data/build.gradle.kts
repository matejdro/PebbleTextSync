plugins {
   androidLibraryModule
   di
   unmock
}

custom {
   enableEmulatorTests = true
}

dependencies {
   api(projects.bluetooth.api)

   implementation(projects.bluetoothCommon)
   implementation(projects.bucketsync.api)
   implementation(projects.files.api)
   implementation(projects.tools.api)
   implementation(libs.androidx.core)
   implementation(libs.androidx.datastore.preferences)
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
   androidTestImplementation(testFixtures(projects.files.api))
   androidTestImplementation(projects.bucketsync.test)
   androidTestImplementation(libs.androidx.test.runner)
   androidTestImplementation(libs.androidx.test.core)
   androidTestImplementation(libs.kotlin.coroutines.test)
   androidTestImplementation(libs.kotlinova.core.test)
   androidTestImplementation(libs.junit4)
}
