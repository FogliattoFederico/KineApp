package frgp.utn.edu.kineapp.ui.activity;

import android.content.Intent;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.model.HorarioAtencion;
import frgp.utn.edu.kineapp.model.Paciente;
import frgp.utn.edu.kineapp.repository.PacienteRepository;

public class FormularioPacienteActivity extends AppCompatActivity {

    private TextInputEditText etBuscar, etNombre, etApellido, etDni,
            etTelefono, etDireccion, etDiagnostico, etObservaciones,
            etObraSocial, etNumeroAfiliado, etSesionesSemanales,
            etSesionesOrden, etValorSesion, etEmailPaciente;
    private TextInputLayout tilSesionesSemanales, tilSesionesOrden,
            tilObraSocial, tilNumeroAfiliado, tilValorSesion, tilEmailPaciente, tilBuscar;
    private SwitchMaterial switchDiscapacidad, switchParticular;
    private ChipGroup chipGroupModalidad;
    private LinearLayout containerHorarios, layoutFormulario;
    private MaterialButton btnAgregarHorario, btnBuscarPaciente, btnCrearNuevo;
    private MaterialCardView cardPacienteEncontrado;
    private TextView tvPacienteEncontradoNombre, tvPacienteEncontradoDni, tvPacienteEncontradoEmail, tvSesionesRealizadas;
    private PacienteRepository repository;
    private Paciente pacienteExistente = null;
    private List<HorarioAtencion> horariosOriginales = new ArrayList<>();
    private View layoutCud;
    private TextView tvTituloCud;
    private int cantidadBoxes = 1;
    private boolean modoEdicion = false;
    private int indiceEdicionIndividual = -1;
    private String professionalNombre = "";
    private String professionalDireccionConsultorio = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_paciente);

        repository = new PacienteRepository();
        cargarDatosProfesional();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etBuscar = findViewById(R.id.et_buscar_dni);
        tilBuscar = findViewById(R.id.til_buscar_dni);
        if (tilBuscar != null) tilBuscar.setHint("DNI, Nombre o Apellido");
        
        btnBuscarPaciente = findViewById(R.id.btn_buscar_paciente);
        btnCrearNuevo = findViewById(R.id.btn_crear_nuevo_paciente);
        cardPacienteEncontrado = findViewById(R.id.card_paciente_encontrado);
        tvPacienteEncontradoDni = findViewById(R.id.tv_paciente_encontrado_dni);
        tvPacienteEncontradoNombre = findViewById(R.id.tv_paciente_encontrado_nombre);
        tvPacienteEncontradoEmail = findViewById(R.id.tv_paciente_email);
        tvSesionesRealizadas = findViewById(R.id.tv_sesiones_realizadas);
        layoutFormulario = findViewById(R.id.layout_formulario_completo);

        etNombre = findViewById(R.id.et_nombre);
        etApellido = findViewById(R.id.et_apellido);
        etDni = findViewById(R.id.et_dni);
        etTelefono = findViewById(R.id.et_telefono);
        etDireccion = findViewById(R.id.et_direccion);
        etDiagnostico = findViewById(R.id.et_diagnostico);
        etObservaciones = findViewById(R.id.et_observaciones);
        etObraSocial = findViewById(R.id.et_obra_social);
        etNumeroAfiliado = findViewById(R.id.et_numero_afiliado);
        etSesionesSemanales = findViewById(R.id.et_sesiones_semanales);
        etSesionesOrden = findViewById(R.id.et_sesiones_orden);
        etValorSesion = findViewById(R.id.et_valor_sesion);
        etEmailPaciente = findViewById(R.id.et_email_paciente);

        tilSesionesSemanales = findViewById(R.id.til_sesiones_semanales);
        tilSesionesOrden = findViewById(R.id.til_sesiones_orden);
        tilObraSocial = findViewById(R.id.til_obra_social);
        tilNumeroAfiliado = findViewById(R.id.til_numero_afiliado);
        tilValorSesion = findViewById(R.id.til_valor_sesion);
        tilEmailPaciente = findViewById(R.id.til_email_paciente);

        switchDiscapacidad = findViewById(R.id.switch_discapacidad);
        switchParticular = findViewById(R.id.switch_particular);
        chipGroupModalidad = findViewById(R.id.chip_group_modalidad);
        containerHorarios = findViewById(R.id.container_horarios);
        btnAgregarHorario = findViewById(R.id.btn_agregar_horario);
        layoutCud = findViewById(R.id.card_cud);
        tvTituloCud = findViewById(R.id.tv_titulo_cud);

        layoutFormulario.setVisibility(View.GONE);

        btnBuscarPaciente.setOnClickListener(v -> buscarPacientes());
        
        btnCrearNuevo.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormularioPacienteSimpleActivity.class);
            startActivity(intent);
        });

        btnAgregarHorario.setOnClickListener(v -> {
            if (puedeAgregarHorario()) {
                agregarFilaHorario(null);
                actualizarBotonHorario();
            }
        });

        MaterialButton btnGuardar = findViewById(R.id.btn_guardar);
        btnGuardar.setOnClickListener(v -> guardarPaciente());

        String pacienteId = getIntent().getStringExtra("pacienteId");
        indiceEdicionIndividual = getIntent().getIntExtra("horarioIndice", -1);

        if (pacienteId != null) {
            modoEdicion = true;
            toolbar.setTitle(indiceEdicionIndividual != -1 ? "Editar Turno" : "Editar Turnos");
            findViewById(R.id.layout_buscar_dni).setVisibility(View.GONE);
            layoutFormulario.setVisibility(View.VISIBLE);
            cargarPacienteParaEditar(pacienteId);
        } else {
            toolbar.setTitle("Asignar turno");
            actualizarBotonHorario();
        }
    }

    private void cargarDatosProfesional() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    professionalNombre = (doc.getString("nombre") != null ? doc.getString("nombre") : "") + " " + (doc.getString("apellido") != null ? doc.getString("apellido") : "");
                    professionalDireccionConsultorio = doc.getString("direccionConsultorio");
                    Long boxes = doc.getLong("cantidadBoxes");
                    if (boxes != null) cantidadBoxes = boxes.intValue();
                });
    }

    private void buscarPacientes() {
        String query = etBuscar.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            etBuscar.setError("Ingresá un dato para buscar");
            return;
        }

        btnBuscarPaciente.setEnabled(false);
        btnBuscarPaciente.setText("Buscando...");

        repository.obtenerTodos().addOnSuccessListener(snapshot -> {
            btnBuscarPaciente.setEnabled(true);
            btnBuscarPaciente.setText("Buscar");
            
            List<Paciente> resultados = new ArrayList<>();
            String queryNormalizada = normalizarTexto(query);

            for (var doc : snapshot.getDocuments()) {
                Paciente p = doc.toObject(Paciente.class);
                if (p != null) {
                    p.setId(doc.getId());
                    boolean coincideDni = p.getDni() != null && p.getDni().contains(query);
                    boolean coincideNombre = p.getNombre() != null && normalizarTexto(p.getNombre()).contains(queryNormalizada);
                    boolean coincideApellido = p.getApellido() != null && normalizarTexto(p.getApellido()).contains(queryNormalizada);

                    if (coincideDni || coincideNombre || coincideApellido) {
                        resultados.add(p);
                    }
                }
            }

            if (resultados.isEmpty()) {
                cardPacienteEncontrado.setVisibility(View.GONE);
                layoutFormulario.setVisibility(View.GONE);
                btnCrearNuevo.setVisibility(View.VISIBLE);
                Toast.makeText(this, "No se encontraron pacientes", Toast.LENGTH_SHORT).show();
            } else if (resultados.size() == 1) {
                seleccionarPaciente(resultados.get(0));
            } else {
                mostrarDialogoSeleccion(resultados);
            }
        }).addOnFailureListener(e -> {
            btnBuscarPaciente.setEnabled(true);
            btnBuscarPaciente.setText("Buscar");
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarDialogoSeleccion(List<Paciente> resultados) {
        String[] nombres = new String[resultados.size()];
        for (int i = 0; i < resultados.size(); i++) {
            nombres[i] = resultados.get(i).getNombreCompleto() + " (DNI: " + resultados.get(i).getDni() + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Seleccioná un paciente")
                .setItems(nombres, (dialog, which) -> seleccionarPaciente(resultados.get(which)))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void seleccionarPaciente(Paciente p) {
        pacienteExistente = p;
        horariosOriginales = p.getHorarios() != null ? new ArrayList<>(p.getHorarios()) : new ArrayList<>();
        btnCrearNuevo.setVisibility(View.GONE);
        cardPacienteEncontrado.setVisibility(View.VISIBLE);
        layoutFormulario.setVisibility(View.VISIBLE);
        actualizarUIHeaderCard();
        precargarFormulario();
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private void actualizarUIHeaderCard() {
        if (pacienteExistente == null) return;

        tvPacienteEncontradoDni.setText("DNI: " + pacienteExistente.getDni());
        tvPacienteEncontradoNombre.setText(pacienteExistente.getNombreCompleto());
        
        if (pacienteExistente.getEmail() != null && !pacienteExistente.getEmail().isEmpty()) {
            tvPacienteEncontradoEmail.setText("Email: " + pacienteExistente.getEmail());
            tvPacienteEncontradoEmail.setVisibility(View.VISIBLE);
        } else {
            tvPacienteEncontradoEmail.setVisibility(View.GONE);
        }

        if (pacienteExistente.isCertificadoDiscapacidad()) {
            tvSesionesRealizadas.setText("Sesiones semanales: " + pacienteExistente.getSesionesSemanales());
            tvSesionesRealizadas.setVisibility(View.VISIBLE);
        } else {
            tvSesionesRealizadas.setText("Sesiones: " + pacienteExistente.getSesionesAtendidas() + "/" + pacienteExistente.getSesionesOrden());
            tvSesionesRealizadas.setVisibility(View.VISIBLE);
        }

        TextView tvObraSocialCard = findViewById(R.id.tv_paciente_obra_social);
        TextView tvAfiliadoCard = findViewById(R.id.tv_paciente_afiliado);
        if (pacienteExistente.getObraSocial() != null && !pacienteExistente.getObraSocial().isEmpty()) {
            tvObraSocialCard.setText("Obra Social: " + pacienteExistente.getObraSocial());
            tvObraSocialCard.setVisibility(View.VISIBLE);
            if (pacienteExistente.getNumeroAfiliado() != null && !pacienteExistente.getNumeroAfiliado().isEmpty()) {
                tvAfiliadoCard.setText("N° Afiliado: " + pacienteExistente.getNumeroAfiliado());
                tvAfiliadoCard.setVisibility(View.VISIBLE);
            } else {
                tvAfiliadoCard.setVisibility(View.GONE);
            }
        } else {
            tvObraSocialCard.setVisibility(View.GONE);
            tvAfiliadoCard.setVisibility(View.GONE);
        }
    }

    private void precargarFormulario() {
        if (pacienteExistente == null) return;

        etNombre.setText(pacienteExistente.getNombre());
        etApellido.setText(pacienteExistente.getApellido());
        etDni.setText(pacienteExistente.getDni());
        etTelefono.setText(pacienteExistente.getTelefono());
        etDireccion.setText(pacienteExistente.getDireccion());
        if (pacienteExistente.getEmail() != null) etEmailPaciente.setText(pacienteExistente.getEmail());
        
        etDiagnostico.setText(pacienteExistente.getDiagnostico() != null ? pacienteExistente.getDiagnostico() : "");
        etObservaciones.setText(pacienteExistente.getObservaciones() != null ? pacienteExistente.getObservaciones() : "");

        etNombre.setEnabled(false);
        etApellido.setEnabled(false);
        etDni.setEnabled(false);
        etTelefono.setEnabled(false);
        etEmailPaciente.setEnabled(false);
        switchDiscapacidad.setEnabled(false);
        switchParticular.setEnabled(false);
        etSesionesSemanales.setEnabled(false);
        etSesionesOrden.setEnabled(false);
        etDiagnostico.setEnabled(false);
        etObservaciones.setEnabled(false);

        boolean tieneOS = pacienteExistente.getObraSocial() != null && !pacienteExistente.getObraSocial().isEmpty();
        boolean tieneCUD = pacienteExistente.isCertificadoDiscapacidad();

        if (tieneOS) {
            findViewById(R.id.card_particular).setVisibility(View.GONE);
            switchParticular.setChecked(false);
        } else {
            findViewById(R.id.card_particular).setVisibility(View.VISIBLE);
            switchParticular.setChecked(true);
        }

        if (tieneCUD) {
            findViewById(R.id.card_cud).setVisibility(View.VISIBLE);
            tvTituloCud.setVisibility(View.VISIBLE);
            switchDiscapacidad.setChecked(true);
            tilSesionesSemanales.setVisibility(View.VISIBLE);
            tilSesionesOrden.setVisibility(View.GONE);
            etSesionesSemanales.setText(String.valueOf(pacienteExistente.getSesionesSemanales()));
        } else {
            findViewById(R.id.card_cud).setVisibility(View.GONE);
            tvTituloCud.setVisibility(View.GONE);
            switchDiscapacidad.setChecked(false);
            tilSesionesSemanales.setVisibility(View.GONE);
            tilSesionesOrden.setVisibility(View.VISIBLE);
            etSesionesOrden.setText(String.valueOf(pacienteExistente.getSesionesOrden()));
        }

        if (modoEdicion) {
            String modalidad = pacienteExistente.getModalidad();
            if ("domicilio".equals(modalidad)) {
                Chip chip = findViewById(R.id.chip_domicilio);
                if (chip != null) chip.setChecked(true);
            } else if ("consultorio".equals(modalidad)) {
                Chip chip = findViewById(R.id.chip_consultorio);
                if (chip != null) chip.setChecked(true);
            }

            if (pacienteExistente.isParticular()) {
                etValorSesion.setText(String.valueOf(pacienteExistente.getValorSesion()));
            }
        } else {
            if (chipGroupModalidad != null) chipGroupModalidad.clearCheck();
        }

        containerHorarios.removeAllViews();
        actualizarBotonHorario();
    }

    private String getModalidadSeleccionada() {
        if (chipGroupModalidad == null) return "";
        int checkedId = chipGroupModalidad.getCheckedChipId();
        if (checkedId == R.id.chip_domicilio) return "domicilio";
        if (checkedId == R.id.chip_consultorio) return "consultorio";
        return "";
    }

    private boolean puedeAgregarHorario() {
        if (pacienteExistente == null) return false;
        if (indiceEdicionIndividual != -1) return false; // No agregar más si edito uno solo
        
        int turnosActuales = containerHorarios.getChildCount();
        
        if (pacienteExistente.isCertificadoDiscapacidad()) {
            // Permitir cargar turnos para varias semanas (ej: hasta 12 turnos en total si son 3 por semana)
            return turnosActuales < (pacienteExistente.getSesionesSemanales() * 4);
        } else {
            int restantes = pacienteExistente.getSesionesOrden() - pacienteExistente.getSesionesAtendidas();
            return turnosActuales < restantes;
        }
    }

    private void actualizarBotonHorario() {
        if (indiceEdicionIndividual != -1) {
            btnAgregarHorario.setVisibility(View.GONE);
            return;
        }
        
        boolean puede = puedeAgregarHorario();
        btnAgregarHorario.setEnabled(puede);
        if (pacienteExistente == null) {
             btnAgregarHorario.setText("Buscá al paciente primero");
        } else if (!puede) {
            btnAgregarHorario.setText("Límite de turnos alcanzado");
        } else {
            btnAgregarHorario.setText("+ Agregar horario");
        }
    }

    private void agregarFilaHorario(HorarioAtencion horario) {
        View fila = LayoutInflater.from(this).inflate(R.layout.item_horario, containerHorarios, false);

        TextInputEditText etFechaTurno = fila.findViewById(R.id.et_fecha_turno);
        TextInputEditText etHoraInicio = fila.findViewById(R.id.et_hora_inicio);
        TextInputEditText etHoraFin = fila.findViewById(R.id.et_hora_fin);

        Calendar hoy = Calendar.getInstance();
        etFechaTurno.setOnClickListener(v -> {
            new android.app.DatePickerDialog(this, (dp, y, m, d) -> {
                Calendar sel = Calendar.getInstance();
                sel.set(y, m, d);
                String[] diasNombres = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
                String nombreDia = diasNombres[sel.get(Calendar.DAY_OF_WEEK) - 1];
                String fechaStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y);
                etFechaTurno.setText(fechaStr + " (" + nombreDia + ")");
                etFechaTurno.setTag(new String[]{fechaStr, nombreDia});
            }, hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH)).show();
        });

        etHoraInicio.setOnClickListener(v -> mostrarTimePicker(etHoraInicio));
        etHoraFin.setOnClickListener(v -> mostrarTimePicker(etHoraFin));

        View btnEliminar = fila.findViewById(R.id.btn_eliminar_horario);
        if (indiceEdicionIndividual != -1) {
            btnEliminar.setVisibility(View.GONE);
        } else {
            btnEliminar.setOnClickListener(v -> {
                containerHorarios.removeView(fila);
                actualizarBotonHorario();
            });
        }

        if (horario != null) {
            if (horario.getFecha() != null && !horario.getFecha().isEmpty()) {
                etFechaTurno.setText(horario.getFecha() + " (" + horario.getDia() + ")");
                etFechaTurno.setTag(new String[]{horario.getFecha(), horario.getDia()});
            } else {
                etFechaTurno.setText(horario.getDia());
                etFechaTurno.setTag(new String[]{"", horario.getDia()});
            }
            etHoraInicio.setText(horario.getHoraInicio());
            etHoraFin.setText(horario.getHoraFin());
        }

        containerHorarios.addView(fila);
    }

    private void mostrarTimePicker(TextInputEditText campo) {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> campo.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private int aMinutos(String hora) {
        try {
            if (hora == null || !hora.contains(":")) return -1;
            String[] partes = hora.split(":");
            return Integer.parseInt(partes[0].trim()) * 60 + Integer.parseInt(partes[1].trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean hayCruce(HorarioAtencion h1, HorarioAtencion h2) {
        boolean mismaBaseTemporal = false;
        // Si ambos tienen fecha, comparamos fechas
        if (h1.getFecha() != null && !h1.getFecha().isEmpty() && h2.getFecha() != null && !h2.getFecha().isEmpty()) {
            mismaBaseTemporal = h1.getFecha().equals(h2.getFecha());
        } else {
            // Si al menos uno es recurrente (sin fecha), comparamos por nombre de día
            mismaBaseTemporal = h1.getDia().equalsIgnoreCase(h2.getDia());
        }
        
        if (!mismaBaseTemporal) return false;

        int inicio1 = aMinutos(h1.getHoraInicio());
        int fin1 = aMinutos(h1.getHoraFin());
        int inicio2 = aMinutos(h2.getHoraInicio());
        int fin2 = aMinutos(h2.getHoraFin());

        if (inicio1 == -1 || fin1 == -1 || inicio2 == -1 || fin2 == -1) return false;

        // Lógica de cruce de intervalos: (InicioA < FinB) Y (InicioB < FinA)
        return inicio1 < fin2 && inicio2 < fin1;
    }

    private void guardarPaciente() {
        if (pacienteExistente == null) {
            Toast.makeText(this, "Primero buscá al paciente", Toast.LENGTH_SHORT).show();
            return;
        }

        String modalidad = getModalidadSeleccionada();
        if (modalidad.isEmpty()) {
            Toast.makeText(this, "Seleccioná la modalidad", Toast.LENGTH_SHORT).show();
            return;
        }

        List<HorarioAtencion> horariosEnPantalla = obtenerHorarios();
        
        if (horariosEnPantalla.size() < containerHorarios.getChildCount()) {
             Toast.makeText(this, "Completá fecha y horas para todos los turnos", Toast.LENGTH_SHORT).show();
             return;
        }

        for (HorarioAtencion h : horariosEnPantalla) {
            if (h.getFecha() == null || h.getFecha().isEmpty() || h.getHoraInicio() == null || h.getHoraInicio().isEmpty() || h.getHoraFin() == null || h.getHoraFin().isEmpty()) {
                Toast.makeText(this, "Completá fecha, hora de inicio y fin para todos los horarios", Toast.LENGTH_SHORT).show();
                return;
            }
            if (aMinutos(h.getHoraFin()) <= aMinutos(h.getHoraInicio())) {
                Toast.makeText(this, "La hora de finalización debe ser posterior a la de inicio", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Validar cruce entre los turnos que se están cargando ahora para este mismo paciente
        for (int i = 0; i < horariosEnPantalla.size(); i++) {
            for (int j = i + 1; j < horariosEnPantalla.size(); j++) {
                if (hayCruce(horariosEnPantalla.get(i), horariosEnPantalla.get(j))) {
                    Toast.makeText(this, "Error: Estás intentando asignar dos turnos que se superponen entre sí para este paciente.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        if (horariosEnPantalla.isEmpty() && modoEdicion) {
            Toast.makeText(this, "El paciente debe tener al menos un horario", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (horariosEnPantalla.isEmpty() && !modoEdicion) {
            Toast.makeText(this, "Asigná al menos un horario al turno", Toast.LENGTH_SHORT).show();
            return;
        }

        List<HorarioAtencion> horariosFinales = new ArrayList<>();
        if (indiceEdicionIndividual != -1) {
            // Edición de un solo turno específico
            horariosFinales.addAll(horariosOriginales);
            if (indiceEdicionIndividual < horariosFinales.size()) {
                horariosFinales.set(indiceEdicionIndividual, horariosEnPantalla.get(0));
            }
        } else if (modoEdicion) {
            // Edición masiva (reemplaza todo)
            horariosFinales.addAll(horariosEnPantalla);
        } else {
            // Alta nueva (agrega a los existentes)
            horariosFinales.addAll(horariosOriginales);
            horariosFinales.addAll(horariosEnPantalla);
        }

        // --- VALIDACIÓN SEMANAL PARA PACIENTES CON CUD ---
        if (pacienteExistente.isCertificadoDiscapacidad()) {
            Map<String, Integer> conteoSemanas = new HashMap<>();
            int limite = pacienteExistente.getSesionesSemanales();
            
            for (HorarioAtencion h : horariosFinales) {
                if (h.getFecha() != null && !h.getFecha().isEmpty()) {
                    String semanaKey = getSemanaAnio(h.getFecha());
                    int count = conteoSemanas.getOrDefault(semanaKey, 0) + 1;
                    if (count > limite) {
                        Toast.makeText(this, "Error: La semana del " + h.getFecha() + " ya tiene el máximo de " + limite + " sesiones asignadas.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    conteoSemanas.put(semanaKey, count);
                }
            }
        }

        pacienteExistente.setModalidad(modalidad);
        pacienteExistente.setHorarios(horariosFinales);
        pacienteExistente.setUltimaActualizacion(com.google.firebase.Timestamp.now());

        if (!etValorSesion.getText().toString().isEmpty())
            pacienteExistente.setValorSesion(Double.parseDouble(etValorSesion.getText().toString()));

        guardarEnFirestore(pacienteExistente, horariosEnPantalla);
    }

    private String getSemanaAnio(String fechaStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.setMinimalDaysInFirstWeek(4);
            cal.setTime(sdf.parse(fechaStr));
            int week = cal.get(Calendar.WEEK_OF_YEAR);
            int year = cal.get(Calendar.YEAR);
            // Ajuste para semanas que caen en el año siguiente/anterior
            if (week == 1 && cal.get(Calendar.MONTH) == Calendar.DECEMBER) year++;
            if (week >= 52 && cal.get(Calendar.MONTH) == Calendar.JANUARY) year--;
            return year + "-W" + week;
        } catch (Exception e) {
            return "";
        }
    }

    private void guardarEnFirestore(Paciente paciente, List<HorarioAtencion> horariosNuevos) {
        if ("domicilio".equals(paciente.getModalidad()) && paciente.getHorarios() != null) {
            verificarSuperposicionYGuardar(paciente, horariosNuevos);
        } else if ("consultorio".equals(paciente.getModalidad()) && paciente.getHorarios() != null) {
            verificarBoxesYGuardar(paciente, horariosNuevos);
        } else {
            ejecutarGuardado(paciente, horariosNuevos);
        }
    }

    private void ejecutarGuardado(Paciente paciente, List<HorarioAtencion> horariosNuevos) {
        repository.guardar(paciente)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show();
                    preguntarEnviarWhatsApp(paciente, horariosNuevos);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void preguntarEnviarWhatsApp(Paciente paciente, List<HorarioAtencion> horarios) {
        if (paciente.getTelefono() == null || paciente.getTelefono().isEmpty()) {
            finish();
            return;
        }

        String titulo = modoEdicion ? "Turno modificado" : "Turno registrado";
        String mensajePregunta = modoEdicion 
                ? "¿Deseás enviar la reprogramación por WhatsApp al paciente?" 
                : "¿Deseás enviar una notificación por WhatsApp al paciente?";

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensajePregunta)
                .setPositiveButton("Enviar", (dialog, which) -> {
                    enviarWhatsApp(paciente, horarios);
                    finish();
                })
                .setNegativeButton("Ahora no", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void enviarWhatsApp(Paciente paciente, List<HorarioAtencion> horarios) {
        try {
            String modal = paciente.getModalidad();
            StringBuilder msj = new StringBuilder();
            
            msj.append("Hola ").append(paciente.getNombre()).append("! ");
            
            if (modoEdicion) {
                msj.append("Te informo que tu sesión de Kinesiología ha sido reprogramada:\n\n");
            } else if ("domicilio".equals(modal)) {
                msj.append("Te confirmo que te visitaré para tu sesión de Kinesiología:\n\n");
            } else {
                msj.append("Te confirmo tu turno de Kinesiología en consultorio:\n\n");
            }

            for (HorarioAtencion h : horarios) {
                msj.append("📅 *").append(h.getFecha()).append("* (").append(h.getDia()).append(")\n");
                msj.append("🕒 *").append(h.getHoraInicio()).append(" hs*\n\n");
            }

            if ("consultorio".equals(modal) && professionalDireccionConsultorio != null && !professionalDireccionConsultorio.isEmpty()) {
                msj.append("📍 Dirección: ").append(professionalDireccionConsultorio).append("\n\n");
            }

            if (professionalNombre != null && !professionalNombre.isEmpty()) {
                msj.append("Atiende: ").append(professionalNombre).append("\n");
            }

            msj.append("\n⚠️ *Por favor, avisame con anticipación si no podés asistir.*");
            msj.append("\n\nTe esperamos!");

            String tel = paciente.getTelefono().replaceAll("[^0-9]", "");
            if (!tel.startsWith("54")) {
                tel = "549" + tel;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone=" + tel + "&text=" + URLEncoder.encode(msj.toString(), "UTF-8");
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    private void verificarSuperposicionYGuardar(Paciente paciente, List<HorarioAtencion> horariosNuevos) {
        repository.obtenerTodos().addOnSuccessListener(query -> {
            for (var doc : query.getDocuments()) {
                // No comparar contra el mismo paciente que estamos editando
                if (paciente.getId() != null && doc.getId().equals(paciente.getId())) {
                     // Pero SI comparar contra sus otros horarios que no estamos editando ahora
                     Paciente miPropio = doc.toObject(Paciente.class);
                     if (miPropio != null && miPropio.getHorarios() != null && indiceEdicionIndividual != -1) {
                         for (HorarioAtencion hNuevo : horariosNuevos) {
                             for (int k = 0; k < miPropio.getHorarios().size(); k++) {
                                 if (k == indiceEdicionIndividual) continue; // Ignorar el que estoy editando
                                 if (hayCruce(hNuevo, miPropio.getHorarios().get(k))) {
                                      Toast.makeText(this, "Error: El horario se superpone con otro turno del mismo paciente.", Toast.LENGTH_LONG).show();
                                      return;
                                 }
                             }
                         }
                     }
                     continue;
                }
                
                Paciente existente = doc.toObject(Paciente.class);
                if (existente == null || existente.getHorarios() == null) continue;
                if (!"domicilio".equals(existente.getModalidad())) continue;

                for (HorarioAtencion hNuevo : horariosNuevos) {
                    for (HorarioAtencion hExistente : existente.getHorarios()) {
                        if (hayCruce(hNuevo, hExistente)) {
                            Toast.makeText(this, "Error: El horario del " + hNuevo.getFecha() + " a las " + hNuevo.getHoraInicio() + " ya está ocupado por " + existente.getNombreCompleto() + " (Domicilio)", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }
            }
            ejecutarGuardado(paciente, horariosNuevos);
        });
    }

    private void verificarBoxesYGuardar(Paciente paciente, List<HorarioAtencion> horariosNuevos) {
        repository.obtenerTodos().addOnSuccessListener(query -> {
            for (HorarioAtencion hNuevo : horariosNuevos) {
                int ocupados = 0;
                for (var doc : query.getDocuments()) {
                    Paciente existente = doc.toObject(Paciente.class);
                    if (existente == null || existente.getHorarios() == null) continue;
                    if (!"consultorio".equals(existente.getModalidad())) continue;

                    for (int k = 0; k < existente.getHorarios().size(); k++) {
                        // Si es el mismo paciente, no contar el slot que estamos editando
                        if (paciente.getId() != null && doc.getId().equals(paciente.getId()) && k == indiceEdicionIndividual) continue;
                        
                        if (hayCruce(hNuevo, existente.getHorarios().get(k))) {
                            if (hNuevo.getFecha() != null && hNuevo.getFecha().equals(existente.getHorarios().get(k).getFecha())) {
                                ocupados++;
                            } else if (hNuevo.getFecha() == null && hNuevo.getDia().equalsIgnoreCase(existente.getHorarios().get(k).getDia())) {
                                // Fallback para recurrentes si fuera necesario, aunque la app usa fechas
                                ocupados++;
                            }
                        }
                    }
                }
                if (ocupados >= cantidadBoxes) {
                    Toast.makeText(this, "Error: Sin boxes disponibles el " + hNuevo.getFecha() + " de " + hNuevo.getHoraInicio() + " a " + hNuevo.getHoraFin() + ". (Ocupados: " + ocupados + "/" + cantidadBoxes + ")", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            ejecutarGuardado(paciente, horariosNuevos);
        });
    }

    private List<HorarioAtencion> obtenerHorarios() {
        List<HorarioAtencion> horarios = new ArrayList<>();
        for (int i = 0; i < containerHorarios.getChildCount(); i++) {
            View fila = containerHorarios.getChildAt(i);
            TextInputEditText etFecha = fila.findViewById(R.id.et_fecha_turno);
            TextInputEditText etInicio = fila.findViewById(R.id.et_hora_inicio);
            TextInputEditText etFin = fila.findViewById(R.id.et_hora_fin);

            String inicio = etInicio.getText().toString().trim();
            String fin = etFin.getText().toString().trim();
            Object tag = etFecha.getTag();

            if (tag == null || inicio.isEmpty() || fin.isEmpty()) {
                continue;
            }

            String[] datos = (String[]) tag;
            horarios.add(new HorarioAtencion(datos[1], datos[0], inicio, fin, ""));
        }
        return horarios;
    }

    private void cargarPacienteParaEditar(String pacienteId) {
        FirebaseFirestore.getInstance().collection("pacientes").document(pacienteId).get()
                .addOnSuccessListener(doc -> {
                    pacienteExistente = doc.toObject(Paciente.class);
                    pacienteExistente.setId(doc.getId());
                    horariosOriginales = pacienteExistente.getHorarios() != null ? new ArrayList<>(pacienteExistente.getHorarios()) : new ArrayList<>();
                    actualizarUIHeaderCard();
                    precargarFormulario();
                    
                    if (pacienteExistente.getHorarios() != null) {
                        if (indiceEdicionIndividual != -1 && indiceEdicionIndividual < pacienteExistente.getHorarios().size()) {
                            // Solo cargar el turno específico
                            agregarFilaHorario(pacienteExistente.getHorarios().get(indiceEdicionIndividual));
                        } else {
                            // Cargar todos (comportamiento anterior)
                            for (HorarioAtencion h : pacienteExistente.getHorarios()) agregarFilaHorario(h);
                        }
                    }
                });
    }

    private void cargarBoxes() {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    Long boxes = doc.getLong("cantidadBoxes");
                    if (boxes != null) cantidadBoxes = boxes.intValue();
                });
    }
}
