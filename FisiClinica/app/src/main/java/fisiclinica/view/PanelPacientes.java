package fisiclinica.view;

import com.toedter.calendar.JDateChooser;

import fisiclinica.dao.ConsultaDAO;
import fisiclinica.dao.PacienteDAO;
import fisiclinica.model.ConsultaMedica;
import fisiclinica.model.Paciente;
import fisiclinica.util.Estilos;
import fisiclinica.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PanelPacientes extends JPanel {
    private JTable tabla;
    private DefaultTableModel modelo;
    private PacienteDAO pacienteDAO;
    private List<Paciente> listaPacientesCache;
    private JTextField txtBuscar;

    public PanelPacientes() {
        pacienteDAO = new PacienteDAO();
        setLayout(new BorderLayout());
        initUI();
        cargarDatos();
    }

    private void initUI() {
        // --- TOOLBAR ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(Color.WHITE);

        txtBuscar = new JTextField(15);
        JButton btnBuscar = new JButton("Filtrar");
        Estilos.aplicarEstiloBoton(btnBuscar);

        JButton btnNuevo = new JButton("Nuevo Paciente");
        Estilos.aplicarEstiloBoton(btnNuevo);
        
        JButton btnEditar = new JButton("Editar");
        Estilos.aplicarEstiloBoton(btnEditar);

        JButton btnAtender = new JButton("ATENDER CONSULTA");
        Estilos.aplicarEstiloBoton(btnAtender);
        btnAtender.setBackground(new Color(0, 153, 76)); // Verde

        toolbar.add(new JLabel("Buscar (DNI/Nombre):"));
        toolbar.add(txtBuscar);
        toolbar.add(btnBuscar);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(btnNuevo);
        toolbar.add(btnEditar);
        toolbar.add(btnAtender);

        add(toolbar, BorderLayout.NORTH);

        // --- TABLA ---
        String[] columnas = {"ID", "DNI", "Nombre", "Apellido", "Teléfono", "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(25);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --- EVENTOS ---
        btnNuevo.addActionListener(e -> mostrarFormulario(null));
        btnEditar.addActionListener(e -> editarPacienteSeleccionado());
        btnAtender.addActionListener(e -> atenderPacienteSeleccionado());
        
        btnBuscar.addActionListener(e -> filtrarDatos(txtBuscar.getText()));
        
        // Búsqueda en tiempo real
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filtrarDatos(txtBuscar.getText());
            }
        });
    }

    private void editarPacienteSeleccionado() {
        int row = tabla.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un paciente de la lista para editar.");
            return;
        }
        
        // Obtenemos el objeto real desde la lista caché usando el ID
        int idPaciente = (int) modelo.getValueAt(row, 0);
        Paciente paciente = listaPacientesCache.stream()
                .filter(p -> p.getIdPaciente() == idPaciente)
                .findFirst().orElse(null);
        
        if(paciente != null) {
            mostrarFormulario(paciente);
        }
    }

    private void mostrarFormulario(Paciente pacienteEditar) {
        boolean esEdicion = (pacienteEditar != null);
        String titulo = esEdicion ? "Editar Paciente" : "Nuevo Paciente";
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), titulo, true);
        dialog.setSize(450, 500);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.setLocationRelativeTo(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- CAMPOS DEL FORMULARIO ---
        JTextField txtDni = new JTextField(esEdicion ? pacienteEditar.getDni() : "");
        JTextField txtNombre = new JTextField(esEdicion ? pacienteEditar.getNombre() : "");
        JTextField txtApellido = new JTextField(esEdicion ? pacienteEditar.getApellido() : "");
        
        // Selector de fecha de nacimiento
        JDateChooser dateNacimiento = new JDateChooser();
        dateNacimiento.setDateFormatString("yyyy-MM-dd");
        if(esEdicion && pacienteEditar.getFechaNacimiento() != null) {
            dateNacimiento.setDate(pacienteEditar.getFechaNacimiento());
        } else {
            dateNacimiento.setDate(new Date()); // Fecha hoy por defecto
        }
        
        JComboBox<String> cmbGenero = new JComboBox<>(new String[]{"MASCULINO", "FEMENINO", "OTRO"});
        if(esEdicion && pacienteEditar.getGenero() != null) {
            cmbGenero.setSelectedItem(pacienteEditar.getGenero());
        }
        
        JTextField txtTelefono = new JTextField(esEdicion ? pacienteEditar.getTelefono() : "");
        JTextField txtDireccion = new JTextField(esEdicion ? pacienteEditar.getDireccion() : "");
        JTextField txtEmail = new JTextField(esEdicion ? pacienteEditar.getEmail() : "");

        // REGLA: El DNI es único, no se debe editar. El resto SÍ se puede editar.
        if(esEdicion) {
            txtDni.setEditable(false);
            txtDni.setBackground(new Color(230, 230, 230));
        }

        // Construcción visual
        int y = 0;
        agregarCampo(dialog, "DNI:", txtDni, y++, gbc);
        agregarCampo(dialog, "Nombre:", txtNombre, y++, gbc);
        agregarCampo(dialog, "Apellido:", txtApellido, y++, gbc);
        agregarCampo(dialog, "Nacimiento:", dateNacimiento, y++, gbc);
        agregarCampo(dialog, "Género:", cmbGenero, y++, gbc);
        agregarCampo(dialog, "Teléfono:", txtTelefono, y++, gbc);
        agregarCampo(dialog, "Dirección:", txtDireccion, y++, gbc);
        agregarCampo(dialog, "Email:", txtEmail, y++, gbc);

        JButton btnGuardar = new JButton("Guardar Datos");
        Estilos.aplicarEstiloBoton(btnGuardar);
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.insets = new Insets(15, 5, 5, 5);
        dialog.add(btnGuardar, gbc);

        // --- LÓGICA DE GUARDADO ---
        btnGuardar.addActionListener(e -> {
            try {
                // Validaciones
                if(txtDni.getText().trim().isEmpty() || txtNombre.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "El DNI y el Nombre son obligatorios.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if(dateNacimiento.getDate() == null) {
                    JOptionPane.showMessageDialog(dialog, "Seleccione una fecha de nacimiento.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (esEdicion) {
                    // Actualizar objeto existente
                    pacienteEditar.setNombre(txtNombre.getText());
                    pacienteEditar.setApellido(txtApellido.getText());
                    pacienteEditar.setFechaNacimiento(dateNacimiento.getDate());
                    pacienteEditar.setGenero(cmbGenero.getSelectedItem().toString());
                    pacienteEditar.setTelefono(txtTelefono.getText());
                    pacienteEditar.setDireccion(txtDireccion.getText());
                    pacienteEditar.setEmail(txtEmail.getText());
                    
                    // Llamar al DAO
                    pacienteDAO.actualizarPaciente(pacienteEditar);
                    JOptionPane.showMessageDialog(dialog, "Paciente actualizado correctamente.");
                } else {
                    // Crear nuevo objeto
                    Paciente nuevo = new Paciente();
                    nuevo.setDni(txtDni.getText());
                    nuevo.setNombre(txtNombre.getText());
                    nuevo.setApellido(txtApellido.getText());
                    nuevo.setFechaNacimiento(dateNacimiento.getDate());
                    nuevo.setGenero(cmbGenero.getSelectedItem().toString());
                    nuevo.setTelefono(txtTelefono.getText());
                    nuevo.setDireccion(txtDireccion.getText());
                    nuevo.setEmail(txtEmail.getText());
                    
                    int idUser = UserSession.getInstance().getUsuario().getIdUsuario();
                    pacienteDAO.registrarPaciente(nuevo, idUser);
                    JOptionPane.showMessageDialog(dialog, "Paciente registrado con éxito.");
                }
                
                dialog.dispose();
                cargarDatos(); // Refrescar la tabla
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error en base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private void agregarCampo(JDialog d, String lbl, JComponent cmp, int y, GridBagConstraints gbc) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; 
        JLabel label = new JLabel(lbl);
        label.setForeground(Estilos.TEXTO_NEGRO);
        d.add(label, gbc);
        
        gbc.gridx = 1; 
        d.add(cmp, gbc);
    }

    private void cargarDatos() {
        try {
            listaPacientesCache = pacienteDAO.listarPacientes();
            filtrarDatos(txtBuscar.getText());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void filtrarDatos(String texto) {
        modelo.setRowCount(0);
        if (listaPacientesCache == null) return;

        String filtro = texto.toLowerCase();
        List<Paciente> filtrados = listaPacientesCache.stream()
            .filter(p -> p.getDni().contains(filtro) || 
                         p.getNombre().toLowerCase().contains(filtro) || 
                         p.getApellido().toLowerCase().contains(filtro))
            .collect(Collectors.toList());

        for (Paciente p : filtrados) {
            modelo.addRow(new Object[]{p.getIdPaciente(), p.getDni(), p.getNombre(), p.getApellido(), p.getTelefono(), p.getEstado()});
        }
    }
    
    private void atenderPacienteSeleccionado() {
        int row = tabla.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un paciente de la tabla para iniciar la consulta.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener datos de la fila seleccionada
        int idPaciente = (int) modelo.getValueAt(row, 0);
        String nombreCompleto = modelo.getValueAt(row, 2) + " " + modelo.getValueAt(row, 3);
        String dni = (String) modelo.getValueAt(row, 1);

        // Llamar al método que abre el diálogo
        mostrarDialogoConsulta(idPaciente, nombreCompleto, dni);
    }

    private void mostrarDialogoConsulta(int idPaciente, String nombrePaciente, String dni) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nueva Consulta - " + nombrePaciente, true);
        dialog.setSize(600, 500);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Header Informativo
        JLabel lblInfo = new JLabel("Paciente: " + nombrePaciente + " (DNI: " + dni + ")");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblInfo.setForeground(new Color(0, 102, 204));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(lblInfo, gbc);

        // Campos
        JTextArea txtMotivo = new JTextArea(3, 20);
        JTextArea txtSintomas = new JTextArea(3, 20);
        JTextArea txtDiagnostico = new JTextArea(3, 20);
        JTextArea txtTratamiento = new JTextArea(3, 20);
        JTextArea txtObservaciones = new JTextArea(3, 20);

        // Bordes para los TextAreas
        txtMotivo.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtSintomas.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtDiagnostico.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtTratamiento.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtObservaciones.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        int y = 1;
        addArea(form, "Motivo Consulta (*):", txtMotivo, y++, gbc);
        addArea(form, "Síntomas:", txtSintomas, y++, gbc);
        addArea(form, "Diagnóstico (*):", txtDiagnostico, y++, gbc);
        addArea(form, "Tratamiento:", txtTratamiento, y++, gbc);
        addArea(form, "Observaciones:", txtObservaciones, y++, gbc);

        JButton btnGuardar = new JButton("Guardar e Iniciar (EN PROGRESO)");
        Estilos.aplicarEstiloBoton(btnGuardar);
        btnGuardar.setBackground(new Color(0, 153, 76)); // Verde

        btnGuardar.addActionListener(e -> {
            if(txtMotivo.getText().trim().isEmpty() || txtDiagnostico.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(dialog, "Motivo y Diagnóstico son obligatorios.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                ConsultaMedica consulta = new ConsultaMedica();
                consulta.setIdPaciente(idPaciente);
                consulta.setIdMedico(UserSession.getInstance().getUsuario().getIdUsuario());
                consulta.setMotivoConsulta(txtMotivo.getText());
                consulta.setSintomas(txtSintomas.getText());
                consulta.setDiagnostico(txtDiagnostico.getText());
                consulta.setTratamientoPrescrito(txtTratamiento.getText());
                consulta.setObservaciones(txtObservaciones.getText());
                
                // El DAO se encargará de poner el estado EN_PROCESO
                ConsultaDAO dao = new ConsultaDAO();
                int id = dao.registrarConsulta(consulta);
                
                if(id > 0) {
                    JOptionPane.showMessageDialog(dialog, "Consulta registrada con ID: " + id);
                    dialog.dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnGuardar);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true); // ¡IMPORTANTE PARA QUE SE VEA!
    }

    private void addArea(JPanel p, String lbl, JTextArea area, int y, GridBagConstraints gbc) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; gbc.weightx = 0;
        p.add(new JLabel(lbl), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        area.setLineWrap(true);
        p.add(area, gbc);
    }
}