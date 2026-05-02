plugins {
   androidLibraryModule
   di
}

dependencies {
   api(projects.commonRetrofit)

   implementation(libs.kotlin.coroutines)
   implementation(libs.dispatch)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.retrofit)
   implementation(libs.okhttp)
   implementation(libs.kotlin.serialization.json)
}
