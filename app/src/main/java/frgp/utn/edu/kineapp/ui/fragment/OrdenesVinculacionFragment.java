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
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.adapter.OrdenVinculacionAdapter;
import frgp.utn.edu.kineapp.model.Factura;
import frgp.utn.edu.kineapp.model.LiquidacionColegio;
import frgp.utn.edu.kineapp.model.OrdenRemito;
import frgp.utn.edu.kineapp.model.Paciente;
import frgp.utn.edu.kineapp.model.Remito;
import frgp.utn.edu.kineapp.repository.FacturaRepository;
import frgp.utn.edu.kineapp.repository.LiquidacionRepository;
import frgp.utn.edu.kineapp.repository.PacienteRepository;
import frgp.utn.edu.kineapp.repository.RemitoRepository;
import frgp.utn.edu.kineapp.ui.activity.FormularioPacienteSimpleActivity;

public class OrdenesVinculacionFragment extends Fragment {

    private RecyclerView rvOrdenes;
    private LinearLayout layoutEmpty;
    private TextView tvEmpty;
    private ChipGroup chipGroupFiltro;
    private RemitoRepository remitoRepository;
    private FacturaRepository facturaRepository;
    private PacienteRepository pacienteRepository;
    private LiquidacionRepository liquidacionRepository;
    private List<Remito> listaRemitos = new ArrayList<>();
    private List<OrdenRemito> listaMostrar = new ArrayList<>();
    private List<Paciente> listaPacientes = new ArrayList<>();
    private List<Factura> todasLasFacturas = new ArrayList<>();
    private List<LiquidacionColegio> todasLasLiquidaciones = new ArrayList<>();
    private String[] nombresPacientes = {};
    private OrdenVinculacionAdapter adapter;
    private boolean filtrandoAsociadas = false;
    
    private TextView tvPeriodo;
    private int mesSeleccionado, anioSeleccionado;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ordenes_vinculacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        remitoRepository = new RemitoRepository();
        facturaRepository = new FacturaRepository();
        pacienteRepository = new PacienteRepository();
        liquidacionRepository = new LiquidacionRepository();
        
