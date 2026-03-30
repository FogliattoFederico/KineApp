package frgp.utn.edu.kineapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.ui.activity.LoginActivity;

public class PerfilFragment extends Fragment {

    private TextView tvAvatar;
    private TextInputEditText etNombre, etApellido, etMatricula,
            etEmail, etPassword, etConfirmPassword, etBoxes, etDireccionConsultorio;
    private TextInputLayout tilBoxes, tilDireccionConsultorio;
    private ChipGroup chipGroupModalidad;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvAvatar = view.findViewById(R.id.tv_avatar);
        etNombre = view.findViewById(R.id.et_nombre);
        etApellido = view.findViewById(R.id.et_apellido);
        etMatricula = view.findViewById(R.id.et_matricula);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        etBoxes = view.findViewById(R.id.et_boxes);
        tilBoxes = view.findViewById(R.id.til_boxes);
        etDireccionConsultorio = view.findViewById(R.id.et_direccion_consultorio);
        tilDireccionConsultorio = view.findViewById(R.id.til_direccion_consultorio);
        chipGroupModalidad = view.findViewById(R.id.chip_group_modalidad);

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

        MaterialButton btnGuardar = view.findViewById(R.id.btn_guardar_perfil);
        MaterialButton btnCerrar = view.findViewById(R.id.btn_cerrar_sesion);

        cargarDatos();

        btnGuardar.setOnClickListener(v -> guardarCambios());

        btnCerrar.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void cargarDatos() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        etEmail.setText(user.getEmail());

        db.collection("usuarios").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String nombre = doc.getString("nombre");
                    String apellido = doc.getString("apellido");
                    String matricula = doc.getString("matricula");
                    String modalidad = doc.getString("modalidadTrabajo");
                    String direccionC = doc.getString("direccionConsultorio");
                    Long boxes = doc.getLong("cantidadBoxes");

                    if (nombre != null) {
                        etNombre.setText(nombre);
                        tvAvatar.setText(nombre.substring(0, 1).toUpperCase());
                    }
                    if (apellido != null) etApellido.setText(apellido);
                    if (matricula != null) etMatricula.setText(matricula);
                    if (direccionC != null) etDireccionConsultorio.setText(direccionC);

                    if (modalidad != null) {
                        if (modalidad.equals("domicilio") || modalidad.equals("ambos")) {
                            Chip chip = chipGroupModalidad.findViewById(R.id.chip_domicilio);
                            if (chip != null) chip.setChecked(true);
                        }
                        if (modalidad.equals("consultorio") || modalidad.equals("ambos")) {
                            Chip chip = chipGroupModalidad.findViewById(R.id.chip_consultorio);
                            if (chip != null) chip.setChecked(true);
                            tilBoxes.setVisibility(View.VISIBLE);
                            tilDireccionConsultorio.setVisibility(View.VISIBLE);
                        }
                    }

                    if (boxes != null && boxes > 0) {
                        etBoxes.setText(String.valueOf(boxes));
                    }
                });
    }

    private String getModalidadSeleccionada() {
        boolean domicilio = false;
        boolean consultorio = false;
        for (int id : chipGroupModalidad.getCheckedChipIds()) {
            Chip chip = chipGroupModalidad.findViewById(id);
            if (chip != null) {
                if (chip.getText().toString().equals("Domicilio")) domicilio = true;
                if (chip.getText().toString().equals("Consultorio")) consultorio = true;
            }
        }
        if (domicilio && consultorio) return "ambos";
        if (consultorio) return "consultorio";
        if (domicilio) return "domicilio";
        return "";
    }

    private void guardarCambios() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String matricula = etMatricula.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String modalidad = getModalidadSeleccionada();
        String direccionC = etDireccionConsultorio.getText().toString().trim();

        if (nombre.isEmpty() || apellido.isEmpty()) {
            Toast.makeText(getContext(), "Nombre y apellido son obligatorios",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (modalidad.isEmpty()) {
            Toast.makeText(getContext(), "Seleccioná al menos una modalidad",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int boxes = 0;
        if (modalidad.equals("consultorio") || modalidad.equals("ambos")) {
            String boxesStr = etBoxes.getText().toString().trim();
            if (boxesStr.isEmpty()) {
                Toast.makeText(getContext(), "Ingresá la cantidad de boxes",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            boxes = Integer.parseInt(boxesStr);
            if (direccionC.isEmpty()) {
                Toast.makeText(getContext(), "Ingresá la dirección del consultorio",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);
        datos.put("apellido", apellido);
        datos.put("matricula", matricula);
        datos.put("modalidadTrabajo", modalidad);
        datos.put("cantidadBoxes", boxes);
        datos.put("direccionConsultorio", direccionC);

        db.collection("usuarios").document(user.getUid())
                .update(datos)
                .addOnSuccessListener(a -> {
                    if (!email.equals(user.getEmail())) {
                        user.updateEmail(email)
                                .addOnSuccessListener(b ->
                                        Toast.makeText(getContext(),
                                                "Perfil actualizado",
                                                Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Error al actualizar email: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show()
                                );
                    } else {
                        Toast.makeText(getContext(), "Perfil actualizado",
                                Toast.LENGTH_SHORT).show();
                    }

                    if (!password.isEmpty()) {
                        user.updatePassword(password)
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Error al actualizar contraseña: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show()
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
}
