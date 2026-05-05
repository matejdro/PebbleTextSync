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

   implementation(libs.kotlin.coroutines)
   implementation(libs.kotlinova.core)
   implementation(libs.dispatch)

   testImplementation(libs.kotlinova.core.test)
}
