package fisiclinica.dao;

import fisiclinica.model.*;
import fisiclinica.util.DatabaseConnection;
import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {
    
    private Connection connection;
    
    public PacienteDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    /**
     * Registrar nuevo paciente
     */
    public String registrarPaciente(Paciente paciente, int idUsuarioRegistro) throws SQLException {
        CallableStatement stmt = null;
        String mensaje = "";
        
        try {
            stmt = connection.prepareCall(
                "{call fisiclinica_admin.PKG_PACIENTES.sp_registrar_paciente(?,?,?,?,?,?,?,?,?,?,?)}"
            );
            
            stmt.setString(1, paciente.getDni());
            stmt.setString(2, paciente.getNombre());
            stmt.setString(3, paciente.getApellido());
            stmt.setDate(4, new java.sql.Date(paciente.getFechaNacimiento().getTime()));
            stmt.setString(5, paciente.getGenero());
            stmt.setString(6, paciente.getTelefono());
            stmt.setString(7, paciente.getDireccion());
            stmt.setString(8, paciente.getEmail());
            stmt.setInt(9, idUsuarioRegistro);
            stmt.registerOutParameter(10, Types.INTEGER);  // p_resultado
            stmt.registerOutParameter(11, Types.VARCHAR);  // p_mensaje
            
            stmt.execute();
            
            int resultado = stmt.getInt(10);
            mensaje = stmt.getString(11);
            
            if (resultado != 1) {
                throw new SQLException(mensaje);
            }
            
        } finally {
            if (stmt != null) stmt.close();
        }
        
        return mensaje;
    }
    
    /**
     * Buscar paciente por DNI
     */
    public Paciente buscarPorDni(String dni) throws SQLException {
        CallableStatement stmt = null;
        ResultSet rs = null;
        Paciente paciente = null;
        
        try {
            stmt = connection.prepareCall(
                "{call fisiclinica_admin.PKG_PACIENTES.sp_buscar_paciente_dni(?,?)}"
            );
            
            stmt.setString(1, dni);
            stmt.registerOutParameter(2, OracleTypes.CURSOR);
            
            stmt.execute();
            
            rs = (ResultSet) stmt.getObject(2);
            
            if (rs.next()) {
                paciente = new Paciente();
                paciente.setIdPaciente(rs.getInt("id_paciente"));
                paciente.setDni(rs.getString("dni"));
                paciente.setNombre(rs.getString("nombre"));
                paciente.setApellido(rs.getString("apellido"));
                paciente.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                paciente.setGenero(rs.getString("genero"));
                paciente.setTelefono(rs.getString("telefono"));
                paciente.setDireccion(rs.getString("direccion"));
                paciente.setEmail(rs.getString("email"));
                paciente.setFechaRegistro(rs.getDate("fecha_registro"));
                paciente.setEstado(rs.getString("estado"));
            }
            
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        
        return paciente;
    }
    
    /**
     * Listar todos los pacientes activos
     */
    public List<Paciente> listarPacientes() throws SQLException {
        CallableStatement stmt = null;
        ResultSet rs = null;
        List<Paciente> pacientes = new ArrayList<>();
        
        try {
            stmt = connection.prepareCall(
                "{call fisiclinica_admin.PKG_PACIENTES.sp_listar_pacientes(?)}"
            );
            
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.execute();
            
            rs = (ResultSet) stmt.getObject(1);
            
            while (rs.next()) {
                Paciente paciente = new Paciente();
                paciente.setIdPaciente(rs.getInt("id_paciente"));
                paciente.setDni(rs.getString("dni"));
                paciente.setNombre(rs.getString("nombre"));
                paciente.setApellido(rs.getString("apellido"));
                paciente.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                paciente.setGenero(rs.getString("genero"));
                paciente.setTelefono(rs.getString("telefono"));
                paciente.setEstado(rs.getString("estado"));
                pacientes.add(paciente);
            }
            
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        
        return pacientes;
    }
    public void actualizarPaciente(Paciente p) throws SQLException {
        // Actualizamos Nombre, Apellido, Telefono, Direccion y Email
        String sql = "UPDATE paciente SET nombre=?, apellido=?, telefono=?, direccion=?, email=? WHERE id_paciente=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getApellido());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getDireccion());
            ps.setString(5, p.getEmail());
            ps.setInt(6, p.getIdPaciente());
            ps.executeUpdate();
        }
    }

    public void eliminarPaciente(int idPaciente, int idUsuario) throws SQLException {
        CallableStatement stmt = null;
        try {
            stmt = connection.prepareCall("{call fisiclinica_admin.PKG_PACIENTES.sp_eliminar_paciente(?,?,?,?)}");
            stmt.setInt(1, idPaciente);
            stmt.setInt(2, idUsuario);
            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.registerOutParameter(4, Types.VARCHAR);
            stmt.execute();
            
            if (stmt.getInt(3) != 1) throw new SQLException(stmt.getString(4));
        } finally { if(stmt!=null) stmt.close(); }
    }
    public List<Paciente> listarInactivos() throws SQLException {
        CallableStatement stmt = null;
        ResultSet rs = null;
        List<Paciente> pacientes = new ArrayList<>();
        try {
            stmt = connection.prepareCall("{call fisiclinica_admin.PKG_PACIENTES.sp_listar_inactivos(?)}");
            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.execute();
            rs = (ResultSet) stmt.getObject(1);
            while (rs.next()) {
                Paciente p = new Paciente();
                p.setIdPaciente(rs.getInt("id_paciente"));
                p.setDni(rs.getString("dni"));
                p.setNombre(rs.getString("nombre"));
                p.setApellido(rs.getString("apellido"));
                p.setTelefono(rs.getString("telefono"));
                p.setEstado(rs.getString("estado"));
                pacientes.add(p);
            }
        } finally { if(rs!=null) rs.close(); if(stmt!=null) stmt.close(); }
        return pacientes;
    }

    public void reactivarPaciente(int idPaciente, int idUsuario) throws SQLException {
        CallableStatement stmt = null;
        try {
            stmt = connection.prepareCall("{call fisiclinica_admin.PKG_PACIENTES.sp_reactivar_paciente(?,?,?,?)}");
            stmt.setInt(1, idPaciente);
            stmt.setInt(2, idUsuario);
            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.registerOutParameter(4, Types.VARCHAR);
            stmt.execute();
            if (stmt.getInt(3) != 1) throw new SQLException(stmt.getString(4));
        } finally { if(stmt!=null) stmt.close(); }
    }
}
