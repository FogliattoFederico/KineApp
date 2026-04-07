package frgp.utn.edu.kineapp.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import frgp.utn.edu.kineapp.model.Factura;

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
        if (factura.getId() == null || factura.getId().isEmpty()) {
            String id = coleccion.document().getId();
            factura.setId(id);
        }
        return coleccion.document(factura.getId()).set(factura);
    }

    public Task<Void> actualizarCobrada(String id, boolean cobrada, Timestamp fechaPago) {
        if (cobrada) {
            return coleccion.document(id).update("cobrada", true, "fechaPago", fechaPago);
        } else {
            return coleccion.document(id).update("cobrada", false, "fechaPago", null);
        }
    }

    public Task<Void> eliminar(String id) {
        return coleccion.document(id).delete();
    }

    public Task<QuerySnapshot> obtenerTodas() {
        return coleccion
                .whereEqualTo("uidKinesiologo", uid)
                .get();
    }

    public Task<QuerySnapshot> obtenerCobradasPorObraSocial(String obraSocial) {
        return coleccion
                .whereEqualTo("uidKinesiologo", uid)
                .whereEqualTo("obraSocial", obraSocial)
                .whereEqualTo("cobrada", true)
                .get();
    }
}