package fisiclinica.model;

import java.sql.Date;
import java.sql.Timestamp;

public class ConsultaMedica {
    private int idConsulta;
    private int idPaciente;
    private int idMedico;
    private Date fechaConsulta;
    private String motivoConsulta;
    private String sintomas;
    private String diagnostico;
    private String tratamientoPrescrito;
    private String observaciones;
    private String estado;
    
    // Datos adicionales para mostrar
    private String nombrePaciente;
    private String nombreMedico;
    private String dniPaciente;
    
    public ConsultaMedica() {}
    
    // Getters y Setters
    public int getIdConsulta() { return idConsulta; }
    public void setIdConsulta(int idConsulta) { this.idConsulta = idConsulta; }
    
    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }
    
    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }
    
    public Date getFechaConsulta() { return fechaConsulta; }
    public void setFechaConsulta(Date fechaConsulta) { this.fechaConsulta = fechaConsulta; }
    
    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }
    
    public String getSintomas() { return sintomas; }
    public void setSintomas(String sintomas) { this.sintomas = sintomas; }
    
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    
    public String getTratamientoPrescrito() { return tratamientoPrescrito; }
    public void setTratamientoPrescrito(String tratamientoPrescrito) { 
        this.tratamientoPrescrito = tratamientoPrescrito; 
    }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }
    
    public String getNombreMedico() { return nombreMedico; }
    public void setNombreMedico(String nombreMedico) { this.nombreMedico = nombreMedico; }
    
    public String getDniPaciente() { return dniPaciente; }
    public void setDniPaciente(String dniPaciente) { this.dniPaciente = dniPaciente; }

    public void setFechaConsulta(Timestamp timestamp) {
        this.fechaConsulta = new Date(timestamp.getTime());
    }
}
