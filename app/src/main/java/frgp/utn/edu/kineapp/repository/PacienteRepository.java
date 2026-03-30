package frgp.utn.edu.kineapp.repository;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import frgp.utn.edu.kineapp.model.Paciente;

public class PacienteRepository {

    private final CollectionReference coleccion;
    private final String uidKinesiologo;
    private final FirebaseFirestore db;

    public PacienteRepository() {
        db = FirebaseFirestore.getInstance();
        coleccion = db.collection("pacientes");
        uidKinesiologo = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public Task<Void> guardar(Paciente paciente) {
        paciente.setUidKinesiologo(uidKinesiologo);
        if (paciente.getId() == null || paciente.getId().isEmpty()) {
            String id = coleccion.document().getId();
            paciente.setId(id);
        }
        return coleccion.document(paciente.getId()).set(paciente);
    }

    public Task<QuerySnapshot> obtenerTodos() {
        return coleccion
                .whereEqualTo("uidKinesiologo", uidKinesiologo)
                .get();
    }

    public Task<Void> actualizar(Paciente paciente) {
        return coleccion.document(paciente.getId()).set(paciente);
    }

    // ELIMINAR MEJORADO: Incluye filtro de seguridad y logs
    public Task<Void> eliminar(String idPaciente) {
        Log.d("KineApp", "Iniciando eliminación de paciente: " + idPaciente);

        // Importante: Incluimos uidKinesiologo en la consulta para cumplir con las reglas de seguridad
        return db.collection("atenciones")
                .whereEqualTo("uidKinesiologo", uidKinesiologo)
                .whereEqualTo("pacienteId", idPaciente)
                .get()
                .continueWithTask(task -> {
                    WriteBatch batch = db.batch();
                    
                    if (task.isSuccessful() && task.getResult() != null) {
                        int cantidadAtenciones = task.getResult().size();
                        Log.d("KineApp", "Atenciones encontradas para borrar: " + cantidadAtenciones);
                        
                        for (DocumentSnapshot doc : task.getResult()) {
                            batch.delete(doc.getReference());
                        }
                    } else {
                        Log.e("KineApp", "Error al buscar atenciones", task.getException());
                    }

                    // Borrar el paciente
                    batch.delete(coleccion.document(idPaciente));
                    Log.d("KineApp", "Paciente añadido al batch de borrado");

                    return batch.commit();
                }).addOnFailureListener(e -> {
                    Log.e("KineApp", "EL BATCH FALLÓ: " + e.getMessage());
                });
    }

    public Task<QuerySnapshot> buscarPorDni(String dni) {
        return coleccion
                .whereEqualTo("uidKinesiologo", uidKinesiologo)
                .whereEqualTo("dni", dni)
                .get();
    }
}
