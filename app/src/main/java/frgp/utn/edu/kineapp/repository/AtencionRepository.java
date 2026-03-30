package frgp.utn.edu.kineapp.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;
import java.util.Calendar;
import java.util.Date;

import frgp.utn.edu.kineapp.model.Atencion;

public class AtencionRepository {

    private final CollectionReference coleccion;
    private final String uid;

    public AtencionRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        coleccion = db.collection("atenciones");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public Task<Void> guardar(Atencion atencion) {
        atencion.setUidKinesiologo(uid);
        String id = coleccion.document().getId();
        atencion.setId(id);
        return coleccion.document(id).set(atencion);
    }

    public Task<Void> eliminar(String id) {
        return coleccion.document(id).delete();
    }

    public Task<QuerySnapshot> obtenerPorPaciente(String pacienteId) {
        return coleccion
                .whereEqualTo("uidKinesiologo", uid)
                .whereEqualTo("pacienteId", pacienteId)
                .get();
    }

    public Task<QuerySnapshot> obtenerDelMes() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Timestamp inicio = new Timestamp(new Date(cal.getTimeInMillis()));

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        Timestamp fin = new Timestamp(new Date(cal.getTimeInMillis()));

        return coleccion
                .whereEqualTo("uidKinesiologo", uid)
                .whereGreaterThanOrEqualTo("fecha", inicio)
                .whereLessThanOrEqualTo("fecha", fin)
                .get();
    }

    public Task<QuerySnapshot> buscarAtencionDelDia(String pacienteId, Timestamp desde,
                                                    Timestamp hasta) {
        return coleccion
                .whereEqualTo("uidKinesiologo", uid)
                .whereEqualTo("pacienteId", pacienteId)
                .whereGreaterThanOrEqualTo("fecha", desde)
                .whereLessThanOrEqualTo("fecha", hasta)
                .get();
    }
}