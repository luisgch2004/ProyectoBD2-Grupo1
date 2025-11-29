package fisiclinica.util;

import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    // Configuración de conexión
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String DB_USER = "fisiclinica_admin";
    private static final String DB_PASSWORD = "Admin2024$Fisiclinica";
    
    /**
     * Constructor privado para implementar Singleton
     */
    private DatabaseConnection() {
        try {
            // Cargar el driver de Oracle
            Class.forName("oracle.jdbc.driver.OracleDriver");
            
            // Establecer propiedades de conexión
            Properties props = new Properties();
            props.setProperty("user", DB_USER);
            props.setProperty("password", DB_PASSWORD);
            props.setProperty("oracle.jdbc.ReadTimeout", "30000");
            
            // Establecer conexión
            connection = DriverManager.getConnection(DB_URL, props);
            
            System.out.println("✓ Conexión exitosa a Oracle Database - FISICLINICA");
            
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Error: Driver Oracle JDBC no encontrado");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ Error al conectar con la base de datos");
            e.printStackTrace();
        }
    }
    
    /**
     * Obtener instancia única de la conexión (Singleton)
     */
    public static DatabaseConnection getInstance() {
        if (instance == null || !isConnectionValid()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Obtener la conexión activa
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
    
    /**
     * Verificar si la conexión es válida
     */
    private static boolean isConnectionValid() {
        try {
            return instance != null && 
                   instance.connection != null && 
                   !instance.connection.isClosed() &&
                   instance.connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Cerrar la conexión
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al cerrar la conexión");
            e.printStackTrace();
        }
    }
    
    /**
     * Ejecutar procedimiento almacenado sin resultado
     */
    public void executeProcedure(String procedureName, Object... parameters) throws SQLException {
        StringBuilder call = new StringBuilder("{call " + procedureName + "(");
        for (int i = 0; i < parameters.length; i++) {
            call.append("?");
            if (i < parameters.length - 1) call.append(",");
        }
        call.append(")}");
        
        try (CallableStatement stmt = connection.prepareCall(call.toString())) {
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            stmt.execute();
        }
    }
    
    /**
     * Ejecutar query y retornar ResultSet
     */
    public ResultSet executeQuery(String query, Object... parameters) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(query);
        for (int i = 0; i < parameters.length; i++) {
            stmt.setObject(i + 1, parameters[i]);
        }
        return stmt.executeQuery();
    }
    
    /**
     * Ejecutar update/insert/delete
     */
    public int executeUpdate(String query, Object... parameters) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            return stmt.executeUpdate();
        }
    }
    
    /**
     * Hacer commit manual
     */
    public void commit() throws SQLException {
        if (connection != null) {
            connection.commit();
        }
    }
    
    /**
     * Hacer rollback manual
     */
    public void rollback() throws SQLException {
        if (connection != null) {
            connection.rollback();
        }
    }
}