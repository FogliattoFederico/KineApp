package frgp.utn.edu.kineapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RemitosFragment extends Fragment {

    private RecyclerView rvRemitos;
    private LinearLayout layoutEmpty;
    private List<Remito> listaRemitos = new ArrayList<>();
    private RemitoRepository repository;
    private RemitoAdapter adapter;
    private TextInputEditText etBuscar;
    private TextView tvPeriodo;
    private int mesSeleccionado, anioSeleccionado;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_remitos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new RemitoRepository();
        rvRemitos = view.findViewById(R.id.rv_remitos);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        etBuscar = view.findViewById(R.id.et_buscar_remito);
        tvPeriodo = view.findViewById(R.id.tv_periodo_remitos);
        View layoutPeriodo = view.findViewById(R.id.layout_seleccionar_periodo);
        FloatingActionButton fab = view.findViewById(R.id.fab_agregar_remito);

        Calendar cal = Calendar.getInstance();
        mesSeleccionado = cal.get(Calendar.MONTH);
        anioSeleccionado = cal.get(Calendar.YEAR);
        actualizarTextoPeriodo();

        rvRemitos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RemitoAdapter(listaRemitos, new RemitoAdapter.OnRemitoClickListener() {
            @Override
            public void onClick(Remito remito) {
                Intent intent = new Intent(getContext(), FormularioRemitoActivity.class);
                intent.putExtra("remitoId", remito.getId());
                startActivity(intent);
            }
            @Override
            public void onLongClick(Remito remito) {
                confirmarEliminacion(remito);
            }
        });
        rvRemitos.setAdapter(adapter);

        layoutPeriodo.setOnClickListener(v -> mostrarDialogoMesAnio());

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FormularioRemitoActivity.class);
            startActivity(intent);
        });

        cargarRemitos();
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
                    cargarRemitos();
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
        cargarRemitos();
    }

    private void cargarRemitos() {
        repository.obtenerPorMes(mesSeleccionado, anioSeleccionado).addOnSuccessListener(query -> {
            if (!isAdded()) return;
            listaRemitos.clear();
            Calendar calRemito = Calendar.getInstance();
            for (var doc : query.getDocuments()) {
                Remito r = doc.toObject(Remito.class);
                if (r != null) {
                    r.setId(doc.getId());
                    // FILTRO: No mostrar remitos de tipo "Directo" en esta pestaña
                    if (!r.isEsDirecto() && r.getFechaCreacion() != null) {
                        calRemito.setTime(r.getFechaCreacion().toDate());
                        if (calRemito.get(Calendar.MONTH) == mesSeleccionado && 
                            calRemito.get(Calendar.YEAR) == anioSeleccionado) {
                            listaRemitos.add(r);
                        }
                    }
                }
            }
            listaRemitos.sort((a, b) -> {
                String numA = a.getNumeroRemito() != null ? a.getNumeroRemito() : "";
                String numB = b.getNumeroRemito() != null ? b.getNumeroRemito() : "";
                return numB.compareTo(numA);
            });
            actualizarVista();
            if (etBuscar.getText() != null && !etBuscar.getText().toString().isEmpty()) {
                adapter.filtrar(etBuscar.getText().toString());
            }
        });
    }

    private void actualizarVista() {
        if (listaRemitos.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvRemitos.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvRemitos.setVisibility(View.VISIBLE);
            adapter.actualizar(listaRemitos);
        }
    }

    private void confirmarEliminacion(Remito remito) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar remito")
                .setMessage("¿Estás seguro que deseás eliminar este remito?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    repository.eliminar(remito.getId()).addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Remito eliminado", Toast.LENGTH_SHORT).show();
                        cargarRemitos();
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}