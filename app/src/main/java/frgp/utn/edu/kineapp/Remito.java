package frgp.utn.edu.kineapp;

import java.util.ArrayList;
import java.util.List;

public class Remito {
    private String id;
    private String uidKinesiologo;
    private List<OrdenRemito> ordenes;
    private String numeroRemito;

    public Remito() {
        this.ordenes = new ArrayList<>();
    }

    public Remito(String uidKinesiologo, List<OrdenRemito> ordenes) {
        this.uidKinesiologo = uidKinesiologo;
        this.ordenes = ordenes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUidKinesiologo() { return uidKinesiologo; }
    public void setUidKinesiologo(String uidKinesiologo) { this.uidKinesiologo = uidKinesiologo; }

    public List<OrdenRemito> getOrdenes() { return ordenes; }
    public void setOrdenes(List<OrdenRemito> ordenes) { this.ordenes = ordenes; }

    public String getNumeroRemito() { return numeroRemito; }
    public void setNumeroRemito(String numeroRemito) { this.numeroRemito = numeroRemito; }
}