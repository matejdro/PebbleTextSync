import com.android.build.gradle.internal.tasks.factory.dependsOn
import jacoco.setupJacocoMergingPureKotlin
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins {
   id("org.jetbrains.kotlin.jvm")

   id("allModulesCommons")

   jacoco
}

val runDebugTestsTask = tasks.register("runDebugTests")
runDebugTestsTask.dependsOn(tasks.test)

val runDebugDetektTask = tasks.register("runDebugDetekt")
runDebugDetektTask.dependsOn("detektMain")
runDebugDetektTask.dependsOn("detektTest")

tasks.test {
   useJUnitPlatform()

   // Better test output
   systemProperty("kotest.assertions.collection.print.size", "300")
   systemProperty("kotest.assertions.collection.enumerate.size", "300")
}

jacoco {
   toolVersion = libs.versions.jacoco.get()
}

setupJacocoMergingPureKotlin()

dependencies {
}
