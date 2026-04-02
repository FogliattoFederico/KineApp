package frgp.utn.edu.kineapp.ui.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.adapter.FacturaAdapter;
import frgp.utn.edu.kineapp.model.Factura;
import frgp.utn.edu.kineapp.model.LiquidacionColegio;
import frgp.utn.edu.kineapp.model.OrdenRemito;
import frgp.utn.edu.kineapp.model.Remito;
import frgp.utn.edu.kineapp.repository.FacturaRepository;
import frgp.utn.edu.kineapp.repository.LiquidacionRepository;
import frgp.utn.edu.kineapp.repository.RemitoRepository;
import frgp.utn.edu.kineapp.ui.activity.FormularioPacienteSimpleActivity;

public class FacturasEmitidasFragment extends Fragment {

    private RecyclerView rvFacturas;
    private LinearLayout layoutEmpty;
    private FacturaRepository repository;
    private RemitoRepository remitoRepository;
    private LiquidacionRepository liquidacionRepository;
    private List<Factura> listaFacturas = new ArrayList<>();
    private List<Factura> listaFiltrada = new ArrayList<>();
    private FacturaAdapter adapter;
    private ChipGroup chipGroupEstado, chipGroupObrasSociales;
    private String obraSocialSeleccionada = null;
    
    private TextView tvPeriodo;
    private int mesSeleccionado, anioSeleccionado;

