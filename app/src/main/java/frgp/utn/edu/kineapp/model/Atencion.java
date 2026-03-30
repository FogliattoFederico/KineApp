package frgp.utn.edu.kineapp.model;

import com.google.firebase.Timestamp;

public class Atencion {

    private String id;
    private String pacienteId;
    private String nombrePaciente;
    private Timestamp fecha;
    private String modalidad;
    private String tipoCobertura;
    private double monto;
    private int sesionNumero;
    private int sesionesTotal;
    private String uidKinesiologo;
    private String observaciones;
    private String objetivos;

    public Atencion() {}

    public Atencion(String pacienteId, String nombrePaciente, String modalidad,
                    String tipoCobertura, double monto, int sesionNumero,
                    int sesionesTotal, String uidKinesiologo,
                    com.google.firebase.Timestamp fechaTurno) {
        this.pacienteId = pacienteId;
        this.nombrePaciente = nombrePaciente;
        this.fecha = fechaTurno;
        this.modalidad = modalidad;
        this.tipoCobertura = tipoCobertura;
        this.monto = monto;
        this.sesionNumero = sesionNumero;
        this.sesionesTotal = sesionesTotal;
        this.uidKinesiologo = uidKinesiologo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }

    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public String getTipoCobertura() { return tipoCobertura; }
    public void setTipoCobertura(String tipoCobertura) { this.tipoCobertura = tipoCobertura; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public int getSesionNumero() { return sesionNumero; }
    public void setSesionNumero(int sesionNumero) { this.sesionNumero = sesionNumero; }

    public int getSesionesTotal() { return sesionesTotal; }
    public void setSesionesTotal(int sesionesTotal) { this.sesionesTotal = sesionesTotal; }

    public String getUidKinesiologo() { return uidKinesiologo; }
    public void setUidKinesiologo(String uidKinesiologo) { this.uidKinesiologo = uidKinesiologo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getObjetivos() { return objetivos; }
    public void setObjetivos(String objetivos) { this.objetivos = objetivos; }
}