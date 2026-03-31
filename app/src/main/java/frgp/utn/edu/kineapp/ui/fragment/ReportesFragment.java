package frgp.utn.edu.kineapp.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.model.Atencion;
import frgp.utn.edu.kineapp.ui.view.SimplePieChartView;

public class ReportesFragment extends Fragment {

    private TextView tvMesAnio, tvCantAtenciones, tvCantPacientesUnicos, tvTotalDinero;
    private SimplePieChartView chartModalidad, chartCobertura;
    private int mesSeleccionado, anioSeleccionado;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reportes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvMesAnio = view.findViewById(R.id.tv_periodo_reporte);
        tvCantAtenciones = view.findViewById(R.id.tv_cant_atenciones);
        tvCantPacientesUnicos = view.findViewById(R.id.tv_cant_pacientes_unicos);
        tvTotalDinero = view.findViewById(R.id.tv_total_dinero);
        chartModalidad = view.findViewById(R.id.chart_modalidad);
        chartCobertura = view.findViewById(R.id.chart_cobertura);
        View layoutPeriodo = view.findViewById(R.id.layout_seleccionar_periodo);

        Calendar cal = Calendar.getInstance();
        mesSeleccionado = cal.get(Calendar.MONTH);
        anioSeleccionado = cal.get(Calendar.YEAR);

        actualizarTextoPeriodo();
        layoutPeriodo.setOnClickListener(v -> mostrarDialogoMesAnio());

        cargarReportes();
    }

    private void mostrarDialogoMesAnio() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_month_year_picker, null);
        NumberPicker monthPicker = dialogView.findViewById(R.id.picker_month);
        NumberPicker yearPicker = dialogView.findViewById(R.id.picker_year);

        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(meses);
        monthPicker.setValue(mesSeleccionado);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 5);
        yearPicker.setMaxValue(currentYear + 1);
        yearPicker.setValue(anioSeleccionado);

        new AlertDialog.Builder(getContext())
                .setTitle("Seleccionar Período")
                .setView(dialogView)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    mesSeleccionado = monthPicker.getValue();
                    anioSeleccionado = yearPicker.getValue();
                    actualizarTextoPeriodo();
                    cargarReportes();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarTextoPeriodo() {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        tvMesAnio.setText(meses[mesSeleccionado] + " " + anioSeleccionado);
    }

    private void cargarReportes() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("atenciones")
                .whereEqualTo("uidKinesiologo", uid)
                .get()
                .addOnSuccessListener(query -> {
                    if (!isAdded()) return;

                    double totalDinero = 0;
                    int atencionesMes = 0;
                    Set<String> pacientesUnicos = new HashSet<>();
                    
                    int dom = 0, cons = 0;
                    int cud = 0, ord = 0, part = 0;
                    
                    for (var doc : query.getDocuments()) {
                        Atencion a = doc.toObject(Atencion.class);
                        if (a != null && a.getFecha() != null) {
                            Calendar calAtencion = Calendar.getInstance();
                            calAtencion.setTime(a.getFecha().toDate());
                            if (calAtencion.get(Calendar.MONTH) == mesSeleccionado && 
                                calAtencion.get(Calendar.YEAR) == anioSeleccionado) {
                                
                                totalDinero += a.getMonto();
                                atencionesMes++;
                                if (a.getPacienteId() != null) {
                                    pacientesUnicos.add(a.getPacienteId());
                                }
                                
                                // Modalidad
                                if ("domicilio".equalsIgnoreCase(a.getModalidad())) dom++;
                                else if ("consultorio".equalsIgnoreCase(a.getModalidad())) cons++;
                                
                                // Cobertura
                                if ("CUD".equalsIgnoreCase(a.getTipoCobertura())) cud++;
                                else if ("Orden".equalsIgnoreCase(a.getTipoCobertura())) ord++;
                                else if ("Particular".equalsIgnoreCase(a.getTipoCobertura())) part++;
                            }
                        }
                    }
                    
                    tvTotalDinero.setText(String.format(new Locale("es", "AR"), "$ %,.0f", totalDinero));
                    tvCantAtenciones.setText(String.valueOf(atencionesMes));
                    tvCantPacientesUnicos.setText(String.valueOf(pacientesUnicos.size()));
                    
                    actualizarGraficos(dom, cons, cud, ord, part);
                });
    }

    private void actualizarGraficos(int dom, int cons, int cud, int ord, int part) {
        // Gráfico de Modalidad
        List<SimplePieChartView.Entry> modalidadEntries = new ArrayList<>();
        if (dom > 0) modalidadEntries.add(new SimplePieChartView.Entry("Domicilio", dom, 0xFF2196F3)); // Blue
        if (cons > 0) modalidadEntries.add(new SimplePieChartView.Entry("Consultorio", cons, 0xFFFFC107)); // Amber
        chartModalidad.setEntries(modalidadEntries);

        // Gráfico de Cobertura
        List<SimplePieChartView.Entry> coberturaEntries = new ArrayList<>();
        if (cud > 0) coberturaEntries.add(new SimplePieChartView.Entry("CUD", cud, 0xFF4CAF50)); // Green
        if (ord > 0) coberturaEntries.add(new SimplePieChartView.Entry("Orden", ord, 0xFF9C27B0)); // Purple
        if (part > 0) coberturaEntries.add(new SimplePieChartView.Entry("Particular", part, 0xFFFF5722)); // Deep Orange
        chartCobertura.setEntries(coberturaEntries);
    }
}