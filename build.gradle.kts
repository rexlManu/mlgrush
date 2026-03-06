import org.gradle.api.tasks.compile.JavaCompile

plugins {
  base
  id("com.diffplug.spotless") version "6.25.0"
}

group = "de.rexlmanu.mlgrush"
version = providers.gradleProperty("version").get()

subprojects {
  group = rootProject.group
  version = rootProject.version

  apply(plugin = "com.diffplug.spotless")

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

  extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension>("spotless") {
    java {
      target("src/**/*.java")
      googleJavaFormat("1.22.0")
      removeUnusedImports()
      trimTrailingWhitespace()
      endWithNewline()
    }

    kotlinGradle {
      target("*.gradle.kts", "**/*.gradle.kts")
      ktfmt()
      trimTrailingWhitespace()
      endWithNewline()
    }

    format("misc") {
      target("*.md", ".gitignore", "**/.gitignore", "**/*.yml", "**/*.yaml", "gradle.properties")
      trimTrailingWhitespace()
      endWithNewline()
    }
  }
}

tasks.register("runServer") {
  group = "application"
  description = "Runs the Paper development server"
  dependsOn(":plugin:runServer")
}
