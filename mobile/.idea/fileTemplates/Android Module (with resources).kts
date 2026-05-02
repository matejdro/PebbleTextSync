plugins {
   androidLibraryModule
   di
}

android {
    namespace = "com.matejdro.pebbletextsync.${NAME}"
    
    androidResources.enable = true
}

dependencies {
    testImplementation(testFixtures(projects.common))    
}
