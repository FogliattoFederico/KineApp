package frgp.utn.edu.kineapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
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

public class FacturasEmitidasFragment extends Fragment {

    private RecyclerView rvFacturas;
    private LinearLayout layoutEmpty;
    private FacturaRepository repository;
    private List<Factura> listaFacturas = new ArrayList<>();
    private List<Factura> listaFiltrada = new ArrayList<>();
    private FacturaAdapter adapter;
    private ChipGroup chipGroupEstado, chipGroupObrasSociales;
    private String obraSocialSeleccionada = null;

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
        rvFacturas = view.findViewById(R.id.rv_facturas);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        chipGroupEstado = view.findViewById(R.id.chip_group_estado);
        chipGroupObrasSociales = view.findViewById(R.id.chip_group_obras_sociales);

        adapter = new FacturaAdapter(listaFiltrada, new FacturaAdapter.OnFacturaClickListener() {
            @Override
            public void onClick(Factura factura) {
                mostrarDialogoFactura(factura);
            }

            @Override
            public void onLongClick(Factura factura) {
                confirmarEliminacion(factura);
            }
        }, (factura, cobrada) -> {
            repository.actualizarCobrada(factura.getId(), cobrada)
                    .addOnSuccessListener(unused -> cargarFacturas());
        });

        rvFacturas.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFacturas.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_agregar_factura);
        fab.setOnClickListener(v -> mostrarDialogoFactura(null));

        chipGroupEstado.setOnCheckedStateChangeListener((group, checkedIds) -> aplicarFiltros());

        cargarFacturas();
    }

    private void confirmarEliminacion(Factura factura) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar factura")
                .setMessage("¿Estás seguro que deseás eliminar esta factura definitivamente?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    repository.eliminar(factura.getId())
                            .addOnSuccessListener(a -> {
                                Toast.makeText(getContext(), "Factura eliminada", Toast.LENGTH_SHORT).show();
                                cargarFacturas();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
                    for (var doc : query.getDocuments()) {
                        Factura f = doc.toObject(Factura.class);
                        if (f != null) {
                            f.setId(doc.getId());
                            listaFacturas.add(f);
                        }
                    }
                    listaFacturas.sort((a, b) -> {
                        if (a.getFecha() == null) return 1;
                        if (b.getFecha() == null) return -1;
                        return b.getFecha().compareTo(a.getFecha());
                    });
                    
                    cargarObrasSocialesChips();
                    aplicarFiltros();
                });
    }

    private void cargarObrasSocialesChips() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("pacientes")
                .whereEqualTo("uidKinesiologo", uid)
                .get()
                .addOnSuccessListener(query -> {
                    if (!isAdded()) return;
                    List<String> obrasSociales = new ArrayList<>();
                    for (var doc : query.getDocuments()) {
                        Paciente p = doc.toObject(Paciente.class);
                        if (p != null && p.getObraSocial() != null && !p.getObraSocial().isEmpty()) {
                            String os = p.getObraSocial();
                            if (!os.equalsIgnoreCase("IAPOS") && 
                                !os.equalsIgnoreCase("Colegio de Kinesiologos") &&
                                !obrasSociales.contains(os)) {
                                obrasSociales.add(os);
                            }
                        }
                    }
                    
                    chipGroupObrasSociales.removeAllViews();
                    for (String os : obrasSociales) {
                        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_filter, chipGroupObrasSociales, false);
                        chip.setText(os);
                        
                        if (os.equals(obraSocialSeleccionada)) chip.setChecked(true);
                        
                        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                obraSocialSeleccionada = os;
                            } else if (obraSocialSeleccionada != null && obraSocialSeleccionada.equals(os)) {
                                obraSocialSeleccionada = null;
                            }
                            aplicarFiltros();
                        });
                        chipGroupObrasSociales.addView(chip);
                    }
                });
    }

    private void aplicarFiltros() {
        int checkedEstadoId = chipGroupEstado.getCheckedChipId();
        listaFiltrada.clear();

        for (Factura f : listaFacturas) {
            boolean pasaEstado = true;
            if (checkedEstadoId == R.id.chip_cobradas) pasaEstado = f.isCobrada();
            else if (checkedEstadoId == R.id.chip_no_cobradas) pasaEstado = !f.isCobrada();

            boolean pasaOS = true;
            if (obraSocialSeleccionada != null) {
                pasaOS = obraSocialSeleccionada.equals(f.getObraSocial());
            }

            if (pasaEstado && pasaOS) {
                listaFiltrada.add(f);
            }
        }

        adapter.actualizar(listaFiltrada);
        layoutEmpty.setVisibility(listaFiltrada.isEmpty() ? View.VISIBLE : View.GONE);
        rvFacturas.setVisibility(listaFiltrada.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void mostrarDialogoFactura(Factura facturaExistente) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_factura, null);

        AutoCompleteTextView etTipo = dialogView.findViewById(R.id.et_tipo);
        TextInputEditText etNumeroParte1 = dialogView.findViewById(R.id.et_numero_parte1);
        TextInputEditText etNumeroParte2 = dialogView.findViewById(R.id.et_numero_parte2);
        TextInputEditText etFecha = dialogView.findViewById(R.id.et_fecha);
        AutoCompleteTextView etObraSocial = dialogView.findViewById(R.id.et_obra_social);
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
            new DatePickerDialog(getContext(), (dp, y, m, d) -> {
                cal.set(y, m, d);
                etFecha.setText(sdf.format(cal.getTime()));
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        List<String> todasObrasSociales = new ArrayList<>(Arrays.asList(FormularioPacienteSimpleActivity.OBRAS_SOCIALES));
        Collections.sort(todasObrasSociales);
        
        ArrayAdapter<String> osAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, todasObrasSociales);
        etObraSocial.setAdapter(osAdapter);

        if (facturaExistente != null) {
            tvTitulo.setText("Editar factura");
            etTipo.setText(facturaExistente.getTipoComprobante(), false);
            etImporte.setText(String.valueOf(facturaExistente.getImporte()));
            etObraSocial.setText(facturaExistente.getObraSocial(), false);
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
            String importeStr = etImporte.getText().toString().trim();

            if (tipo.isEmpty() || parte1.length() != 5 || parte2.length() != 8 || obraSocial.isEmpty() || importeStr.isEmpty()) {
                Toast.makeText(getContext(), "Completá todos los campos correctamente", Toast.LENGTH_SHORT).show();
                return;
            }

            String numero = parte1 + "-" + parte2;
            double importe = Double.parseDouble(importeStr);
            Timestamp fecha = new Timestamp(new Date(fechaSeleccionada[0].getTimeInMillis()));

            if (facturaExistente == null) {
                Factura nueva = new Factura(tipo, numero, fecha, importe, obraSocial, "");
                repository.guardar(nueva).addOnSuccessListener(a -> { cargarFacturas(); dialog.dismiss(); });
            } else {
                facturaExistente.setTipoComprobante(tipo);
                facturaExistente.setNumero(numero);
                facturaExistente.setFecha(fecha);
                facturaExistente.setImporte(importe);
                facturaExistente.setObraSocial(obraSocial);
                repository.actualizar(facturaExistente).addOnSuccessListener(a -> { cargarFacturas(); dialog.dismiss(); });
            }
        });
        dialog.show();
    }
}