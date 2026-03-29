package frgp.utn.edu.kineapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class PacientesFragment extends Fragment {

    private RecyclerView rvPacientes;
    private LinearLayout layoutEmpty;
    private TextView tvContador;
    private ChipGroup chipGroupFiltros;
    private TextInputEditText etBuscar;
    private FloatingActionButton fabAgregar;
    private PacienteAdapter adapter;
    private PacienteRepository repository;
    private List<Paciente> listaTodos = new ArrayList<>();
    private List<Paciente> listaFiltrada = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pacientes, container, false);
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

        repository = new PacienteRepository();
        rvPacientes = view.findViewById(R.id.rv_pacientes);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        tvContador = view.findViewById(R.id.tv_contador);
        chipGroupFiltros = view.findViewById(R.id.chip_group_filtros);
        etBuscar = view.findViewById(R.id.et_buscar);
        fabAgregar = view.findViewById(R.id.fab_agregar);

        adapter = new PacienteAdapter(listaFiltrada, paciente -> {
            Intent intent = new Intent(getContext(), DetallePacienteActivity.class);
            intent.putExtra("pacienteId", paciente.getId());
            startActivity(intent);
        });

        rvPacientes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPacientes.setAdapter(adapter);

        fabAgregar.setOnClickListener(v -> 
            startActivity(new Intent(getContext(), FormularioPacienteSimpleActivity.class))
        );

        configurarFiltrosYExclusividad();

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltros();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        cargarPacientes();
    }

    private void configurarFiltrosYExclusividad() {
        Chip chipTurnos = chipGroupFiltros.findViewById(R.id.chip_filtro_tiene_turnos);
        Chip chipOS = chipGroupFiltros.findViewById(R.id.chip_filtro_tiene_os);
        Chip chipPart = chipGroupFiltros.findViewById(R.id.chip_filtro_particular);
        Chip chipCUD = chipGroupFiltros.findViewById(R.id.chip_filtro_tiene_cud);
        Chip chipDom = chipGroupFiltros.findViewById(R.id.chip_filtro_domicilio);
        Chip chipCons = chipGroupFiltros.findViewById(R.id.chip_filtro_consultorio);

        // Grupo Cobertura: Solo uno entre OS, Particular y CUD
        View.OnClickListener listenerCobertura = v -> {
            Chip clicked = (Chip) v;
            if (clicked.isChecked()) {
                if (clicked != chipOS) chipOS.setChecked(false);
                if (clicked != chipPart) chipPart.setChecked(false);
                if (clicked != chipCUD) chipCUD.setChecked(false);
            }
            aplicarFiltros();
        };

        // Grupo Modalidad: Solo uno entre Domicilio y Consultorio
        View.OnClickListener listenerModalidad = v -> {
            Chip clicked = (Chip) v;
            if (clicked.isChecked()) {
                if (clicked != chipDom) chipDom.setChecked(false);
                if (clicked != chipCons) chipCons.setChecked(false);
            }
            aplicarFiltros();
        };

        chipOS.setOnClickListener(listenerCobertura);
        chipPart.setOnClickListener(listenerCobertura);
        chipCUD.setOnClickListener(listenerCobertura);

        chipDom.setOnClickListener(listenerModalidad);
        chipCons.setOnClickListener(listenerModalidad);

        chipTurnos.setOnClickListener(v -> aplicarFiltros());
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarPacientes();
    }

    private void cargarPacientes() {
        repository.obtenerTodos()
                .addOnSuccessListener(querySnapshot -> {
                    listaTodos.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        Paciente p = doc.toObject(Paciente.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            listaTodos.add(p);
                        }
                    }
                    listaTodos.sort((a, b) -> {
                        String apA = a.getApellido() != null ? a.getApellido() : "";
                        String apB = b.getApellido() != null ? b.getApellido() : "";
                        return apA.compareToIgnoreCase(apB);
                    });
                    aplicarFiltros();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar pacientes",
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void aplicarFiltros() {
        String queryBusqueda = etBuscar.getText().toString().toLowerCase().trim();
        
        listaFiltrada.clear();
        for (Paciente p : listaTodos) {
            // 1. Filtro por Búsqueda
            if (!queryBusqueda.isEmpty()) {
                String nombreCompleto = p.getNombreCompleto().toLowerCase();
                String dni = p.getDni() != null ? p.getDni() : "";
                if (!nombreCompleto.contains(queryBusqueda) && !dni.contains(queryBusqueda)) {
                    continue;
                }
            }

            // 2. Filtros por Chips
            boolean fTurnos = isChipChecked(R.id.chip_filtro_tiene_turnos);
            boolean fOS = isChipChecked(R.id.chip_filtro_tiene_os);
            boolean fPart = isChipChecked(R.id.chip_filtro_particular);
            boolean fCUD = isChipChecked(R.id.chip_filtro_tiene_cud);
            boolean fDom = isChipChecked(R.id.chip_filtro_domicilio);
            boolean fCons = isChipChecked(R.id.chip_filtro_consultorio);

            if (fTurnos && (p.getHorarios() == null || p.getHorarios().isEmpty())) continue;
            
            // Filtro de cobertura (excluyentes)
            if (fOS && (p.getObraSocial() == null || p.getObraSocial().isEmpty())) continue;
            if (fPart && !p.isParticular()) continue;
            if (fCUD && !p.isCertificadoDiscapacidad()) continue;
            
            // Filtro de modalidad (excluyentes)
            if (fDom && !"domicilio".equals(p.getModalidad())) continue;
            if (fCons && !"consultorio".equals(p.getModalidad())) continue;

            listaFiltrada.add(p);
        }

        adapter.actualizarLista(listaFiltrada);
        actualizarUIResultados(!queryBusqueda.isEmpty());
    }

    private boolean isChipChecked(int chipId) {
        View chip = chipGroupFiltros.findViewById(chipId);
        return chip instanceof com.google.android.material.chip.Chip && 
               ((com.google.android.material.chip.Chip) chip).isChecked();
    }

    private void actualizarUIResultados(boolean hayBusqueda) {
        String textoContador = listaFiltrada.size() + " paciente" + (listaFiltrada.size() != 1 ? "s" : "");
        tvContador.setText(textoContador);

        if (listaFiltrada.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvPacientes.setVisibility(View.GONE);
            TextView tvEmpty = layoutEmpty.findViewById(R.id.tv_empty_text);
            
            if (hayBusqueda) {
                tvEmpty.setText("No se encontraron resultados");
            } else {
                tvEmpty.setText("No hay pacientes registrados");
            }
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvPacientes.setVisibility(View.VISIBLE);
        }
    }
}
