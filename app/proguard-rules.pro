# ProGuard / R8 Rules for Jyotish AI

# Keep Room Database and DAO implementations
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Data Models and Entities (Room, Moshi, Retrofit, Firestore)
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
    @com.squareup.moshi.JsonClass *;
}
-keep class com.example.model.** { *; }
-keep class com.example.data.** { *; }
-keep class com.example.ai.** { *; }
-keep class com.example.astro.** { *; }
-keep class com.example.billing.** { *; }

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Google Play Billing Library v8
-keep class com.android.billingclient.api.** { *; }

# Retrofit / OkHttp / Okio
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepattributes SourceFile, LineNumberTable
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
