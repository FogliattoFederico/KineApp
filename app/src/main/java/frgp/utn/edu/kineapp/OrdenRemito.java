package frgp.utn.edu.kineapp;

public class OrdenRemito {
    private String obraSocialNombre;
    private int cantidadSesiones;
    private String codigoPractica;
    private String fecha;
    private String pacienteNombreCompleto;
    private String numeroAfiliado;

    public OrdenRemito() {}

    public OrdenRemito(String obraSocialNombre, int cantidadSesiones, 
                       String codigoPractica, String fecha, String pacienteNombreCompleto, 
                       String numeroAfiliado) {
        this.obraSocialNombre = obraSocialNombre;
        this.cantidadSesiones = cantidadSesiones;
        this.codigoPractica = codigoPractica;
        this.fecha = fecha;
        this.pacienteNombreCompleto = pacienteNombreCompleto;
        this.numeroAfiliado = numeroAfiliado;
    }

    // Getters y Setters
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
}