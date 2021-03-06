plugins {
    `kotlin-dsl`
    `maven-publish`
}

// tag::repositories-and-dependencies[]
repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    implementation("gradle.plugin.com.github.spotbugs.snom:spotbugs-gradle-plugin:4.0.5")
    testImplementation("junit:junit:4.12")
}
// end::repositories-and-dependencies[]

// tag::publishing[]
group = "com.example.conventions"
version = "1.0"

// NOTE: This is only needed to demonstrate publishing the plugin in this sample.
// Your plugin should be published to an artifact repository or to the Plugin Portal
publishing {
    repositories {
        maven {
            url = uri("build/my-repo")
        }
    }
}
// end::publishing[]
