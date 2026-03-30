package frgp.utn.edu.kineapp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

import frgp.utn.edu.kineapp.R;
import frgp.utn.edu.kineapp.adapter.PacienteAdapter;
import frgp.utn.edu.kineapp.model.Paciente;
import frgp.utn.edu.kineapp.repository.PacienteRepository;

public class ListaPacientesActivity extends AppCompatActivity {

    private RecyclerView rvPacientes;
    private LinearLayout layoutEmpty;
    private PacienteAdapter adapter;
    private PacienteRepository repository;
    private List<Paciente> listaPacientes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_pacientes);

        repository = new PacienteRepository();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvPacientes = findViewById(R.id.rv_pacientes);
        layoutEmpty = findViewById(R.id.layout_empty);

        adapter = new PacienteAdapter(listaPacientes, paciente -> {
            // Ir al detalle del paciente
            Intent intent = new Intent(this, DetallePacienteActivity.class);
            intent.putExtra("pacienteId", paciente.getId());
            startActivity(intent);
        });

        rvPacientes.setLayoutManager(new LinearLayoutManager(this));
        rvPacientes.setAdapter(adapter);

        ExtendedFloatingActionButton fab = findViewById(R.id.fab_agregar);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, FormularioPacienteActivity.class))
        );

        cargarPacientes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPacientes();
    }

    private void cargarPacientes() {
        repository.obtenerTodos()
                .addOnSuccessListener(querySnapshot -> {
                    listaPacientes.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        Paciente p = doc.toObject(Paciente.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            listaPacientes.add(p);
                        }
                    }
                    adapter.actualizarLista(listaPacientes);
                    layoutEmpty.setVisibility(listaPacientes.isEmpty() ? View.VISIBLE : View.GONE);
                    rvPacientes.setVisibility(listaPacientes.isEmpty() ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar pacientes: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_pacientes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}