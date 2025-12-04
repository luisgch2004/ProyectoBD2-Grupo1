package fisiclinica.view;

import fisiclinica.dao.HistorialClinicoDAO;
import fisiclinica.model.HistorialClinico;
import fisiclinica.util.Estilos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class PanelHistorialClinico extends JPanel {
    private JTable tabla;
    private DefaultTableModel modelo;
    private HistorialClinicoDAO historialDAO;
    private JTextField txtDni;
    private List<HistorialClinico> listaCache;

    public PanelHistorialClinico() {
        historialDAO = new HistorialClinicoDAO();
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);

        JLabel lblDni = new JLabel("Buscar por DNI:");
        lblDni.setForeground(Estilos.TEXTO_NEGRO);
        txtDni = new JTextField(15);
        
        JButton btnBuscar = new JButton("Buscar");
        Estilos.aplicarEstiloBoton(btnBuscar);
        
        // Botón solo lectura
        JButton btnVerDetalle = new JButton("Ver Detalle");
        Estilos.aplicarEstiloBoton(btnVerDetalle);
        
        // BOTÓN CLAVE PARA EDITAR
        JButton btnModificar = new JButton("Modificar Datos");
        Estilos.aplicarEstiloBoton(btnModificar);
        btnModificar.setBackground(new Color(255, 140, 0)); // Naranja

        topPanel.add(lblDni);
        topPanel.add(txtDni);
        topPanel.add(btnBuscar);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(btnVerDetalle);
        topPanel.add(btnModificar);
        
        add(topPanel, BorderLayout.NORTH);

        String[] columnas = {"ID", "DNI", "Paciente", "Alergias (Resumen)", "Condiciones (Resumen)"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        
        tabla = new JTable(modelo);
        tabla.setRowHeight(25);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Dar espacio a las columnas para que se vea algo si hay texto
        tabla.getColumnModel().getColumn(3).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Eventos
        btnBuscar.addActionListener(e -> cargarHistoriales(txtDni.getText()));
        txtDni.addActionListener(e -> cargarHistoriales(txtDni.getText()));
        txtDni.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { if(txtDni.getText().trim().isEmpty()) cargarHistoriales(""); }
        });

        btnVerDetalle.addActionListener(e -> abrirFormulario(false)); // Solo ver
        btnModificar.addActionListener(e -> abrirFormulario(true));  // Editar

        cargarHistoriales("");
    }

    private void cargarHistoriales(String dni) {
        modelo.setRowCount(0);
        try {
            listaCache = historialDAO.buscarHistorial(dni);
            for (HistorialClinico h : listaCache) {
                modelo.addRow(new Object[]{
                    h.getIdHistorial(),
                    h.getDniPaciente(),
                    h.getNombrePaciente(),
                    h.getAlergias(),           // Debería mostrarse si no es null
                    h.getCondicionesCronicas() // Debería mostrarse si no es null
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void abrirFormulario(boolean modoEdicion) {
        int row = tabla.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un paciente de la tabla.");
            return;
        }

        int idHistorial = (int) modelo.getValueAt(row, 0);
        HistorialClinico h = listaCache.stream().filter(x -> x.getIdHistorial() == idHistorial).findFirst().orElse(null);
        if(h == null) return;

        String titulo = modoEdicion ? "Modificar Historial: " : "Detalle Historial: ";
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), titulo + h.getNombrePaciente(), true);
        dialog.setSize(500, 600);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 1, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        form.setBackground(Color.WHITE);

        // Inicializamos con los datos existentes (o vacíos si es null)
        JTextArea txtAlergias = new JTextArea(h.getAlergias() != null ? h.getAlergias() : "");
        JTextArea txtCond = new JTextArea(h.getCondicionesCronicas() != null ? h.getCondicionesCronicas() : "");
        JTextArea txtMed = new JTextArea(h.getMedicamentosActuales() != null ? h.getMedicamentosActuales() : "");
        JTextArea txtObs = new JTextArea(h.getObservacionesGenerales() != null ? h.getObservacionesGenerales() : "");

        // AQUÍ ESTÁ LA CLAVE: Habilitar o deshabilitar edición
        txtAlergias.setEditable(modoEdicion);
        txtCond.setEditable(modoEdicion);
        txtMed.setEditable(modoEdicion);
        txtObs.setEditable(modoEdicion);

        form.add(crearCampo("Alergias:", txtAlergias));
        form.add(crearCampo("Condiciones Crónicas:", txtCond));
        form.add(crearCampo("Medicamentos Actuales:", txtMed));
        form.add(crearCampo("Observaciones Generales:", txtObs));

        dialog.add(form, BorderLayout.CENTER);

        if (modoEdicion) {
            JButton btnGuardar = new JButton("Guardar Cambios");
            Estilos.aplicarEstiloBoton(btnGuardar);
            btnGuardar.addActionListener(e -> {
                try {
                    // Actualizamos el objeto en memoria
                    h.setAlergias(txtAlergias.getText());
                    h.setCondicionesCronicas(txtCond.getText());
                    h.setMedicamentosActuales(txtMed.getText());
                    h.setObservacionesGenerales(txtObs.getText());

                    // Enviamos a DB
                    historialDAO.actualizarHistorial(h);
                    
                    JOptionPane.showMessageDialog(dialog, "Historial actualizado.");
                    dialog.dispose();
                    cargarHistoriales(txtDni.getText()); // Refrescar tabla
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error al guardar: " + ex.getMessage());
                }
            });
            dialog.add(btnGuardar, BorderLayout.SOUTH);
        }

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel crearCampo(String titulo, JTextArea area) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.add(new JLabel(titulo), BorderLayout.NORTH);
        area.setLineWrap(true);
        area.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }
}