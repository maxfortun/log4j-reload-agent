/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java library project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.9/userguide/building_java_projects.html in the Gradle documentation.
 * This project uses @Incubating APIs which are subject to change.
 */

base {
	archivesName.set(rootProject.name)
}

plugins {
	`java-library`
	`project-report`
}

repositories {
	// Use Maven Central for resolving dependencies.
	mavenCentral()
	mavenLocal()
}

dependencies {
	// This dependency is exported to consumers, that is to say found on their compile classpath.
	api(libs.commons.math3)

	// This dependency is used internally, and not exposed to consumers on their own compile classpath.
	implementation(libs.guava)

	implementation("ch.qos.reload4j:reload4j:1.2.25")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

tasks {
	jar {
		manifest {
			attributes(mapOf(
				"Implementation-Title" to rootProject.name,
				"Implementation-Version" to rootProject.version,
				"Premain-Class" to "org.apache.log4j.ReloadAgent"
			))
		}
	}

	test {
		jvmArgs("-javaagent:${layout.buildDirectory.get()}/libs/${rootProject.name}-${rootProject.version}.jar")
	}
}

testing {
	suites {
		// Configure the built-in test suite
		val test by getting(JvmTestSuite::class) {
			// Use JUnit Jupiter test framework
			useJUnitJupiter("5.10.2")
		}
	}
}
