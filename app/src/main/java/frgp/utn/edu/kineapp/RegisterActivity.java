package frgp.utn.edu.kineapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etApellido, etMatricula, etEmail, etPassword,
            etConfirmPassword, etBoxes, etDireccionConsultorio;
    private TextInputLayout tilBoxes, tilDireccionConsultorio;
    private ChipGroup chipGroupModalidad;
    private Button btnRegister;
    private TextView tvLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etNombre = findViewById(R.id.et_nombre);
        etApellido = findViewById(R.id.et_apellido);
        etMatricula = findViewById(R.id.et_matricula);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etBoxes = findViewById(R.id.et_boxes);
        tilBoxes = findViewById(R.id.til_boxes);
        etDireccionConsultorio = findViewById(R.id.et_direccion_consultorio);
        tilDireccionConsultorio = findViewById(R.id.til_direccion_consultorio);
        chipGroupModalidad = findViewById(R.id.chip_group_modalidad);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        chipGroupModalidad.setOnCheckedStateChangeListener((group, checkedIds) -> {
            boolean tieneConsultorio = false;
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                if (chip != null && chip.getText().toString().equals("Consultorio")) {
                    tieneConsultorio = true;
                    break;
                }
            }
            tilBoxes.setVisibility(tieneConsultorio ? View.VISIBLE : View.GONE);
            tilDireccionConsultorio.setVisibility(tieneConsultorio ? View.VISIBLE : View.GONE);
        });

        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String matricula = etMatricula.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String direccionC = etDireccionConsultorio.getText().toString().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || matricula.isEmpty() ||
                email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completá todos los campos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Modalidad
        java.util.List<Integer> checkedIds = chipGroupModalidad.getCheckedChipIds();
        if (checkedIds.isEmpty()) {
            Toast.makeText(this, "Seleccioná al menos una modalidad de trabajo",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        boolean tieneDomicilio = false;
        boolean tieneConsultorio = false;
        for (int id : checkedIds) {
            Chip chip = chipGroupModalidad.findViewById(id);
            if (chip != null) {
                if (chip.getText().toString().equals("Domicilio")) tieneDomicilio = true;
                if (chip.getText().toString().equals("Consultorio")) tieneConsultorio = true;
            }
        }

        String modalidad;
        if (tieneDomicilio && tieneConsultorio) modalidad = "ambos";
        else if (tieneConsultorio) modalidad = "consultorio";
        else modalidad = "domicilio";

        int boxes = 0;
        if (tieneConsultorio) {
            String boxesStr = etBoxes.getText().toString().trim();
            if (boxesStr.isEmpty()) {
                Toast.makeText(this, "Ingresá la cantidad de boxes",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            boxes = Integer.parseInt(boxesStr);
            
            if (direccionC.isEmpty()) {
                Toast.makeText(this, "Ingresá la dirección del consultorio",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        btnRegister.setEnabled(false);
        final String modalidadFinal = modalidad;
        final int boxesFinal = boxes;
        final String matriculaFinal = matricula;
        final String direccionFinal = direccionC;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        guardarPerfil(uid, nombre, apellido, matriculaFinal, email,
                                modalidadFinal, boxesFinal, direccionFinal);
                    } else {
                        btnRegister.setEnabled(true);
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void guardarPerfil(String uid, String nombre, String apellido, String matricula,
                               String email, String modalidad, int boxes, String direccionConsultorio) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombre", nombre);
        usuario.put("apellido", apellido);
        usuario.put("matricula", matricula);
        usuario.put("email", email);
        usuario.put("rol", "kinesiologo");
        usuario.put("plan", "free");
        usuario.put("modalidadTrabajo", modalidad);
        usuario.put("cantidadBoxes", boxes);
        usuario.put("direccionConsultorio", direccionConsultorio);
        usuario.put("fechaRegistro", com.google.firebase.Timestamp.now());

        db.collection("usuarios").document(uid)
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cuenta creada correctamente",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
