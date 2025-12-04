package fisiclinica.model;

import java.util.Date;

public class HistorialClinico {
    private int idHistorial;
    private int idPaciente;
    private Date fechaCreacion;
    private String alergias;
    private String condicionesCronicas;
    private String medicamentosActuales;
    private String observacionesGenerales;

    // --- ATRIBUTOS ADICIONALES PARA LA VISTA (JOIN) ---
    private String dniPaciente;
    private String nombrePaciente;

    public HistorialClinico() {}

    // Getters y Setters
    public int getIdHistorial() { return idHistorial; }
    public void setIdHistorial(int idHistorial) { this.idHistorial = idHistorial; }

    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }

    public String getCondicionesCronicas() { return condicionesCronicas; }
    public void setCondicionesCronicas(String condicionesCronicas) { this.condicionesCronicas = condicionesCronicas; }

    public String getMedicamentosActuales() { return medicamentosActuales; }
    public void setMedicamentosActuales(String medicamentosActuales) { this.medicamentosActuales = medicamentosActuales; }

    public String getObservacionesGenerales() { return observacionesGenerales; }
    public void setObservacionesGenerales(String observacionesGenerales) { this.observacionesGenerales = observacionesGenerales; }

    // Getters y Setters para datos visuales
    public String getDniPaciente() { return dniPaciente; }
    public void setDniPaciente(String dniPaciente) { this.dniPaciente = dniPaciente; }

    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }
}