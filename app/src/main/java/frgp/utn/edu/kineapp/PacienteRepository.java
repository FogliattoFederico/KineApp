package frgp.utn.edu.kineapp;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class PacienteRepository {

    private final CollectionReference coleccion;
    private final String uidKinesiologo;
    private final FirebaseFirestore db;

    public PacienteRepository() {
        db = FirebaseFirestore.getInstance();
        coleccion = db.collection("pacientes");
        uidKinesiologo = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // Guardar nuevo paciente
    public Task<Void> guardar(Paciente paciente) {
        paciente.setUidKinesiologo(uidKinesiologo);
        if (paciente.getId() == null || paciente.getId().isEmpty()) {
            String id = coleccion.document().getId();
            paciente.setId(id);
        }
        return coleccion.document(paciente.getId()).set(paciente);
    }

    // Obtener todos los pacientes del kinesiólogo logueado
    public Task<com.google.firebase.firestore.QuerySnapshot> obtenerTodos() {
        return coleccion
                .whereEqualTo("uidKinesiologo", uidKinesiologo)
                .get();
    }

    // Actualizar paciente existente
    public Task<Void> actualizar(Paciente paciente) {
        return coleccion.document(paciente.getId()).set(paciente);
    }

    // Eliminar paciente y sus atenciones de forma robusta
    public Task<Void> eliminar(String idPaciente) {
        // 1. Buscamos todas las atenciones que coincidan con el pacienteId
        return db.collection("atenciones")
                .whereEqualTo("pacienteId", idPaciente)
                .get()
                .continueWithTask(task -> {
                    WriteBatch batch = db.batch();

                    // 2. Si la consulta fue exitosa, agregamos cada atención al borrado
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            batch.delete(doc.getReference());
                        }
                    }

                    // 3. Agregamos el documento del paciente al borrado
                    batch.delete(coleccion.document(idPaciente));

                    // 4. Ejecutamos el lote (batch)
                    return batch.commit();
                });
    }

    // Buscar por DNI
    public Task<com.google.firebase.firestore.QuerySnapshot> buscarPorDni(String dni) {
        return coleccion
                .whereEqualTo("uidKinesiologo", uidKinesiologo)
                .whereEqualTo("dni", dni)
                .get();
    }
}
