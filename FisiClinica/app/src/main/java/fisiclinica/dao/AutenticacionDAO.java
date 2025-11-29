package fisiclinica.dao;

import fisiclinica.model.*;
import fisiclinica.util.DatabaseConnection;

import java.sql.*;

public class AutenticacionDAO {
    
    private Connection connection;
    
    public AutenticacionDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
        
    }
    
    /**
     * Validar login de usuario
     */
    public Usuario validarLogin(String email, String passwordHash) throws SQLException {
        CallableStatement stmt = null;
        Usuario usuario = null;
        
        try {
            // Llamar al procedimiento PKG_AUTENTICACION.sp_validar_login
            stmt = connection.prepareCall("{call fisiclinica_admin.PKG_AUTENTICACION.sp_validar_login(?,?,?,?,?,?)}");
            
            stmt.setString(1, email);
            stmt.setString(2, passwordHash);
            stmt.registerOutParameter(3, Types.INTEGER);  // p_resultado
            stmt.registerOutParameter(4, Types.INTEGER);  // p_id_usuario
            stmt.registerOutParameter(5, Types.VARCHAR);  // p_nombre_completo
            stmt.registerOutParameter(6, Types.VARCHAR);  // p_rol
            
            stmt.execute();
            
            int resultado = stmt.getInt(3);
            
            if (resultado == 1) {
                // Login exitoso
                usuario = new Usuario();
                usuario.setIdUsuario(stmt.getInt(4));
                String nombreCompleto = stmt.getString(5);
                String[] nombres = nombreCompleto.split(" ", 2);
                usuario.setNombre(nombres[0]);
                usuario.setApellido(nombres.length > 1 ? nombres[1] : "");
                usuario.setEmail(email);
                usuario.setNombreRol(stmt.getString(6));
                usuario.setEstado("ACTIVO");
                
                // Registrar el login
                registrarLogin(email, 1, "127.0.0.1");
            }
            
        } finally {
            if (stmt != null) stmt.close();
        }
        
        return usuario;
    }
    
    /**
     * Registrar intento de login
     */
    private void registrarLogin(String email, int exitoso, String ip) {
        try {
            CallableStatement stmt = connection.prepareCall(
                "{call fisiclinica_admin.PKG_AUTENTICACION.sp_registrar_login(?,?,?)}"
            );
            stmt.setString(1, email);
            stmt.setInt(2, exitoso);
            stmt.setString(3, ip);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
}

