package frgp.utn.edu.kineapp.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import java.util.List;

public class Paciente {

    private String id;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String direccion;
    private String diagnostico;
    private String obraSocial;
    private String numeroAfiliado;
    private boolean certificadoDiscapacidad;
    private List<HorarioAtencion> horarios;
    private Timestamp fechaAlta;
    private Timestamp ultimaActualizacion;
    private String uidKinesiologo;
    private int sesionesSemanales;   // solo si tiene CUD
    private int sesionesOrden;       // solo si no tiene CUD
    private boolean particular;
    private String observaciones;
    public boolean isParticular() { return particular; }
    public void setParticular(boolean particular) { this.particular = particular; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    private String modalidad;        // "domicilio", "consultorio", "ambos"
    private double valorSesion;      // solo para particulares
    private int sesionesAtendidas;   // contador de sesiones atendidas
    private int sesionesRestantes;   // calculado desde sesionesOrden
    private String fechaNacimiento; // "dd/MM/yyyy"
    private int edad;
    private String email;

    // Constructor vacío requerido por Firestore
    public Paciente() {}

    // Constructor completo
    public Paciente(String nombre, String apellido, String dni, String telefono,
                    String direccion, String diagnostico, String obraSocial,
                    String numeroAfiliado, boolean certificadoDiscapacidad,
                    String uidKinesiologo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
        this.direccion = direccion;
        this.diagnostico = diagnostico;
        this.obraSocial = obraSocial;
        this.numeroAfiliado = numeroAfiliado;
        this.certificadoDiscapacidad = certificadoDiscapacidad;
        this.fechaAlta = Timestamp.now();
        this.ultimaActualizacion = Timestamp.now();
        this.uidKinesiologo = uidKinesiologo;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getObraSocial() { return obraSocial; }
    public void setObraSocial(String obraSocial) { this.obraSocial = obraSocial; }

    public String getNumeroAfiliado() { return numeroAfiliado; }
    public void setNumeroAfiliado(String numeroAfiliado) { this.numeroAfiliado = numeroAfiliado; }

    public boolean isCertificadoDiscapacidad() { return certificadoDiscapacidad; }
    public void setCertificadoDiscapacidad(boolean certificadoDiscapacidad) {
        this.certificadoDiscapacidad = certificadoDiscapacidad;
    }

    public List<HorarioAtencion> getHorarios() { return horarios; }
    public void setHorarios(List<HorarioAtencion> horarios) { this.horarios = horarios; }

    public Timestamp getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(Timestamp fechaAlta) { this.fechaAlta = fechaAlta; }

    public Timestamp getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(Timestamp ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }

    public String getUidKinesiologo() { return uidKinesiologo; }
    public void setUidKinesiologo(String uidKinesiologo) { this.uidKinesiologo = uidKinesiologo; }

    @Exclude
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public int getSesionesSemanales() { return sesionesSemanales; }
    public void setSesionesSemanales(int sesionesSemanales) { this.sesionesSemanales = sesionesSemanales; }

    public int getSesionesOrden() { return sesionesOrden; }
    public void setSesionesOrden(int sesionesOrden) { this.sesionesOrden = sesionesOrden; }

    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }

    public double getValorSesion() { return valorSesion; }
    public void setValorSesion(double valorSesion) { this.valorSesion = valorSesion; }

    public int getSesionesAtendidas() { return sesionesAtendidas; }
    public void setSesionesAtendidas(int sesionesAtendidas) { this.sesionesAtendidas = sesionesAtendidas; }

    public int getSesionesRestantes() { return sesionesRestantes; }
    public void setSesionesRestantes(int sesionesRestantes) { this.sesionesRestantes = sesionesRestantes; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }
}