package fisiclinica.model;

import java.sql.Date;

public class Donacion {
    private int idDonacion;
    private String dniDonante;
    private String nombreDonante;
    private String tipoDonacion;
    private Date fechaDonacion;
    private double monto;
    private String moneda;
    private String descripcion;
    private String estado;
    private int idUsuarioRegistro;
    private int idAreaDestino;
    private String nombreAreaDestino;
    
    public Donacion() {}
    
    // Getters y Setters
    public int getIdDonacion() { return idDonacion; }
    public void setIdDonacion(int idDonacion) { this.idDonacion = idDonacion; }
    
    public String getDniDonante() { return dniDonante; }
    public void setDniDonante(String dniDonante) { this.dniDonante = dniDonante; }
    
    public String getNombreDonante() { return nombreDonante; }
    public void setNombreDonante(String nombreDonante) { this.nombreDonante = nombreDonante; }
    
    public String getTipoDonacion() { return tipoDonacion; }
    public void setTipoDonacion(String tipoDonacion) { this.tipoDonacion = tipoDonacion; }
    
    public Date getFechaDonacion() { return fechaDonacion; }
    public void setFechaDonacion(Date fechaDonacion) { this.fechaDonacion = fechaDonacion; }
    
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public int getIdUsuarioRegistro() { return idUsuarioRegistro; }
    public void setIdUsuarioRegistro(int idUsuarioRegistro) { this.idUsuarioRegistro = idUsuarioRegistro; }
    
    public int getIdAreaDestino() { return idAreaDestino; }
    public void setIdAreaDestino(int idAreaDestino) { this.idAreaDestino = idAreaDestino; }
    
    public String getNombreAreaDestino() { return nombreAreaDestino; }
    public void setNombreAreaDestino(String nombreAreaDestino) { this.nombreAreaDestino = nombreAreaDestino; }
}
