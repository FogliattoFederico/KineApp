package frgp.utn.edu.kineapp;

import android.content.Intent;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FormularioPacienteActivity extends AppCompatActivity {

    private TextInputEditText etBuscarDni, etNombre, etApellido, etDni,
            etTelefono, etDireccion, etDiagnostico, etObservaciones,
            etObraSocial, etNumeroAfiliado, etSesionesSemanales,
            etSesionesOrden, etValorSesion, etEmailPaciente;
    private TextInputLayout tilSesionesSemanales, tilSesionesOrden,
            tilObraSocial, tilNumeroAfiliado, tilValorSesion, tilEmailPaciente;
    private SwitchMaterial switchDiscapacidad, switchParticular;
    private ChipGroup chipGroupModalidad;
    private LinearLayout containerHorarios, layoutFormulario;
    private MaterialButton btnAgregarHorario, btnBuscarPaciente, btnCrearNuevo;
    private MaterialCardView cardPacienteEncontrado;
    private TextView tvPacienteEncontradoNombre, tvPacienteEncontradoDni, tvPacienteEncontradoEmail;
    private PacienteRepository repository;
    private Paciente pacienteExistente = null;
    private View layoutCud;
    private TextView tvTituloCud;
    private int cantidadBoxes = 1;
    private boolean modoEdicion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_paciente);

        repository = new PacienteRepository();
        cargarBoxes();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Búsqueda DNI
        etBuscarDni = findViewById(R.id.et_buscar_dni);
        btnBuscarPaciente = findViewById(R.id.btn_buscar_paciente);
        btnCrearNuevo = findViewById(R.id.btn_crear_nuevo_paciente);
        cardPacienteEncontrado = findViewById(R.id.card_paciente_encontrado);
        tvPacienteEncontradoDni = findViewById(R.id.tv_paciente_encontrado_dni);
        tvPacienteEncontradoNombre = findViewById(R.id.tv_paciente_encontrado_nombre);
        tvPacienteEncontradoEmail = findViewById(R.id.tv_paciente_email);
        layoutFormulario = findViewById(R.id.layout_formulario_completo);

        // Campos del formulario
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

        // Ocultar formulario hasta encontrar paciente
        layoutFormulario.setVisibility(View.GONE);

        btnBuscarPaciente.setOnClickListener(v -> buscarPacientePorDni());
        
        btnCrearNuevo.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormularioPacienteSimpleActivity.class);
            startActivity(intent);
        });

        switchDiscapacidad.setOnCheckedChangeListener((btn, checked) -> {
            tilSesionesSemanales.setVisibility(checked ? View.VISIBLE : View.GONE);
            tilSesionesOrden.setVisibility(checked ? View.GONE : View.VISIBLE);
            actualizarBotonHorario();
        });

        switchParticular.setOnCheckedChangeListener((btn, checked) -> {
            tilValorSesion.setVisibility(checked ? View.VISIBLE : View.GONE);
            actualizarBotonHorario();
        });

        etSesionesSemanales.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(android.text.Editable s) { actualizarBotonHorario(); }
        });

        etSesionesOrden.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(android.text.Editable s) { actualizarBotonHorario(); }
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
        if (pacienteId != null) {
            modoEdicion = true;
            toolbar.setTitle("Editar Turno");
            findViewById(R.id.layout_buscar_dni).setVisibility(View.GONE);
            layoutFormulario.setVisibility(View.VISIBLE);
            cargarPacienteParaEditar(pacienteId);
        } else {
            toolbar.setTitle("Asignar turno");
            actualizarBotonHorario();
        }
    }

    private void buscarPacientePorDni() {
        String dniBusqueda = etBuscarDni.getText().toString().trim();
        if (dniBusqueda.isEmpty()) {
            etBuscarDni.setError("Ingresá el DNI");
            return;
        }

        btnBuscarPaciente.setEnabled(false);
        btnBuscarPaciente.setText("Buscando...");
        btnCrearNuevo.setVisibility(View.GONE);

        repository.buscarPorDni(dniBusqueda)
                .addOnSuccessListener(query -> {
                    btnBuscarPaciente.setEnabled(true);
                    btnBuscarPaciente.setText("Buscar");

                    if (query.isEmpty()) {
                        cardPacienteEncontrado.setVisibility(View.GONE);
                        layoutFormulario.setVisibility(View.GONE);
                        btnCrearNuevo.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "No se encontró ningún paciente con DNI " + dniBusqueda, Toast.LENGTH_LONG).show();
                        return;
                    }

                    pacienteExistente = query.getDocuments().get(0).toObject(Paciente.class);
                    pacienteExistente.setId(query.getDocuments().get(0).getId());

                    actualizarUIHeaderCard();

                    cardPacienteEncontrado.setVisibility(View.VISIBLE);
                    layoutFormulario.setVisibility(View.VISIBLE);
                    precargarFormulario();
                })
                .addOnFailureListener(e -> {
                    btnBuscarPaciente.setEnabled(true);
                    btnBuscarPaciente.setText("Buscar");
                    Toast.makeText(this, "Error al buscar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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

        boolean tieneOS = pacienteExistente.getObraSocial() != null && !pacienteExistente.getObraSocial().isEmpty();
        boolean tieneCUD = pacienteExistente.isCertificadoDiscapacidad();

        if (tieneOS) {
            findViewById(R.id.card_particular).setVisibility(View.GONE);
            switchParticular.setChecked(false);
        } else {
            findViewById(R.id.card_particular).setVisibility(View.VISIBLE);
            if (!modoEdicion) switchParticular.setChecked(true);
        }

        if (tieneCUD) {
            findViewById(R.id.card_cud).setVisibility(View.VISIBLE);
            tvTituloCud.setVisibility(View.VISIBLE);
            if (!modoEdicion) switchDiscapacidad.setChecked(true);
        } else {
            findViewById(R.id.card_cud).setVisibility(View.GONE);
            tvTituloCud.setVisibility(View.GONE);
            switchDiscapacidad.setChecked(false);
        }

        if (modoEdicion) {
            etDiagnostico.setText(pacienteExistente.getDiagnostico());
            etObservaciones.setText(pacienteExistente.getObservaciones());
            etObraSocial.setText(pacienteExistente.getObraSocial());
            etNumeroAfiliado.setText(pacienteExistente.getNumeroAfiliado());
            
            String modalidad = pacienteExistente.getModalidad();
            if ("domicilio".equals(modalidad)) {
                Chip chip = findViewById(R.id.chip_domicilio);
                if (chip != null) chip.setChecked(true);
            } else if ("consultorio".equals(modalidad)) {
                Chip chip = findViewById(R.id.chip_consultorio);
                if (chip != null) chip.setChecked(true);
            }

            switchParticular.setChecked(pacienteExistente.isParticular());
            switchDiscapacidad.setChecked(pacienteExistente.isCertificadoDiscapacidad());

            if (pacienteExistente.isParticular()) {
                etValorSesion.setText(String.valueOf(pacienteExistente.getValorSesion()));
            } else if (pacienteExistente.isCertificadoDiscapacidad()) {
                etSesionesSemanales.setText(String.valueOf(pacienteExistente.getSesionesSemanales()));
            } else {
                etSesionesOrden.setText(String.valueOf(pacienteExistente.getSesionesOrden()));
            }
        } else {
            etDiagnostico.setText("");
            etObservaciones.setText("");
            etSesionesSemanales.setText("");
            etSesionesOrden.setText("");
            etValorSesion.setText("");
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

    private boolean sesionesIngresadas() {
        if (switchParticular.isChecked()) return true;
        if (switchDiscapacidad.isChecked())
            return !etSesionesSemanales.getText().toString().trim().isEmpty();
        return !etSesionesOrden.getText().toString().trim().isEmpty();
    }

    private boolean puedeAgregarHorario() {
        if (!sesionesIngresadas()) return false;
        if (switchDiscapacidad.isChecked()) {
            String txt = etSesionesSemanales.getText().toString().trim();
            if (txt.isEmpty()) return false;
            int req = Integer.parseInt(txt);
            int totalTurnos = containerHorarios.getChildCount();
            if (!modoEdicion && pacienteExistente != null && pacienteExistente.getHorarios() != null) {
                totalTurnos += pacienteExistente.getHorarios().size();
            }
            return totalTurnos < req;
        }
        return true;
    }

    private void actualizarBotonHorario() {
        boolean puede = puedeAgregarHorario();
        btnAgregarHorario.setEnabled(puede);
        if (!sesionesIngresadas())
            btnAgregarHorario.setText("Ingresá la cantidad de sesiones primero");
        else if (!puede)
            btnAgregarHorario.setText("Límite de horarios alcanzado");
        else
            btnAgregarHorario.setText("+ Agregar horario");
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

        fila.findViewById(R.id.btn_eliminar_horario).setOnClickListener(v -> {
            containerHorarios.removeView(fila);
            actualizarBotonHorario();
        });

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

    private boolean esHoraValida(String inicio, String fin) {
        if (inicio == null || fin == null || inicio.isEmpty() || fin.isEmpty()) return false;
        try {
            String[] partsInicio = inicio.split(":");
            String[] partsFin = fin.split(":");
            int minInicio = Integer.parseInt(partsInicio[0]) * 60 + Integer.parseInt(partsInicio[1]);
            int minFin = Integer.parseInt(partsFin[0]) * 60 + Integer.parseInt(partsFin[1]);
            return minFin > minInicio;
        } catch (Exception e) {
            return false;
        }
    }

    private void guardarPaciente() {
        if (pacienteExistente == null) {
            Toast.makeText(this, "Primero buscá al paciente", Toast.LENGTH_SHORT).show();
            return;
        }

        String diagnostico = etDiagnostico.getText().toString().trim();
        String modalidad = getModalidadSeleccionada();
        if (modalidad.isEmpty()) {
            Toast.makeText(this, "Seleccioná la modalidad", Toast.LENGTH_SHORT).show();
            return;
        }

        List<HorarioAtencion> horariosNuevos = obtenerHorarios();
        
        // --- VALIDACIÓN DE FECHA Y HORA (Fix BUG) ---
        if (horariosNuevos.size() < containerHorarios.getChildCount()) {
             Toast.makeText(this, "Completá fecha y horas para todos los turnos", Toast.LENGTH_SHORT).show();
             return;
        }

        for (HorarioAtencion h : horariosNuevos) {
            if (h.getFecha() == null || h.getFecha().isEmpty() || h.getHoraInicio() == null || h.getHoraInicio().isEmpty() || h.getHoraFin() == null || h.getHoraFin().isEmpty()) {
                Toast.makeText(this, "Completá fecha, hora de inicio y fin para todos los horarios", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!esHoraValida(h.getHoraInicio(), h.getHoraFin())) {
                Toast.makeText(this, "La hora de finalización debe ser posterior a la de inicio", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (horariosNuevos.isEmpty() && modoEdicion) {
            Toast.makeText(this, "El paciente debe tener al menos un horario", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (horariosNuevos.isEmpty() && !modoEdicion) {
            Toast.makeText(this, "Asigná al menos un horario al turno", Toast.LENGTH_SHORT).show();
            return;
        }

        List<HorarioAtencion> horariosFinales = new ArrayList<>();
        if (modoEdicion) {
            horariosFinales.addAll(horariosNuevos);
        } else {
            if (pacienteExistente.getHorarios() != null) {
                horariosFinales.addAll(pacienteExistente.getHorarios());
            }
            horariosFinales.addAll(horariosNuevos);
        }

        if (switchDiscapacidad.isChecked()) {
            String sessStr = etSesionesSemanales.getText().toString().trim();
            if (sessStr.isEmpty()) {
                Toast.makeText(this, "Ingresá sesiones semanales", Toast.LENGTH_SHORT).show();
                return;
            }
            int req = Integer.parseInt(sessStr);
            if (horariosFinales.size() > req) {
                Toast.makeText(this, "No se puede guardar: El paciente tiene " + horariosFinales.size() + 
                        " turnos asignados, pero el límite semanal es de " + req, Toast.LENGTH_LONG).show();
                return;
            }
            if (horariosFinales.size() < req) {
                Toast.makeText(this, "Debés asignar las " + req + " sesiones semanales (actualmente tiene " + 
                        horariosFinales.size() + ")", Toast.LENGTH_LONG).show();
                return;
            }
        } else if (!switchParticular.isChecked()) {
            String sessStr = etSesionesOrden.getText().toString().trim();
            if (!sessStr.isEmpty()) {
                int totalOrden = Integer.parseInt(sessStr);
                if (horariosFinales.size() > totalOrden) {
                    Toast.makeText(this, "Error: Los turnos asignados (" + horariosFinales.size() + 
                            ") superan el total de la orden (" + totalOrden + ")", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        pacienteExistente.setDiagnostico(diagnostico);
        pacienteExistente.setObservaciones(etObservaciones.getText().toString());
        pacienteExistente.setEmail(etEmailPaciente.getText().toString());
        pacienteExistente.setCertificadoDiscapacidad(switchDiscapacidad.isChecked());
        pacienteExistente.setParticular(switchParticular.isChecked());
        pacienteExistente.setModalidad(modalidad);
        pacienteExistente.setHorarios(horariosFinales);
        pacienteExistente.setUltimaActualizacion(com.google.firebase.Timestamp.now());

        if (!etSesionesSemanales.getText().toString().isEmpty())
            pacienteExistente.setSesionesSemanales(Integer.parseInt(etSesionesSemanales.getText().toString()));
        if (!etSesionesOrden.getText().toString().isEmpty())
            pacienteExistente.setSesionesOrden(Integer.parseInt(etSesionesOrden.getText().toString()));
        if (!etValorSesion.getText().toString().isEmpty())
            pacienteExistente.setValorSesion(Double.parseDouble(etValorSesion.getText().toString()));

        guardarEnFirestore(pacienteExistente);
    }

    private void guardarEnFirestore(Paciente paciente) {
        if ("domicilio".equals(paciente.getModalidad()) && paciente.getHorarios() != null) {
            verificarSuperposicionYGuardar(paciente);
        } else if ("consultorio".equals(paciente.getModalidad()) && paciente.getHorarios() != null) {
            verificarBoxesYGuardar(paciente);
        } else {
            ejecutarGuardado(paciente);
        }
    }

    private void ejecutarGuardado(Paciente paciente) {
        repository.guardar(paciente)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void verificarSuperposicionYGuardar(Paciente paciente) {
        repository.obtenerTodos().addOnSuccessListener(query -> {
            for (var doc : query.getDocuments()) {
                if (paciente.getId() != null && doc.getId().equals(paciente.getId())) continue;
                Paciente existente = doc.toObject(Paciente.class);
                if (existente == null || existente.getHorarios() == null) continue;
                if (!"domicilio".equals(existente.getModalidad())) continue;

                for (HorarioAtencion hNuevo : paciente.getHorarios()) {
                    if (hNuevo.getFecha() == null || hNuevo.getHoraInicio() == null) continue;
                    for (HorarioAtencion hExistente : existente.getHorarios()) {
                        if (hNuevo.getFecha().equals(hExistente.getFecha()) && 
                            hNuevo.getHoraInicio().trim().equals(hExistente.getHoraInicio().trim())) {
                            Toast.makeText(this, "Horario ocupado en domicilio: " + hNuevo.getFecha() + " " + hNuevo.getHoraInicio() + " por " + existente.getNombreCompleto(), Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }
            }
            ejecutarGuardado(paciente);
        });
    }

    private void verificarBoxesYGuardar(Paciente paciente) {
        repository.obtenerTodos().addOnSuccessListener(query -> {
            for (HorarioAtencion hNuevo : paciente.getHorarios()) {
                if (hNuevo.getFecha() == null || hNuevo.getHoraInicio() == null) continue;
                int ocupados = 0;
                for (var doc : query.getDocuments()) {
                    if (paciente.getId() != null && doc.getId().equals(paciente.getId())) continue;
                    Paciente existente = doc.toObject(Paciente.class);
                    if (existente == null || existente.getHorarios() == null) continue;
                    if (!"consultorio".equals(existente.getModalidad())) continue;

                    for (HorarioAtencion hExistente : existente.getHorarios()) {
                        if (hNuevo.getFecha().equals(hExistente.getFecha()) && 
                            hNuevo.getHoraInicio().trim().equals(hExistente.getHoraInicio().trim())) {
                            ocupados++;
                        }
                    }
                }
                if (ocupados >= cantidadBoxes) {
                    Toast.makeText(this, "Sin boxes disponibles el " + hNuevo.getFecha() + " a las " + hNuevo.getHoraInicio(), Toast.LENGTH_LONG).show();
                    return;
                }
            }
            ejecutarGuardado(paciente);
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
                // Si no hay tag (fecha) o faltan horas, no lo tomamos como válido
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
                    actualizarUIHeaderCard();
                    precargarFormulario();
                    if (pacienteExistente.getHorarios() != null) {
                        for (HorarioAtencion h : pacienteExistente.getHorarios()) agregarFilaHorario(h);
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
