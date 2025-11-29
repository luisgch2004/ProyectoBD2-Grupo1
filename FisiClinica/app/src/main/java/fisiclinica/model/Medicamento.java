package fisiclinica.model;

import java.sql.Date;

public class Medicamento {
    private int idMedicamento;
    private String nombre;
    private String descripcion;
    private String principioActivo;
    private String concentracion;
    private String formaFarmaceutica;
    private int stockActual;
    private int stockMinimo;
    private double precioUnitario;
    private String estado;
    private Date fechaRegistro;
    
    public Medicamento() {}
    
    public Medicamento(String nombre, String principioActivo, int stockActual, int stockMinimo) {
        this.nombre = nombre;
        this.principioActivo = principioActivo;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
    }
    
    // Getters y Setters
    public int getIdMedicamento() { return idMedicamento; }
    public void setIdMedicamento(int idMedicamento) { this.idMedicamento = idMedicamento; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getPrincipioActivo() { return principioActivo; }
    public void setPrincipioActivo(String principioActivo) { this.principioActivo = principioActivo; }
    
    public String getConcentracion() { return concentracion; }
    public void setConcentracion(String concentracion) { this.concentracion = concentracion; }
    
    public String getFormaFarmaceutica() { return formaFarmaceutica; }
    public void setFormaFarmaceutica(String formaFarmaceutica) { this.formaFarmaceutica = formaFarmaceutica; }
    
    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }
    
    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    
    public String getEstadoStock() {
        if (stockActual <= stockMinimo) return "CRÍTICO";
        if (stockActual <= stockMinimo * 1.5) return "BAJO";
        return "NORMAL";
    }
    
    @Override
    public String toString() {
        return nombre + " (" + concentracion + ") - Stock: " + stockActual;
    }
}