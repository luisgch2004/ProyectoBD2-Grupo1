package fisiclinica.dao;

import fisiclinica.model.HistorialClinico;
import fisiclinica.util.DatabaseConnection;
import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistorialClinicoDAO {
    private Connection connection;

    public HistorialClinicoDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public List<HistorialClinico> buscarHistorial(String dni) throws SQLException {
        List<HistorialClinico> lista = new ArrayList<>();
        // Aseguramos traer TODOS los campos
        String sql = "SELECT h.id_historial, h.fecha_creacion, h.alergias, " +
                     "h.condiciones_cronicas, h.medicamentos_actuales, h.observaciones_generales, " +
                     "p.id_paciente, p.dni, p.nombre || ' ' || p.apellido AS nombre_completo " +
                     "FROM historial_clinico h " +
                     "JOIN paciente p ON h.id_paciente = p.id_paciente " +
                     "WHERE p.dni LIKE ? AND p.estado = 'ACTIVO' " +
                     "ORDER BY p.apellido";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (dni == null || dni.trim().isEmpty()) ps.setString(1, "%");
            else ps.setString(1, "%" + dni + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialClinico h = new HistorialClinico();
                    h.setIdHistorial(rs.getInt("id_historial"));
                    h.setIdPaciente(rs.getInt("id_paciente"));
                    h.setFechaCreacion(rs.getDate("fecha_creacion"));
                    // Usamos getString, Oracle JDBC maneja CLOBs pequeños automáticamente
                    h.setAlergias(rs.getString("alergias")); 
                    h.setCondicionesCronicas(rs.getString("condiciones_cronicas"));
                    h.setMedicamentosActuales(rs.getString("medicamentos_actuales"));
                    h.setObservacionesGenerales(rs.getString("observaciones_generales"));
                    h.setDniPaciente(rs.getString("dni"));
                    h.setNombrePaciente(rs.getString("nombre_completo"));
                    lista.add(h);
                }
            }
        }
        return lista;
    }
    
    // METODO CORREGIDO PARA ACTUALIZAR
    public void actualizarHistorial(HistorialClinico h) throws SQLException {
        CallableStatement stmt = null;
        try {
            stmt = connection.prepareCall("{call fisiclinica_admin.PKG_PACIENTES.sp_actualizar_historial(?,?,?,?,?,?,?)}");
            
            stmt.setInt(1, h.getIdHistorial());
            // Enviamos strings, el driver lo maneja hacia CLOB
            stmt.setString(2, h.getAlergias());
            stmt.setString(3, h.getCondicionesCronicas());
            stmt.setString(4, h.getMedicamentosActuales());
            stmt.setString(5, h.getObservacionesGenerales());
            
            stmt.registerOutParameter(6, Types.INTEGER);
            stmt.registerOutParameter(7, Types.VARCHAR);
            
            stmt.execute();
            
            if (stmt.getInt(6) != 1) {
                throw new SQLException("Error BD: " + stmt.getString(7));
            }
        } finally {
            if (stmt != null) stmt.close();
        }
    }
}