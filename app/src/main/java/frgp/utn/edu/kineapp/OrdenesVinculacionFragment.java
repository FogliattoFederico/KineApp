package frgp.utn.edu.kineapp;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OrdenesVinculacionFragment extends Fragment {

    private RecyclerView rvOrdenes;
    private LinearLayout layoutEmpty;
    private TextView tvEmpty;
    private ChipGroup chipGroupFiltro;
    private RemitoRepository remitoRepository;
    private List<Remito> listaRemitos = new ArrayList<>();
    private List<OrdenRemito> listaMostrar = new ArrayList<>();
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
        rvOrdenes = view.findViewById(R.id.rv_ordenes_vinculacion);
        layoutEmpty = view.findViewById(R.id.layout_empty_ordenes);
        tvEmpty = view.findViewById(R.id.tv_empty_ordenes);
        chipGroupFiltro = view.findViewById(R.id.chip_group_filtro_ordenes);
        tvPeriodo = view.findViewById(R.id.tv_periodo_ordenes);
        View layoutPeriodo = view.findViewById(R.id.layout_seleccionar_periodo_ordenes);

        Calendar cal = Calendar.getInstance();
        mesSeleccionado = cal.get(Calendar.MONTH);
        anioSeleccionado = cal.get(Calendar.YEAR);
        actualizarTextoPeriodo();

        adapter = new OrdenVinculacionAdapter(listaMostrar, new OrdenVinculacionAdapter.OnOrdenClickListener() {
            @Override
            public void onToggleAsociada(OrdenRemito orden) {
                orden.setAsociadaAPago(!orden.isAsociadaAPago());
                actualizarOrdenEnRemito(orden);
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

        cargarDatos();
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
                    cargarDatos();
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
        cargarDatos();
    }

    private void cargarDatos() {
        remitoRepository.obtenerPorMes(mesSeleccionado, anioSeleccionado).addOnSuccessListener(query -> {
            if (!isAdded()) return;
            listaRemitos.clear();
            Calendar calRemito = Calendar.getInstance();
            for (var doc : query.getDocuments()) {
                Remito r = doc.toObject(Remito.class);
                if (r != null) {
                    r.setId(doc.getId());
                    // Filtrado manual por fecha de creación del remito
                    if (r.getFechaCreacion() != null) {
                        calRemito.setTime(r.getFechaCreacion().toDate());
                        if (calRemito.get(Calendar.MONTH) == mesSeleccionado && 
                            calRemito.get(Calendar.YEAR) == anioSeleccionado) {
                            if (r.getOrdenes() != null) {
                                for (OrdenRemito o : r.getOrdenes()) {
                                    o.setParentRemitoId(r.getId());
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
        TextInputEditText etPaciente = v.findViewById(R.id.et_edit_paciente);
        TextInputEditText etAfiliado = v.findViewById(R.id.et_edit_afiliado);
        TextInputEditText etOS = v.findViewById(R.id.et_edit_os);
        TextInputEditText etCodigo = v.findViewById(R.id.et_edit_codigo);
        TextInputEditText etCant = v.findViewById(R.id.et_edit_cant);
        etPaciente.setText(orden.getPacienteNombreCompleto());
        etAfiliado.setText(orden.getNumeroAfiliado());
        etOS.setText(orden.getObraSocialNombre());
        etCodigo.setText(orden.getCodigoPractica());
        etCant.setText(String.valueOf(orden.getCantidadSesiones()));
        new AlertDialog.Builder(getContext()).setTitle("Editar Orden").setView(v)
                .setPositiveButton("Guardar", (d, w) -> {
                    orden.setPacienteNombreCompleto(etPaciente.getText().toString());
                    orden.setNumeroAfiliado(etAfiliado.getText().toString());
                    orden.setObraSocialNombre(etOS.getText().toString());
                    orden.setCodigoPractica(etCodigo.getText().toString());
                    try { orden.setCantidadSesiones(Integer.parseInt(etCant.getText().toString())); } catch (Exception e) {}
                    actualizarOrdenEnRemito(orden);
                }).setNegativeButton("Cancelar", null).show();
    }

    private void confirmarEliminacion(OrdenRemito orden) {
        if (orden.isAsociadaAPago()) { Toast.makeText(getContext(), "No se puede eliminar una orden asociada a un pago", Toast.LENGTH_SHORT).show(); return; }
        new AlertDialog.Builder(getContext()).setTitle("Eliminar Orden").setMessage("¿Estás seguro que deseas eliminar esta orden del remito?")
                .setPositiveButton("Eliminar", (d, w) -> eliminarOrdenDeRemito(orden)).setNegativeButton("Cancelar", null).show();
    }

    private void eliminarOrdenDeRemito(OrdenRemito ordenAEliminar) {
        for (Remito r : listaRemitos) {
            if (r.getId().equals(ordenAEliminar.getParentRemitoId())) {
                List<OrdenRemito> ordenes = r.getOrdenes();
                ordenes.remove(ordenAEliminar);
                remitoRepository.guardar(r).addOnSuccessListener(a -> { Toast.makeText(getContext(), "Orden eliminada", Toast.LENGTH_SHORT).show(); cargarDatos(); });
                return;
            }
        }
    }
}