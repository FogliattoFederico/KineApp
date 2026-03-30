package frgp.utn.edu.kineapp.model;

import com.google.firebase.Timestamp;

public class Factura {

    private String id;
    private String tipoComprobante; // "Factura A", "Factura B", "Factura C", "Recibo A", etc.
    private String numero;
    private Timestamp fecha;
    private double importe;
    private boolean cobrada;
    private String obraSocial;
    private String uidKinesiologo;
    private String descripcion;

    public Factura() {}

    public Factura(String tipoComprobante, String numero, Timestamp fecha,
                   double importe, String obraSocial, String uidKinesiologo) {
        this.tipoComprobante = tipoComprobante;
        this.numero = numero;
        this.fecha = fecha;
        this.importe = importe;
        this.cobrada = false;
        this.obraSocial = obraSocial;
        this.uidKinesiologo = uidKinesiologo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public double getImporte() { return importe; }
    public void setImporte(double importe) { this.importe = importe; }

    public boolean isCobrada() { return cobrada; }
    public void setCobrada(boolean cobrada) { this.cobrada = cobrada; }

    public String getObraSocial() { return obraSocial; }
    public void setObraSocial(String obraSocial) { this.obraSocial = obraSocial; }

    public String getUidKinesiologo() { return uidKinesiologo; }
    public void setUidKinesiologo(String uidKinesiologo) { this.uidKinesiologo = uidKinesiologo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}