package frgp.utn.edu.kineapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

    // Para la selección de órdenes
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
        
        cardTotalAdeudado = view.findViewById(R.id.card_total_adeudado);
        tvTotalAdeudado = view.findViewById(R.id.tv_total_adeudado);

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
                        .setTitle(titulo)
                        .setMessage(mensaje)
                        .setPositiveButton(botonPositivo, (d, w) -> {
                            repository.marcarComoFacturada(liq.getId(), !liq.isFacturada()).addOnSuccessListener(a -> {
                                if (isAdded()) {
                                    cargarLiquidaciones();
                                }
                            });
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }

            @Override
            public void onEdit(LiquidacionColegio liq) {
                mostrarDialogoAgregar(liq);
            }
        });

        rvLiquidaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLiquidaciones.setAdapter(adapter);

        chipGroupFiltro.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filtrandoFacturadas = checkedIds.contains(R.id.chip_liq_facturadas);
            cargarLiquidaciones();
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_agregar_liquidacion);
        fab.setOnClickListener(v -> mostrarDialogoAgregar(null));

        cargarLiquidaciones();
        cargarOrdenesDisponibles();
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
                    if (r.getOrdenes() != null) {
                        for (OrdenRemito o : r.getOrdenes()) {
                            o.setParentRemitoId(r.getId());
                            // Agregamos las que NO están asociadas a un pago
                            if (!o.isAsociadaAPago()) {
                                listaOrdenesDisponibles.add(o);
                            }
                        }
                    }
                }
            }
        });
    }

    private void cargarLiquidaciones() {
        repository.obtenerTodas()
                .addOnSuccessListener(query -> {
                    if (!isAdded()) return;
                    actualizarListaUI(query);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        if (e.getMessage() != null && e.getMessage().contains("FAILED_PRECONDITION")) {
                            cargarLiquidacionesSinOrden();
                        } else {
                            Toast.makeText(getContext(), "Error al cargar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void cargarLiquidacionesSinOrden() {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("liquidaciones_colegio")
                .whereEqualTo("uidKinesiologo", com.google.firebase.auth.FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(query -> {
                    if (!isAdded()) return;
                    actualizarListaUI(query);
                });
    }

    private void actualizarListaUI(com.google.firebase.firestore.QuerySnapshot query) {
        listaLiquidaciones.clear();
        double totalPendiente = 0;
        
        for (var doc : query.getDocuments()) {
            LiquidacionColegio liq = doc.toObject(LiquidacionColegio.class);
            if (liq != null) {
                liq.setId(doc.getId());
                if (!liq.isFacturada()) {
                    totalPendiente += liq.getImporte();
                }
                if (liq.isFacturada() == filtrandoFacturadas) {
                    listaLiquidaciones.add(liq);
                }
            }
        }
        
        adapter.actualizar(listaLiquidaciones);
        layoutEmpty.setVisibility(listaLiquidaciones.isEmpty() ? View.VISIBLE : View.GONE);
        rvLiquidaciones.setVisibility(listaLiquidaciones.isEmpty() ? View.GONE : View.VISIBLE);
        
        if (!filtrandoFacturadas && totalPendiente > 0) {
            cardTotalAdeudado.setVisibility(View.VISIBLE);
            tvTotalAdeudado.setText(String.format(new Locale("es", "AR"), "$ %,.2f", totalPendiente));
        } else {
            cardTotalAdeudado.setVisibility(View.GONE);
        }

        TextView tvEmpty = layoutEmpty.findViewById(R.id.tv_empty_liq);
        if (tvEmpty != null) {
            tvEmpty.setText(filtrandoFacturadas ? "No hay liquidaciones facturadas" : "No hay liquidaciones pendientes");
        }
    }

    private void mostrarDialogoAgregar(LiquidacionColegio liqExistente) {
        ordenesSeleccionadas.clear();
        if (liqExistente != null && liqExistente.getOrdenesVinculadas() != null) {
            ordenesSeleccionadas.addAll(liqExistente.getOrdenesVinculadas());
        }
        
        // Recargar para tener lo último (disponibles + las que ya tiene este recibo si estamos editando)
        remitoRepository.obtenerTodos().addOnSuccessListener(query -> {
            listaRemitosParaActualizar.clear();
            listaOrdenesDisponibles.clear();
            for (var doc : query.getDocuments()) {
                Remito r = doc.toObject(Remito.class);
                if (r != null) {
                    r.setId(doc.getId());
                    listaRemitosParaActualizar.add(r);
                    if (r.getOrdenes() != null) {
                        for (OrdenRemito o : r.getOrdenes()) {
                            o.setParentRemitoId(r.getId());
                            // Incluimos si está libre O si ya pertenece a este recibo que estamos editando
                            if (!o.isAsociadaAPago() || (liqExistente != null && ordenesSeleccionadas.contains(o))) {
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
            new DatePickerDialog(getContext(), (dp, y, m, d) -> {
                cal.set(y, m, d);
                etFecha.setText(sdf.format(cal.getTime()));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSeleccionarOrdenes.setOnClickListener(v -> {
            if (listaOrdenesDisponibles.isEmpty()) {
                Toast.makeText(getContext(), "No hay órdenes disponibles", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] items = new String[listaOrdenesDisponibles.size()];
            boolean[] checkedItems = new boolean[listaOrdenesDisponibles.size()];
            
            for (int i = 0; i < listaOrdenesDisponibles.size(); i++) {
                OrdenRemito o = listaOrdenesDisponibles.get(i);
                items[i] = String.format("%s\nFecha: %s | OS: %s",
                        o.getPacienteNombreCompleto(),
                        o.getFecha(),
                        o.getObraSocialNombre());
                
                checkedItems[i] = ordenesSeleccionadas.contains(o);
            }

            new AlertDialog.Builder(getContext())
                    .setTitle("Vincular Órdenes")
                    .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                        OrdenRemito o = listaOrdenesDisponibles.get(which);
                        if (isChecked) {
                            if (!ordenesSeleccionadas.contains(o)) ordenesSeleccionadas.add(o);
                        } else {
                            ordenesSeleccionadas.remove(o);
                        }
                    })
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        if (ordenesSeleccionadas.isEmpty()) {
                            tvOrdenesSeleccionadas.setText("Ninguna orden seleccionada");
                        } else {
                            tvOrdenesSeleccionadas.setText(ordenesSeleccionadas.size() + " orden(es) seleccionada(s)");
                        }
                    })
                    .show();
        });

        new AlertDialog.Builder(getContext())
                .setTitle(liqExistente == null ? "Nueva Liquidación Pendiente" : "Editar Liquidación")
                .setView(dialogView)
                .setPositiveButton(liqExistente == null ? "Agregar" : "Guardar", (d, w) -> {
                    String impStr = etImporte.getText().toString().trim();
                    if (!impStr.isEmpty()) {
                        try {
                            double importe = Double.parseDouble(impStr);
                            LiquidacionColegio liq = liqExistente != null ? liqExistente : new LiquidacionColegio(new Timestamp(cal.getTime()), importe, com.google.firebase.auth.FirebaseAuth.getInstance().getUid());
                            
                            // Si editamos, primero liberamos las órdenes viejas que ya no estén
                            if (liqExistente != null) {
                                liberarOrdenesNoSeleccionadas(liqExistente, ordenesSeleccionadas);
                                liq.setFechaLiquidacion(new Timestamp(cal.getTime()));
                                liq.setImporte(importe);
                            }

                            liq.setOrdenesVinculadas(new ArrayList<>(ordenesSeleccionadas));
                            
                            repository.guardar(liq)
                                    .addOnSuccessListener(a -> {
                                        asociarOrdenesConfirmadas();
                                        if (isAdded()) {
                                            cargarLiquidaciones();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (isAdded()) {
                                            Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Importe inválido", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void asociarOrdenesConfirmadas() {
        for (OrdenRemito o : ordenesSeleccionadas) {
            for (Remito r : listaRemitosParaActualizar) {
                if (r.getId().equals(o.getParentRemitoId())) {
                    for (OrdenRemito or : r.getOrdenes()) {
                        if (or.getId().equals(o.getId())) {
                            or.setAsociadaAPago(true);
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
            if (!nuevasSeleccionadas.contains(antigua)) {
                // Esta orden fue desmarcada, hay que liberarla
                for (Remito r : listaRemitosParaActualizar) {
                    if (r.getId().equals(antigua.getParentRemitoId())) {
                        for (OrdenRemito or : r.getOrdenes()) {
                            if (or.getId().equals(antigua.getId())) {
                                or.setAsociadaAPago(false);
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
