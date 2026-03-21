# Reglas para que Firebase funcione correctamente en versiones Release
-keepattributes Signature
-keepattributes *Annotation*

# Evita que se ofusquen las clases de tu paquete principal (Modelos de datos)
-keep class frgp.utn.edu.kineapp.** { *; }

# Específicamente para Firebase Firestore
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.internal.** { *; }

# Mantener los nombres de los métodos getter/setter que usa Firebase
-keepclassmembers class frgp.utn.edu.kineapp.** {
    void set*(***);
    *** get*();
    *** is*();
}