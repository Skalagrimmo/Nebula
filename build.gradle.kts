// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  // Puts Kotlin 2.4.21 on the build classpath: AGP 9 built-in Kotlin compiles with this
  // version instead of falling back to the Kotlin bundled with AGP (2.2.10).
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false
}
