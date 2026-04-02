package frgp.utn.edu.kineapp.model;

public class HorarioAtencion {

    private String dia;        // "Lunes", "Martes" — se calcula desde fecha
    private String fecha;      // "15/04/2026" — fecha específica
    private String horaInicio;
    private String horaFin;
    //private String observacion;

    public HorarioAtencion() {}

    public HorarioAtencion(String dia, String horaInicio, String horaFin,
                           String observacion) {
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        //this.observacion = observacion;
    }

    public HorarioAtencion(String dia, String fecha, String horaInicio,
                           String horaFin, String observacion) {
        this.dia = dia;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        //this.observacion = observacion;
    }

    public String getDia() { return dia; }
    public void setDia(String dia) { this.dia = dia; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    /*public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }*/
}