package frgp.utn.edu.kineapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import java.util.List;

public class RemitosFragment extends Fragment {

    private RecyclerView rvRemitos;
    private LinearLayout layoutEmpty;
    private List<Remito> listaRemitos = new ArrayList<>();
    private RemitoRepository repository;
    private RemitoAdapter adapter;
    private TextInputEditText etBuscar;

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
        FloatingActionButton fab = view.findViewById(R.id.fab_agregar_remito);

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

    @Override
    public void onResume() {
        super.onResume();
        cargarRemitos();
    }

    private void cargarRemitos() {
        repository.obtenerTodos().addOnSuccessListener(query -> {
            listaRemitos.clear();
            for (var doc : query.getDocuments()) {
                Remito r = doc.toObject(Remito.class);
                if (r != null) {
                    r.setId(doc.getId());
                    listaRemitos.add(r);
                }
            }
            
            // Ordenamos por número de remito (Descendente) si es numérico, o alfabéticamente
            listaRemitos.sort((a, b) -> {
                String numA = a.getNumeroRemito() != null ? a.getNumeroRemito() : "";
                String numB = b.getNumeroRemito() != null ? b.getNumeroRemito() : "";
                return numB.compareTo(numA);
            });

            actualizarVista();
            
            if (etBuscar.getText() != null && !etBuscar.getText().toString().isEmpty()) {
                adapter.filtrar(etBuscar.getText().toString());
            }
            
        }).addOnFailureListener(e -> {
            if (isAdded()) {
                Toast.makeText(getContext(), "Error al cargar remitos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
