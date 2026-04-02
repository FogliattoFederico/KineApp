package frgp.utn.edu.kineapp.model;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Remito {
    private String id;
    private String uidKinesiologo;
    private List<OrdenRemito> ordenes;
    private String periodoRemito;
    private Timestamp fechaCreacion;
    private boolean esDirecto; // Nuevo: Para diferenciar remitos de colegio vs facturación directa

    public Remito() {
        this.ordenes = new ArrayList<>();
        this.fechaCreacion = Timestamp.now();
        this.esDirecto = false;
    }

    public Remito(String uidKinesiologo, List<OrdenRemito> ordenes) {
        this();
        this.uidKinesiologo = uidKinesiologo;
        this.ordenes = ordenes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUidKinesiologo() { return uidKinesiologo; }
    public void setUidKinesiologo(String uidKinesiologo) { this.uidKinesiologo = uidKinesiologo; }

    public List<OrdenRemito> getOrdenes() { return ordenes; }
    public void setOrdenes(List<OrdenRemito> ordenes) { this.ordenes = ordenes; }

    public String getPeriodoRemito() { return periodoRemito; }
    public void setPeriodoRemito(String periodoRemito) { this.periodoRemito = periodoRemito; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public boolean isEsDirecto() { return esDirecto; }
    public void setEsDirecto(boolean esDirecto) { this.esDirecto = esDirecto; }
}