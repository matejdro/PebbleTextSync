plugins {
   pureKotlinModule
   compose
   serialization
}

dependencies {
   api(libs.kotlinova.navigation)
   implementation(libs.kotlinova.core)
   implementation(libs.kotlinova.compose)
   implementation(libs.kotlin.coroutines)
}
