package frgp.utn.edu.kineapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnReset;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.et_email);
        btnReset = findViewById(R.id.btn_reset);

        btnReset.setOnClickListener(v -> enviarEmailRecuperacion());
        findViewById(R.id.tv_back_login).setOnClickListener(v -> finish());
    }

    private void enviarEmailRecuperacion() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Ingresá tu email", Toast.LENGTH_SHORT).show();
            return;
        }

        btnReset.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnReset.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Se envió el email de recuperación a " + email,
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}