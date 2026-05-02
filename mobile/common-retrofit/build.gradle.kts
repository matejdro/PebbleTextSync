plugins {
   pureKotlinModule
   di
   testFixtures
}

dependencies {
   api(libs.kotlinova.retrofit)

   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlin.serialization.json)
   implementation(libs.kotlinova.core)
   implementation(libs.okhttp)
   implementation(libs.okio)
   implementation(libs.retrofit)
   implementation(libs.retrofit.serialization)

   testImplementation(testFixtures(projects.commonRetrofit))
   testImplementation(libs.kotlinova.retrofit.test)
   testImplementation(libs.okhttp.mockWebServer)
   testImplementation(libs.turbine)

   testFixturesApi(libs.kotlinova.retrofit.test)
   testFixturesImplementation(libs.kotlin.coroutines)
   testFixturesImplementation(libs.kotlin.serialization.json)
   testFixturesImplementation(libs.kotlinova.core)
   testFixturesImplementation(libs.kotlinova.core.test)
   testFixturesImplementation(libs.okhttp)
   testFixturesImplementation(libs.kotlin.coroutines.test)
}
