package frgp.utn.edu.kineapp;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class FacturaRepository {

    private final CollectionReference coleccion;
    private final String uid;

    public FacturaRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        coleccion = db.collection("facturas");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public Task<Void> guardar(Factura factura) {
        factura.setUidKinesiologo(uid);
        String id = coleccion.document().getId();
        factura.setId(id);
        return coleccion.document(id).set(factura);
    }

    public Task<Void> actualizar(Factura factura) {
        return coleccion.document(factura.getId()).set(factura);
    }

    public Task<Void> eliminar(String id) {
        return coleccion.document(id).delete();
    }

    public Task<Void> actualizarCobrada(String id, boolean cobrada) {
        return coleccion.document(id).update("cobrada", cobrada);
    }

    public Task<QuerySnapshot> obtenerTodas() {
        return coleccion
                .whereEqualTo("uidKinesiologo", uid)
                .get();
    }
}