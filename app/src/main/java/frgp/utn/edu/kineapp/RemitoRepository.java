package frgp.utn.edu.kineapp;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class RemitoRepository {
    private final CollectionReference coleccion;
    private final String uid;

    public RemitoRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        coleccion = db.collection("remitos");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public Task<Void> guardar(Remito remito) {
        remito.setUidKinesiologo(uid);
        if (remito.getId() == null) {
            String id = coleccion.document().getId();
            remito.setId(id);
        }
        return coleccion.document(remito.getId()).set(remito);
    }

    public Task<Void> eliminar(String id) {
        return coleccion.document(id).delete();
    }

    public Task<QuerySnapshot> obtenerTodos() {
        // Quitamos el orderBy para evitar el error de índice
        return coleccion
                .whereEqualTo("uidKinesiologo", uid)
                .get();
    }
}