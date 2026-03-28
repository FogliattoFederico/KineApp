package frgp.utn.edu.kineapp;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LiquidacionRepository {
    private final CollectionReference coleccion;
    private final String uidKinesiologo;

    public LiquidacionRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        coleccion = db.collection("liquidaciones_colegio");
        uidKinesiologo = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public Task<Void> guardar(LiquidacionColegio liq) {
        liq.setUidKinesiologo(uidKinesiologo);
        if (liq.getId() == null || liq.getId().isEmpty()) {
            String id = coleccion.document().getId();
            liq.setId(id);
        }
        return coleccion.document(liq.getId()).set(liq);
    }

    public Task<QuerySnapshot> obtenerTodas() {
        // Quitamos el orderBy para evitar error de índice FAILED_PRECONDITION
        return coleccion
                .whereEqualTo("uidKinesiologo", uidKinesiologo)
                .get();
    }

    public Task<QuerySnapshot> obtenerPorMes(int mes, int anio) {
        // Quitamos el filtrado de fecha por el mismo motivo
        return coleccion
                .whereEqualTo("uidKinesiologo", uidKinesiologo)
                .get();
    }

    public Task<Void> eliminar(String id) {
        return coleccion.document(id).delete();
    }

    public Task<Void> marcarComoFacturada(String id, boolean facturada) {
        return coleccion.document(id).update("facturada", facturada);
    }
}