        rvOrdenes = view.findViewById(R.id.rv_ordenes_vinculacion);
        layoutEmpty = view.findViewById(R.id.layout_empty_ordenes);
        tvEmpty = view.findViewById(R.id.tv_empty_ordenes);
        chipGroupFiltro = view.findViewById(R.id.chip_group_filtro_ordenes);
        tvPeriodo = view.findViewById(R.id.tv_periodo_ordenes);
        View layoutPeriodo = view.findViewById(R.id.layout_seleccionar_periodo_ordenes);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_agregar_orden_directa);

        Calendar cal = Calendar.getInstance();
        mesSeleccionado = cal.get(Calendar.MONTH);
        anioSeleccionado = cal.get(Calendar.YEAR);
        actualizarTextoPeriodo();

        adapter = new OrdenVinculacionAdapter(listaMostrar, new OrdenVinculacionAdapter.OnOrdenClickListener() {
            @Override
            public void onToggleAsociada(OrdenRemito orden) {
                if (!orden.isAsociadaAPago()) {
                    if (orden.isEsDeRemitoDirecto()) {
                        seleccionarFacturaParaVinculo(orden);
                    } else {
                        Toast.makeText(getContext(), "Las órdenes de remitos oficiales se asocian desde la pestaña de Pagos", Toast.LENGTH_LONG).show();
                    }
                } else {
                    desvincularOrden(orden);
                }
            }
            @Override
            public void onEdit(OrdenRemito orden) { mostrarDialogoEditar(orden); }
            @Override
            public void onDelete(OrdenRemito orden) { confirmarEliminacion(orden); }
        });

        rvOrdenes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrdenes.setAdapter(adapter);

        layoutPeriodo.setOnClickListener(v -> mostrarDialogoMesAnio());

        chipGroupFiltro.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filtrandoAsociadas = checkedIds.contains(R.id.chip_ordenes_asociadas);
            filtrarLista();
        });

        fabAdd.setOnClickListener(v -> mostrarDialogoNuevaOrdenDirecta());

        cargarPacientes();
        cargarDatosDeVinculos();
    }

    private void cargarPacientes() {
        pacienteRepository.obtenerTodos().addOnSuccessListener(query -> {
            listaPacientes.clear();
            List<String> nombres = new ArrayList<>();
            for (var doc : query.getDocuments()) {
                Paciente p = doc.toObject(Paciente.class);
                if (p != null) {
                    p.setId(doc.getId());
                    listaPacientes.add(p);
                    nombres.add(p.getNombreCompleto());
                }
            }
            nombresPacientes = nombres.toArray(new String[0]);
        });
    }

    private void cargarDatosDeVinculos() {
        facturaRepository.obtenerTodas().addOnSuccessListener(query -> {
            todasLasFacturas.clear();
            for (var doc : query.getDocuments()) {
                Factura f = doc.toObject(Factura.class);
                if (f != null) {
                    f.setId(doc.getId());
                    todasLasFacturas.add(f);
                }
            }
            liquidacionRepository.obtenerTodas().addOnSuccessListener(queryLiq -> {
                todasLasLiquidaciones.clear();
                for (var doc : queryLiq.getDocuments()) {
                    LiquidacionColegio l = doc.toObject(LiquidacionColegio.class);
                    if (l != null) {
                        l.setId(doc.getId());
                        todasLasLiquidaciones.add(l);
                    }
                }
                cargarDatos();
            });
        });
    }

    private void seleccionarFacturaParaVinculo(OrdenRemito orden) {
        // Filtrar facturas que:
        // 1. No estén cobradas (pendientes)
        // 2. No sean del Colegio de Kinesiologos
        List<Factura> facturasFiltradas = todasLasFacturas.stream()
                .filter(f -> !f.isCobrada())
                .filter(f -> {
                    if (f.getObraSocial() == null) return true;
                    String os = f.getObraSocial().toLowerCase().trim();
                    return !os.contains("colegio") && !os.contains("kinesiologo");
                })
                .collect(Collectors.toList());

        if (facturasFiltradas.isEmpty()) {
            Toast.makeText(getContext(), "No hay facturas pendientes (fuera del Colegio) para vincular", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> items = new ArrayList<>();
        for (Factura f : facturasFiltradas) {
            items.add(f.getTipoComprobante() + " " + f.getNumero() + " (" + f.getObraSocial() + ")");
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Seleccionar Factura de Respaldo")
                .setItems(items.toArray(new String[0]), (dialog, which) -> {
                    Factura f = facturasFiltradas.get(which);
                    orden.setAsociadaAPago(true);
                    orden.setTipoVinculo("DIRECTO");
                    orden.setIdVinculoAsociado(f.getId());
                    
                    if (f.getFecha() != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(f.getFecha().toDate());
                        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
                        orden.setMesVinculo(meses[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR));
                    }
                    
                    actualizarOrdenEnRemito(orden);
                    Toast.makeText(getContext(), "Orden vinculada a factura", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void desvincularOrden(OrdenRemito orden) {
        new AlertDialog.Builder(getContext())
                .setTitle("Desvincular Orden")
                .setMessage("¿Deseas marcar esta orden como pendiente nuevamente?")
                .setPositiveButton("Sí, desvincular", (d, w) -> {
                    String tipoAnterior = orden.getTipoVinculo();
                    String idAnterior = orden.getId();
                    
                    orden.setAsociadaAPago(false);
                    orden.setTipoVinculo(null);
                    orden.setIdVinculoAsociado(null);
                    orden.setMesVinculo(null);
                    
                    if ("COLEGIO".equals(tipoAnterior)) {
                        removerOrdenDeLiquidaciones(idAnterior);
                    }
                    
                    actualizarOrdenEnRemito(orden);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void removerOrdenDeLiquidaciones(String ordenId) {
        liquidacionRepository.obtenerTodas().addOnSuccessListener(query -> {
            for (var doc : query.getDocuments()) {
                LiquidacionColegio liq = doc.toObject(LiquidacionColegio.class);
                if (liq != null) {
                    liq.setId(doc.getId());
                    if (liq.getOrdenesVinculadas() != null) {
                        boolean encontrada = false;
                        List<OrdenRemito> ordenesLiq = new ArrayList<>(liq.getOrdenesVinculadas());
                        for (int i = 0; i < ordenesLiq.size(); i++) {
                            if (ordenesLiq.get(i).getId().equals(ordenId)) {
                                ordenesLiq.remove(i);
                                encontrada = true;
                                break;
                            }
                        }
                        if (encontrada) {
                            liq.setOrdenesVinculadas(ordenesLiq);
                            liquidacionRepository.guardar(liq);
                        }
                    }
                }
            }
        });
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
                    cargarDatosDeVinculos();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarTextoPeriodo() {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                          "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        tvPeriodo.setText(meses[mesSeleccionado] + " " + anioSeleccionado);
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarPacientes();
        cargarDatosDeVinculos();
    }

    private void cargarDatos() {
        remitoRepository.obtenerPorMes(mesSeleccionado, anioSeleccionado).addOnSuccessListener(query -> {
            if (!isAdded()) return;
            listaRemitos.clear();
            Calendar calRemito = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (var doc : query.getDocuments()) {
                Remito r = doc.toObject(Remito.class);
                if (r != null) {
                    r.setId(doc.getId());
                    if (r.getFechaCreacion() != null) {
                        calRemito.setTime(r.getFechaCreacion().toDate());
                        if (calRemito.get(Calendar.MONTH) == mesSeleccionado && 
                            calRemito.get(Calendar.YEAR) == anioSeleccionado) {
                            if (r.getOrdenes() != null) {
                                for (OrdenRemito o : r.getOrdenes()) {
                                    o.setParentRemitoId(r.getId());
                                    o.setEsDeRemitoDirecto(r.isEsDirecto());
                                    o.setNombreRemito(r.getNumeroRemito());
                                    
                                    // Buscar detalle y fecha del vínculo
                                    if (o.isAsociadaAPago()) {
                                        if ("DIRECTO".equals(o.getTipoVinculo())) {
                                            for (Factura f : todasLasFacturas) {
                                                if (f.getId().equals(o.getIdVinculoAsociado())) {
                                                    o.setDetalleVinculo(f.getTipoComprobante() + " " + f.getNumero());
                                                    if (f.getFecha() != null) {
                                                        o.setFechaVinculo(sdf.format(f.getFecha().toDate()));
                                                    }
                                                    break;
                                                }
                                            }
                                        } else if ("COLEGIO".equals(o.getTipoVinculo())) {
                                            // Fallback: Si no tiene idVinculoAsociado, buscamos en las listas de liquidaciones
                                            boolean fechaEncontrada = false;
                                            
                                            // Primero intentamos por ID directo si lo tiene
                                            if (o.getIdVinculoAsociado() != null) {
                                                for (LiquidacionColegio l : todasLasLiquidaciones) {
                                                    if (l.getId().equals(o.getIdVinculoAsociado())) {
                                                        if (l.getFechaLiquidacion() != null) {
                                                            o.setFechaVinculo(sdf.format(l.getFechaLiquidacion().toDate()));
                                                            fechaEncontrada = true;
                                                        }
                                                        break;
                                                    }
                                                }
                                            }
                                            
                                            // Si no lo encontramos por ID, buscamos en la lista ordenesVinculadas de cada pago
                                            if (!fechaEncontrada) {
                                                for (LiquidacionColegio l : todasLasLiquidaciones) {
                                                    if (l.getOrdenesVinculadas() != null) {
                                                        for (OrdenRemito ov : l.getOrdenesVinculadas()) {
                                                            if (ov.getId().equals(o.getId())) {
                                                                if (l.getFechaLiquidacion() != null) {
                                                                    o.setFechaVinculo(sdf.format(l.getFechaLiquidacion().toDate()));
                                                                    // Aprovechamos para reparar el ID de vínculo en la orden para futuras cargas
                                                                    o.setIdVinculoAsociado(l.getId());
                                                                    fechaEncontrada = true;
                                                                }
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if (fechaEncontrada) break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            listaRemitos.add(r);
                        }
                    }
                }
            }
            filtrarLista();
        });
    }

    private void filtrarLista() {
        listaMostrar.clear();
        for (Remito r : listaRemitos) {
            if (r.getOrdenes() != null) {
                for (OrdenRemito o : r.getOrdenes()) {
                    if (o.isAsociadaAPago() == filtrandoAsociadas) listaMostrar.add(o);
                }
            }
        }
        adapter.notifyDataSetChanged();
        layoutEmpty.setVisibility(listaMostrar.isEmpty() ? View.VISIBLE : View.GONE);
        rvOrdenes.setVisibility(listaMostrar.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty.setText(filtrandoAsociadas ? "No hay órdenes asociadas a pagos en este período" : "No hay órdenes pendientes en este período");
    }

    private void actualizarOrdenEnRemito(OrdenRemito ordenModificada) {
        for (Remito r : listaRemitos) {
            if (r.getId().equals(ordenModificada.getParentRemitoId())) {
                List<OrdenRemito> ordenes = r.getOrdenes();
                for (int i = 0; i < ordenes.size(); i++) {
                    if (ordenes.get(i).getId().equals(ordenModificada.getId())) {
                        ordenes.set(i, ordenModificada);
                        remitoRepository.guardar(r).addOnSuccessListener(a -> filtrarLista());
                        return;
                    }
                }
            }
        }
    }

    private void mostrarDialogoEditar(OrdenRemito orden) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_editar_orden, null);
        AutoCompleteTextView etPaciente = v.findViewById(R.id.et_edit_paciente);
        TextInputEditText etAfiliado = v.findViewById(R.id.et_edit_afiliado);
        AutoCompleteTextView etOS = v.findViewById(R.id.et_edit_os);
        TextInputEditText etCodigo = v.findViewById(R.id.et_edit_codigo);
        TextInputEditText etCant = v.findViewById(R.id.et_edit_cant);
        TextInputEditText etFecha = v.findViewById(R.id.et_edit_fecha);
        TextInputLayout tilImporte = v.findViewById(R.id.til_edit_importe);
        TextInputEditText etImporte = v.findViewById(R.id.et_edit_importe);
        
        configurarAutocompleteEnDialogo(etPaciente, etAfiliado, etOS);

        etPaciente.setText(orden.getPacienteNombreCompleto(), false);
        etAfiliado.setText(orden.getNumeroAfiliado());
        etOS.setText(orden.getObraSocialNombre(), false);
        etCodigo.setText(orden.getCodigoPractica());
        etCant.setText(String.valueOf(orden.getCantidadSesiones()));
        etFecha.setText(orden.getFecha());

        if (orden.isEsDeRemitoDirecto()) {
            tilImporte.setVisibility(View.VISIBLE);
            etImporte.setText(String.valueOf(orden.getImporte()));
        }

        etFecha.setOnClickListener(view -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(getContext(), (dp, y, m, d) -> {
                String fechaStr = String.format(Locale.getDefault(), "%02d/%02d", d, m + 1);
                etFecha.setText(fechaStr);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(getContext()).setTitle("Editar Orden").setView(v)
                .setPositiveButton("Guardar", (d, w) -> {
                    orden.setPacienteNombreCompleto(etPaciente.getText().toString());
                    orden.setNumeroAfiliado(etAfiliado.getText().toString());
                    orden.setObraSocialNombre(etOS.getText().toString());
                    orden.setCodigoPractica(etCodigo.getText().toString());
                    orden.setFecha(etFecha.getText().toString());
                    try { orden.setCantidadSesiones(Integer.parseInt(etCant.getText().toString())); } catch (Exception e) {}
                    if (orden.isEsDeRemitoDirecto()) {
                        try { orden.setImporte(Double.parseDouble(etImporte.getText().toString())); } catch (Exception e) {}
                    }
                    actualizarOrdenEnRemito(orden);
                }).setNegativeButton("Cancelar", null).show();
    }

    private void mostrarDialogoNuevaOrdenDirecta() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_editar_orden, null);
        AutoCompleteTextView etPaciente = v.findViewById(R.id.et_edit_paciente);
        TextInputEditText etAfiliado = v.findViewById(R.id.et_edit_afiliado);
        AutoCompleteTextView etOS = v.findViewById(R.id.et_edit_os);
        TextInputEditText etCodigo = v.findViewById(R.id.et_edit_codigo);
        TextInputEditText etCant = v.findViewById(R.id.et_edit_cant);
        TextInputEditText etFecha = v.findViewById(R.id.et_edit_fecha);
        TextInputLayout tilImporte = v.findViewById(R.id.til_edit_importe);
        TextInputEditText etImporte = v.findViewById(R.id.et_edit_importe);

        tilImporte.setVisibility(View.VISIBLE);
        configurarAutocompleteEnDialogo(etPaciente, etAfiliado, etOS);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        etFecha.setText(sdf.format(new Date()));

        etFecha.setOnClickListener(view -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(getContext(), (dp, y, m, d) -> {
                String fechaStr = String.format(Locale.getDefault(), "%02d/%02d", d, m + 1);
                etFecha.setText(fechaStr);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(getContext()).setTitle("Nueva Orden Directa").setView(v)
                .setPositiveButton("Crear", (d, w) -> {
                    String paciente = etPaciente.getText().toString().trim();
                    String os = etOS.getText().toString().trim();
                    String cantStr = etCant.getText().toString().trim();
                    String impStr = etImporte.getText().toString().trim();
                    
                    if (paciente.isEmpty() || os.isEmpty() || cantStr.isEmpty()) {
                        Toast.makeText(getContext(), "Completá los campos obligatorios", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    OrdenRemito nueva = new OrdenRemito(os, Integer.parseInt(cantStr), 
                            etCodigo.getText().toString(), etFecha.getText().toString(), 
                            paciente, etAfiliado.getText().toString());
                    
                    if (!impStr.isEmpty()) {
                        try { nueva.setImporte(Double.parseDouble(impStr)); } catch (Exception e) {}
                    }
                    
                    guardarNuevaOrdenDirecta(nueva);
                }).setNegativeButton("Cancelar", null).show();
    }

    private void configurarAutocompleteEnDialogo(AutoCompleteTextView etPaciente, TextInputEditText etAfiliado, AutoCompleteTextView etOS) {
        ArrayAdapter<String> adapterPacientes = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, nombresPacientes);
        etPaciente.setAdapter(adapterPacientes);
        etPaciente.setOnClickListener(v -> etPaciente.showDropDown());

        etPaciente.setOnItemClickListener((parent, view, position, id) -> {
            String seleccionado = (String) parent.getItemAtPosition(position);
            for (Paciente p : listaPacientes) {
                if (p.getNombreCompleto().equals(seleccionado)) {
                    etAfiliado.setText(p.getNumeroAfiliado() != null ? p.getNumeroAfiliado() : "");
                    etOS.setText(p.getObraSocial() != null ? p.getObraSocial() : "", false);
                    break;
                }
            }
        });

        ArrayAdapter<String> adapterOS = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, FormularioPacienteSimpleActivity.OBRAS_SOCIALES);
        etOS.setAdapter(adapterOS);
        etOS.setOnClickListener(v -> etOS.showDropDown());
    }

    private void guardarNuevaOrdenDirecta(OrdenRemito orden) {
        Remito remitoDirecto = null;
        for (Remito r : listaRemitos) {
            if (r.isEsDirecto() && "Órdenes Directas".equals(r.getNumeroRemito())) {
                remitoDirecto = r;
                break;
            }
        }

        if (remitoDirecto == null) {
            remitoDirecto = new Remito(FirebaseAuth.getInstance().getUid(), new ArrayList<>());
            remitoDirecto.setNumeroRemito("Órdenes Directas");
            remitoDirecto.setEsDirecto(true);
            Calendar cal = Calendar.getInstance();
            cal.set(anioSeleccionado, mesSeleccionado, 1);
            remitoDirecto.setFechaCreacion(new com.google.firebase.Timestamp(cal.getTime()));
        }

        orden.setParentRemitoId(remitoDirecto.getId());
        remitoDirecto.getOrdenes().add(orden);

        remitoRepository.guardar(remitoDirecto).addOnSuccessListener(a -> {
            Toast.makeText(getContext(), "Orden guardada", Toast.LENGTH_SHORT).show();
            cargarDatos();
        });
    }

    private void confirmarEliminacion(OrdenRemito orden) {
        if (orden.isAsociadaAPago()) { Toast.makeText(getContext(), "No se puede eliminar una orden asociada a un pago", Toast.LENGTH_SHORT).show(); return; }
        new AlertDialog.Builder(getContext()).setTitle("Eliminar Orden").setMessage("¿Estás seguro que deseas eliminar esta orden?")
                .setPositiveButton("Eliminar", (d, w) -> eliminarOrdenDeRemito(orden)).setNegativeButton("Cancelar", null).show();
    }

    private void eliminarOrdenDeRemito(OrdenRemito ordenAEliminar) {
        for (Remito r : listaRemitos) {
            if (r.getId().equals(ordenAEliminar.getParentRemitoId())) {
                List<OrdenRemito> ordenes = r.getOrdenes();
                ordenes.remove(ordenAEliminar);
                remitoRepository.guardar(r).addOnSuccessListener(a -> {
                    Toast.makeText(getContext(), "Orden eliminada", Toast.LENGTH_SHORT).show();
                    cargarDatos();
                });
                return;
            }
        }
    }
}