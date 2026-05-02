---
name: module-creation
description: Create a set of new Gradle modules for a new feature
---

To create a set of modules:

1. Create a new folder in the project root with a lowercase single word module name
2. In this folder (feature folder), create an `api` folder with the following files:

`.gitignore`:

```
build/
```

`build.gradle.kts`:

```
plugins {
   pureKotlinModule
}
```

3. Inside feature folder, create a `data` folder with the following files:

`.gitignore`:

```
build/
```

`build.gradle.kts`:

```
plugins {
   pureKotlinModule
}

dependencies {
    api(projects.NAME.api)
}
```

where NAME is the name of the feature folder

4. Ask user if this feature will contain UI. If yes, create inside a feature folder a `ui` folder with the following files:

`.gitignore`:

```
build/
```

`build.gradle.kts`:

```
plugins {
   androidLibraryModule
   compose
   di
   navigation
}

android {
    namespace = "com.matejdro.pebbletextsync.NAME.ui"
    
    androidResources.enable = true
}

dependencies {
    api(projects.NAME.api)
}
```

where NAME is the name of the feature folder

5. Add new modules to the `settings.gradle.kts`
6. Add data and ui modules to the dependencies of the `app/build.gradle.kts`
7. Run `./gradlew tasks` to ensure gradle still builds
