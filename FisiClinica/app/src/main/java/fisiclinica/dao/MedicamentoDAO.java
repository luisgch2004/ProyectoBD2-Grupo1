package fisiclinica.dao;

import fisiclinica.model.*;
import fisiclinica.util.DatabaseConnection;
import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoDAO {
    
    private Connection connection;
    
    public MedicamentoDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    /**
     * Registrar medicamento
     */
    public String registrarMedicamento(Medicamento medicamento, int idUsuario) throws SQLException {
        CallableStatement stmt = null;
        String mensaje = "";
        
        try {
            stmt = connection.prepareCall(
                "{call fisiclinica_admin.PKG_MEDICAMENTOS.sp_registrar_medicamento(?,?,?,?,?,?,?,?,?,?)}"
            );
            
            stmt.setString(1, medicamento.getNombre());
            stmt.setString(2, medicamento.getDescripcion());
            stmt.setString(3, medicamento.getPrincipioActivo());
            stmt.setString(4, medicamento.getConcentracion());
            stmt.setString(5, medicamento.getFormaFarmaceutica());
            stmt.setInt(6, medicamento.getStockMinimo());
            stmt.setDouble(7, medicamento.getPrecioUnitario());
            stmt.setInt(8, idUsuario);
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
     * Buscar medicamentos
     */
    public List<Medicamento> buscarMedicamentos(String criterio) throws SQLException {
        CallableStatement stmt = null;
        ResultSet rs = null;
        List<Medicamento> medicamentos = new ArrayList<>();
        
        try {
            stmt = connection.prepareCall(
                "{call fisiclinica_admin.PKG_MEDICAMENTOS.sp_buscar_medicamentos(?,?)}"
            );
            
            stmt.setString(1, criterio);
            stmt.registerOutParameter(2, OracleTypes.CURSOR);
            
            stmt.execute();
            
            rs = (ResultSet) stmt.getObject(2);
            
            while (rs.next()) {
                Medicamento med = new Medicamento();
                med.setIdMedicamento(rs.getInt("id_medicamento"));
                med.setNombre(rs.getString("nombre"));
                med.setPrincipioActivo(rs.getString("principio_activo"));
                med.setConcentracion(rs.getString("concentracion"));
                med.setFormaFarmaceutica(rs.getString("forma_farmaceutica"));
                med.setStockActual(rs.getInt("stock_actual"));
                med.setStockMinimo(rs.getInt("stock_minimo"));
                med.setPrecioUnitario(rs.getDouble("precio_unitario"));
                med.setEstado(rs.getString("estado"));
                medicamentos.add(med);
            }
            
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        
        return medicamentos;
    }
    
    /**
     * Obtener medicamentos con stock bajo
     */
    public List<Medicamento> obtenerStockBajo() throws SQLException {
        CallableStatement stmt = null;
        ResultSet rs = null;
        List<Medicamento> medicamentos = new ArrayList<>();
        
        try {
            stmt = connection.prepareCall("{call fisiclinica_admin.PKG_MEDICAMENTOS.sp_stock_bajo(?)}");
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.execute();
            
            rs = (ResultSet) stmt.getObject(1);
            
            while (rs.next()) {
                Medicamento med = new Medicamento();
                // AHORA LEEMOS TODOS LOS CAMPOS IGUAL QUE EN LA BÚSQUEDA NORMAL
                med.setIdMedicamento(rs.getInt("id_medicamento"));
                med.setNombre(rs.getString("nombre"));
                med.setDescripcion(rs.getString("descripcion"));
                med.setPrincipioActivo(rs.getString("principio_activo")); // <-- IMPORTANTE
                med.setConcentracion(rs.getString("concentracion"));
                med.setFormaFarmaceutica(rs.getString("forma_farmaceutica"));
                med.setStockActual(rs.getInt("stock_actual"));
                med.setStockMinimo(rs.getInt("stock_minimo"));
                med.setPrecioUnitario(rs.getDouble("precio_unitario"));   // <-- IMPORTANTE
                med.setEstado(rs.getString("estado"));
                
                medicamentos.add(med);
            }
            
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        
        return medicamentos;
    }

    public void actualizarMedicamento(Medicamento m) throws SQLException {
        String sql = "UPDATE medicamento SET nombre=?, stock_minimo=?, precio_unitario=? WHERE id_medicamento=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, m.getNombre());
            ps.setInt(2, m.getStockMinimo());
            ps.setDouble(3, m.getPrecioUnitario());
            ps.setInt(4, m.getIdMedicamento());
            ps.executeUpdate();
        }
    }
}
