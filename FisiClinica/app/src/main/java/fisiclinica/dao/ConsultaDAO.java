package fisiclinica.dao;

import fisiclinica.model.ConsultaMedica;
import fisiclinica.util.DatabaseConnection;
import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultaDAO {
    
    private Connection connection;
    
    public ConsultaDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public int registrarConsulta(ConsultaMedica consulta) throws SQLException {
        CallableStatement stmt = null;
        int idConsulta = 0;
        
        try {
            stmt = connection.prepareCall(
                "{call fisiclinica_admin.PKG_CONSULTAS.sp_registrar_consulta(?,?,?,?,?,?,?,?,?,?)}"
            );
            
            stmt.setInt(1, consulta.getIdPaciente());
            stmt.setInt(2, consulta.getIdMedico());
            stmt.setString(3, consulta.getMotivoConsulta());
            stmt.setString(4, consulta.getSintomas());
            stmt.setString(5, consulta.getDiagnostico());
            stmt.setString(6, consulta.getTratamientoPrescrito());
            stmt.setString(7, consulta.getObservaciones());
            stmt.registerOutParameter(8, Types.INTEGER);  // p_resultado
            stmt.registerOutParameter(9, Types.INTEGER);  // p_id_consulta
            stmt.registerOutParameter(10, Types.VARCHAR); // p_mensaje
            
            stmt.execute();
            
            int resultado = stmt.getInt(8);
            String mensaje = stmt.getString(10);
            
            if (resultado == 1) {
                idConsulta = stmt.getInt(9);
                // IMPORTANTE: Por defecto el SP podría poner 'COMPLETADA', 
                // forzamos la actualización a 'EN_PROCESO' para cumplir tu requerimiento
                actualizarEstadoDirecto(idConsulta, "EN_PROCESO");
            } else {
                throw new SQLException(mensaje);
            }
            
        } finally {
            if (stmt != null) stmt.close();
        }
        return idConsulta;
    }
    
    // Método auxiliar privado para forzar el estado inicial
    private void actualizarEstadoDirecto(int id, String estado) throws SQLException {
        String sql = "UPDATE consulta_medica SET estado = ? WHERE id_consulta = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public List<ConsultaMedica> listarTodas() throws SQLException {
        List<ConsultaMedica> lista = new ArrayList<>();
        String sql = "SELECT * FROM (SELECT c.*, u.nombre || ' ' || u.apellido as nombre_medico " +
                     "FROM consulta_medica c JOIN usuario u ON c.id_medico = u.id_usuario " +
                     "ORDER BY c.fecha_consulta DESC) WHERE ROWNUM <= 50";
        
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ConsultaMedica c = mapResultSet(rs);
                lista.add(c);
            }
        }
        return lista;
    }

    public List<ConsultaMedica> listarConsultasPaciente(int idPaciente) throws SQLException {
        CallableStatement stmt = null;
        ResultSet rs = null;
        List<ConsultaMedica> consultas = new ArrayList<>();
        
        try {
            stmt = connection.prepareCall("{call fisiclinica_admin.PKG_CONSULTAS.sp_listar_consultas_paciente(?,?)}");
            stmt.setInt(1, idPaciente);
            stmt.registerOutParameter(2, OracleTypes.CURSOR);
            stmt.execute();
            rs = (ResultSet) stmt.getObject(2);
            while (rs.next()) {
                consultas.add(mapResultSet(rs));
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        return consultas;
    }

    // ACTUALIZADO: Ahora permite cambiar el ESTADO
    public void actualizarConsulta(ConsultaMedica c) throws SQLException {
        String sql = "UPDATE consulta_medica SET diagnostico=?, tratamiento_prescrito=?, observaciones=?, estado=? WHERE id_consulta=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c.getDiagnostico());
            ps.setString(2, c.getTratamientoPrescrito());
            ps.setString(3, c.getObservaciones());
            ps.setString(4, c.getEstado()); // Nuevo campo editable
            ps.setInt(5, c.getIdConsulta());
            ps.executeUpdate();
        }
    }
    
    private ConsultaMedica mapResultSet(ResultSet rs) throws SQLException {
        ConsultaMedica c = new ConsultaMedica();
        c.setIdConsulta(rs.getInt("id_consulta"));
        c.setFechaConsulta(rs.getTimestamp("fecha_consulta"));
        c.setMotivoConsulta(rs.getString("motivo_consulta"));
        c.setSintomas(rs.getString("sintomas"));
        c.setDiagnostico(rs.getString("diagnostico"));
        c.setTratamientoPrescrito(rs.getString("tratamiento_prescrito"));
        c.setObservaciones(rs.getString("observaciones"));
        c.setEstado(rs.getString("estado"));
        c.setNombreMedico(rs.getString("nombre_medico"));
        return c;
    }

    public void eliminarConsulta(int idConsulta, int idUsuario) throws SQLException {
    CallableStatement stmt = null;
    try {
        stmt = connection.prepareCall("{call fisiclinica_admin.PKG_CONSULTAS.sp_eliminar_consulta(?,?,?,?)}");
        stmt.setInt(1, idConsulta);
        stmt.setInt(2, idUsuario);
        stmt.registerOutParameter(3, Types.INTEGER);
        stmt.registerOutParameter(4, Types.VARCHAR);
        stmt.execute();
        
        if (stmt.getInt(3) != 1) throw new SQLException(stmt.getString(4));
    } finally { if(stmt!=null) stmt.close(); }
}
}