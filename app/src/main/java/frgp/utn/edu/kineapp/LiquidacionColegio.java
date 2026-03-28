package frgp.utn.edu.kineapp;

import com.google.firebase.Timestamp;
import java.util.Date;
import java.util.List;

public class LiquidacionColegio {
    private String id;
    private Timestamp fechaLiquidacion;
    private double importe;
    private boolean facturada;
    private String uidKinesiologo;
    private List<OrdenRemito> ordenesVinculadas; // Lista de órdenes vinculadas a este pago

    public LiquidacionColegio() {}

    public LiquidacionColegio(Timestamp fechaLiquidacion, double importe, String uidKinesiologo) {
        this.fechaLiquidacion = fechaLiquidacion;
        this.importe = importe;
        this.facturada = false;
        this.uidKinesiologo = uidKinesiologo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Timestamp getFechaLiquidacion() { return fechaLiquidacion; }
    public void setFechaLiquidacion(Timestamp fechaLiquidacion) { this.fechaLiquidacion = fechaLiquidacion; }
    public double getImporte() { return importe; }
    public void setImporte(double importe) { this.importe = importe; }
    public boolean isFacturada() { return facturada; }
    public void setFacturada(boolean facturada) { this.facturada = facturada; }
    public String getUidKinesiologo() { return uidKinesiologo; }
    public void setUidKinesiologo(String uidKinesiologo) { this.uidKinesiologo = uidKinesiologo; }
    public List<OrdenRemito> getOrdenesVinculadas() { return ordenesVinculadas; }
    public void setOrdenesVinculadas(List<OrdenRemito> ordenesVinculadas) { this.ordenesVinculadas = ordenesVinculadas; }

    public long getDiasPendientes() {
        if (fechaLiquidacion == null) return 0;
        long diff = new Date().getTime() - fechaLiquidacion.toDate().getTime();
        return diff / (1000 * 60 * 60 * 24);
    }
}