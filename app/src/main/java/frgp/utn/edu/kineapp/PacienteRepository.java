package frgp.utn.edu.kineapp;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;

public class PacienteRepository {

    private final CollectionReference coleccion;
    private final String uidKinesiologo;

    public PacienteRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
    public Task<QuerySnapshot> obtenerTodos() {
        return coleccion
                .whereEqualTo("uidKinesiologo", uidKinesiologo)
                .get();
    }

    // Actualizar paciente existente
    public Task<Void> actualizar(Paciente paciente) {
        return coleccion.document(paciente.getId()).set(paciente);
    }

    // Eliminar paciente
    public Task<Void> eliminar(String idPaciente) {
        return coleccion.document(idPaciente).delete();
    }

    // Buscar por DNI
    public Task<QuerySnapshot> buscarPorDni(String dni) {
        return coleccion
                .whereEqualTo("uidKinesiologo", uidKinesiologo)
                .whereEqualTo("dni", dni)
                .get();
    }
}
