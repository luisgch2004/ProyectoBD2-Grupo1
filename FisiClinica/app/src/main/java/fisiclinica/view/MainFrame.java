package fisiclinica.view;

import fisiclinica.util.Estilos;
import fisiclinica.util.UserSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // Lista para controlar los botones del menú
    private List<JButton> menuButtons;
    private JButton currentActiveButton;

    public MainFrame() {
        // Validación de sesión
        if (UserSession.getInstance().getUsuario() == null) {
            System.exit(0);
        }

        setTitle("FISICLINICA - Sistema de Gestión [Usuario: " + 
                 UserSession.getInstance().getUsuario().getNombreCompleto() + " - " + 
                 UserSession.getInstance().getUsuario().getNombreRol() + "]");
        setSize(1175, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        menuButtons = new ArrayList<>();
        initUI(UserSession.getInstance().getUsuario().getNombreRol());
    }

    private void initUI(String rol) {
        setLayout(new BorderLayout());

        // --- Menu Lateral ---
        JPanel sideMenu = new JPanel();
        // Usamos BoxLayout vertical para tener control total
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        sideMenu.setBackground(new Color(44, 62, 80));
        sideMenu.setPreferredSize(new Dimension(220, getHeight()));
        sideMenu.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen interno

        // --- Panel Central ---
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(Color.WHITE);
        JLabel lblWelcome = new JLabel("Bienvenido al Sistema FISICLINICA");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcome.setForeground(new Color(200, 200, 200));
        welcomePanel.add(lblWelcome);
        mainPanel.add(welcomePanel, "HOME");

        // --- LÓGICA DE ROLES ---
        boolean showPac = false, showCons = false, showMed = false, showDon = false, showHistorial = false;

        switch (rol) {
            case "ADMINISTRADOR":
                showPac = true; showCons = true; showMed = true; showDon = true; showHistorial = true;
                break;
            case "MEDICO":
                showPac = true; showCons = true; showMed = true; showHistorial = true;
                break;
            case "CAJA":
                showPac = true; showCons = true; showHistorial = true;
                break;
            case "FARMACIA":
                showMed = true; showHistorial = true;
                break;

            case "FINANZAS":
                showDon = true;
                break;
        }

        // --- AGREGAR BOTONES ---
        // Usamos un método especial 'crearBotonMenu' para controlar el color verde/azul
        
        if (showPac) {
            sideMenu.add(crearBotonMenu("Pacientes", "PACIENTES", new PanelPacientes()));
            sideMenu.add(Box.createVerticalStrut(10));
        }if (showHistorial) {
            sideMenu.add(crearBotonMenu("Historial Clínico", "HISTORIAL", new PanelHistorialClinico()));
            sideMenu.add(Box.createVerticalStrut(10));
        }
        if (showCons) {
             sideMenu.add(crearBotonMenu("Consultas", "CONSULTAS", new PanelConsultas()));
             sideMenu.add(Box.createVerticalStrut(10));
        }
        if (showMed) {
            sideMenu.add(crearBotonMenu("Medicamentos", "FARMACIA", new PanelMedicamentos()));
            sideMenu.add(Box.createVerticalStrut(10));
        }
        if (showDon) {
            sideMenu.add(crearBotonMenu("Donaciones", "DONACIONES", new PanelDonaciones()));
            sideMenu.add(Box.createVerticalStrut(10));
        }

        // Espaciador para empujar Salir al fondo
        sideMenu.add(Box.createVerticalGlue());

        // Botón Salir (Estilo diferente, rojo)
        JButton btnSalir = new JButton("Cerrar Sesión");
        btnSalir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSalir.setBackground(new Color(220, 53, 69)); 
        btnSalir.setForeground(Color.BLACK);
        btnSalir.setFocusPainted(false);
        btnSalir.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnSalir.addActionListener(e -> {
            UserSession.getInstance().logout();
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        sideMenu.add(btnSalir);

        add(sideMenu, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);
        
        // Seleccionar la primera pestaña por defecto
        if (!menuButtons.isEmpty()) {
            menuButtons.get(0).doClick();
        }
    }

    private JButton crearBotonMenu(String texto, String cardName, JPanel panelInstancia) {
        // Agregamos el panel al layout
        mainPanel.add(panelInstancia, cardName);

        JButton btn = new JButton(texto);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Estilo Base (Azul)
        btn.setBackground(Estilos.AZUL_OSCURO);
        btn.setForeground(Color.WHITE);

        // Añadir a la lista de control
        menuButtons.add(btn);

        // Lógica de Selección (Click)
        btn.addActionListener(e -> {
            cardLayout.show(mainPanel, cardName);
            actualizarEstilosBotones(btn);
        });

        // Hover Effect personalizado que respeta si está activo o no
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != currentActiveButton) {
                    btn.setBackground(Estilos.CELESTE);
                    btn.setForeground(Color.BLACK);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != currentActiveButton) {
                    btn.setBackground(Estilos.AZUL_OSCURO);
                    btn.setForeground(Color.WHITE);
                }
            }
        });

        return btn;
    }

    private void actualizarEstilosBotones(JButton botonSeleccionado) {
        currentActiveButton = botonSeleccionado;

        for (JButton btn : menuButtons) {
            if (btn == botonSeleccionado) {
                // ESTADO ACTIVO: VERDE
                btn.setBackground(new Color(0, 153, 76)); 
                btn.setForeground(Color.WHITE);
            } else {
                // ESTADO INACTIVO: AZUL
                btn.setBackground(Estilos.AZUL_OSCURO);
                btn.setForeground(Color.WHITE);
            }
        }
    }
}