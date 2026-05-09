package frgp.utn.edu.kineapp.ui.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.model.Paciente;
import frgp.utn.edu.kineapp.repository.PacienteRepository;

public class FormularioPacienteSimpleActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etApellido, etDni, etTelefono,
            etDireccion, etFechaNacimiento, etEdad, etNumeroAfiliado,
            etEmailPaciente, etSesionesSemanales, etSesionesOrden, 
            etDiagnostico, etObservaciones, etValorSesion,
            etFechaInicioPeriodo, etFechaFinPeriodo;
    private TextInputLayout tilObraSocial, tilNumeroAfiliado, tilSesionesSemanales, 
            tilSesionesOrden, tilValorSesion;
    private LinearLayout layoutPeriodoCud;
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
        etDiagnostico = findViewById(R.id.et_diagnostico);
        etObservaciones = findViewById(R.id.et_observaciones);
        etValorSesion = findViewById(R.id.et_valor_sesion);
        etFechaInicioPeriodo = findViewById(R.id.et_fecha_inicio_periodo);
        etFechaFinPeriodo = findViewById(R.id.et_fecha_fin_periodo);

        tilObraSocial = findViewById(R.id.til_obra_social);
        tilNumeroAfiliado = findViewById(R.id.til_numero_afiliado);
        tilSesionesSemanales = findViewById(R.id.til_sesiones_semanales);
        tilSesionesOrden = findViewById(R.id.til_sesiones_orden);
        tilValorSesion = findViewById(R.id.til_valor_sesion);
        layoutPeriodoCud = findViewById(R.id.layout_periodo_cud);

        switchObraSocial = findViewById(R.id.switch_obra_social);
        switchCud = findViewById(R.id.switch_cud);

        switchObraSocial.setOnCheckedChangeListener((btn, checked) -> {
            tilObraSocial.setVisibility(checked ? View.VISIBLE : View.GONE);
            tilNumeroAfiliado.setVisibility(checked ? View.VISIBLE : View.GONE);
            tilValorSesion.setVisibility(checked ? View.GONE : View.VISIBLE);
            if (!checked) {
                etObraSocial.setText("");
                etNumeroAfiliado.setText("");
            } else {
                etValorSesion.setText("");
            }
        });

        switchCud.setOnCheckedChangeListener((btn, checked) -> {
            tilSesionesSemanales.setVisibility(checked ? View.VISIBLE : View.GONE);
            layoutPeriodoCud.setVisibility(checked ? View.VISIBLE : View.GONE);
            tilSesionesOrden.setVisibility(checked ? View.GONE : View.VISIBLE);
            if (checked) {
                etSesionesOrden.setText("");
            } else {
                etSesionesSemanales.setText("");
                etFechaInicioPeriodo.setText("");
                etFechaFinPeriodo.setText("");
            }
        });

        String[] obrasSocialesOrdenadas = OBRAS_SOCIALES.clone();
        Arrays.sort(obrasSocialesOrdenadas);

        ArrayAdapter<String> osAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, obrasSocialesOrdenadas);
        etObraSocial.setAdapter(osAdapter);
        etObraSocial.setThreshold(1);

        etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker(etFechaNacimiento, true));
        etFechaInicioPeriodo.setOnClickListener(v -> mostrarDatePicker(etFechaInicioPeriodo, false));
        etFechaFinPeriodo.setOnClickListener(v -> mostrarDatePicker(etFechaFinPeriodo, false));

        String pacienteId = getIntent().getStringExtra("pacienteId");
        if (pacienteId != null) {
            toolbar.setTitle("Editar Paciente");
            cargarPaciente(pacienteId);
        } else {
            tilValorSesion.setVisibility(switchObraSocial.isChecked() ? View.GONE : View.VISIBLE);
        }

        MaterialButton btnGuardar = findViewById(R.id.btn_guardar);
        btnGuardar.setOnClickListener(v -> guardarPaciente());
    }

    private void mostrarDatePicker(TextInputEditText editText, boolean esNacimiento) {
        Calendar hoy = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (dp, y, m, d) -> {
                    String fechaStr = String.format(Locale.getDefault(),
                            "%02d/%02d/%04d", d, m + 1, y);
                    editText.setText(fechaStr);
                    
                    if (esNacimiento) {
                        int edad = hoy.get(Calendar.YEAR) - y;
                        if (hoy.get(Calendar.MONTH) < m ||
                                (hoy.get(Calendar.MONTH) == m &&
                                        hoy.get(Calendar.DAY_OF_MONTH) < d)) {
                            edad--;
                        }
                        etEdad.setText(String.valueOf(edad));
                    }
                },
                hoy.get(Calendar.YEAR) - (esNacimiento ? 30 : 0),
                hoy.get(Calendar.MONTH),
                hoy.get(Calendar.DAY_OF_MONTH));
        
        if (esNacimiento) dialog.getDatePicker().setMaxDate(hoy.getTimeInMillis());
        dialog.show();
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
                    etDiagnostico.setText(pacienteExistente.getDiagnostico());
                    etObservaciones.setText(pacienteExistente.getObservaciones());

                    if (pacienteExistente.getEmail() != null)
                        etEmailPaciente.setText(pacienteExistente.getEmail());

                    boolean tieneOS = pacienteExistente.getObraSocial() != null
                            && !pacienteExistente.getObraSocial().isEmpty();
                    switchObraSocial.setChecked(tieneOS);
                    if (tieneOS) {
                        etObraSocial.setText(pacienteExistente.getObraSocial());
                        tilObraSocial.setVisibility(View.VISIBLE);
                        tilNumeroAfiliado.setVisibility(View.VISIBLE);
                        tilValorSesion.setVisibility(View.GONE);
                        if (pacienteExistente.getNumeroAfiliado() != null)
                            etNumeroAfiliado.setText(pacienteExistente.getNumeroAfiliado());
                    } else {
                        tilObraSocial.setVisibility(View.GONE);
                        tilNumeroAfiliado.setVisibility(View.GONE);
                        tilValorSesion.setVisibility(View.VISIBLE);
                        if (pacienteExistente.getValorSesion() > 0)
                            etValorSesion.setText(String.valueOf(pacienteExistente.getValorSesion()));
                    }

                    switchCud.setChecked(pacienteExistente.isCertificadoDiscapacidad());
                    if (pacienteExistente.isCertificadoDiscapacidad()) {
                        tilSesionesSemanales.setVisibility(View.VISIBLE);
                        layoutPeriodoCud.setVisibility(View.VISIBLE);
                        tilSesionesOrden.setVisibility(View.GONE);
                        etSesionesSemanales.setText(String.valueOf(pacienteExistente.getSesionesSemanales()));
                        if (pacienteExistente.getFechaInicioPeriodo() != null)
                            etFechaInicioPeriodo.setText(pacienteExistente.getFechaInicioPeriodo());
                        if (pacienteExistente.getFechaFinPeriodo() != null)
                            etFechaFinPeriodo.setText(pacienteExistente.getFechaFinPeriodo());
                    } else {
                        tilSesionesSemanales.setVisibility(View.GONE);
                        layoutPeriodoCud.setVisibility(View.GONE);
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
        final String nombre = etNombre.getText().toString().trim();
        final String apellido = etApellido.getText().toString().trim();
        final String dni = etDni.getText().toString().trim();
        final String telefono = etTelefono.getText().toString().trim();
        final String direccion = etDireccion.getText().toString().trim();
        final String fechaNac = etFechaNacimiento.getText().toString().trim();
        final String emailPaciente = etEmailPaciente.getText().toString().trim();
        final String diagnostico = etDiagnostico.getText().toString().trim();
        final String observaciones = etObservaciones.getText().toString().trim();
        final boolean tieneOS = switchObraSocial.isChecked();
        final boolean tieneCud = switchCud.isChecked();
        final String obraSocial = tieneOS ? etObraSocial.getText().toString().trim() : "";
        final String numeroAfiliado = tieneOS ? etNumeroAfiliado.getText().toString().trim() : "";
        final String edadStr = etEdad.getText().toString().trim();
        final int edad = edadStr.isEmpty() ? 0 : Integer.parseInt(edadStr);

        final String sesionesSemStr = etSesionesSemanales.getText().toString().trim();
        final String sesionesTotStr = etSesionesOrden.getText().toString().trim();
        final String valorSesionStr = etValorSesion.getText().toString().trim();
        final String fechaInicio = etFechaInicioPeriodo.getText().toString().trim();
        final String fechaFin = etFechaFinPeriodo.getText().toString().trim();

        if (nombre.isEmpty()) { etNombre.setError("Requerido"); etNombre.requestFocus(); return; }
        if (apellido.isEmpty()) { etApellido.setError("Requerido"); etApellido.requestFocus(); return; }
        if (dni.isEmpty()) { etDni.setError("Requerido"); etDni.requestFocus(); return; }
        if (telefono.isEmpty()) { etTelefono.setError("Requerido"); etTelefono.requestFocus(); return; }
        if (direccion.isEmpty()) { etDireccion.setError("Requerido"); etDireccion.requestFocus(); return; }
        if (fechaNac.isEmpty()) { etFechaNacimiento.setError("Requerido"); etFechaNacimiento.requestFocus(); return; }
        if (emailPaciente.isEmpty()) { etEmailPaciente.setError("Requerido"); etEmailPaciente.requestFocus(); return; }
        
        if (tieneCud && sesionesSemStr.isEmpty()) {
            etSesionesSemanales.setError("Requerido"); etSesionesSemanales.requestFocus(); return;
        }
        if (!tieneCud && sesionesTotStr.isEmpty()) {
            etSesionesOrden.setError("Requerido"); etSesionesOrden.requestFocus(); return;
        }

        if (tieneCud) {
            if (fechaInicio.isEmpty()) {
                etFechaInicioPeriodo.setError("Requerido"); etFechaInicioPeriodo.requestFocus(); return;
            }
            if (fechaFin.isEmpty()) {
                etFechaFinPeriodo.setError("Requerido"); etFechaFinPeriodo.requestFocus(); return;
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date start = sdf.parse(fechaInicio);
                Date end = sdf.parse(fechaFin);
                if (start != null && end != null && end.before(start)) {
                    etFechaFinPeriodo.setError("No puede ser anterior al inicio");
                    Toast.makeText(this, "Fecha de fin inválida", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception ignored) {}
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailPaciente).matches()) {
            etEmailPaciente.setError("Email inválido"); return;
        }
        if (tieneOS && obraSocial.isEmpty()) {
            etObraSocial.setError("Seleccioná una obra social"); return;
        }
        if (tieneOS && numeroAfiliado.isEmpty()) {
            etNumeroAfiliado.setError("Requerido"); return;
        }

        final int sesSem = tieneCud ? Integer.parseInt(sesionesSemStr) : 0;
        final int sesTot = !tieneCud ? Integer.parseInt(sesionesTotStr) : 0;
        final double valorSesion = (!tieneOS && !valorSesionStr.isEmpty()) ? Double.parseDouble(valorSesionStr) : 0.0;

        procederConGuardado(nombre, apellido, dni, telefono, direccion, fechaNac, emailPaciente, diagnostico, observaciones, obraSocial, numeroAfiliado, edad, tieneCud, tieneOS, sesSem, sesTot, valorSesion, fechaInicio, fechaFin);
    }

    private void procederConGuardado(String nombre, String apellido, String dni, String telefono, String direccion, String fechaNac, String email, String diagnostico, String observaciones, String obraSocial, String numeroAfiliado, int edad, boolean tieneCud, boolean tieneOS, int sesSem, int sesTot, double valorSesion, String fechaInicio, String fechaFin) {
        if (pacienteExistente == null) {
             repository.buscarPorDni(dni)
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            Toast.makeText(this, "DNI ya registrado", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Paciente nuevo = new Paciente(nombre, apellido, dni, telefono, direccion, diagnostico, obraSocial, numeroAfiliado, tieneCud, null);
                        nuevo.setFechaNacimiento(fechaNac);
                        nuevo.setEdad(edad);
                        nuevo.setEmail(email);
                        nuevo.setParticular(!tieneOS);
                        nuevo.setSesionesSemanales(sesSem);
                        nuevo.setSesionesOrden(sesTot);
                        nuevo.setValorSesion(valorSesion);
                        nuevo.setObservaciones(observaciones);
                        if (tieneCud) {
                            nuevo.setFechaInicioPeriodo(fechaInicio);
                            nuevo.setFechaFinPeriodo(fechaFin);
                        }
                        ejecutarFirebaseGuardado(nuevo);
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
            pacienteExistente.setFechaNacimiento(fechaNac);
            pacienteExistente.setEdad(edad);
            pacienteExistente.setEmail(email);
            pacienteExistente.setParticular(!tieneOS);
            pacienteExistente.setSesionesSemanales(sesSem);
            pacienteExistente.setSesionesOrden(sesTot);
            pacienteExistente.setValorSesion(valorSesion);
            pacienteExistente.setDiagnostico(diagnostico);
            pacienteExistente.setObservaciones(observaciones);
            if (tieneCud) {
                pacienteExistente.setFechaInicioPeriodo(fechaInicio);
                pacienteExistente.setFechaFinPeriodo(fechaFin);
            } else {
                pacienteExistente.setFechaInicioPeriodo(null);
                pacienteExistente.setFechaFinPeriodo(null);
            }
            ejecutarFirebaseGuardado(pacienteExistente);
        }
    }

    private void ejecutarFirebaseGuardado(Paciente p) {
        repository.guardar(p)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Paciente guardado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
