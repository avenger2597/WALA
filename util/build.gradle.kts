import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.errorprone.CheckSeverity

plugins {
  id("com.ibm.wala.gradle.java")
  id("com.ibm.wala.gradle.NullAway")
  id("com.ibm.wala.gradle.publishing")
  id("net.ltgt.errorprone")
}

repositories {
  // to get the google-java-format jar and dependencies
  mavenCentral()
  mavenLocal()
}

eclipse.project.natures("org.eclipse.pde.PluginNature")

dependencies {
  // Annotation processors
  annotationProcessor("edu.ucr.cs.riple.annotator:annotator-scanner:1.3.16-SNAPSHOT")
  annotationProcessor("com.uber.nullaway:nullaway:0.12.4")

  // ErrorProne and its javac
  errorprone("com.google.errorprone:error_prone_core:2.4.0")
  errorproneJavac("com.google.errorprone:javac:9+181-r4173-1")

  // Needed annotations
  compileOnly("com.uber.nullaway:nullaway-annotations:0.12.4")
  compileOnly("com.google.code.findbugs:jsr305:3.0.2")
  compileOnly("org.jspecify:jspecify:0.3.0")

  api(libs.jspecify)
  javadocClasspath(projects.core)
  testImplementation(libs.hamcrest)
  testImplementation(libs.junit.jupiter.api)
}

tasks.named<Javadoc>("javadoc") {
  val currentJavaVersion = JavaVersion.current()
  val linksPrefix = if (currentJavaVersion >= JavaVersion.VERSION_11) "en/java/" else ""
  (options as StandardJavadocDocletOptions).run {
    links(
      "https://docs.oracle.com/${linksPrefix}javase/${currentJavaVersion.majorVersion}/docs/api/")
    source = "8" // workaround https://bugs.openjdk.java.net/browse/JDK-8212233.
  }
}



// Same config-path variables from Groovy
val scanner_config_path = "/home/ankit/fork/WALA/util/annotator-out/scanner.xml"
val nullaway_config_path = "/home/ankit/fork/WALA/util/annotator-out/nullaway.xml"

// Keep the same logic in a Kotlin DSL form
tasks.withType<JavaCompile>().configureEach {
  if (!name.lowercase().contains("test")) {
    options.errorprone {
      check("NullAway", CheckSeverity.ERROR)
      check("AnnotatorScanner", CheckSeverity.ERROR)
      option("NullAway:AnnotatedPackages", "com.ibm.wala")
      option("NullAway:SerializeFixMetadata", "true")
      option("NullAway:FixSerializationConfigPath", nullaway_config_path)
      option("AnnotatorScanner:ConfigPath", scanner_config_path)
    }
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "100000", "-Xmaxwarns", "100000"))
  }
}