plugins {
   androidLibraryModule
   di
   unmock
}

android {
   namespace = "com.matejdro.textsync.tasker"

   androidResources.enable = true
}


dependencies {
   api(projects.tasker.api)
   api(libs.dispatch)

   implementation(projects.bluetooth.api)
   implementation(projects.commonAndroid)
   implementation(projects.sharedResources)
   implementation(libs.androidx.core)
   implementation(libs.androidx.datastore.preferences)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
   implementation(libs.logcat)

   testImplementation(testFixtures(projects.bluetooth.api))
}
