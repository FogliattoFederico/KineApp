package frgp.utn.edu.kineapp.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.model.Atencion;
import frgp.utn.edu.kineapp.model.HorarioAtencion;
import frgp.utn.edu.kineapp.model.Paciente;
import frgp.utn.edu.kineapp.repository.AtencionRepository;
import frgp.utn.edu.kineapp.repository.PacienteRepository;

public class DetallePacienteActivity extends AppCompatActivity {

    private TextView tvAvatarGrande, tvNombreCompleto, tvDniHeader,
            tvBadgeCud, tvBadgeModalidad;
    private LinearLayout containerHorarios, containerHistorial;
    private Paciente paciente;
    private String pacienteId;
    private boolean modoTurno = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_paciente);

        View spacer = findViewById(R.id.status_bar_spacer);
        int resourceId = getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0 && spacer != null) {
            int height = getResources().getDimensionPixelSize(resourceId);
            spacer.getLayoutParams().height = height;
            spacer.requestLayout();
        }

        pacienteId = getIntent().getStringExtra("pacienteId");
        modoTurno = getIntent().getBooleanExtra("modoTurno", false);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvAvatarGrande = findViewById(R.id.tv_avatar_grande);
        tvNombreCompleto = findViewById(R.id.tv_nombre_completo);
        tvDniHeader = findViewById(R.id.tv_dni_header);
        tvBadgeCud = findViewById(R.id.tv_badge_cud);
        tvBadgeModalidad = findViewById(R.id.tv_badge_modalidad);
        containerHorarios = findViewById(R.id.container_horarios);
        containerHistorial = findViewById(R.id.container_historial);

        cargarPaciente();
    }

    private void cargarPaciente() {
        FirebaseFirestore.getInstance()
                .collection("pacientes")
                .document(pacienteId)
                .get()
                .addOnSuccessListener(doc -> {
                    paciente = doc.toObject(Paciente.class);
                    if (paciente != null) {
                        paciente.setId(doc.getId());
                        mostrarDatos();
                        cargarHistorial();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar paciente",
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void mostrarDatos() {
        String inicial = paciente.getApellido() != null && !paciente.getApellido().isEmpty()
                ? String.valueOf(paciente.getApellido().charAt(0)).toUpperCase() : "?";
        tvAvatarGrande.setText(inicial);
        tvNombreCompleto.setText(paciente.getNombreCompleto());
        tvDniHeader.setText("DNI: " + paciente.getDni());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(paciente.getNombreCompleto());
        }

        if (paciente.isCertificadoDiscapacidad()) {
            tvBadgeCud.setVisibility(View.VISIBLE);
        } else {
            tvBadgeCud.setVisibility(View.GONE);
        }

        if (paciente.getModalidad() != null) {
            tvBadgeModalidad.setVisibility(View.VISIBLE);
            if ("domicilio".equals(paciente.getModalidad())) {
                tvBadgeModalidad.setText("Domicilio");
                tvBadgeModalidad.setTextColor(Color.parseColor("#27500A"));
            } else {
                tvBadgeModalidad.setText("Consultorio");
                tvBadgeModalidad.setTextColor(Color.parseColor("#5F5E5A"));
            }
        } else {
            tvBadgeModalidad.setVisibility(View.GONE);
        }

        mostrarCampo(R.id.tv_telefono, paciente.getTelefono());
        mostrarCampo(R.id.tv_direccion, paciente.getDireccion());
        
        TextView tvEmail = findViewById(R.id.tv_email_detalle);
        if (tvEmail != null) tvEmail.setText(paciente.getEmail() != null ? paciente.getEmail() : "—");

        TextView tvFechaNac = findViewById(R.id.tv_fecha_nacimiento_detalle);
        if (tvFechaNac != null) tvFechaNac.setText(paciente.getFechaNacimiento() != null ? paciente.getFechaNacimiento() : "—");
        
        TextView tvEdad = findViewById(R.id.tv_edad_detalle);
        if (tvEdad != null) tvEdad.setText(paciente.getEdad() > 0 ? paciente.getEdad() + " años" : "—");

        findViewById(R.id.label_clinica_seccion).setVisibility(View.VISIBLE);
        findViewById(R.id.card_clinica).setVisibility(View.VISIBLE);
        mostrarCampo(R.id.tv_diagnostico, paciente.getDiagnostico());
        mostrarCampo(R.id.tv_observaciones, paciente.getObservaciones());

        findViewById(R.id.label_cobertura_seccion).setVisibility(View.VISIBLE);
        findViewById(R.id.card_cobertura).setVisibility(View.VISIBLE);
        
        boolean tieneOS = paciente.getObraSocial() != null && !paciente.getObraSocial().isEmpty();
        boolean tieneCUD = paciente.isCertificadoDiscapacidad();
        
        String tipoCobertura;
        if (paciente.isParticular()) tipoCobertura = "Particular";
        else if (tieneCUD) tipoCobertura = "Con Obra Social (Posee CUD)";
        else tipoCobertura = "Con Obra Social";
        
        mostrarCampo(R.id.tv_tipo_cobertura, tipoCobertura);

        findViewById(R.id.divider_obra_social).setVisibility(View.GONE);
        findViewById(R.id.label_obra_social).setVisibility(View.GONE);
        findViewById(R.id.tv_obra_social).setVisibility(View.GONE);
        findViewById(R.id.divider_afiliado).setVisibility(View.GONE);
        findViewById(R.id.label_numero_afiliado).setVisibility(View.GONE);
        findViewById(R.id.tv_numero_afiliado).setVisibility(View.GONE);
        findViewById(R.id.divider_sesiones).setVisibility(View.GONE);
        findViewById(R.id.label_sesiones).setVisibility(View.GONE);
        findViewById(R.id.tv_sesiones).setVisibility(View.GONE);

        if (tieneOS) {
            setVisible(R.id.divider_obra_social, R.id.label_obra_social, R.id.tv_obra_social);
            mostrarCampo(R.id.tv_obra_social, paciente.getObraSocial());

            if (paciente.getNumeroAfiliado() != null && !paciente.getNumeroAfiliado().isEmpty()) {
                setVisible(R.id.divider_afiliado, R.id.label_numero_afiliado, R.id.tv_numero_afiliado);
                mostrarCampo(R.id.tv_numero_afiliado, paciente.getNumeroAfiliado());
            }
        }

        if (tieneCUD) {
            setVisible(R.id.divider_sesiones, R.id.label_sesiones, R.id.tv_sesiones);
            ((TextView) findViewById(R.id.label_sesiones)).setText("SESIONES SEMANALES");
            mostrarCampo(R.id.tv_sesiones, String.valueOf(paciente.getSesionesSemanales()));
        } else if (paciente.isParticular() && paciente.getValorSesion() > 0) {
            setVisible(R.id.divider_sesiones, R.id.label_sesiones, R.id.tv_sesiones);
            ((TextView) findViewById(R.id.label_sesiones)).setText("VALOR SESIÓN");
            mostrarCampo(R.id.tv_sesiones, String.format(new Locale("es", "AR"), "$ %,.0f", paciente.getValorSesion()));
        } else if (paciente.getSesionesOrden() > 0) {
            setVisible(R.id.divider_sesiones, R.id.label_sesiones, R.id.tv_sesiones);
            ((TextView) findViewById(R.id.label_sesiones)).setText("SESIONES");
            mostrarCampo(R.id.tv_sesiones, paciente.getSesionesAtendidas() + " / " + paciente.getSesionesOrden());
        }

        findViewById(R.id.label_horarios_seccion).setVisibility(View.VISIBLE);
        containerHorarios.setVisibility(View.VISIBLE);
        containerHorarios.removeAllViews();
        
        if (paciente.getHorarios() != null && !paciente.getHorarios().isEmpty()) {
            for (int i = 0; i < paciente.getHorarios().size(); i++) {
                final int index = i;
                HorarioAtencion h = paciente.getHorarios().get(i);
                View fila = getLayoutInflater().inflate(R.layout.item_horario_detalle, containerHorarios, false);
                TextView tvDia = fila.findViewById(R.id.tv_dia);
                TextView tvHorario = fila.findViewById(R.id.tv_horario);
                
                String displayFecha = (h.getFecha() != null && !h.getFecha().isEmpty()) 
                    ? h.getFecha() + " (" + h.getDia() + ")" 
                    : h.getDia();
                    
                tvDia.setText(displayFecha);
                tvHorario.setText(h.getHoraInicio() + " - " + h.getHoraFin());
                
                fila.setOnLongClickListener(v -> {
                    confirmarEliminarHorario(index);
                    return true;
                });

                LinearLayout layoutFila = fila.findViewById(R.id.layout_item_horario);
                if (layoutFila != null) {
                    ImageView ivEdit = new ImageView(this);
                    ivEdit.setImageResource(R.drawable.ic_edit);
                    ivEdit.setPadding(16, 8, 16, 8);
                    layoutFila.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    layoutFila.addView(ivEdit);
                    
                    ivEdit.setOnClickListener(v -> {
                        Intent intent = new Intent(this, FormularioPacienteActivity.class);
                        intent.putExtra("pacienteId", pacienteId);
                        intent.putExtra("horarioIndice", index);
                        startActivity(intent);
                    });
                }
                
                containerHorarios.addView(fila);
            }
        } else {
            TextView tvVacio = new TextView(this);
            tvVacio.setText("Sin turnos asignados");
            tvVacio.setPadding(0, 8, 0, 8);
            containerHorarios.addView(tvVacio);
        }
    }

    private void confirmarEliminarHorario(int index) {
        HorarioAtencion h = paciente.getHorarios().get(index);
        String fechaDisplay = (h.getFecha() != null && !h.getFecha().isEmpty()) ? h.getFecha() : h.getDia();
        
        new AlertDialog.Builder(this)
                .setTitle("Eliminar turno")
                .setMessage("¿Deseás eliminar el turno del día " + fechaDisplay + " a las " + h.getHoraInicio() + " hs?\nSe eliminará la atención registrada hoy (si existe) y se descontará la sesión.")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarTurnoYAtencion(index))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarTurnoYAtencion(int index) {
        HorarioAtencion h = paciente.getHorarios().get(index);
        AtencionRepository atencionRepo = new AtencionRepository();

        Calendar calInicio = Calendar.getInstance();
        Calendar calFin = Calendar.getInstance();
        
        // 1. Determinar el día de búsqueda (Fecha del turno o Hoy si es recurrente)
        Date fechaBusqueda = new Date(); // Hoy por defecto
        if (h.getFecha() != null && !h.getFecha().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date d = sdf.parse(h.getFecha());
                if (d != null) fechaBusqueda = d;
            } catch (Exception ignored) {}
        }

        calInicio.setTime(fechaBusqueda);
        calInicio.set(Calendar.HOUR_OF_DAY, 0);
        calInicio.set(Calendar.MINUTE, 0);
        calInicio.set(Calendar.SECOND, 0);
        
        calFin.setTime(fechaBusqueda);
        calFin.set(Calendar.HOUR_OF_DAY, 23);
        calFin.set(Calendar.MINUTE, 59);
        calFin.set(Calendar.SECOND, 59);

        // 2. Buscar y eliminar la atención si existe
        atencionRepo.buscarAtencionDelDia(pacienteId, new Timestamp(calInicio.getTime()), new Timestamp(calFin.getTime()))
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String atencionId = query.getDocuments().get(0).getId();
                        atencionRepo.eliminar(atencionId).addOnSuccessListener(v -> {
                            // 3. Descontar sesión si corresponde
                            if (!paciente.isParticular() && !paciente.isCertificadoDiscapacidad()) {
                                if (paciente.getSesionesAtendidas() > 0) {
                                    paciente.setSesionesAtendidas(paciente.getSesionesAtendidas() - 1);
                                }
                            }
                            guardarCambiosPaciente(index);
                        });
                    } else {
                        // Si no hay atención hoy, solo borra el turno
                        guardarCambiosPaciente(index);
                    }
                })
                .addOnFailureListener(e -> guardarCambiosPaciente(index));
    }

    private void guardarCambiosPaciente(int index) {
        if (index >= 0 && index < paciente.getHorarios().size()) {
            paciente.getHorarios().remove(index);
        }
        FirebaseFirestore.getInstance().collection("pacientes").document(pacienteId)
                .set(paciente)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Turno eliminado correctamente", Toast.LENGTH_SHORT).show();
                    cargarPaciente();
                });
    }

    private void cargarHistorial() {
        new AtencionRepository().obtenerPorPaciente(pacienteId)
                .addOnSuccessListener(query -> {
                    containerHistorial.setVisibility(View.VISIBLE);
                    findViewById(R.id.label_historial_seccion).setVisibility(View.VISIBLE);
                    containerHistorial.removeAllViews();

                    if (query.isEmpty()) {
                        TextView tvVacio = new TextView(this);
                        tvVacio.setText("Sin atenciones registradas");
                        tvVacio.setTextColor(Color.parseColor("#9E9E9E"));
                        tvVacio.setTextSize(14);
                        tvVacio.setPadding(0, 8, 0, 8);
                        containerHistorial.addView(tvVacio);
                        return;
                    }

                    java.util.List<Atencion> atenciones = new java.util.ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        Atencion a = doc.toObject(Atencion.class);
                        if (a != null) {
                            a.setId(doc.getId());
                            atenciones.add(a);
                        }
                    }
                    atenciones.sort((a, b) -> {
                        if (a.getFecha() == null) return 1;
                        if (b.getFecha() == null) return -1;
                        return b.getFecha().compareTo(a.getFecha());
                    });

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("es", "AR"));

                    for (Atencion a : atenciones) {
                        View fila = getLayoutInflater().inflate(R.layout.item_atencion, containerHistorial, false);
                        TextView tvFecha = fila.findViewById(R.id.tv_fecha_atencion);
                        TextView tvInfo = fila.findViewById(R.id.tv_info_atencion);
                        TextView tvMonto = fila.findViewById(R.id.tv_monto_atencion);
                        
                        View layoutDetalle = fila.findViewById(R.id.layout_detalle_clinico);
                        TextView tvObjetivo = fila.findViewById(R.id.tv_objetivo_detalle);
                        TextView tvObs = fila.findViewById(R.id.tv_observaciones_detalle);
                        ImageView ivExpand = fila.findViewById(R.id.iv_expand);

                        if (a.getFecha() != null) tvFecha.setText(sdf.format(a.getFecha().toDate()));
                        
                        // Sincronizar el total de sesiones con los datos actuales del paciente
                        String info = a.getTipoCobertura();
                        if (a.getSesionNumero() > 0) {
                            int total = a.getSesionesTotal();
                            // Si el paciente es de Obra Social (Orden), usamos el total actual del paciente
                            if (paciente.getSesionesOrden() > 0 && "Orden".equalsIgnoreCase(a.getTipoCobertura())) {
                                total = paciente.getSesionesOrden();
                            }
                            info += " · Sesión " + a.getSesionNumero() + "/" + total;
                        }
                        tvInfo.setText(info);
                        
                        if (a.getMonto() > 0) {
                            tvMonto.setVisibility(View.VISIBLE);
                            tvMonto.setText(String.format(new Locale("es", "AR"), "$ %,.0f", a.getMonto()));
                        } else {
                            tvMonto.setVisibility(View.GONE);
                        }

                        tvObjetivo.setText(a.getObjetivos() != null && !a.getObjetivos().isEmpty() 
                            ? a.getObjetivos() : "Sin objetivos registrados");
                        tvObs.setText(a.getObservaciones() != null && !a.getObservaciones().isEmpty() 
                            ? a.getObservaciones() : "Sin observaciones registradas");

                        fila.setOnClickListener(v -> {
                            if (layoutDetalle.getVisibility() == View.GONE) {
                                layoutDetalle.setVisibility(View.VISIBLE);
                                if (ivExpand != null) ivExpand.setRotation(180f);
                            } else {
                                layoutDetalle.setVisibility(View.GONE);
                                if (ivExpand != null) ivExpand.setRotation(0f);
                            }
                        });

                        fila.setOnLongClickListener(v -> {
                            confirmarEliminarAtencion(a);
                            return true;
                        });

                        containerHistorial.addView(fila);
                    }
                });
    }

    private void confirmarEliminarAtencion(Atencion a) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaStr = a.getFecha() != null ? sdf.format(a.getFecha().toDate()) : "";

        new AlertDialog.Builder(this)
                .setTitle("Eliminar del historial")
                .setMessage("¿Deseás eliminar la atención del día " + fechaStr + "?\nSi es un paciente de obra social, se restará una sesión del contador.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    new AtencionRepository().eliminar(a.getId()).addOnSuccessListener(v -> {
                        if (!paciente.isParticular() && !paciente.isCertificadoDiscapacidad()) {
                            FirebaseFirestore.getInstance().collection("pacientes").document(pacienteId)
                                    .update("sesionesAtendidas", FieldValue.increment(-1))
                                    .addOnSuccessListener(u -> cargarPaciente());
                        } else {
                            cargarPaciente();
                        }
                        Toast.makeText(this, "Atención eliminada", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setVisible(int... ids) {
        for (int id : ids) {
            View v = findViewById(id);
            if (v != null) v.setVisibility(View.VISIBLE);
        }
    }

    private void mostrarCampo(int viewId, String valor) {
        TextView tv = findViewById(viewId);
        if (tv != null) {
            tv.setText(valor != null && !valor.isEmpty() ? valor : "—");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detalle_paciente, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_editar) {
            Intent intent = new Intent(this, FormularioPacienteSimpleActivity.class);
            intent.putExtra("pacienteId", pacienteId);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_eliminar) {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar paciente")
                    .setMessage("¿Estás seguro que querés eliminar a " + paciente.getNombreCompleto() + "?")
                    .setPositiveButton("Eliminar", (dialog, which) -> eliminarPaciente())
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void eliminarPaciente() {
        new PacienteRepository().eliminar(pacienteId)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Paciente eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pacienteId != null) cargarPaciente();
    }
}
