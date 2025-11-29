package fisiclinica.view;

import fisiclinica.dao.MedicamentoDAO;
import fisiclinica.model.Medicamento;
import fisiclinica.util.Estilos;
import fisiclinica.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelMedicamentos extends JPanel {
    private JTable tabla;
    private DefaultTableModel modelo;
    private MedicamentoDAO dao;
    private JTextField txtBuscar;
    
    // --- VARIABLES DE ESTADO ---
    // Controla si estamos filtrando o viendo todo
    private boolean mostrandoStockBajo = false;
    // Referencia al botón para cambiar su color/texto dinámicamente
    private JButton btnAlertas; 
    // Almacena los objetos reales traídos de la BD (Source of Truth)
    private List<Medicamento> listaActual;

    public PanelMedicamentos() {
        dao = new MedicamentoDAO();
        listaActual = new ArrayList<>();
        setLayout(new BorderLayout());
        
        initUI();
        
        // Carga inicial de todos los datos
        buscar(""); 
    }

    private void initUI() {
        // --- TOOLBAR SUPERIOR ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(Color.WHITE);

        JLabel lblBuscar = new JLabel("Buscar (Nombre/Principio):");
        lblBuscar.setForeground(Estilos.TEXTO_NEGRO);
        
        txtBuscar = new JTextField(15);
        
        JButton btnBuscar = new JButton("Buscar");
        Estilos.aplicarEstiloBoton(btnBuscar);

        JButton btnNuevo = new JButton("Nuevo Medicamento");
        Estilos.aplicarEstiloBoton(btnNuevo);
        
        JButton btnEditar = new JButton("Editar");
        Estilos.aplicarEstiloBoton(btnEditar);
        
        // Inicializamos el botón ANTES de usarlo o agregarlo
        btnAlertas = new JButton("⚠ Ver Stock Bajo");
        Estilos.aplicarEstiloBoton(btnAlertas);
        btnAlertas.setBackground(new Color(255, 140, 0)); // Color Naranja inicial

        // Agregar componentes
        toolbar.add(lblBuscar);
        toolbar.add(txtBuscar);
        toolbar.add(btnBuscar);
        toolbar.add(Box.createHorizontalStrut(20)); // Espaciador
        toolbar.add(btnNuevo);
        toolbar.add(btnEditar);
        toolbar.add(btnAlertas);
        
        add(toolbar, BorderLayout.NORTH);

        // --- TABLA ---
        String[] cols = {"ID", "Nombre", "Principio Activo", "Stock", "Mínimo", "Precio"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(25);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // ScrollPane con fondo blanco por si la tabla está vacía
        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // --- EVENTOS ---
        
        // 1. Buscar: Resetea el modo alerta y busca por texto
        btnBuscar.addActionListener(e -> {
            mostrandoStockBajo = false; 
            configurarBotonAlerta(false); 
            buscar(txtBuscar.getText());
        });

        // 2. Alertas: Alterna entre ver todo y ver stock bajo
        btnAlertas.addActionListener(e -> toggleStockBajo());
        
        // 3. Nuevo y Editar
        btnNuevo.addActionListener(e -> nuevoMedicamento());
        btnEditar.addActionListener(e -> editarMedicamento());
        
        // 4. Búsqueda en tiempo real (solo si no estamos en modo alerta)
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if(!mostrandoStockBajo) {
                    buscar(txtBuscar.getText());
                }
            }
        });
    }

    /**
     * Busca medicamentos en la BD y actualiza la tabla.
     */
    private void buscar(String criterio) {
        try {
            listaActual = dao.buscarMedicamentos(criterio);
            llenarTabla();
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    /**
     * Lógica del interruptor para Stock Bajo.
     */
    private void toggleStockBajo() {
        // Invertimos el estado actual
        mostrandoStockBajo = !mostrandoStockBajo;

        if (mostrandoStockBajo) {
            // -- ACTIVAR MODO ALERTA --
            try {
                listaActual = dao.obtenerStockBajo();
                
                if(listaActual.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "¡Excelente! No hay medicamentos con stock crítico.");
                    // Si no hay nada, volvemos automáticamente al modo normal
                    mostrandoStockBajo = false;
                    buscar("");
                    return;
                }
                
                llenarTabla();
                configurarBotonAlerta(true); // Cambiar visualmente a "Ver Todos"
                
            } catch (Exception e) { 
                e.printStackTrace(); 
                JOptionPane.showMessageDialog(this, "Error al consultar stock bajo: " + e.getMessage());
            }
        } else {
            // -- VOLVER A MODO NORMAL --
            configurarBotonAlerta(false); // Volver a naranja
            txtBuscar.setText(""); // Limpiar caja de texto
            buscar(""); // Cargar todo
        }
    }
    
    /**
     * Cambia el color y texto del botón de alertas según el estado.
     */
    private void configurarBotonAlerta(boolean esModoAlerta) {
        if (btnAlertas == null) return; // Protección extra

        if (esModoAlerta) {
            btnAlertas.setText("Ver Todos");
            btnAlertas.setBackground(new Color(0, 153, 76)); // Verde
        } else {
            btnAlertas.setText("⚠ Ver Stock Bajo");
            btnAlertas.setBackground(new Color(255, 140, 0)); // Naranja
        }
    }

    /**
     * Vuelca el contenido de 'listaActual' en el modelo visual de la tabla.
     */
    private void llenarTabla() {
        modelo.setRowCount(0); // Limpiar visualmente
        for(Medicamento m : listaActual){
            modelo.addRow(new Object[]{
                m.getIdMedicamento(), 
                m.getNombre(), 
                m.getPrincipioActivo(),
                m.getStockActual(), 
                m.getStockMinimo(), 
                m.getPrecioUnitario()
            });
        }
    }

    private void editarMedicamento() {
        int row = tabla.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un medicamento de la lista para editar.");
            return;
        }
        
        // Obtenemos el objeto real de la lista, no de la tabla visual
        // Esto evita errores si la tabla tuviera formatos de moneda o texto
        Medicamento m = listaActual.get(row);

        JTextField txtNombre = new JTextField(m.getNombre());
        JTextField txtMin = new JTextField(String.valueOf(m.getStockMinimo()));
        JTextField txtPrecio = new JTextField(String.valueOf(m.getPrecioUnitario()));

        Object[] msg = {
            "Nombre:", txtNombre,
            "Stock Mínimo (Alerta):", txtMin,
            "Precio Unitario:", txtPrecio
        };

        int op = JOptionPane.showConfirmDialog(this, msg, "Editar Medicamento", JOptionPane.OK_CANCEL_OPTION);
        if (op == JOptionPane.OK_OPTION) {
            try {
                // Actualizamos los valores en el objeto
                m.setNombre(txtNombre.getText());
                m.setStockMinimo(Integer.parseInt(txtMin.getText()));
                m.setPrecioUnitario(Double.parseDouble(txtPrecio.getText()));
                
                // Enviamos a BD
                dao.actualizarMedicamento(m);
                
                // Refrescamos la vista inteligente
                if(mostrandoStockBajo) {
                    // Si estamos viendo stock bajo, recargamos esa lista
                    listaActual = dao.obtenerStockBajo();
                    if(listaActual.isEmpty()) {
                        // Si al editar ya no queda ninguno bajo, volvemos a la vista normal
                        mostrandoStockBajo = false;
                        configurarBotonAlerta(false);
                        buscar("");
                    } else {
                        llenarTabla();
                    }
                } else {
                    // Si estamos en vista normal, buscamos con el texto actual
                    buscar(txtBuscar.getText());
                }
                
                JOptionPane.showMessageDialog(this, "Medicamento actualizado correctamente.");
                
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Error: Ingrese valores numéricos válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
            }
        }
    }
    
    private void nuevoMedicamento() {
        JTextField txtNom = new JTextField();
        JTextField txtPrinc = new JTextField();
        JTextField txtStockMin = new JTextField();
        JTextField txtPrecio = new JTextField();
        
        Object[] message = {
            "Nombre:", txtNom,
            "Principio Activo:", txtPrinc,
            "Stock Mínimo:", txtStockMin,
            "Precio Unitario:", txtPrecio
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Nuevo Medicamento", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                // Validaciones básicas
                if (txtNom.getText().isEmpty() || txtStockMin.getText().isEmpty() || txtPrecio.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Por favor complete los campos obligatorios.");
                    return;
                }

                Medicamento m = new Medicamento();
                m.setNombre(txtNom.getText());
                m.setPrincipioActivo(txtPrinc.getText());
                m.setDescripcion("Registro manual");
                m.setConcentracion("N/A");
                m.setFormaFarmaceutica("Generico");
                m.setStockMinimo(Integer.parseInt(txtStockMin.getText()));
                m.setPrecioUnitario(Double.parseDouble(txtPrecio.getText()));
                
                int idUser = UserSession.getInstance().getUsuario().getIdUsuario();
                dao.registrarMedicamento(m, idUser);
                
                // Si agregamos uno nuevo, forzamos volver a la vista "Ver Todos" para que aparezca
                if (mostrandoStockBajo) {
                    mostrandoStockBajo = false;
                    configurarBotonAlerta(false);
                }
                buscar("");
                
                JOptionPane.showMessageDialog(this, "Medicamento registrado con éxito.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error: Stock y Precio deben ser números.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error de BD: " + ex.getMessage());
            }
        }
    }
}