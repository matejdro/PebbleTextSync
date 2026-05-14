plugins {
   pureKotlinModule
   di
   sqldelight
}

sqldelight {
   databases {
      create("Database") {
         packageName.set("com.matejdro.pebbletextsync.files.sqldelight.generated")
         schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
      }
   }
}

dependencies {
   api(projects.files.api)

   implementation(projects.bluetooth.api)
   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
   implementation(libs.dispatch)

   testImplementation(testFixtures(projects.bluetooth.api))
   testImplementation(libs.kotlinova.core.test)
}
