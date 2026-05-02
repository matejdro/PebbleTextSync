---
name: screen-creation
description: Create a screen 
---

Familiarize yourself with the navigation system first: https://github.com/inovait/kotlinova/tree/master/navigation/README.MD

Then:

1. Make sure the api module of the feature you want to add the screen to has `navigationApi` gradle plugin
2. Add a `NAMEScreenKey` class to the api module, in the appropriate package, where the NAME is the name of the screen.
3. In the ui module, create a Screen class, with the same package as the Screen Key. There is no need to use
   ContributesScreenBinding.
4. If needed (ask user), also create a ViewModel class next to the screen class with the following contents:

```kotlin
@Stable
@Inject
@ContributesScopedService
class NAMEViewModel(
   private val resources: CoroutineResourceManager
) : SingleScreenViewModel<NAMEScreenKey>(resources.scope) {

}
```

5. Add that ViewModel to the screen's constructor
6. Run `./gradlew assembleDebug` to ensure everything compiles