    private final String[] TIPOS = {
            "Factura A", "Factura B", "Factura C",
            "Recibo A", "Recibo B", "Recibo C"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_facturas_emitidas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new FacturaRepository();
        remitoRepository = new RemitoRepository();
        liquidacionRepository = new LiquidacionRepository();
        
        rvFacturas = view.findViewById(R.id.rv_facturas);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        chipGroupEstado = view.findViewById(R.id.chip_group_estado);
        chipGroupObrasSociales = view.findViewById(R.id.chip_group_obras_sociales);
        tvPeriodo = view.findViewById(R.id.tv_periodo_facturas);
        View layoutPeriodo = view.findViewById(R.id.layout_seleccionar_periodo_facturas);

        Calendar cal = Calendar.getInstance();
        mesSeleccionado = cal.get(Calendar.MONTH);
        anioSeleccionado = cal.get(Calendar.YEAR);
        actualizarTextoPeriodo();

        adapter = new FacturaAdapter(listaFiltrada, new FacturaAdapter.OnFacturaClickListener() {
            @Override
            public void onClick(Factura factura) {
                mostrarDialogoFactura(factura);
            }
            @Override
            public void onLongClick(Factura factura) {
                verificarYConfirmarEliminacion(factura);
            }
        }, (factura, cobrada) -> {
            repository.actualizarCobrada(factura.getId(), cobrada)
                    .addOnSuccessListener(unused -> cargarFacturas());
        });

        rvFacturas.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFacturas.setAdapter(adapter);

        layoutPeriodo.setOnClickListener(v -> mostrarDialogoMesAnio());

        FloatingActionButton fab = view.findViewById(R.id.fab_agregar_factura);
        fab.setOnClickListener(v -> mostrarDialogoFactura(null));

        chipGroupEstado.setOnCheckedStateChangeListener((group, checkedIds) -> aplicarFiltros());

        cargarFacturas();
        cargarObrasSocialesChips();
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
                    cargarFacturas();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarTextoPeriodo() {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        tvPeriodo.setText(meses[mesSeleccionado] + " " + anioSeleccionado);
    }

    private void verificarYConfirmarEliminacion(Factura factura) {
        // Verificar si hay liquidaciones (pagos) asociados a esta factura
        liquidacionRepository.obtenerTodas().addOnSuccessListener(query -> {
            boolean tienePagosAsociados = false;
            for (var doc : query.getDocuments()) {
                LiquidacionColegio liq = doc.toObject(LiquidacionColegio.class);
                if (liq != null && factura.getId().equals(liq.getFacturaId())) {
                    tienePagosAsociados = true;
                    break;
                }
            }
            confirmarEliminacion(factura, tienePagosAsociados);
        });
    }

    private void confirmarEliminacion(Factura factura, boolean tienePagosAsociados) {
        String mensaje = "¿Estás seguro que deseás eliminar esta factura definitivamente? Esto desvinculará cualquier orden asociada.";
        if (tienePagosAsociados) {
            mensaje = "ATENCIÓN: Esta factura está asociada a un PAGO (liquidación). Si la eliminás, el pago se desvinculará y volverá a quedar pendiente de facturar. ¿Deseás continuar?";
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar factura")
                .setMessage(mensaje)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    String facturaId = factura.getId();
                    repository.eliminar(facturaId)
                            .addOnSuccessListener(a -> {
                                desvincularOrdenesDeFactura(facturaId);
                                desvincularPagosDeFactura(facturaId);
                                Toast.makeText(getContext(), "Factura eliminada", Toast.LENGTH_SHORT).show();
                                cargarFacturas();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void desvincularOrdenesDeFactura(String facturaId) {
        remitoRepository.obtenerTodos().addOnSuccessListener(query -> {
            for (var doc : query.getDocuments()) {
                Remito r = doc.toObject(Remito.class);
                if (r != null && r.getOrdenes() != null) {
                    boolean modificado = false;
                    for (OrdenRemito o : r.getOrdenes()) {
                        if (o.isAsociadaAPago() && "DIRECTO".equals(o.getTipoVinculo()) 
                                && facturaId.equals(o.getIdVinculoAsociado())) {
                            o.setAsociadaAPago(false);
                            o.setTipoVinculo(null);
                            o.setIdVinculoAsociado(null);
                            o.setMesVinculo(null);
                            modificado = true;
                        }
                    }
                    if (modificado) {
                        remitoRepository.guardar(r);
                    }
                }
            }
        });
    }

    private void desvincularPagosDeFactura(String facturaId) {
        liquidacionRepository.obtenerTodas().addOnSuccessListener(query -> {
            for (var doc : query.getDocuments()) {
                LiquidacionColegio liq = doc.toObject(LiquidacionColegio.class);
                if (liq != null && facturaId.equals(liq.getFacturaId())) {
                    liq.setFacturada(false);
                    liq.setFacturaId(null);
                    liq.setFacturaNumero(null);
                    liq.setFacturaTipo(null);
                    liq.setFacturaFecha(null);
                    liquidacionRepository.guardar(liq);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarFacturas();
    }

    private void cargarFacturas() {
        repository.obtenerTodas()
                .addOnSuccessListener(query -> {
                    if (!isAdded()) return;
                    listaFacturas.clear();
                    Calendar calFactura = Calendar.getInstance();
                    for (var doc : query.getDocuments()) {
                        Factura f = doc.toObject(Factura.class);
                        if (f != null) {
                            f.setId(doc.getId());
                            if (f.getFecha() != null) {
                                calFactura.setTime(f.getFecha().toDate());
                                if (calFactura.get(Calendar.MONTH) == mesSeleccionado && 
                                    calFactura.get(Calendar.YEAR) == anioSeleccionado) {
                                    listaFacturas.add(f);
                                }
                            }
                        }
                    }
                    listaFacturas.sort((a, b) -> {
                        if (a.getFecha() == null) return 1;
                        if (b.getFecha() == null) return -1;
                        return b.getFecha().compareTo(a.getFecha());
                    });
                    aplicarFiltros();
                });
    }

    private void cargarObrasSocialesChips() {
        List<String> obrasSociales = new ArrayList<>(Arrays.asList(FormularioPacienteSimpleActivity.OBRAS_SOCIALES));
        obrasSociales.remove("Corte de Crédito");
        Collections.sort(obrasSociales);
        
        chipGroupObrasSociales.removeAllViews();
        for (String os : obrasSociales) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_filter, chipGroupObrasSociales, false);
            chip.setText(os);
            if (os.equals(obraSocialSeleccionada)) chip.setChecked(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) obraSocialSeleccionada = os;
                else if (obraSocialSeleccionada != null && obraSocialSeleccionada.equals(os)) obraSocialSeleccionada = null;
                aplicarFiltros();
            });
            chipGroupObrasSociales.addView(chip);
        }
    }

    private void aplicarFiltros() {
        int checkedEstadoId = chipGroupEstado.getCheckedChipId();
        listaFiltrada.clear();
        for (Factura f : listaFacturas) {
            boolean pasaEstado = true;
            if (checkedEstadoId == R.id.chip_cobradas) pasaEstado = f.isCobrada();
            else if (checkedEstadoId == R.id.chip_no_cobradas) pasaEstado = !f.isCobrada();
            boolean pasaOS = true;
            if (obraSocialSeleccionada != null) pasaOS = obraSocialSeleccionada.equals(f.getObraSocial());
            if (pasaEstado && pasaOS) listaFiltrada.add(f);
        }
        adapter.actualizar(listaFiltrada);
        layoutEmpty.setVisibility(listaFiltrada.isEmpty() ? View.VISIBLE : View.GONE);
        rvFacturas.setVisibility(listaFiltrada.isEmpty() ? View.GONE : View.VISIBLE);
        TextView tvEmpty = layoutEmpty.findViewById(R.id.tv_empty_text);
        if (tvEmpty != null) tvEmpty.setText("No hay facturas en este período");
    }

    private void mostrarDialogoFactura(Factura facturaExistente) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_factura, null);
        AutoCompleteTextView etTipo = dialogView.findViewById(R.id.et_tipo);
        TextInputEditText etNumeroParte1 = dialogView.findViewById(R.id.et_numero_parte1);
        TextInputEditText etNumeroParte2 = dialogView.findViewById(R.id.et_numero_parte2);
        TextInputEditText etFecha = dialogView.findViewById(R.id.et_fecha);
        AutoCompleteTextView etObraSocial = dialogView.findViewById(R.id.et_obra_social);
        TextInputEditText etDescripcion = dialogView.findViewById(R.id.et_descripcion);
        TextInputEditText etImporte = dialogView.findViewById(R.id.et_importe);
        com.google.android.material.button.MaterialButton btnGuardar = dialogView.findViewById(R.id.btn_guardar);
        com.google.android.material.button.MaterialButton btnCancelar = dialogView.findViewById(R.id.btn_cancelar);
        TextView tvTitulo = dialogView.findViewById(R.id.tv_titulo_dialog);

        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, TIPOS);
        etTipo.setAdapter(tipoAdapter);

        final java.util.Calendar[] fechaSeleccionada = {java.util.Calendar.getInstance()};
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "AR"));
        etFecha.setText(sdf.format(fechaSeleccionada[0].getTime()));
        etFecha.setOnClickListener(v -> {
            java.util.Calendar cal = fechaSeleccionada[0];
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (dp, y, m, d) -> {
                cal.set(y, m, d);
                etFecha.setText(sdf.format(cal.getTime()));
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        List<String> todasObrasSociales = new ArrayList<>(Arrays.asList(FormularioPacienteSimpleActivity.OBRAS_SOCIALES));
        Collections.sort(todasObrasSociales);
        ArrayAdapter<String> osAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, todasObrasSociales);
        etObraSocial.setAdapter(osAdapter);
        etObraSocial.setThreshold(1);
        etObraSocial.setOnClickListener(v -> etObraSocial.showDropDown());

        if (facturaExistente != null) {
            tvTitulo.setText("Editar factura");
            etTipo.setText(facturaExistente.getTipoComprobante(), false);
            etImporte.setText(String.valueOf(facturaExistente.getImporte()));
            etObraSocial.setText(facturaExistente.getObraSocial(), false);
            etDescripcion.setText(facturaExistente.getDescripcion());
            if (facturaExistente.getFecha() != null) {
                fechaSeleccionada[0].setTime(facturaExistente.getFecha().toDate());
                etFecha.setText(sdf.format(fechaSeleccionada[0].getTime()));
            }
            if (facturaExistente.getNumero() != null && facturaExistente.getNumero().contains("-")) {
                String[] partes = facturaExistente.getNumero().split("-");
                if (partes.length == 2) {
                    etNumeroParte1.setText(partes[0]);
                    etNumeroParte2.setText(partes[1]);
                }
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(true).create();
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnGuardar.setOnClickListener(v -> {
            String tipo = etTipo.getText().toString().trim();
            String parte1 = etNumeroParte1.getText().toString().trim();
            String parte2 = etNumeroParte2.getText().toString().trim();
            String obraSocial = etObraSocial.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            String importeStr = etImporte.getText().toString().trim();
            
            if (tipo.isEmpty() || parte1.length() != 5 || parte2.length() != 8 || obraSocial.isEmpty() || importeStr.isEmpty()) {
                Toast.makeText(getContext(), "Completá todos los campos correctamente", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String numero = parte1 + "-" + parte2;
            btnGuardar.setEnabled(false); // Evitar múltiples clics

            // Validar contra Firebase para evitar duplicados históricos
            FirebaseFirestore.getInstance().collection("facturas")
                    .whereEqualTo("uidKinesiologo", FirebaseAuth.getInstance().getUid())
                    .whereEqualTo("tipoComprobante", tipo)
                    .whereEqualTo("numero", numero)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            boolean esMismaFactura = false;
                            if (facturaExistente != null) {
                                for (var doc : query.getDocuments()) {
                                    if (doc.getId().equals(facturaExistente.getId())) {
                                        esMismaFactura = true;
                                        break;
                                    }
                                }
                            }

                            if (!esMismaFactura) {
                                Toast.makeText(getContext(), "ERROR: Ya existe una " + tipo + " con el número " + numero + " registrada.", Toast.LENGTH_LONG).show();
                                btnGuardar.setEnabled(true);
                                return;
                            }
                        }

                        // Si pasa la validación, guardar
                        double importe = Double.parseDouble(importeStr);
                        Timestamp fecha = new Timestamp(new Date(fechaSeleccionada[0].getTimeInMillis()));
                        
                        if (facturaExistente == null) {
                            Factura nueva = new Factura(tipo, numero, fecha, importe, obraSocial, FirebaseAuth.getInstance().getUid());
                            nueva.setDescripcion(descripcion);
                            repository.guardar(nueva).addOnSuccessListener(a -> { 
                                cargarFacturas(); 
                                dialog.dismiss(); 
                            });
                        } else {
                            facturaExistente.setTipoComprobante(tipo);
                            facturaExistente.setNumero(numero);
                            facturaExistente.setFecha(fecha);
                            facturaExistente.setImporte(importe);
                            facturaExistente.setObraSocial(obraSocial);
                            facturaExistente.setDescripcion(descripcion);
                            repository.guardar(facturaExistente).addOnSuccessListener(a -> { 
                                cargarFacturas(); 
                                dialog.dismiss(); 
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        btnGuardar.setEnabled(true);
                        Toast.makeText(getContext(), "Error de validación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
        dialog.show();
    }
}
