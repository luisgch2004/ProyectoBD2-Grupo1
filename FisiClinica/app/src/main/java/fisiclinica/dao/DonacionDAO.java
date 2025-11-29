package fisiclinica.dao;

import fisiclinica.model.*;
import fisiclinica.util.DatabaseConnection;
import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonacionDAO {
    
    private Connection connection;
    
    public DonacionDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    /**
     * Registrar donación
     */
    public String registrarDonacion(Donacion donacion) throws SQLException {
        CallableStatement stmt = null;
        String mensaje = "";
        
        try {
            stmt = connection.prepareCall(
                "{call fisiclinica_admin.PKG_DONACIONES.sp_registrar_donacion(?,?,?,?,?,?,?,?,?,?)}"
            );
            
            stmt.setString(1, donacion.getDniDonante());
            stmt.setString(2, donacion.getNombreDonante());
            stmt.setString(3, donacion.getTipoDonacion());
            stmt.setDouble(4, donacion.getMonto());
            stmt.setString(5, donacion.getMoneda());
            stmt.setString(6, donacion.getDescripcion());
            stmt.setInt(7, donacion.getIdAreaDestino());
            stmt.setInt(8, donacion.getIdUsuarioRegistro());
            stmt.registerOutParameter(9, Types.INTEGER);
            stmt.registerOutParameter(10, Types.VARCHAR);
            
            stmt.execute();
            
            int resultado = stmt.getInt(9);
            mensaje = stmt.getString(10);
            
            if (resultado != 1) {
                throw new SQLException(mensaje);
            }
            
        } finally {
            if (stmt != null) stmt.close();
        }
        
        return mensaje;
    }
    
    /**
     * Listar donaciones por período
     */
    public List<Donacion> listarDonaciones(java.util.Date fechaInicio, java.util.Date fechaFin) throws SQLException {
        CallableStatement stmt = null;
        ResultSet rs = null;
        List<Donacion> donaciones = new ArrayList<>();
        
        try {
            stmt = connection.prepareCall(
                "{call fisiclinica_admin.PKG_DONACIONES.sp_listar_donaciones(?,?,?)}"
            );
            
            stmt.setDate(1, new java.sql.Date(fechaInicio.getTime()));
            stmt.setDate(2, new java.sql.Date(fechaFin.getTime()));
            stmt.registerOutParameter(3, OracleTypes.CURSOR);
            
            stmt.execute();
            
            rs = (ResultSet) stmt.getObject(3);
            
            while (rs.next()) {
                Donacion donacion = new Donacion();
                donacion.setIdDonacion(rs.getInt("id_donacion"));
                donacion.setNombreDonante(rs.getString("nombre_donante"));
                donacion.setDniDonante(rs.getString("dni_donante"));
                donacion.setTipoDonacion(rs.getString("tipo_donacion"));
                donacion.setFechaDonacion(rs.getDate("fecha_donacion"));
                donacion.setMonto(rs.getDouble("monto"));
                donacion.setMoneda(rs.getString("moneda"));
                donacion.setEstado(rs.getString("estado"));
                donacion.setNombreAreaDestino(rs.getString("area_destino"));
                donaciones.add(donacion);
            }
            
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        
        return donaciones;
    }
}
