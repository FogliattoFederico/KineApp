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
    private List<OrdenRemito> listaTodasLasOrdenes = new ArrayList<>();
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
                            repository.eliminar(liq.getId()).addOnSuccessListener(a -> cargarLiquidaciones());
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
        });

        rvLiquidaciones.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLiquidaciones.setAdapter(adapter);

        chipGroupFiltro.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filtrandoFacturadas = checkedIds.contains(R.id.chip_liq_facturadas);
            cargarLiquidaciones();
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_agregar_liquidacion);
        fab.setOnClickListener(v -> mostrarDialogoAgregar());

        cargarLiquidaciones();
        cargarOrdenesDisponibles();
    }

    private void cargarOrdenesDisponibles() {
        remitoRepository.obtenerTodos().addOnSuccessListener(query -> {
            listaTodasLasOrdenes.clear();
            for (var doc : query.getDocuments()) {
                Remito r = doc.toObject(Remito.class);
                if (r != null && r.getOrdenes() != null) {
                    listaTodasLasOrdenes.addAll(r.getOrdenes());
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

    private void mostrarDialogoAgregar() {
        ordenesSeleccionadas.clear();
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_liquidacion, null);
        TextInputEditText etFecha = dialogView.findViewById(R.id.et_fecha_liq);
        TextInputEditText etImporte = dialogView.findViewById(R.id.et_importe_liq);
        MaterialButton btnSeleccionarOrdenes = dialogView.findViewById(R.id.btn_seleccionar_remitos);
        TextView tvOrdenesSeleccionadas = dialogView.findViewById(R.id.tv_remitos_seleccionados);
        
        final Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etFecha.setText(sdf.format(cal.getTime()));

        etFecha.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (dp, y, m, d) -> {
                cal.set(y, m, d);
                etFecha.setText(sdf.format(cal.getTime()));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSeleccionarOrdenes.setOnClickListener(v -> {
            if (listaTodasLasOrdenes.isEmpty()) {
                Toast.makeText(getContext(), "No hay órdenes cargadas", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] items = new String[listaTodasLasOrdenes.size()];
            boolean[] checkedItems = new boolean[listaTodasLasOrdenes.size()];
            
            for (int i = 0; i < listaTodasLasOrdenes.size(); i++) {
                OrdenRemito o = listaTodasLasOrdenes.get(i);
                items[i] = String.format("%s\nFecha: %s | OS: %s\nAfiliado: %s | Código: %s",
                        o.getPacienteNombreCompleto(),
                        o.getFecha(),
                        o.getObraSocialNombre(),
                        o.getNumeroAfiliado() != null ? o.getNumeroAfiliado() : "S/N",
                        o.getCodigoPractica() != null ? o.getCodigoPractica() : "S/C");
                
                checkedItems[i] = ordenesSeleccionadas.contains(o);
            }

            new AlertDialog.Builder(getContext())
                    .setTitle("Vincular Órdenes")
                    .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                        OrdenRemito o = listaTodasLasOrdenes.get(which);
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
                .setTitle("Nueva Liquidación Pendiente")
                .setView(dialogView)
                .setPositiveButton("Agregar", (d, w) -> {
                    String impStr = etImporte.getText().toString().trim();
                    if (!impStr.isEmpty()) {
                        try {
                            double importe = Double.parseDouble(impStr);
                            LiquidacionColegio liq = new LiquidacionColegio(new Timestamp(cal.getTime()), importe, com.google.firebase.auth.FirebaseAuth.getInstance().getUid());
                            if (!ordenesSeleccionadas.isEmpty()) {
                                liq.setOrdenesVinculadas(new ArrayList<>(ordenesSeleccionadas));
                            }
                            repository.guardar(liq)
                                    .addOnSuccessListener(a -> {
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
}
