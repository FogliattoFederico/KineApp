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
        // VITAL: Asignar siempre el dueño del dato
        factura.setUidKinesiologo(uid);
        
        if (factura.getId() == null || factura.getId().isEmpty()) {
            // Si es nueva, generamos ID
            String id = coleccion.document().getId();
            factura.setId(id);
        }
        
        // Usamos el ID de la factura (sea nuevo o existente) para no duplicar
        return coleccion.document(factura.getId()).set(factura);
    }

    public Task<Void> actualizarCobrada(String id, boolean cobrada) {
        // Al usar update, Firebase mantiene el uidKinesiologo existente, por lo que las reglas permiten el cambio
        return coleccion.document(id).update("cobrada", cobrada);
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