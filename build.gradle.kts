plugins {
    // Порядок не критичен здесь, так как apply = false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.3.7" apply false  // ✅ KSP 2.3.7 — актуальная версия для 2026 [[14]]
    id("com.google.dagger.hilt.android") version "2.59.2" apply false
}