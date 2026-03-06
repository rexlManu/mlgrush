import org.gradle.api.tasks.compile.JavaCompile

plugins {
  base
}

group = "de.rexlmanu.mlgrush"
version = providers.gradleProperty("version").get()

subprojects {
  group = rootProject.group
  version = rootProject.version

  repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
  }

  plugins.withId("java") {
    extensions.configure<org.gradle.api.plugins.JavaPluginExtension>("java") {
      toolchain.languageVersion.set(JavaLanguageVersion.of(21))
      withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
      options.encoding = "UTF-8"
      options.release.set(21)
    }

    tasks.withType<Test>().configureEach {
      useJUnitPlatform()
    }
  }
}
