import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  java
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
  compileOnly("io.papermc.paper:paper-api:${rootProject.providers.gradleProperty("paperApiVersion").get()}")

  compileOnly("org.projectlombok:lombok:1.18.38")
  annotationProcessor("org.projectlombok:lombok:1.18.38")

  implementation(project(":arenalib"))

  compileOnly("org.jetbrains:annotations:26.0.2")

  testImplementation("org.junit.jupiter:junit-jupiter:5.12.0")
}

tasks.processResources {
  filteringCharset = "UTF-8"
  val pluginProperties = mapOf(
    "version" to project.version.toString(),
    "description" to rootProject.providers.gradleProperty("plugin.description").get(),
    "author" to rootProject.providers.gradleProperty("plugin.author").get(),
    "website" to rootProject.providers.gradleProperty("plugin.website").get(),
  )

  filesMatching("plugin.yml") {
    expand(pluginProperties)
  }
}

tasks.named<ShadowJar>("shadowJar") {
  archiveFileName.set("arenacreator-${project.version}.jar")
}

tasks.build {
  dependsOn(tasks.shadowJar)
}
