pluginManagement {
	repositories {
		maven { url = uri("https://maven.vaadin.com/vaadin-prereleases") }
		gradlePluginPortal()
		mavenCentral()
	}
	plugins {
		id("com.vaadin") version "25.1.5"
	}
}

rootProject.name = "ai-training-5th-may-breakout-2"
