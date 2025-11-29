package fisiclinica.view;

import fisiclinica.dao.AutenticacionDAO;
import fisiclinica.model.Usuario;
import fisiclinica.util.Estilos; // Importamos estilos
import fisiclinica.util.PasswordUtil;
import fisiclinica.util.UserSession;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginFrame() {
        setTitle("FISICLINICA - Acceso");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE); // Fondo blanco para que el texto negro resalte
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Logo o Título
        JLabel lblTitle = new JLabel("FISICLINICA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(Estilos.TEXTO_NEGRO); // Texto Negro
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        // Email
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setForeground(Estilos.TEXTO_NEGRO);
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panel.add(lblEmail, gbc);
        
        txtEmail = new JTextField(20);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(txtEmail, gbc);

        // Password
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setForeground(Estilos.TEXTO_NEGRO);
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(lblPass, gbc);
        
        txtPassword = new JPasswordField(20);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(txtPassword, gbc);

        // Botón con Estilo Personalizado
        btnLogin = new JButton("Iniciar Sesión");
        Estilos.aplicarEstiloBoton(btnLogin); // Aplicamos tu estilo Azul/Celeste
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(btnLogin, gbc);

        add(panel);

        // Acción del botón (Enter también activa el botón)
        getRootPane().setDefaultButton(btnLogin);
        btnLogin.addActionListener(e -> login());
    }

    private void login() {
        String email = txtEmail.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if(email.isEmpty() || pass.isEmpty()){
            JOptionPane.showMessageDialog(this, "Complete todos los campos", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            AutenticacionDAO dao = new AutenticacionDAO();
            String hash = PasswordUtil.hashPassword(pass);
            Usuario usuario = dao.validarLogin(email, hash);

            if (usuario != null) {
                UserSession.getInstance().setUsuario(usuario);
                new MainFrame().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Credenciales incorrectas", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
        }
    }
}