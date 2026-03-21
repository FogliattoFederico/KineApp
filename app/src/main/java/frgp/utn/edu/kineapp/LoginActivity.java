package frgp.utn.edu.kineapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private MaterialButton btnBiometric;
    private TextView tvRegister, tvForgotPassword;
    private FirebaseAuth mAuth;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        executor = ContextCompat.getMainExecutor(this);
        
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            prefs = EncryptedSharedPreferences.create(
                "KineAppPrefs", masterKeyAlias, this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            prefs = getSharedPreferences("KineAppPrefs", MODE_PRIVATE);
        }

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnBiometric = findViewById(R.id.btn_biometric);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        btnLogin.setOnClickListener(v -> loginUser());
        btnBiometric.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class))
        );

        setupBiometrics();
    }

    private void setupBiometrics() {
        biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                String savedEmail = prefs.getString("last_email", "");
                String savedPassword = prefs.getString("last_pass", "");
                
                // SEGURIDAD: Solo loguear si tenemos credenciales guardadas
                if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
                    etEmail.setText(savedEmail);
                    etPassword.setText(savedPassword);
                    loginUser();
                } else {
                    Toast.makeText(LoginActivity.this, "Iniciá sesión manualmente una vez", Toast.LENGTH_SHORT).show();
                }
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Acceso Biométrico")
                .setSubtitle("Protegiendo tus datos")
                .setNegativeButtonText("Usar contraseña")
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String savedEmail = prefs.getString("last_email", "");
        
        // MOSTRAR HUELLA SOLO SI HAY DATOS GUARDADOS
        if (!savedEmail.isEmpty()) {
            btnBiometric.setVisibility(View.VISIBLE);
            etEmail.setText(savedEmail);
            
            // Si hay sesión activa en Firebase Y datos locales, pedimos huella
            if (currentUser != null) {
                biometricPrompt.authenticate(promptInfo);
            }
        } else {
            btnBiometric.setVisibility(View.GONE);
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) return;

        btnLogin.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        prefs.edit().putString("last_email", email).putString("last_pass", password).apply();
                        goToMain();
                    } else {
                        Toast.makeText(this, "Error de acceso", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}