package frgp.utn.edu.kineapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PagosColegioFragment extends Fragment {

    private RecyclerView rvLiquidaciones;
    private LinearLayout layoutEmpty;
    private LiquidacionRepository repository;
    private RemitoRepository remitoRepository;
    private List<LiquidacionColegio> listaLiquidaciones = new ArrayList<>();
    private LiquidacionAdapter adapter;
    private ChipGroup chipGroupFiltro;
    private boolean filtrandoFacturadas = false;
    
    private CardView cardTotalAdeudado;
    private TextView tvTotalAdeudado;
    private TextView tvPeriodo;
    private int mesSeleccionado, anioSeleccionado;

    private List<Remito> listaRemitosParaActualizar = new ArrayList<>();
    private List<OrdenRemito> listaOrdenesDisponibles = new ArrayList<>();
    private List<OrdenRemito> ordenesSeleccionadas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pagos_colegio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new LiquidacionRepository();
        remitoRepository = new RemitoRepository();
        rvLiquidaciones = view.findViewById(R.id.rv_liquidaciones);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        chipGroupFiltro = view.findViewById(R.id.chip_group_filtro_liq);
        tvPeriodo = view.findViewById(R.id.tv_periodo_pago);
        View layoutPeriodo = view.findViewById(R.id.layout_seleccionar_periodo_pago);
        cardTotalAdeudado = view.findViewById(R.id.card_total_adeudado);
        tvTotalAdeudado = view.findViewById(R.id.tv_total_adeudado);

        Calendar cal = Calendar.getInstance();
        mesSeleccionado = cal.get(Calendar.MONTH);
        anioSeleccionado = cal.get(Calendar.YEAR);
        actualizarTextoPeriodo();

        adapter = new LiquidacionAdapter(listaLiquidaciones, new LiquidacionAdapter.OnLiquidacionClickListener() {
            @Override
            public void onDelete(LiquidacionColegio liq) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Eliminar liquidación")
                        .setMessage("¿Deseás borrar este registro definitivamente?")
                        .setPositiveButton("Eliminar", (d, w) -> {
                            repository.eliminar(liq.getId()).addOnSuccessListener(a -> {
                                liberarOrdenes(liq);
                                cargarLiquidaciones();
                            });
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
            @Override
            public void onMarkAsFacturada(LiquidacionColegio liq) {
                String titulo = liq.isFacturada() ? "Mover a pendientes" : "Liquidación facturada";
                String mensaje = liq.isFacturada() ? "¿Deseás marcar esta liquidación como pendiente nuevamente?" : "¿Ya emitiste el recibo para este pago?";
                String botonPositivo = liq.isFacturada() ? "Mover" : "Sí, ya facturé";
                new AlertDialog.Builder(getContext())
                        .setTitle(titulo).setMessage(mensaje)
                        .setPositiveButton(botonPositivo, (d, w) -> {
                            repository.marcarComoFacturada(liq.getId(), !liq.isFacturada()).addOnSuccessListener(a -> {
                                if (isAdded()) cargarLiquidaciones();
                            });
                        }).setNegativeButton("Cancelar", null).show();
            }
            @Override
            public void onEdit(LiquidacionColegio liq) {
                mostrarDialogoAgregar(liq);
            }
        });

        rvLiquidaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLiquidaciones.setAdapter(adapter);

        layoutPeriodo.setOnClickListener(v -> mostrarDialogoMesAnio());

        chipGroupFiltro.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filtrandoFacturadas = checkedIds.contains(R.id.chip_liq_facturadas);
            cargarLiquidaciones();
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_agregar_liquidacion);
        fab.setOnClickListener(v -> mostrarDialogoAgregar(null));

        cargarLiquidaciones();
        cargarOrdenesDisponibles();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarLiquidaciones();
        cargarOrdenesDisponibles();
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
                    cargarLiquidaciones();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarTextoPeriodo() {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        tvPeriodo.setText(meses[mesSeleccionado] + " " + anioSeleccionado);
    }

    private void cargarOrdenesDisponibles() {
        remitoRepository.obtenerTodos().addOnSuccessListener(query -> {
            listaRemitosParaActualizar.clear();
            listaOrdenesDisponibles.clear();
            for (var doc : query.getDocuments()) {
                Remito r = doc.toObject(Remito.class);
                if (r != null) {
                    r.setId(doc.getId());
                    listaRemitosParaActualizar.add(r);
                    // SOLO permitir vincular órdenes de remitos oficiales (NO directos)
                    if (!r.isEsDirecto() && r.getOrdenes() != null) {
                        for (OrdenRemito o : r.getOrdenes()) {
                            o.setParentRemitoId(r.getId());
                            if (!o.isAsociadaAPago()) listaOrdenesDisponibles.add(o);
                        }
                    }
                }
            }
        });
    }

    private void cargarLiquidaciones() {
        repository.obtenerPorMes(mesSeleccionado, anioSeleccionado)
                .addOnSuccessListener(query -> {
                    if (!isAdded()) return;
                    actualizarListaUI(query);
                });
    }

    private void actualizarListaUI(com.google.firebase.firestore.QuerySnapshot query) {
        listaLiquidaciones.clear();
        double totalPendiente = 0;
        Calendar calLiq = Calendar.getInstance();
        for (var doc : query.getDocuments()) {
            LiquidacionColegio liq = doc.toObject(LiquidacionColegio.class);
            if (liq != null) {
                liq.setId(doc.getId());
                if (liq.getFechaLiquidacion() != null) {
                    calLiq.setTime(liq.getFechaLiquidacion().toDate());
                    if (calLiq.get(Calendar.MONTH) == mesSeleccionado && calLiq.get(Calendar.YEAR) == anioSeleccionado) {
                        if (!liq.isFacturada()) totalPendiente += liq.getImporte();
                        if (liq.isFacturada() == filtrandoFacturadas) listaLiquidaciones.add(liq);
                    }
                }
            }
        }
        adapter.actualizar(listaLiquidaciones);
        layoutEmpty.setVisibility(listaLiquidaciones.isEmpty() ? View.VISIBLE : View.GONE);
        rvLiquidaciones.setVisibility(listaLiquidaciones.isEmpty() ? View.GONE : View.VISIBLE);
        if (!filtrandoFacturadas && totalPendiente > 0) {
            cardTotalAdeudado.setVisibility(View.VISIBLE);
            tvTotalAdeudado.setText(String.format(new Locale("es", "AR"), "$ %,.2f", totalPendiente));
        } else cardTotalAdeudado.setVisibility(View.GONE);
    }

    private void mostrarDialogoAgregar(LiquidacionColegio liqExistente) {
        ordenesSeleccionadas.clear();
        if (liqExistente != null && liqExistente.getOrdenesVinculadas() != null) {
            ordenesSeleccionadas.addAll(liqExistente.getOrdenesVinculadas());
        }
        remitoRepository.obtenerTodos().addOnSuccessListener(query -> {
            listaRemitosParaActualizar.clear();
            listaOrdenesDisponibles.clear();
            for (var doc : query.getDocuments()) {
                Remito r = doc.toObject(Remito.class);
                if (r != null) {
                    r.setId(doc.getId());
                    listaRemitosParaActualizar.add(r);
                    // SOLO permitir vincular órdenes de remitos oficiales (NO directos)
                    if (!r.isEsDirecto() && r.getOrdenes() != null) {
                        for (OrdenRemito o : r.getOrdenes()) {
                            o.setParentRemitoId(r.getId());
                            if (!o.isAsociadaAPago() || (liqExistente != null && ordenesSeleccionadas.stream().anyMatch(os -> os.getId().equals(o.getId())))) {
                                listaOrdenesDisponibles.add(o);
                            }
                        }
                    }
                }
            }
            abrirDialogo(liqExistente);
        });
    }

    private void abrirDialogo(LiquidacionColegio liqExistente) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_liquidacion, null);
        TextInputEditText etFecha = dialogView.findViewById(R.id.et_fecha_liq);
        TextInputEditText etImporte = dialogView.findViewById(R.id.et_importe_liq);
        MaterialButton btnSeleccionarOrdenes = dialogView.findViewById(R.id.btn_seleccionar_remitos);
        TextView tvOrdenesSeleccionadas = dialogView.findViewById(R.id.tv_remitos_seleccionados);
        final Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (liqExistente != null) {
            cal.setTime(liqExistente.getFechaLiquidacion().toDate());
            etImporte.setText(String.valueOf(liqExistente.getImporte()));
            tvOrdenesSeleccionadas.setText(ordenesSeleccionadas.size() + " orden(es) seleccionada(s)");
        }
        etFecha.setText(sdf.format(cal.getTime()));
        etFecha.setOnClickListener(v -> {
            android.app.DatePickerDialog dpd = new android.app.DatePickerDialog(getContext(), (dp, y, m, d) -> {
                cal.set(y, m, d);
                etFecha.setText(sdf.format(cal.getTime()));
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH));
            dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
            dpd.show();
        });
        btnSeleccionarOrdenes.setOnClickListener(v -> {
            if (listaOrdenesDisponibles.isEmpty()) { Toast.makeText(getContext(), "No hay órdenes disponibles de remitos oficiales", Toast.LENGTH_SHORT).show(); return; }
            String[] items = new String[listaOrdenesDisponibles.size()];
            boolean[] checkedItems = new boolean[listaOrdenesDisponibles.size()];
            for (int i = 0; i < listaOrdenesDisponibles.size(); i++) {
                OrdenRemito o = listaOrdenesDisponibles.get(i);
                items[i] = String.format("%s (Af: %s)\nFecha: %s | OS: %s | Cód: %s", 
                        o.getPacienteNombreCompleto(), 
                        o.getNumeroAfiliado() != null ? o.getNumeroAfiliado() : "-",
                        o.getFecha(), o.getObraSocialNombre(),
                        o.getCodigoPractica() != null ? o.getCodigoPractica() : "-");
                checkedItems[i] = ordenesSeleccionadas.stream().anyMatch(os -> os.getId().equals(o.getId()));
            }
            new AlertDialog.Builder(getContext()).setTitle("Vincular Órdenes Oficiales")
                    .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                        OrdenRemito o = listaOrdenesDisponibles.get(which);
                        if (isChecked) { 
                            if (ordenesSeleccionadas.stream().noneMatch(os -> os.getId().equals(o.getId()))) ordenesSeleccionadas.add(o); 
                        } else {
                            ordenesSeleccionadas.removeIf(os -> os.getId().equals(o.getId()));
                        }
                    }).setPositiveButton("Aceptar", (dialog, which) -> {
                        tvOrdenesSeleccionadas.setText(ordenesSeleccionadas.isEmpty() ? "Ninguna orden seleccionada" : ordenesSeleccionadas.size() + " orden(es) seleccionada(s)");
                    }).show();
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(liqExistente == null ? "Nueva Liquidación Pendiente" : "Editar Liquidación")
                .setView(dialogView)
                .setPositiveButton(liqExistente == null ? "Agregar" : "Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            View button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String impStr = etImporte.getText().toString().trim();
                if (impStr.isEmpty()) {
                    Toast.makeText(getContext(), "Ingresá un importe", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (ordenesSeleccionadas.isEmpty()) {
                    Toast.makeText(getContext(), "Debes seleccionar al menos una orden", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double importe = Double.parseDouble(impStr);
                    if (cal.getTime().after(new Date())) {
                        Toast.makeText(getContext(), "La fecha no puede ser futura", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    LiquidacionColegio liq = liqExistente != null ? liqExistente : new LiquidacionColegio(new Timestamp(cal.getTime()), importe, com.google.firebase.auth.FirebaseAuth.getInstance().getUid());
                    if (liqExistente != null) {
                        liberarOrdenesNoSeleccionadas(liqExistente, ordenesSeleccionadas);
                        liq.setFechaLiquidacion(new Timestamp(cal.getTime()));
                        liq.setImporte(importe);
                    }
                    liq.setOrdenesVinculadas(new ArrayList<>(ordenesSeleccionadas));
                    repository.guardar(liq).addOnSuccessListener(a -> {
                        asociarOrdenesConfirmadas(liq.getId());
                        if (isAdded()) cargarLiquidaciones();
                        dialog.dismiss();
                    });
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Importe inválido", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void asociarOrdenesConfirmadas(String pagoId) {
        for (OrdenRemito o : ordenesSeleccionadas) {
            for (Remito r : listaRemitosParaActualizar) {
                if (r.getId().equals(o.getParentRemitoId())) {
                    for (OrdenRemito or : r.getOrdenes()) {
                        if (or.getId().equals(o.getId())) { 
                            or.setAsociadaAPago(true); 
                            or.setTipoVinculo("COLEGIO"); 
                            or.setIdVinculoAsociado(pagoId);
                            remitoRepository.guardar(r); 
                            break; 
                        }
                    }
                }
            }
        }
    }

    private void liberarOrdenesNoSeleccionadas(LiquidacionColegio liqOriginal, List<OrdenRemito> nuevasSeleccionadas) {
        if (liqOriginal.getOrdenesVinculadas() == null) return;
        for (OrdenRemito antigua : liqOriginal.getOrdenesVinculadas()) {
            boolean sigueSeleccionada = nuevasSeleccionadas.stream().anyMatch(n -> n.getId().equals(antigua.getId()));
            
            if (!sigueSeleccionada) {
                for (Remito r : listaRemitosParaActualizar) {
                    if (r.getId().equals(antigua.getParentRemitoId())) {
                        for (OrdenRemito or : r.getOrdenes()) {
                            if (or.getId().equals(antigua.getId())) { 
                                or.setAsociadaAPago(false); 
                                or.setTipoVinculo(null);
                                or.setIdVinculoAsociado(null);
                                remitoRepository.guardar(r); 
                                break; 
                            }
                        }
                    }
                }
            }
        }
    }

    private void liberarOrdenes(LiquidacionColegio liq) {
        if (liq.getOrdenesVinculadas() == null) return;
        remitoRepository.obtenerTodos().addOnSuccessListener(query -> {
            for (var doc : query.getDocuments()) {
                Remito r = doc.toObject(Remito.class);
                if (r != null) {
                    r.setId(doc.getId());
                    boolean modificado = false;
                    for (OrdenRemito ov : liq.getOrdenesVinculadas()) {
                        for (OrdenRemito or : r.getOrdenes()) {
                            if (or.getId().equals(ov.getId())) { 
                                or.setAsociadaAPago(false); 
                                or.setTipoVinculo(null);
                                or.setIdVinculoAsociado(null);
                                modificado = true; 
                            }
                        }
                    }
                    if (modificado) remitoRepository.guardar(r);
                }
            }
        });
    }
}