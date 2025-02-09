pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri ("https://jitpack.io") }
        maven { url = uri ("https://oss.sonatype.org/content/repositories/snapshots")}
        maven { url = uri("https://maven.google.com") }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri ("https://oss.sonatype.org/content/repositories/snapshots")}
    }
}

rootProject.name = "Lensnap"
include(":app")
