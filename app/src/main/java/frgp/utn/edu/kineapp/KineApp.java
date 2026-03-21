package frgp.utn.edu.kineapp;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.auth.FirebaseAuth;

public class KineApp extends Application implements Application.ActivityLifecycleCallbacks {

    private Handler logoutHandler = new Handler(Looper.getMainLooper());
    private Runnable logoutRunnable;
    private int activityCount = 0;
    
    // Tiempo de espera antes de cerrar sesión (30 segundos)
    private final long LOGOUT_TIMEOUT = 30000; 

    @Override
    public void onCreate() {
        super.onCreate();
        
        // CAMBIO CLAVE: Permitir que la App siga la configuración del sistema (Claro/Oscuro)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        
        registerActivityLifecycleCallbacks(this);
        
        logoutRunnable = () -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        };
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        activityCount++;
        logoutHandler.removeCallbacks(logoutRunnable);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        activityCount--;
        if (activityCount == 0) {
            logoutHandler.postDelayed(logoutRunnable, LOGOUT_TIMEOUT);
        }
    }

    @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
    @Override public void onActivityResumed(@NonNull Activity activity) {}
    @Override public void onActivityPaused(@NonNull Activity activity) {}
    @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
    @Override public void onActivityDestroyed(@NonNull Activity activity) {}
}