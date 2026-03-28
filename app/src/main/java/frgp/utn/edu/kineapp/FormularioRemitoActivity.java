package frgp.utn.edu.kineapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FormularioRemitoActivity extends AppCompatActivity {

    private TextInputEditText etNumeroRemito;
    private LinearLayout containerOrdenes;
    private MaterialButton btnAgregarOrden, btnGuardarRemito;
    private RemitoRepository repository;
    private PacienteRepository pacienteRepository;
    private List<Paciente> listaPacientes = new ArrayList<>();
    private String[] nombresPacientes = {};
    private String remitoId = null;
    private Remito remitoExistente = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_remito);

        repository = new RemitoRepository();
        pacienteRepository = new PacienteRepository();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etNumeroRemito = findViewById(R.id.et_numero_remito);
        containerOrdenes = findViewById(R.id.container_ordenes);
        btnAgregarOrden = findViewById(R.id.btn_agregar_orden);
        btnGuardarRemito = findViewById(R.id.btn_guardar_remito);

        btnAgregarOrden.setOnClickListener(v -> agregarNuevaOrden(null));
        btnGuardarRemito.setOnClickListener(v -> guardarRemito());

        remitoId = getIntent().getStringExtra("remitoId");

        cargarPacientesParaAutocomplete();

        if (remitoId != null) {
            toolbar.setTitle("Editar Remito");
            cargarRemito();
        } else {
            // Agregar una orden vacía por defecto solo si es nuevo
            agregarNuevaOrden(null);
        }
    }

    private void cargarPacientesParaAutocomplete() {
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
            for (int i = 0; i < containerOrdenes.getChildCount(); i++) {
                configurarAutocompletePaciente(containerOrdenes.getChildAt(i));
            }
        });
    }

    private void cargarRemito() {
        FirebaseFirestore.getInstance().collection("remitos").document(remitoId)
                .get()
                .addOnSuccessListener(doc -> {
                    remitoExistente = doc.toObject(Remito.class);
                    if (remitoExistente != null) {
                        remitoExistente.setId(doc.getId());
                        etNumeroRemito.setText(remitoExistente.getNumeroRemito());
                        
                        containerOrdenes.removeAllViews();
                        if (remitoExistente.getOrdenes() != null) {
                            for (OrdenRemito orden : remitoExistente.getOrdenes()) {
                                agregarNuevaOrden(orden);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar el remito", Toast.LENGTH_SHORT).show();
                });
    }

    private void agregarNuevaOrden(OrdenRemito datosExistentes) {
        View view = getLayoutInflater().inflate(R.layout.item_orden_remito_formulario, containerOrdenes, false);
        
        int numeroOrden = containerOrdenes.getChildCount() + 1;
        TextView tvNumero = view.findViewById(R.id.tv_orden_numero);
        tvNumero.setText(String.valueOf(numeroOrden));

        AutoCompleteTextView etObraSocial = view.findViewById(R.id.et_obra_social_nombre);
        ArrayAdapter<String> osAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, FormularioPacienteSimpleActivity.OBRAS_SOCIALES);
        etObraSocial.setAdapter(osAdapter);

        TextInputEditText etCant = view.findViewById(R.id.et_cant_sesiones);
        TextInputEditText etCodigo = view.findViewById(R.id.et_codigo_practica);
        TextInputEditText etFecha = view.findViewById(R.id.et_fecha_orden);
        AutoCompleteTextView etPaciente = view.findViewById(R.id.et_paciente_nombre);
        TextInputEditText etAfiliado = view.findViewById(R.id.et_nro_afiliado);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        
        if (datosExistentes != null) {
            etObraSocial.setText(datosExistentes.getObraSocialNombre(), false);
            etCant.setText(String.valueOf(datosExistentes.getCantidadSesiones()));
            etCodigo.setText(datosExistentes.getCodigoPractica());
            etFecha.setText(datosExistentes.getFecha());
            etPaciente.setText(datosExistentes.getPacienteNombreCompleto(), false);
            etAfiliado.setText(datosExistentes.getNumeroAfiliado());
        } else {
            etFecha.setText(sdf.format(Calendar.getInstance().getTime()));
        }
        
        etFecha.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (dp, y, m, d) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(y, m, d);
                etFecha.setText(sdf.format(selected.getTime()));
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        configurarAutocompletePaciente(view);

        view.findViewById(R.id.btn_eliminar_orden).setOnClickListener(v -> {
            containerOrdenes.removeView(view);
            renumerarOrdenes();
        });

        containerOrdenes.addView(view);
    }

    private void configurarAutocompletePaciente(View ordenView) {
        AutoCompleteTextView etPaciente = ordenView.findViewById(R.id.et_paciente_nombre);
        TextInputEditText etNroAfiliado = ordenView.findViewById(R.id.et_nro_afiliado);
        AutoCompleteTextView etOS = ordenView.findViewById(R.id.et_obra_social_nombre);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, nombresPacientes);
        etPaciente.setAdapter(adapter);

        etPaciente.setOnClickListener(v -> etPaciente.showDropDown());

        etPaciente.setOnItemClickListener((parent, view, position, id) -> {
            String seleccionado = (String) parent.getItemAtPosition(position);
            for (Paciente p : listaPacientes) {
                if (p.getNombreCompleto().equals(seleccionado)) {
                    etNroAfiliado.setText(p.getNumeroAfiliado() != null ? p.getNumeroAfiliado() : "");
                    etOS.setText(p.getObraSocial() != null ? p.getObraSocial() : "", false);
                    break;
                }
            }
        });
    }

    private void renumerarOrdenes() {
        for (int i = 0; i < containerOrdenes.getChildCount(); i++) {
            View v = containerOrdenes.getChildAt(i);
            TextView tv = v.findViewById(R.id.tv_orden_numero);
            tv.setText(String.valueOf(i + 1));
        }
    }

    private void guardarRemito() {
        String numRemito = etNumeroRemito.getText().toString().trim();
        if (numRemito.isEmpty()) {
            etNumeroRemito.setError("Requerido");
            etNumeroRemito.requestFocus();
            return;
        }

        List<OrdenRemito> ordenes = new ArrayList<>();
        
        for (int i = 0; i < containerOrdenes.getChildCount(); i++) {
            View v = containerOrdenes.getChildAt(i);
            
            AutoCompleteTextView etPacienteInput = v.findViewById(R.id.et_paciente_nombre);
            TextInputEditText etAfiliadoInput = v.findViewById(R.id.et_nro_afiliado);
            AutoCompleteTextView etOSInput = v.findViewById(R.id.et_obra_social_nombre);
            TextInputEditText etCantInput = v.findViewById(R.id.et_cant_sesiones);
            TextInputEditText etCodigoInput = v.findViewById(R.id.et_codigo_practica);
            TextInputEditText etFechaInput = v.findViewById(R.id.et_fecha_orden);

            String paciente = etPacienteInput.getText().toString().trim();
            String nroAfiliado = etAfiliadoInput.getText().toString().trim();
            String osNombre = etOSInput.getText().toString().trim();
            String cantStr = etCantInput.getText().toString().trim();
            String codigo = etCodigoInput.getText().toString().trim();
            String fecha = etFechaInput.getText().toString().trim();

            if (paciente.isEmpty()) { etPacienteInput.setError("Requerido"); etPacienteInput.requestFocus(); return; }
            if (nroAfiliado.isEmpty()) { etAfiliadoInput.setError("Requerido"); etAfiliadoInput.requestFocus(); return; }
            if (osNombre.isEmpty()) { etOSInput.setError("Requerido"); etOSInput.requestFocus(); return; }
            if (cantStr.isEmpty()) { etCantInput.setError("Requerido"); etCantInput.requestFocus(); return; }
            if (codigo.isEmpty()) { etCodigoInput.setError("Requerido"); etCodigoInput.requestFocus(); return; }
            if (fecha.isEmpty()) { etFechaInput.setError("Requerido"); etFechaInput.requestFocus(); return; }

            int cant = Integer.parseInt(cantStr);
            ordenes.add(new OrdenRemito(osNombre, cant, codigo, fecha, paciente, nroAfiliado));
        }

        if (ordenes.isEmpty()) {
            Toast.makeText(this, "Agregá al menos una orden", Toast.LENGTH_SHORT).show();
            return;
        }

        Remito remito;
        if (remitoExistente != null) {
            remito = remitoExistente;
            remito.setOrdenes(ordenes);
            remito.setNumeroRemito(numRemito);
        } else {
            remito = new Remito(FirebaseAuth.getInstance().getUid(), ordenes);
            remito.setNumeroRemito(numRemito);
        }

        // Deshabilitamos el botón para evitar múltiples guardados
        btnGuardarRemito.setEnabled(false);

        repository.guardar(remito).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Remito guardado correctamente", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            btnGuardarRemito.setEnabled(true);
            Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
