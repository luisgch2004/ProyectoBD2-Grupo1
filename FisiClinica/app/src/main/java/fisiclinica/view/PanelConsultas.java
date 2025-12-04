package fisiclinica.view;

import fisiclinica.dao.ConsultaDAO;
import fisiclinica.dao.PacienteDAO;
import fisiclinica.model.ConsultaMedica;
import fisiclinica.model.Paciente;
import fisiclinica.util.Estilos;
import fisiclinica.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelConsultas extends JPanel {
    private JTable tabla;
    private DefaultTableModel modelo;
    private ConsultaDAO consultaDAO;
    private PacienteDAO pacienteDAO;
    private JTextField txtDni;
    private List<ConsultaMedica> listaConsultasCache;

    public PanelConsultas() {
        consultaDAO = new ConsultaDAO();
        pacienteDAO = new PacienteDAO();
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // --- PANEL SUPERIOR ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);

        JLabel lblDni = new JLabel("DNI Paciente:");
        lblDni.setForeground(Estilos.TEXTO_NEGRO);
        
        txtDni = new JTextField(12);
        
        JButton btnBuscar = new JButton("Buscar Historial");
        Estilos.aplicarEstiloBoton(btnBuscar);
        
        JButton btnEditar = new JButton("Editar Estado");
        Estilos.aplicarEstiloBoton(btnEditar);
        
        // --- BOTÓN NUEVO: CANCELAR ---
        JButton btnCancelar = new JButton("Cancelar Consulta");
        Estilos.aplicarEstiloBoton(btnCancelar);
        btnCancelar.setBackground(new Color(220, 53, 69)); // Rojo alerta
        
        JButton btnAyuda = new JButton("?");
        Estilos.aplicarEstiloBoton(btnAyuda);
        btnAyuda.setBackground(new Color(255, 193, 7)); // Amarillo
        btnAyuda.setForeground(Color.BLACK);
        
        // Agregar componentes
        topPanel.add(lblDni);
        topPanel.add(txtDni);
        topPanel.add(btnBuscar);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(btnEditar);
        topPanel.add(btnCancelar); // Agregado aquí
        topPanel.add(btnAyuda);
        
        add(topPanel, BorderLayout.NORTH);

        // --- TABLA ACTUALIZADA ---
        // 1. Agregamos las columnas al modelo
        String[] columnas = {"ID", "DNI Paciente", "Paciente", "Fecha", "Motivo", "Diagnóstico", "Médico", "Estado"};
        
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(25);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 2. Ajustamos anchos para que se vea ordenado
        tabla.getColumnModel().getColumn(0).setPreferredWidth(30);  // ID
        tabla.getColumnModel().getColumn(1).setPreferredWidth(80);  // DNI
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150); // Paciente
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120); // Fecha
        tabla.getColumnModel().getColumn(4).setPreferredWidth(100); // Motivo
        
        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // --- EVENTOS ---
        btnBuscar.addActionListener(e -> buscarConsultas());
        
        btnEditar.addActionListener(e -> editarConsulta());
        
        // Acción para cancelar
        btnCancelar.addActionListener(e -> cancelarConsultaSeleccionada());
        
        btnAyuda.addActionListener(e -> JOptionPane.showMessageDialog(this, 
            "PARA REGISTRAR NUEVA CONSULTA:\n1. Vaya a 'Pacientes'.\n2. Seleccione uno.\n3. Click en 'ATENDER CONSULTA'."));

        // Evento para resetear al borrar el texto
        txtDni.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (txtDni.getText().trim().isEmpty()) {
                    cargarTodasLasConsultas(); // Si está vacío, carga todo
                }
            }
        });
        
        // Carga inicial
        cargarTodasLasConsultas();
    }

    private void cargarTodasLasConsultas() {
        modelo.setRowCount(0);
        try {
            listaConsultasCache = consultaDAO.listarTodas();
            for (ConsultaMedica c : listaConsultasCache) {
                agregarFilaTabla(c);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void buscarConsultas() {
        String dni = txtDni.getText().trim();
        if(dni.isEmpty()) {
            cargarTodasLasConsultas();
            return;
        }

        try {
            Paciente p = pacienteDAO.buscarPorDni(dni);
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Paciente no encontrado.");
                return;
            }

            // Filtrar localmente o llamar a DB (Llamamos a DB para asegurar frescura)
            List<ConsultaMedica> lista = consultaDAO.listarConsultasPaciente(p.getIdPaciente());
            modelo.setRowCount(0);
            for (ConsultaMedica c : lista) {
                agregarFilaTabla(c);
            }
            // Actualizamos la caché local con lo que encontramos
            listaConsultasCache = lista; 

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void agregarFilaTabla(ConsultaMedica c) {
        modelo.addRow(new Object[]{
            c.getIdConsulta(),
            c.getDniPaciente(),      // <--- AHORA SÍ
            c.getNombrePaciente(),   // <--- AHORA SÍ
            c.getFechaConsulta(), 
            c.getMotivoConsulta(),
            c.getDiagnostico(), 
            c.getNombreMedico(), 
            c.getEstado()
        });
    }

    private void editarConsulta() {
        int row = tabla.getSelectedRow();
        if(row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una consulta.");
            return;
        }
        
        int idConsulta = (int) modelo.getValueAt(row, 0);
        ConsultaMedica consulta = listaConsultasCache.stream()
                .filter(c -> c.getIdConsulta() == idConsulta)
                .findFirst().orElse(null);
        
        if(consulta != null) {
            JTextArea txtDiag = new JTextArea(consulta.getDiagnostico(), 3, 20);
            JTextArea txtTrat = new JTextArea(consulta.getTratamientoPrescrito(), 3, 20);
            JTextArea txtObs = new JTextArea(consulta.getObservaciones(), 3, 20);
            
            JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"EN_PROCESO", "COMPLETADA", "CANCELADA"});
            cmbEstado.setSelectedItem(consulta.getEstado());
            
            Object[] message = {
                "Estado Actual:", cmbEstado,
                "Diagnóstico:", new JScrollPane(txtDiag),
                "Tratamiento:", new JScrollPane(txtTrat),
                "Observaciones:", new JScrollPane(txtObs)
            };

            int opt = JOptionPane.showConfirmDialog(this, message, "Editar Consulta", JOptionPane.OK_CANCEL_OPTION);
            if(opt == JOptionPane.OK_OPTION) {
                consulta.setDiagnostico(txtDiag.getText());
                consulta.setTratamientoPrescrito(txtTrat.getText());
                consulta.setObservaciones(txtObs.getText());
                consulta.setEstado((String) cmbEstado.getSelectedItem());
                
                try {
                    consultaDAO.actualizarConsulta(consulta);
                    recargarVista();
                    JOptionPane.showMessageDialog(this, "Consulta actualizada.");
                } catch(Exception ex) { 
                    JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage());
                }
            }
        }
    }

    // --- NUEVO MÉTODO PARA CANCELAR ---
    private void cancelarConsultaSeleccionada() {
        int row = tabla.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una consulta para cancelar.");
            return;
        }
        
        int idConsulta = (int) modelo.getValueAt(row, 0);
        String estadoActual = (String) modelo.getValueAt(row, 5);
        
        if ("CANCELADA".equals(estadoActual)) {
            JOptionPane.showMessageDialog(this, "Esta consulta ya está cancelada.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de CANCELAR esta consulta?\nEsta acción quedará registrada.", 
            "Confirmar Cancelación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Llamamos al DAO (asegúrate de haber agregado 'eliminarConsulta' en ConsultaDAO)
                consultaDAO.eliminarConsulta(idConsulta, UserSession.getInstance().getUsuario().getIdUsuario());
                recargarVista();
                JOptionPane.showMessageDialog(this, "Consulta cancelada exitosamente.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al cancelar: " + ex.getMessage());
            }
        }
    }
    
    // Método auxiliar para no repetir código de recarga
    private void recargarVista() {
        if (txtDni.getText().trim().isEmpty()) {
            cargarTodasLasConsultas();
        } else {
            buscarConsultas();
        }
    }
}