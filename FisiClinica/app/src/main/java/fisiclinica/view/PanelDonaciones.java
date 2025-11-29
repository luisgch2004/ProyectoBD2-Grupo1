package fisiclinica.view;

import com.toedter.calendar.JDateChooser;
import fisiclinica.dao.DonacionDAO;
import fisiclinica.model.Donacion;
import fisiclinica.util.Estilos;
import fisiclinica.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PanelDonaciones extends JPanel {
    private JTable tabla;
    private DefaultTableModel modelo;
    private DonacionDAO donacionDAO;
    private JDateChooser dateInicio;
    private JDateChooser dateFin;
    private List<Donacion> listaActual;

    public PanelDonaciones() {
        donacionDAO = new DonacionDAO();
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // --- PANEL SUPERIOR ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);

        // Fechas por defecto para el filtro (Inicio de mes - Hoy)
        Calendar cal = Calendar.getInstance();
        Date fechaHoy = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date fechaInicioMes = cal.getTime();

        JLabel lblDesde = new JLabel("Desde:");
        lblDesde.setForeground(Estilos.TEXTO_NEGRO);
        dateInicio = new JDateChooser(fechaInicioMes);
        dateInicio.setPreferredSize(new Dimension(130, 30));
        dateInicio.setDateFormatString("yyyy-MM-dd");

        JLabel lblHasta = new JLabel("Hasta:");
        lblHasta.setForeground(Estilos.TEXTO_NEGRO);
        dateFin = new JDateChooser(fechaHoy);
        dateFin.setPreferredSize(new Dimension(130, 30));
        dateFin.setDateFormatString("yyyy-MM-dd");

        JButton btnBuscar = new JButton("Filtrar Fechas");
        Estilos.aplicarEstiloBoton(btnBuscar);

        JButton btnNueva = new JButton("+ Registrar Donación");
        Estilos.aplicarEstiloBoton(btnNueva);

        JButton btnExportar = new JButton("Exportar Reporte");
        Estilos.aplicarEstiloBoton(btnExportar);
        btnExportar.setBackground(new Color(0, 153, 76));

        topPanel.add(lblDesde);
        topPanel.add(dateInicio);
        topPanel.add(lblHasta);
        topPanel.add(dateFin);
        topPanel.add(btnBuscar);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(btnNueva);
        topPanel.add(btnExportar);

        add(topPanel, BorderLayout.NORTH);

        // --- TABLA ---
        String[] columnas = {"ID", "Donante", "Tipo", "Fecha", "Monto", "Moneda", "Destino", "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(25);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // --- EVENTOS ---
        btnBuscar.addActionListener(e -> cargarDonaciones());
        btnNueva.addActionListener(e -> registrarDonacion());
        btnExportar.addActionListener(e -> exportarReporte());

        // Carga inicial
        cargarDonaciones();
    }

    private void registrarDonacion() {
        JTextField txtDonante = new JTextField();
        JTextField txtDni = new JTextField();
        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"MONETARIA", "EQUIPOS", "MEDICAMENTOS"});
        
        // Componente: Fecha de Registro
        JDateChooser dateRegistro = new JDateChooser(new Date()); 
        dateRegistro.setDateFormatString("yyyy-MM-dd");
        
        // Componente: Moneda
        JComboBox<String> cmbMoneda = new JComboBox<>(new String[]{"USD", "PEN", "EUR"});
        
        JTextField txtMonto = new JTextField("0.0");
        JTextArea txtDesc = new JTextArea(3, 20);
        
        Object[] msg = {
            "Nombre Donante:", txtDonante,
            "DNI/RUC:", txtDni,
            "Tipo:", cmbTipo,
            "Fecha Donación:", dateRegistro,
            "Moneda:", cmbMoneda,
            "Monto (si aplica):", txtMonto,
            "Descripción:", new JScrollPane(txtDesc)
        };

        int op = JOptionPane.showConfirmDialog(this, msg, "Registrar Donación", JOptionPane.OK_CANCEL_OPTION);
        
        if(op == JOptionPane.OK_OPTION) {
            try {
                // Validación básica
                if(dateRegistro.getDate() == null) {
                    JOptionPane.showMessageDialog(this, "Debe seleccionar una fecha.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Donacion d = new Donacion();
                d.setNombreDonante(txtDonante.getText());
                d.setDniDonante(txtDni.getText());
                d.setTipoDonacion((String) cmbTipo.getSelectedItem());
                d.setFechaDonacion((java.sql.Date) dateRegistro.getDate());
                d.setMonto(Double.parseDouble(txtMonto.getText()));
                d.setMoneda((String) cmbMoneda.getSelectedItem());
                d.setDescripcion(txtDesc.getText());
                
                // Valores por defecto
                d.setIdAreaDestino(1); // ID 1 = Administración
                d.setIdUsuarioRegistro(UserSession.getInstance().getUsuario().getIdUsuario());
                
                donacionDAO.registrarDonacion(d);
                
                JOptionPane.showMessageDialog(this, "Donación registrada correctamente.");
                
                // ACTUALIZACIÓN INTELIGENTE DE LA VISTA:
                // Si la fecha registrada está fuera del filtro actual, ampliamos el filtro para ver la nueva donación.
                if (dateRegistro.getDate().after(dateFin.getDate())) {
                    dateFin.setDate(dateRegistro.getDate());
                }
                cargarDonaciones();
                
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Error: El monto debe ser un número.", "Error Formato", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error BD: " + ex.getMessage());
            }
        }
    }

    private void cargarDonaciones() {
        modelo.setRowCount(0);
        try {
            if(dateInicio.getDate() == null || dateFin.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Seleccione fechas válidas para filtrar.");
                return;
            }
            
            if (dateInicio.getDate().after(dateFin.getDate())) {
                JOptionPane.showMessageDialog(this, "La fecha de inicio no puede ser mayor a la final.", "Error Fechas", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            listaActual = donacionDAO.listarDonaciones(dateInicio.getDate(), dateFin.getDate());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            for (Donacion d : listaActual) {
                Object[] fila = {
                    d.getIdDonacion(),
                    d.getNombreDonante(),
                    d.getTipoDonacion(),
                    sdf.format(d.getFechaDonacion()),
                    d.getMonto(),
                    d.getMoneda(),
                    d.getNombreAreaDestino(),
                    d.getEstado()
                };
                modelo.addRow(fila);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void exportarReporte() {
        try {
            if(listaActual == null || listaActual.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay datos en la tabla para exportar.");
                return;
            }
            
            // Guardar en la carpeta resources del proyecto
            String ruta = System.getProperty("user.dir") + "/src/main/resources/reporte_donaciones.csv";
            
            PrintWriter pw = new PrintWriter(new FileWriter(ruta));
            pw.println("ID,Donante,Tipo,Fecha,Monto,Moneda,Estado");
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            for(Donacion d : listaActual) {
                pw.println(d.getIdDonacion() + "," + 
                           d.getNombreDonante() + "," + 
                           d.getTipoDonacion() + "," + 
                           sdf.format(d.getFechaDonacion()) + "," + 
                           d.getMonto() + "," + 
                           d.getMoneda() + "," + 
                           d.getEstado());
            }
            pw.close();
            JOptionPane.showMessageDialog(this, "Reporte exportado exitosamente en:\n" + ruta);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + e.getMessage());
        }
    }
}