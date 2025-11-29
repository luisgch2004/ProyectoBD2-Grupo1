package fisiclinica.view;

import fisiclinica.dao.ConsultaDAO;
import fisiclinica.dao.PacienteDAO;
import fisiclinica.model.ConsultaMedica;
import fisiclinica.model.Paciente;
import fisiclinica.util.Estilos; // Importar estilos

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
        
        JButton btnVerDetalle = new JButton("Ver Detalle");
        Estilos.aplicarEstiloBoton(btnVerDetalle);
        
        JButton btnAyuda = new JButton("?");
        Estilos.aplicarEstiloBoton(btnAyuda);
        btnAyuda.setBackground(new Color(255, 193, 7)); // Color Amarillo/Ambar
        btnAyuda.setForeground(Color.BLACK);
        
        JButton btnEditar = new JButton("Editar Diagnóstico");
        Estilos.aplicarEstiloBoton(btnEditar);
        
        topPanel.add(lblDni);
        topPanel.add(txtDni);
        topPanel.add(btnBuscar);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(btnVerDetalle);
        topPanel.add(btnEditar);
        topPanel.add(btnAyuda);
        
        add(topPanel, BorderLayout.NORTH);

        // --- TABLA ---
        String[] columnas = {"ID", "Fecha", "Motivo", "Diagnóstico", "Médico", "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(25);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --- EVENTOS ---
        btnBuscar.addActionListener(e -> buscarConsultas());
        
        btnVerDetalle.addActionListener(e -> {
            if(tabla.getSelectedRow() != -1) {
                String diag = (String) tabla.getValueAt(tabla.getSelectedRow(), 3);
                JOptionPane.showMessageDialog(this, "Diagnóstico completo:\n" + diag, "Detalle de Consulta", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una consulta para ver detalles.");
            }
        });
        
        btnAyuda.addActionListener(e -> JOptionPane.showMessageDialog(this, 
            "PARA AGREGAR UNA CONSULTA:\n\n1. Vaya a la pestaña 'Pacientes'.\n2. Busque al paciente en la lista.\n3. Haga clic en el botón 'ATENDER CONSULTA'.\n\nDesde allí podrá registrar la atención médica.", "Ayuda", JOptionPane.INFORMATION_MESSAGE));
            
        btnEditar.addActionListener(e -> editarConsulta());
        
        // REQ 3: Cargar todas las consultas al inicio
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

    private void agregarFilaTabla(ConsultaMedica c) {
        modelo.addRow(new Object[]{
            c.getIdConsulta(), c.getFechaConsulta(), c.getMotivoConsulta(),
            c.getDiagnostico(), c.getNombreMedico(), c.getEstado()
        });
    }

    private void editarConsulta() {
        int row = tabla.getSelectedRow();
        if(row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una consulta.");
            return;
        }
        
        int idConsulta = (int) modelo.getValueAt(row, 0);
        // Buscar en la lista actual
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
                    
                    // Refrescar vista
                    if (txtDni.getText().isEmpty()) {
                        cargarTodasLasConsultas();
                    } else {
                        buscarConsultas();
                    }
                    JOptionPane.showMessageDialog(this, "Consulta actualizada.");
                } catch(Exception ex) { 
                    JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage());
                }
            }
        }
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
            // Actualizamos la caché local con lo que encontramos para que Editar funcione con estos datos
            listaConsultasCache = lista; 

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}