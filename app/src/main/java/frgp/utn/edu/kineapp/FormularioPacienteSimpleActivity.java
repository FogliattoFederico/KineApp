package frgp.utn.edu.kineapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class FormularioPacienteSimpleActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etApellido, etDni, etTelefono,
            etDireccion, etFechaNacimiento, etEdad, etNumeroAfiliado,
            etEmailPaciente, etSesionesSemanales, etSesionesOrden;
    private TextInputLayout tilObraSocial, tilNumeroAfiliado, tilSesionesSemanales, tilSesionesOrden;
    private AutoCompleteTextView etObraSocial;
    private SwitchMaterial switchObraSocial, switchCud;
    private PacienteRepository repository;
    private Paciente pacienteExistente = null;

    public static final String[] OBRAS_SOCIALES = {
            "Alianza Médica S.A.", "AMR Salud", "Amoeiag", "Andina ART",
            "Asoc. Mutual Sancor", "Avalian", "Británica Salud",
            "Caja de Ingenieros", "Caja Forense 1ra. Circ.",
            "Caja Forense 2da. Circ.", "Centro Asistencial Rafaela",
            "Ciencias Económicas", "Colonia Suiza ART",
            "Conferencia Episcopal Argentina",
            "Dasuten", "Docto Red", "Docthos", "Energía Salud",
            "Ensalud S.A.", "Federación Médica", "Galeno ART",
            "Grupo San Nicolás", "IAPOS", "IAPOS - Accidente de Tránsito",
            "IAPOS - Accidente de Trabajo", "IAPOS - Convenio Recíproco",
            "IAPOS - Discapacidad", "Iter Medicina S.A.", "IOSFA",
            "Jerárquicos Salud", "La Segunda ART", "La Segunda Personas",
            "Luis Pasteur", "Medicar Work", "Mutual Federada",
            "Mutualyf / Osfatlyf", "Omint", "OPDEA", "OSDE", "OSPAC",
            "OSPESGA", "OSPSA", "OSUNR", "Poder Judicial",
            "Prevención Salud S.A.", "Sadaic", "Sindicato de Prensa",
            "Sutiaga", "Swiss Medical Group", "Televisión",
            "Unión Personal", "William Hope", "Colegio de Kinesiologos"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_paciente_simple);

        repository = new PacienteRepository();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etNombre = findViewById(R.id.et_nombre);
        etApellido = findViewById(R.id.et_apellido);
        etDni = findViewById(R.id.et_dni);
        etTelefono = findViewById(R.id.et_telefono);
        etDireccion = findViewById(R.id.et_direccion);
        etFechaNacimiento = findViewById(R.id.et_fecha_nacimiento);
        etEdad = findViewById(R.id.et_edad);
        etObraSocial = findViewById(R.id.et_obra_social);
        etNumeroAfiliado = findViewById(R.id.et_numero_afiliado);
        etEmailPaciente = findViewById(R.id.et_email_paciente);
        etSesionesSemanales = findViewById(R.id.et_sesiones_semanales);
        etSesionesOrden = findViewById(R.id.et_sesiones_orden);

        tilObraSocial = findViewById(R.id.til_obra_social);
        tilNumeroAfiliado = findViewById(R.id.til_numero_afiliado);
        tilSesionesSemanales = findViewById(R.id.til_sesiones_semanales);
        tilSesionesOrden = findViewById(R.id.til_sesiones_orden);

        switchObraSocial = findViewById(R.id.switch_obra_social);
        switchCud = findViewById(R.id.switch_cud);

        switchObraSocial.setOnCheckedChangeListener((btn, checked) -> {
            tilObraSocial.setVisibility(checked ? View.VISIBLE : View.GONE);
            tilNumeroAfiliado.setVisibility(checked ? View.VISIBLE : View.GONE);
            if (!checked) {
                etObraSocial.setText("");
                etNumeroAfiliado.setText("");
            }
        });

        switchCud.setOnCheckedChangeListener((btn, checked) -> {
            tilSesionesSemanales.setVisibility(checked ? View.VISIBLE : View.GONE);
            tilSesionesOrden.setVisibility(checked ? View.GONE : View.VISIBLE);
            if (checked) {
                etSesionesOrden.setText("");
            } else {
                etSesionesSemanales.setText("");
            }
        });

        String[] obrasSocialesOrdenadas = OBRAS_SOCIALES.clone();
        Arrays.sort(obrasSocialesOrdenadas);

        ArrayAdapter<String> osAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, obrasSocialesOrdenadas);
        etObraSocial.setAdapter(osAdapter);
        etObraSocial.setThreshold(1);

        etFechaNacimiento.setOnClickListener(v -> {
            Calendar hoy = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (dp, y, m, d) -> {
                        String fechaStr = String.format(Locale.getDefault(),
                                "%02d/%02d/%04d", d, m + 1, y);
                        etFechaNacimiento.setText(fechaStr);
                        int edad = hoy.get(Calendar.YEAR) - y;
                        if (hoy.get(Calendar.MONTH) < m ||
                                (hoy.get(Calendar.MONTH) == m &&
                                        hoy.get(Calendar.DAY_OF_MONTH) < d)) {
                            edad--;
                        }
                        etEdad.setText(String.valueOf(edad));
                    },
                    hoy.get(Calendar.YEAR) - 30,
                    hoy.get(Calendar.MONTH),
                    hoy.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMaxDate(hoy.getTimeInMillis());
            dialog.show();
        });

        String pacienteId = getIntent().getStringExtra("pacienteId");
        if (pacienteId != null) {
            toolbar.setTitle("Editar Paciente");
            cargarPaciente(pacienteId);
        }

        MaterialButton btnGuardar = findViewById(R.id.btn_guardar);
        btnGuardar.setOnClickListener(v -> guardarPaciente());
    }

    private void cargarPaciente(String pacienteId) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("pacientes")
                .document(pacienteId)
                .get()
                .addOnSuccessListener(doc -> {
                    pacienteExistente = doc.toObject(Paciente.class);
                    if (pacienteExistente == null) return;
                    pacienteExistente.setId(doc.getId());
                    etNombre.setText(pacienteExistente.getNombre());
                    etApellido.setText(pacienteExistente.getApellido());
                    etDni.setText(pacienteExistente.getDni());
                    etTelefono.setText(pacienteExistente.getTelefono());
                    etDireccion.setText(pacienteExistente.getDireccion());

                    if (pacienteExistente.getEmail() != null)
                        etEmailPaciente.setText(pacienteExistente.getEmail());

                    boolean tieneOS = pacienteExistente.getObraSocial() != null
                            && !pacienteExistente.getObraSocial().isEmpty();
                    switchObraSocial.setChecked(tieneOS);
                    if (tieneOS) {
                        etObraSocial.setText(pacienteExistente.getObraSocial());
                        tilObraSocial.setVisibility(View.VISIBLE);
                        tilNumeroAfiliado.setVisibility(View.VISIBLE);
                        if (pacienteExistente.getNumeroAfiliado() != null)
                            etNumeroAfiliado.setText(pacienteExistente.getNumeroAfiliado());
                    }

                    switchCud.setChecked(pacienteExistente.isCertificadoDiscapacidad());
                    if (pacienteExistente.isCertificadoDiscapacidad()) {
                        tilSesionesSemanales.setVisibility(View.VISIBLE);
                        tilSesionesOrden.setVisibility(View.GONE);
                        etSesionesSemanales.setText(String.valueOf(pacienteExistente.getSesionesSemanales()));
                    } else {
                        tilSesionesSemanales.setVisibility(View.GONE);
                        tilSesionesOrden.setVisibility(View.VISIBLE);
                        etSesionesOrden.setText(String.valueOf(pacienteExistente.getSesionesOrden()));
                    }

                    if (pacienteExistente.getFechaNacimiento() != null)
                        etFechaNacimiento.setText(pacienteExistente.getFechaNacimiento());
                    if (pacienteExistente.getEdad() > 0)
                        etEdad.setText(String.valueOf(pacienteExistente.getEdad()));
                });
    }

    private void guardarPaciente() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String fechaNac = etFechaNacimiento.getText().toString().trim();
        String emailPaciente = etEmailPaciente.getText().toString().trim();
        boolean tieneOS = switchObraSocial.isChecked();
        boolean tieneCud = switchCud.isChecked();
        String obraSocial = tieneOS ? etObraSocial.getText().toString().trim() : "";
        String numeroAfiliado = tieneOS ? etNumeroAfiliado.getText().toString().trim() : "";
        String edadStr = etEdad.getText().toString().trim();
        int edad = edadStr.isEmpty() ? 0 : Integer.parseInt(edadStr);

        String sesionesSemStr = etSesionesSemanales.getText().toString().trim();
        String sesionesTotStr = etSesionesOrden.getText().toString().trim();

        if (nombre.isEmpty()) { etNombre.setError("Requerido"); etNombre.requestFocus(); return; }
        if (apellido.isEmpty()) { etApellido.setError("Requerido"); etApellido.requestFocus(); return; }
        if (dni.isEmpty()) { etDni.setError("Requerido"); etDni.requestFocus(); return; }
        if (telefono.isEmpty()) { etTelefono.setError("Requerido"); etTelefono.requestFocus(); return; }
        if (direccion.isEmpty()) { etDireccion.setError("Requerido"); etDireccion.requestFocus(); return; }
        if (fechaNac.isEmpty()) { etFechaNacimiento.setError("Requerido"); etFechaNacimiento.requestFocus(); return; }
        if (emailPaciente.isEmpty()) { etEmailPaciente.setError("Requerido"); etEmailPaciente.requestFocus(); return; }
        
        if (tieneCud && sesionesSemStr.isEmpty()) {
            etSesionesSemanales.setError("Requerido");
            etSesionesSemanales.requestFocus();
            return;
        }
        if (!tieneCud && sesionesTotStr.isEmpty()) {
            etSesionesOrden.setError("Requerido");
            etSesionesOrden.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailPaciente).matches()) {
            etEmailPaciente.setError("Email inválido");
            etEmailPaciente.requestFocus();
            return;
        }
        if (tieneOS && obraSocial.isEmpty()) {
            etObraSocial.setError("Seleccioná una obra social");
            etObraSocial.requestFocus();
            return;
        }
        if (tieneOS && numeroAfiliado.isEmpty()) {
            etNumeroAfiliado.setError("Requerido");
            etNumeroAfiliado.requestFocus();
            return;
        }

        final int edadFinal = edad;
        final String fechaNacFinal = fechaNac;
        final String emailFinal = emailPaciente;
        final int sesSem = tieneCud ? Integer.parseInt(sesionesSemStr) : 0;
        final int sesTot = !tieneCud ? Integer.parseInt(sesionesTotStr) : 0;

        if (pacienteExistente == null) {
            repository.buscarPorDni(dni)
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            Toast.makeText(this, "Ya existe un paciente con ese DNI",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        Paciente nuevo = new Paciente(nombre, apellido, dni,
                                telefono, direccion, "", obraSocial,
                                numeroAfiliado, tieneCud, null);
                        nuevo.setFechaNacimiento(fechaNacFinal);
                        nuevo.setEdad(edadFinal);
                        nuevo.setEmail(emailFinal);
                        nuevo.setParticular(!tieneOS);
                        nuevo.setSesionesSemanales(sesSem);
                        nuevo.setSesionesOrden(sesTot);
                        repository.guardar(nuevo)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Paciente guardado",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show());
                    });
        } else {
            pacienteExistente.setNombre(nombre);
            pacienteExistente.setApellido(apellido);
            pacienteExistente.setDni(dni);
            pacienteExistente.setTelefono(telefono);
            pacienteExistente.setDireccion(direccion);
            pacienteExistente.setObraSocial(obraSocial);
            pacienteExistente.setNumeroAfiliado(numeroAfiliado);
            pacienteExistente.setCertificadoDiscapacidad(tieneCud);
            pacienteExistente.setFechaNacimiento(fechaNacFinal);
            pacienteExistente.setEdad(edadFinal);
            pacienteExistente.setEmail(emailFinal);
            pacienteExistente.setParticular(!tieneOS);
            pacienteExistente.setSesionesSemanales(sesSem);
            pacienteExistente.setSesionesOrden(sesTot);
            repository.guardar(pacienteExistente)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Paciente guardado",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show());
        }
    }
}
