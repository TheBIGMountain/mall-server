plugins {
  id("org.springframework.boot") version "2.4.1"
  id("io.spring.dependency-management") version "1.0.10.RELEASE"
  kotlin("jvm") version "1.4.21"
  kotlin("plugin.spring") version "1.4.21"
  kotlin("kapt") version "1.4.21"
}

ext["best-pay-sdk.version"] = "1.3.0"
ext["embedded-redis.version"] = "0.6"

tasks {
  bootJar { enabled = false }
  jar { enabled = false }
}


allprojects {
  repositories {
    mavenLocal()
    mavenCentral()
  }
}

subprojects {
  apply {
    plugin("java")
    plugin("org.springframework.boot")
    plugin("io.spring.dependency-management")
    plugin("org.jetbrains.kotlin.jvm")
    plugin("org.jetbrains.kotlin.plugin.spring")
    plugin("org.jetbrains.kotlin.kapt")
  }

  group = "com.dqpi"
  version = "0.0.1-SNAPSHOT"
  java.sourceCompatibility = JavaVersion.VERSION_11


  dependencyManagement {
    dependencies {
      dependency("cn.springboot:best-pay-sdk:${property("best-pay-sdk.version")}")
      dependency("com.github.kstyrc:embedded-redis:${property("embedded-redis.version")}")
    }
  }

  tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
      }
    }

    withType<Test> {
      useJUnitPlatform()
    }
  }
}



