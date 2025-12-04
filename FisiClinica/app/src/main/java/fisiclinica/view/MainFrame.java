package fisiclinica.view;

import fisiclinica.model.Usuario;
import fisiclinica.util.UserSession;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public MainFrame() {
        Usuario usuario = UserSession.getInstance().getUsuario();
        if (usuario == null) {
            System.exit(0);
        }

        setTitle("FISICLINICA - Sistema de Gestión [Usuario: " + usuario.getNombreCompleto() + " - " + usuario.getNombreRol() + "]");
        setSize(1175, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initUI(usuario.getNombreRol());
    }

    private void initUI(String rol) {
        setLayout(new BorderLayout());

        // --- Menu Lateral ---
        JPanel sideMenu = new JPanel();
        // Usamos BoxLayout o GridLayout flexible para que no queden huecos feos
        sideMenu.setLayout(new GridLayout(10, 1, 5, 5)); 
        sideMenu.setBackground(new Color(44, 62, 80));
        sideMenu.setPreferredSize(new Dimension(200, getHeight()));

        // --- Panel Central (CardLayout) ---
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Panel de bienvenida (fondo blanco por defecto)
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(Color.WHITE);
        JLabel lblWelcome = new JLabel("Bienvenido al Sistema FISICLINICA");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcome.setForeground(new Color(200, 200, 200));
        welcomePanel.add(lblWelcome);
        mainPanel.add(welcomePanel, "HOME");

        // =========================================================
        // LÓGICA DE PERMISOS POR ROL
        // =========================================================

        boolean mostrarPacientes = false;
        boolean mostrarConsultas = false;
        boolean mostrarMedicamentos = false;
        boolean mostrarDonaciones = false;

        switch (rol) {
            case "ADMINISTRADOR":
                mostrarPacientes = true;
                mostrarConsultas = true;
                mostrarMedicamentos = true;
                mostrarDonaciones = true;
                break;
            case "MEDICO":
                // 2. Medico: Pacientes, Consultas y Medicamentos
                mostrarPacientes = true;
                mostrarConsultas = true;
                mostrarMedicamentos = true;
                break;
            case "CAJA":
                // 1. Caja: Solo Pacientes y Consultas
                mostrarPacientes = true;
                mostrarConsultas = true;
                break;
            case "FARMACIA":
            case "LOGISTICA":
                // 3. Farmaceutica: Solo Medicamentos
                mostrarMedicamentos = true;
                break;
            case "FINANZAS":
                // 4. Finanzas: Solo Donaciones
                mostrarDonaciones = true;
                break;
            case "ENFERMERO":
                mostrarPacientes = true;
                break;
        }

        // =========================================================
        // AGREGAR BOTONES Y PANELES SEGÚN FLAGS
        // =========================================================

        if (mostrarPacientes) {
            agregarBotonMenu(sideMenu, "Pacientes", "PACIENTES");
            mainPanel.add(new PanelPacientes(), "PACIENTES");
        }

        if (mostrarConsultas) {
            agregarBotonMenu(sideMenu, "Consultas", "CONSULTAS");
            mainPanel.add(new PanelConsultas(), "CONSULTAS");
        }

        if (mostrarMedicamentos) {
            agregarBotonMenu(sideMenu, "Medicamentos", "FARMACIA");
            mainPanel.add(new PanelMedicamentos(), "FARMACIA");
        }

        if (mostrarDonaciones) {
            agregarBotonMenu(sideMenu, "Donaciones", "DONACIONES");
            mainPanel.add(new PanelDonaciones(), "DONACIONES");
        }

        // Botón Salir (Siempre visible)
        JButton btnSalir = new JButton("Cerrar Sesión");
        fisiclinica.util.Estilos.aplicarEstiloBoton(btnSalir);
        // Opcional: Color rojo para diferenciar
        // btnSalir.setBackground(new Color(192, 57, 43)); 
        
        btnSalir.addActionListener(e -> {
            fisiclinica.util.UserSession.getInstance().logout();
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        
        // Espaciador para empujar Salir abajo
        sideMenu.add(Box.createVerticalGlue());
        sideMenu.add(btnSalir);

        add(sideMenu, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);
        
        // Seleccionar la primera pestaña disponible automáticamente
        if(mostrarPacientes) cardLayout.show(mainPanel, "PACIENTES");
        else if(mostrarMedicamentos) cardLayout.show(mainPanel, "FARMACIA");
        else if(mostrarDonaciones) cardLayout.show(mainPanel, "DONACIONES");
    }

    private void agregarBotonMenu(JPanel panel, String texto, String cardName) {
        JButton btn = new JButton(texto);
        estilizarBoton(btn);
        btn.addActionListener(e -> cardLayout.show(mainPanel, cardName));
        panel.add(btn);
    }
    
    private void estilizarBoton(JButton btn) {
        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }
}
