plugins {
   androidLibraryModule
   compose
   di
}

android {
    namespace = "com.matejdro.pebbletextsync.${NAME}.ui"
    
    androidResources.enable = true
}

dependencies {
    api(projects.${NAME}.api)
    
    testImplementation(testFixtures(projects.common))    
}
