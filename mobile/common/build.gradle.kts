plugins {
   pureKotlinModule
   testFixtures
}

dependencies {
   implementation(libs.kotlinova.core)

   testFixturesImplementation(libs.kotlin.coroutines)
   testFixturesImplementation(libs.dispatch.test)
   testFixturesImplementation(libs.kotest.assertions)
   testFixturesImplementation(libs.turbine)
   testFixturesImplementation(libs.androidx.datastore.preferences.core)
}
