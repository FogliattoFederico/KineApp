package frgp.utn.edu.kineapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AgendaFragment extends Fragment {

    private TextView tvNombreDia, tvFechaCompleta, tvRecaudacion;
    private TextView btnToggleDomicilio, btnToggleConsultorio;
    private LinearLayout containerDiasSemana, layoutEmpty;
    private RecyclerView rvPacientes;
    private TurnoAdapter adapter;
    private List<TurnoAdapter.Turno> listaTurnos = new ArrayList<>();
    private Calendar fechaActual;
    private String modalidadActual = "domicilio";
    private String modalidadTrabajoPerfil = "ambos";

    private final String[] DIAS_SEMANA = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
    private final String[] DIAS_COMPLETOS = {"Domingo", "Lunes", "Martes",
            "Miércoles", "Jueves", "Viernes", "Sábado"};
    private final String[] DIAS_KINE = {"Domingo", "Lunes", "Martes",
            "Miércoles", "Jueves", "Viernes", "Sábado"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agenda, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View spacer = view.findViewById(R.id.status_bar_spacer);
        int resourceId = getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            int height = getResources().getDimensionPixelSize(resourceId);
            spacer.getLayoutParams().height = height;
            spacer.requestLayout();
        }

        tvNombreDia = view.findViewById(R.id.tv_nombre_dia);
        tvFechaCompleta = view.findViewById(R.id.tv_fecha_completa);
        tvRecaudacion = view.findViewById(R.id.tv_recaudacion);
        btnToggleDomicilio = view.findViewById(R.id.btn_toggle_domicilio);
        btnToggleConsultorio = view.findViewById(R.id.btn_toggle_consultorio);
        containerDiasSemana = view.findViewById(R.id.container_dias_semana);
        rvPacientes = view.findViewById(R.id.rv_turnos);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        fechaActual = Calendar.getInstance();

        adapter = new TurnoAdapter(listaTurnos, (turno, atendido) -> {
                cargarRecaudacionMes();
                actualizarVista();
        });
        adapter.setFechaAgenda(fechaActual);

        rvPacientes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPacientes.addItemDecoration(
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        rvPacientes.setAdapter(adapter);

        btnToggleDomicilio.setOnClickListener(v -> {
            modalidadActual = "domicilio";
            actualizarToggle();
            actualizarVista();
        });

        btnToggleConsultorio.setOnClickListener(v -> {
            modalidadActual = "consultorio";
            actualizarToggle();
            actualizarVista();
        });

        view.findViewById(R.id.btn_dia_anterior).setOnClickListener(v -> {
            fechaActual.add(Calendar.DAY_OF_MONTH, -7);
            actualizarVista();
        });

        view.findViewById(R.id.btn_dia_siguiente).setOnClickListener(v -> {
            fechaActual.add(Calendar.DAY_OF_MONTH, 7);
            actualizarVista();
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_nuevo_paciente);
        fab.setOnClickListener(v ->
                startActivity(new Intent(getContext(), FormularioPacienteActivity.class))
        );

        configurarModalidadesSegunPerfil();
    }

    private void configurarModalidadesSegunPerfil() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    Context context = getContext();
                    if (!isAdded() || context == null) return;
                    
                    String mod = doc.getString("modalidadTrabajo");
                    if (mod != null) {
                        modalidadTrabajoPerfil = mod;
                        if ("domicilio".equals(mod)) {
                            btnToggleConsultorio.setVisibility(View.GONE);
                            modalidadActual = "domicilio";
                        } else if ("consultorio".equals(mod)) {
                            btnToggleDomicilio.setVisibility(View.GONE);
                            modalidadActual = "consultorio";
                        } else {
                            btnToggleDomicilio.setVisibility(View.VISIBLE);
                            btnToggleConsultorio.setVisibility(View.VISIBLE);
                        }
                    }
                    determinarModalidadInicial();
                });
    }

    private void determinarModalidadInicial() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("pacientes")
                .whereEqualTo("uidKinesiologo", uid)
                .orderBy("ultimaActualizacion", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    Context context = getContext();
                    if (!isAdded() || context == null) return;
                    
                    if (!query.isEmpty()) {
                        Paciente p = query.getDocuments().get(0).toObject(Paciente.class);
                        if (p != null && p.getModalidad() != null && !p.getModalidad().isEmpty()) {
                            if (modalidadTrabajoPerfil.equals("ambos") || modalidadTrabajoPerfil.equals(p.getModalidad())) {
                                modalidadActual = p.getModalidad();
                            }
                        }
                    }
                    actualizarToggle();
                    actualizarVista();
                    cargarRecaudacionMes();
                })
                .addOnFailureListener(e -> {
                    Context context = getContext();
                    if (!isAdded() || context == null) return;
                    actualizarToggle();
                    actualizarVista();
                    cargarRecaudacionMes();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded() && getContext() != null) {
            configurarModalidadesSegunPerfil();
        }
    }

    private void actualizarToggle() {
        Context context = getContext();
        if (!isAdded() || context == null) return;
        
        if ("domicilio".equals(modalidadActual)) {
            btnToggleDomicilio.setBackgroundResource(R.drawable.bg_toggle_activo);
            btnToggleDomicilio.setTextColor(Color.parseColor("#1565C0"));
            btnToggleConsultorio.setBackgroundColor(Color.TRANSPARENT);
            btnToggleConsultorio.setTextColor(Color.parseColor("#BBDEFB"));
        } else {
            btnToggleConsultorio.setBackgroundResource(R.drawable.bg_toggle_activo);
            btnToggleConsultorio.setTextColor(Color.parseColor("#1565C0"));
            btnToggleDomicilio.setBackgroundColor(Color.TRANSPARENT);
            btnToggleDomicilio.setTextColor(Color.parseColor("#BBDEFB"));
        }
    }

    private void actualizarVista() {
        Context context = getContext();
        if (!isAdded() || context == null) return;
        
        int diaSemana = fechaActual.get(Calendar.DAY_OF_WEEK) - 1;
        tvNombreDia.setText(DIAS_COMPLETOS[diaSemana]);
        SimpleDateFormat sdf = new SimpleDateFormat(
                "d 'de' MMMM 'de' yyyy", new Locale("es", "AR"));
        tvFechaCompleta.setText(sdf.format(fechaActual.getTime()));
        actualizarChipsSemana();
        cargarTurnosDelDia(DIAS_KINE[diaSemana]);
    }

    private void actualizarChipsSemana() {
        Context context = getContext();
        if (!isAdded() || context == null) return;
        
        containerDiasSemana.removeAllViews();
        Calendar lunes = (Calendar) fechaActual.clone();
        int dow = lunes.get(Calendar.DAY_OF_WEEK);
        int diff = Calendar.MONDAY - dow;
        if (dow == Calendar.SUNDAY) diff = -6;
        lunes.add(Calendar.DAY_OF_MONTH, diff);

        for (int i = 0; i < 7; i++) {
            Calendar dia = (Calendar) lunes.clone();
            dia.add(Calendar.DAY_OF_MONTH, i);

            TextView chip = new TextView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            params.setMargins(3, 0, 3, 0);
            chip.setLayoutParams(params);
            chip.setGravity(android.view.Gravity.CENTER);
            chip.setPadding(0, 8, 0, 8);
            chip.setTextSize(10);

            boolean esHoy = esMismoDia(dia, fechaActual);
            chip.setText(DIAS_SEMANA[dia.get(Calendar.DAY_OF_WEEK) - 1]
                    + "\n" + dia.get(Calendar.DAY_OF_MONTH));

            if (esHoy) {
                chip.setTextColor(android.graphics.Color.WHITE);
                chip.setBackgroundResource(R.drawable.bg_chip_seleccionado);
            } else {
                chip.setTextColor(android.graphics.Color.argb(180, 255, 255, 255));
                chip.setBackgroundResource(R.drawable.bg_chip_normal);
            }

            final Calendar diaFinal = dia;
            chip.setOnClickListener(v -> {
                fechaActual = (Calendar) diaFinal.clone();
                actualizarVista();
            });

            containerDiasSemana.addView(chip);
        }
    }

    private boolean esMismoDia(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private void cargarTurnosDelDia(String nombreDia) {
        Context context = getContext();
        if (!isAdded() || context == null) return;
        
        adapter.setFechaAgenda(fechaActual);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        listaTurnos.clear();

        FirebaseFirestore.getInstance()
                .collection("pacientes")
                .whereEqualTo("uidKinesiologo", uid)
                .get()
                .addOnSuccessListener(query -> {
                    if (!isAdded() || getContext() == null) return;
                    
                    for (var doc : query.getDocuments()) {
                        Paciente p = doc.toObject(Paciente.class);
                        if (p == null || p.getHorarios() == null) continue;
                        p.setId(doc.getId());

                        if (!modalidadActual.equals(p.getModalidad())) continue;

                        for (HorarioAtencion h : p.getHorarios()) {
                            if (normalizarTexto(nombreDia).equalsIgnoreCase(
                                    normalizarTexto(h.getDia()))) {
                                String tipoCobertura;
                                if (p.isParticular()) tipoCobertura = "Particular";
                                else if (p.isCertificadoDiscapacidad()) tipoCobertura = "CUD";
                                else tipoCobertura = "Orden";

                                int sesRestantes = p.getSesionesOrden() > 0
                                        ? p.getSesionesOrden() - p.getSesionesAtendidas()
                                        : -1;

                                listaTurnos.add(new TurnoAdapter.Turno(
                                        h.getHoraInicio(),
                                        p.getNombreCompleto(),
                                        p.getDiagnostico(),
                                        p.getObraSocial(),
                                        tipoCobertura,
                                        false,
                                        p.getId(),
                                        p.getValorSesion(),
                                        sesRestantes,
                                        p.getSesionesOrden(),
                                        p.getModalidad()
                                ));
                            }
                        }
                    }
                    listaTurnos.sort((a, b) -> a.hora.compareTo(b.hora));
                    cargarAtencionesDel(nombreDia);
                });
    }

    private void cargarAtencionesDel(String nombreDia) {
        Context context = getContext();
        if (!isAdded() || context == null) return;
        
        Calendar inicioDia = (Calendar) fechaActual.clone();
        inicioDia.set(Calendar.HOUR_OF_DAY, 0);
        inicioDia.set(Calendar.MINUTE, 0);
        inicioDia.set(Calendar.SECOND, 0);

        Calendar finDia = (Calendar) fechaActual.clone();
        finDia.set(Calendar.HOUR_OF_DAY, 23);
        finDia.set(Calendar.MINUTE, 59);
        finDia.set(Calendar.SECOND, 59);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("atenciones")
                .whereEqualTo("uidKinesiologo", uid)
                .get()
                .addOnSuccessListener(query -> {
                    if (!isAdded() || getContext() == null) return;
                    
                    for (var doc : query.getDocuments()) {
                        Atencion a = doc.toObject(Atencion.class);
                        if (a == null || a.getFecha() == null) continue;

                        long fechaMs = a.getFecha().toDate().getTime();
                        if (fechaMs < inicioDia.getTimeInMillis() ||
                                fechaMs > finDia.getTimeInMillis()) continue;

                        for (TurnoAdapter.Turno turno : listaTurnos) {
                            if (turno.pacienteId.equals(a.getPacienteId())) {
                                turno.atendido = true;
                                turno.atencionId = doc.getId();
                                break;
                            }
                        }
                    }
                    adapter.actualizar(listaTurnos);
                    layoutEmpty.setVisibility(
                            listaTurnos.isEmpty() ? View.VISIBLE : View.GONE);
                    rvPacientes.setVisibility(
                            listaTurnos.isEmpty() ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    adapter.actualizar(listaTurnos);
                    layoutEmpty.setVisibility(
                            listaTurnos.isEmpty() ? View.VISIBLE : View.GONE);
                    rvPacientes.setVisibility(
                            listaTurnos.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }

    private void cargarRecaudacionMes() {
        Context context = getContext();
        if (!isAdded() || context == null) return;
        
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("atenciones")
                .whereEqualTo("uidKinesiologo", uid)
                .get()
                .addOnSuccessListener(query -> {
                    Context context2 = getContext();
                    if (!isAdded() || context2 == null) return;
                    
                    Calendar hoy = Calendar.getInstance();
                    int mesActual = hoy.get(Calendar.MONTH);
                    int anioActual = hoy.get(Calendar.YEAR);

                    double total = 0;
                    for (var doc : query.getDocuments()) {
                        Atencion a = doc.toObject(Atencion.class);
                        if (a == null || a.getFecha() == null || a.getMonto() <= 0) continue;

                        Calendar fechaAtencion = Calendar.getInstance();
                        fechaAtencion.setTime(a.getFecha().toDate());

                        if (fechaAtencion.get(Calendar.MONTH) == mesActual
                                && fechaAtencion.get(Calendar.YEAR) == anioActual) {
                            total += a.getMonto();
                        }
                    }

                    String totalStr = String.format(new Locale("es", "AR"),
                            "$ %,.0f", total);
                    if (tvRecaudacion != null) tvRecaudacion.setText(totalStr);
                });
    }
    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        return java.text.Normalizer
                .normalize(texto.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }
}