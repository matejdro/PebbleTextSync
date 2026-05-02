plugins {
   pureKotlinModule
   di
}

dependencies {
    api(projects.${NAME}.api)
    
    testImplementation(testFixtures(projects.common))    
}
