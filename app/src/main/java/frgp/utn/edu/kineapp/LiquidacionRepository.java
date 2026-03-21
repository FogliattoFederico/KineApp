package frgp.utn.edu.kineapp;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class LiquidacionRepository {
    private final CollectionReference coleccion;
    private final String uidKinesiologo;

    public LiquidacionRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        coleccion = db.collection("liquidaciones_colegio");
        uidKinesiologo = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public Task<DocumentReference> guardar(LiquidacionColegio liq) {
        liq.setUidKinesiologo(uidKinesiologo);
        return coleccion.add(liq);
    }

    public Task<QuerySnapshot> obtenerTodas() {
        return coleccion
                .whereEqualTo("uidKinesiologo", uidKinesiologo)
                .orderBy("fechaLiquidacion", Query.Direction.DESCENDING)
                .get();
    }

    public Task<Void> eliminar(String id) {
        return coleccion.document(id).delete();
    }

    public Task<Void> marcarComoFacturada(String id, boolean facturada) {
        return coleccion.document(id).update("facturada", facturada);
    }
}