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

    // Tabs
    private TextView tvTabTodos, tvTabConTurnos;
    private View indicadorTodos, indicadorConTurnos;
    private boolean verSoloConTurnos = false;

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

        // Inicializar Tabs
        tvTabTodos = view.findViewById(R.id.tv_tab_todos);
        tvTabConTurnos = view.findViewById(R.id.tv_tab_con_turnos);
        indicadorTodos = view.findViewById(R.id.indicador_todos);
        indicadorConTurnos = view.findViewById(R.id.indicador_con_turnos);

        tvTabTodos.setOnClickListener(v -> seleccionarTab(false));
        tvTabConTurnos.setOnClickListener(v -> seleccionarTab(true));

        adapter = new PacienteAdapter(listaFiltrada, paciente -> {
            Intent intent = new Intent(getContext(), DetallePacienteActivity.class);
            intent.putExtra("pacienteId", paciente.getId());
            intent.putExtra("modoTurno", verSoloConTurnos);
            startActivity(intent);
        });

        rvPacientes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPacientes.setAdapter(adapter);

        // Configuración inicial del FAB
        actualizarFab();

        chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) ->
                aplicarFiltros()
        );

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

    private void seleccionarTab(boolean conTurnos) {
        verSoloConTurnos = conTurnos;
        
        // Actualizar UI de Tabs
        tvTabTodos.setAlpha(conTurnos ? 0.6f : 1.0f);
        tvTabConTurnos.setAlpha(conTurnos ? 1.0f : 0.6f);
        indicadorTodos.setVisibility(conTurnos ? View.INVISIBLE : View.VISIBLE);
        indicadorConTurnos.setVisibility(conTurnos ? View.VISIBLE : View.INVISIBLE);
        
        // Avisar al adaptador el cambio de diseño
        adapter.setModoTurno(conTurnos);
        
        // Actualizar acción del FAB
        actualizarFab();
        
        actualizarVisibilidadChips();
        aplicarFiltros();
    }

    private void actualizarFab() {
        if (verSoloConTurnos) {
            fabAgregar.setOnClickListener(v -> 
                startActivity(new Intent(getContext(), FormularioPacienteActivity.class))
            );
        } else {
            fabAgregar.setOnClickListener(v -> 
                startActivity(new Intent(getContext(), FormularioPacienteSimpleActivity.class))
            );
        }
    }

    private void actualizarVisibilidadChips() {
        if (verSoloConTurnos) {
            chipGroupFiltros.findViewById(R.id.chip_filtro_tiene_os).setVisibility(View.GONE);
            chipGroupFiltros.findViewById(R.id.chip_filtro_sin_cobertura).setVisibility(View.GONE);
            chipGroupFiltros.findViewById(R.id.chip_filtro_tiene_cud).setVisibility(View.GONE);
            
            chipGroupFiltros.findViewById(R.id.chip_filtro_particular).setVisibility(View.VISIBLE);
            chipGroupFiltros.findViewById(R.id.chip_filtro_orden).setVisibility(View.VISIBLE);
            chipGroupFiltros.findViewById(R.id.chip_filtro_cud_turnos).setVisibility(View.VISIBLE);
            chipGroupFiltros.findViewById(R.id.chip_filtro_domicilio).setVisibility(View.VISIBLE);
            chipGroupFiltros.findViewById(R.id.chip_filtro_consultorio).setVisibility(View.VISIBLE);
        } else {
            chipGroupFiltros.findViewById(R.id.chip_filtro_tiene_os).setVisibility(View.VISIBLE);
            chipGroupFiltros.findViewById(R.id.chip_filtro_sin_cobertura).setVisibility(View.VISIBLE);
            chipGroupFiltros.findViewById(R.id.chip_filtro_tiene_cud).setVisibility(View.VISIBLE);
            
            chipGroupFiltros.findViewById(R.id.chip_filtro_particular).setVisibility(View.GONE);
            chipGroupFiltros.findViewById(R.id.chip_filtro_orden).setVisibility(View.GONE);
            chipGroupFiltros.findViewById(R.id.chip_filtro_cud_turnos).setVisibility(View.GONE);
            chipGroupFiltros.findViewById(R.id.chip_filtro_domicilio).setVisibility(View.GONE);
            chipGroupFiltros.findViewById(R.id.chip_filtro_consultorio).setVisibility(View.GONE);
        }
        chipGroupFiltros.clearCheck();
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

            // 2. Filtro por Tab y sus Chips específicos
            if (verSoloConTurnos) {
                if (p.getHorarios() == null || p.getHorarios().isEmpty()) continue;

                boolean fPart = isChipChecked(R.id.chip_filtro_particular);
                boolean fOrden = isChipChecked(R.id.chip_filtro_orden);
                boolean fCudT = isChipChecked(R.id.chip_filtro_cud_turnos);
                boolean fDom = isChipChecked(R.id.chip_filtro_domicilio);
                boolean fCons = isChipChecked(R.id.chip_filtro_consultorio);

                if (fPart || fOrden || fCudT || fDom || fCons) {
                    boolean pasaCobertura = true;
                    if (fPart || fOrden || fCudT) {
                        pasaCobertura = (fPart && p.isParticular()) ||
                                        (fCudT && p.isCertificadoDiscapacidad()) ||
                                        (fOrden && !p.isParticular() && !p.isCertificadoDiscapacidad());
                    }

                    boolean pasaModalidad = true;
                    if (fDom || fCons) {
                        pasaModalidad = (fDom && "domicilio".equals(p.getModalidad())) ||
                                        (fCons && "consultorio".equals(p.getModalidad()));
                    }

                    if (!pasaCobertura || !pasaModalidad) continue;
                }
            } else {
                boolean fOS = isChipChecked(R.id.chip_filtro_tiene_os);
                boolean fSinCob = isChipChecked(R.id.chip_filtro_sin_cobertura);
                boolean fCUD = isChipChecked(R.id.chip_filtro_tiene_cud);

                if (fOS && (p.getObraSocial() == null || p.getObraSocial().isEmpty())) continue;
                if (fSinCob && p.getObraSocial() != null && !p.getObraSocial().isEmpty()) continue;
                if (fCUD && !p.isCertificadoDiscapacidad()) continue;
            }

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
            } else if (verSoloConTurnos) {
                tvEmpty.setText("No hay pacientes con turnos asignados");
            } else {
                tvEmpty.setText("No hay pacientes registrados");
            }
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvPacientes.setVisibility(View.VISIBLE);
        }
    }
}