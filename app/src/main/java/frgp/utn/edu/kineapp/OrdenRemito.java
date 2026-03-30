package frgp.utn.edu.kineapp;

import com.google.firebase.firestore.Exclude;
import java.util.Objects;
import java.util.UUID;

public class OrdenRemito {
    private String id;
    private String parentRemitoId;
    private String idVinculoAsociado; // ID de LiquidacionColegio o Factura
    private String tipoVinculo;       // "COLEGIO" o "DIRECTO"
    private String obraSocialNombre;
    private int cantidadSesiones;
    private String codigoPractica;
    private String fecha;
    private String pacienteNombreCompleto;
    private String numeroAfiliado;
    private boolean asociadaAPago;
    private String mesVinculo; // Mes de emisión de la factura/liquidación vinculada
    private double importe;    // Nuevo campo para órdenes directas

    // Campos auxiliares (no se guardan en Firestore directamente bajo el objeto, 
    // se usan para la interfaz)
    private boolean esDeRemitoDirecto;
    private String detalleVinculo;
    private String nombreRemito;
    private String fechaVinculo;

    public OrdenRemito() {
        this.id = UUID.randomUUID().toString();
        this.asociadaAPago = false;
    }

    public OrdenRemito(String obraSocialNombre, int cantidadSesiones, 
                       String codigoPractica, String fecha, String pacienteNombreCompleto, 
                       String numeroAfiliado) {
        this();
        this.obraSocialNombre = obraSocialNombre;
        this.cantidadSesiones = cantidadSesiones;
        this.codigoPractica = codigoPractica;
        this.fecha = fecha;
        this.pacienteNombreCompleto = pacienteNombreCompleto;
        this.numeroAfiliado = numeroAfiliado;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getParentRemitoId() { return parentRemitoId; }
    public void setParentRemitoId(String parentRemitoId) { this.parentRemitoId = parentRemitoId; }

    public String getIdVinculoAsociado() { return idVinculoAsociado; }
    public void setIdVinculoAsociado(String idVinculoAsociado) { this.idVinculoAsociado = idVinculoAsociado; }

    public String getTipoVinculo() { return tipoVinculo; }
    public void setTipoVinculo(String tipoVinculo) { this.tipoVinculo = tipoVinculo; }

    public String getObraSocialNombre() { return obraSocialNombre; }
    public void setObraSocialNombre(String obraSocialNombre) { this.obraSocialNombre = obraSocialNombre; }

    public int getCantidadSesiones() { return cantidadSesiones; }
    public void setCantidadSesiones(int cantidadSesiones) { this.cantidadSesiones = cantidadSesiones; }

    public String getCodigoPractica() { return codigoPractica; }
    public void setCodigoPractica(String codigoPractica) { this.codigoPractica = codigoPractica; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getPacienteNombreCompleto() { return pacienteNombreCompleto; }
    public void setPacienteNombreCompleto(String pacienteNombreCompleto) { this.pacienteNombreCompleto = pacienteNombreCompleto; }

    public String getNumeroAfiliado() { return numeroAfiliado; }
    public void setNumeroAfiliado(String numeroAfiliado) { this.numeroAfiliado = numeroAfiliado; }

    public boolean isAsociadaAPago() { return asociadaAPago; }
    public void setAsociadaAPago(boolean asociadaAPago) { this.asociadaAPago = asociadaAPago; }

    public String getMesVinculo() { return mesVinculo; }
    public void setMesVinculo(String mesVinculo) { this.mesVinculo = mesVinculo; }

    public double getImporte() { return importe; }
    public void setImporte(double importe) { this.importe = importe; }

    @Exclude
    public boolean isEsDeRemitoDirecto() { return esDeRemitoDirecto; }
    public void setEsDeRemitoDirecto(boolean esDeRemitoDirecto) { this.esDeRemitoDirecto = esDeRemitoDirecto; }

    @Exclude
    public String getDetalleVinculo() { return detalleVinculo; }
    public void setDetalleVinculo(String detalleVinculo) { this.detalleVinculo = detalleVinculo; }

    @Exclude
    public String getNombreRemito() { return nombreRemito; }
    public void setNombreRemito(String nombreRemito) { this.nombreRemito = nombreRemito; }

    @Exclude
    public String getFechaVinculo() { return fechaVinculo; }
    public void setFechaVinculo(String fechaVinculo) { this.fechaVinculo = fechaVinculo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdenRemito that = (OrdenRemito) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}