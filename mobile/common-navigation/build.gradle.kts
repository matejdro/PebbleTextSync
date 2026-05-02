plugins {
   pureKotlinModule
   compose
   serialization
}

dependencies {
   api(libs.kotlinova.navigation)
   implementation(libs.kotlinova.compose)
}
