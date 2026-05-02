plugins {
   androidLibraryModule
   compose
   di
   navigation
   showkase
}

android {
   namespace = "com.matejdro.pebbletextsync.home.ui"

   androidResources.enable = true
}

dependencies {
   api(projects.home.api)

   implementation(projects.commonCompose)
   implementation(projects.sharedResources)
   implementation(libs.accompanist.permissions)
   implementation(libs.androidx.core)
   implementation(libs.androidx.compose.material3.sizeClasses)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlin.coroutines)
}
