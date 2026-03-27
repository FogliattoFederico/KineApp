package frgp.utn.edu.kineapp;

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
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Locale;

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
        toolbar.setOverflowIcon(
                androidx.core.content.ContextCompat.getDrawable(this,
                        R.drawable.ic_more_vert_white));
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
                        if (modoTurno) {
                            cargarHistorial();
                        } else {
                            ocultarSeccion(R.id.label_horarios_seccion, containerHorarios);
                            ocultarSeccion(R.id.label_historial_seccion, containerHistorial);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar paciente",
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void ocultarSeccion(int labelId, View container) {
        View label = findViewById(labelId);
        if (label != null) label.setVisibility(View.GONE);
        if (container != null) container.setVisibility(View.GONE);
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

        if (modoTurno && paciente.getModalidad() != null) {
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

        if (modoTurno) {
            findViewById(R.id.label_clinica_seccion).setVisibility(View.VISIBLE);
            findViewById(R.id.card_clinica).setVisibility(View.VISIBLE);
            mostrarCampo(R.id.tv_diagnostico, paciente.getDiagnostico());
            mostrarCampo(R.id.tv_observaciones, paciente.getObservaciones());

            findViewById(R.id.label_cobertura_seccion).setVisibility(View.VISIBLE);
            findViewById(R.id.card_cobertura).setVisibility(View.VISIBLE);
            
            String tipoCobertura;
            if (paciente.isParticular()) tipoCobertura = "Particular";
            else if (paciente.isCertificadoDiscapacidad()) tipoCobertura = "CUD";
            else tipoCobertura = "Orden";
            
            mostrarCampo(R.id.tv_tipo_cobertura, tipoCobertura);

            if (!paciente.isParticular()) {
                setVisible(R.id.divider_obra_social, R.id.label_obra_social, R.id.tv_obra_social);
                mostrarCampo(R.id.tv_obra_social, paciente.getObraSocial());

                setVisible(R.id.divider_afiliado, R.id.label_numero_afiliado, R.id.tv_numero_afiliado);
                mostrarCampo(R.id.tv_numero_afiliado, paciente.getNumeroAfiliado());

                setVisible(R.id.divider_sesiones, R.id.label_sesiones, R.id.tv_sesiones);

                if (paciente.isCertificadoDiscapacidad()) {
                    ((TextView) findViewById(R.id.label_sesiones)).setText("SESIONES SEMANALES");
                    mostrarCampo(R.id.tv_sesiones, String.valueOf(paciente.getSesionesSemanales()));
                } else {
                    ((TextView) findViewById(R.id.label_sesiones)).setText("SESIONES DE LA ORDEN");
                    int restantes = paciente.getSesionesOrden() - paciente.getSesionesAtendidas();
                    mostrarCampo(R.id.tv_sesiones, restantes + " / " + paciente.getSesionesOrden());
                }
            } else if (paciente.getValorSesion() > 0) {
                setVisible(R.id.divider_sesiones, R.id.label_sesiones, R.id.tv_sesiones);
                ((TextView) findViewById(R.id.label_sesiones)).setText("VALOR SESIÓN");
                mostrarCampo(R.id.tv_sesiones, String.format(new Locale("es", "AR"), "$ %,.0f", paciente.getValorSesion()));
            }

            containerHorarios.removeAllViews();
            if (paciente.getHorarios() != null) {
                for (HorarioAtencion h : paciente.getHorarios()) {
                    View fila = getLayoutInflater().inflate(R.layout.item_horario_detalle, containerHorarios, false);
                    TextView tvDia = fila.findViewById(R.id.tv_dia);
                    TextView tvHorario = fila.findViewById(R.id.tv_horario);
                    
                    String displayFecha = (h.getFecha() != null && !h.getFecha().isEmpty()) 
                        ? h.getFecha() + " (" + h.getDia() + ")" 
                        : h.getDia();
                        
                    tvDia.setText(displayFecha);
                    tvHorario.setText(h.getHoraInicio() + " - " + h.getHoraFin());
                    containerHorarios.addView(fila);
                }
            }
        } else {
            findViewById(R.id.label_clinica_seccion).setVisibility(View.GONE);
            findViewById(R.id.card_clinica).setVisibility(View.GONE);

            boolean tieneOS = paciente.getObraSocial() != null && !paciente.getObraSocial().isEmpty();
            boolean tieneCUD = paciente.isCertificadoDiscapacidad();

            findViewById(R.id.label_cobertura_seccion).setVisibility(View.VISIBLE);
            findViewById(R.id.card_cobertura).setVisibility(View.VISIBLE);
            
            String labelTipo = tieneOS ? "Con Obra Social" : "Particular";
            if (tieneCUD) labelTipo += " (Posee CUD)";
            mostrarCampo(R.id.tv_tipo_cobertura, labelTipo);

            if (tieneOS) {
                setVisible(R.id.divider_obra_social, R.id.label_obra_social, R.id.tv_obra_social);
                mostrarCampo(R.id.tv_obra_social, paciente.getObraSocial());
                
                if (paciente.getNumeroAfiliado() != null && !paciente.getNumeroAfiliado().isEmpty()) {
                    setVisible(R.id.divider_afiliado, R.id.label_numero_afiliado, R.id.tv_numero_afiliado);
                    mostrarCampo(R.id.tv_numero_afiliado, paciente.getNumeroAfiliado());
                }
            }
        }
    }

    private void cargarHistorial() {
        new AtencionRepository().obtenerPorPaciente(pacienteId)
                .addOnSuccessListener(query -> {
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
                        
                        // Nuevos campos expandibles
                        View layoutDetalle = fila.findViewById(R.id.layout_detalle_clinico);
                        TextView tvObjetivo = fila.findViewById(R.id.tv_objetivo_detalle);
                        TextView tvObs = fila.findViewById(R.id.tv_observaciones_detalle);
                        ImageView ivExpand = fila.findViewById(R.id.iv_expand);

                        if (a.getFecha() != null) tvFecha.setText(sdf.format(a.getFecha().toDate()));
                        String info = a.getTipoCobertura();
                        if (a.getSesionNumero() > 0) info += " · Sesión " + a.getSesionNumero() + "/" + a.getSesionesTotal();
                        tvInfo.setText(info);
                        
                        if (a.getMonto() > 0) {
                            tvMonto.setVisibility(View.VISIBLE);
                            tvMonto.setText(String.format(new Locale("es", "AR"), "$ %,.0f", a.getMonto()));
                        } else {
                            tvMonto.setVisibility(View.GONE);
                        }

                        // Seteamos la información clínica
                        tvObjetivo.setText(a.getObjetivos() != null && !a.getObjetivos().isEmpty() 
                            ? a.getObjetivos() : "Sin objetivos registrados");
                        tvObs.setText(a.getObservaciones() != null && !a.getObservaciones().isEmpty() 
                            ? a.getObservaciones() : "Sin observaciones registradas");

                        // Lógica de expansión
                        fila.setOnClickListener(v -> {
                            if (layoutDetalle.getVisibility() == View.GONE) {
                                layoutDetalle.setVisibility(View.VISIBLE);
                                if (ivExpand != null) ivExpand.setRotation(180f);
                            } else {
                                layoutDetalle.setVisibility(View.GONE);
                                if (ivExpand != null) ivExpand.setRotation(0f);
                            }
                        });

                        containerHistorial.addView(fila);
                    }
                });
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
            Intent intent;
            if (modoTurno) {
                intent = new Intent(this, FormularioPacienteActivity.class);
            } else {
                intent = new Intent(this, FormularioPacienteSimpleActivity.class);
            }
            intent.putExtra("pacienteId", pacienteId);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_eliminar) {
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