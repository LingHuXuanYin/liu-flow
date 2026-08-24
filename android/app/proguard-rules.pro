# Flow ProGuard rules
# Keep kotlinx-serialization metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <methods>;
}

# Room
-keep class androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
