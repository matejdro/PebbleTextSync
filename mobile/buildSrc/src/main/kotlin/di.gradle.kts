import dev.zacsweers.metro.gradle.ExperimentalMetroGradleApi
import dev.zacsweers.metro.gradle.MetroPluginExtension
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

apply(plugin = "dev.zacsweers.metro")

@OptIn(ExperimentalMetroGradleApi::class)
configure<MetroPluginExtension> {
   generateContributionProviders = true
}
