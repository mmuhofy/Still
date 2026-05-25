# Still — ProGuard Rules

# Keep Room entities
-keep class com.still.app.data.local.entity.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Kotlin Serialization (if added later)
# -keepattributes *Annotation*, InnerClasses
# -keep class kotlinx.serialization.** { *; }

# Suppress warnings for missing classes not used at runtime
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**