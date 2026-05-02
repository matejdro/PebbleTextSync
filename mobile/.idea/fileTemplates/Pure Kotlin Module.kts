plugins {
   pureKotlinModule
   di
}

dependencies {
    testImplementation(testFixtures(projects.common))    
}
