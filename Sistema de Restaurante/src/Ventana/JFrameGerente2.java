package Ventana;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class JFrameGerente2 extends JFrame {
    private JTextField txtNombre, txtDescripcion, txtPrecio, txtStock;
    private JTable tablaPlatillos;
    private DefaultTableModel modeloTabla;
    private JButton btnRegistrar, btnActualizar, btnEliminar, btnLimpiar, btnCerrarSesion;
    private int paginaActual = 1;
    private int registrosPorPagina = 15;
    private JPanel panelPaginacion;

    private JTextField txtBuscar = new JTextField(50);
    private String filtroBusqueda = "";

    public JFrameGerente2() {
        setTitle("Gestión de Platillos");
        setSize(950, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        Color colorHeader = new Color(46, 134, 222);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        header.setBackground(colorHeader);
        JLabel titulo = new JLabel("Platillos");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);
        header.add(titulo);
        add(header, BorderLayout.NORTH);

        // Formulario lateral
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Platillo"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNombre = new JTextField(15);
        txtDescripcion = new JTextField(15);
        txtPrecio = new JTextField(15);
        txtStock = new JTextField(15);

        String[] labels = {"Nombre:", "Descripción:", "Precio:", "Stock:"};
        JTextField[] fields = {txtNombre, txtDescripcion, txtPrecio, txtStock};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            panelFormulario.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            panelFormulario.add(fields[i], gbc);
        }

        add(panelFormulario, BorderLayout.WEST);

        // Buscador
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.add(new JLabel("Buscar:"));
        panelBusqueda.add(txtBuscar);

        // Tabla
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre", "Descripción", "Precio", "Stock"}, 0);
        tablaPlatillos = new JTable(modeloTabla);
        JScrollPane scroll = new JScrollPane(tablaPlatillos);
        scroll.setBorder(BorderFactory.createTitledBorder("Platillos"));

        // Paginación
        panelPaginacion = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Contenedor de búsqueda, tabla y paginación
        JPanel panelTablaYPaginacion = new JPanel(new BorderLayout());
        panelTablaYPaginacion.add(panelBusqueda, BorderLayout.NORTH);
        panelTablaYPaginacion.add(scroll, BorderLayout.CENTER);
        panelTablaYPaginacion.add(panelPaginacion, BorderLayout.SOUTH);
        add(panelTablaYPaginacion, BorderLayout.CENTER);
        
 

        // Buscador en tiempo real
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                actualizarFiltro();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                actualizarFiltro();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                actualizarFiltro();
            }

            private void actualizarFiltro() {
                filtroBusqueda = txtBuscar.getText().trim();
                paginaActual = 1;
                cargarPlatillos();
            }
        });

        // Panel Sur: Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
       btnRegistrar = estiloBoton("Registrar", new Color(46, 204, 113), Color.WHITE);
btnActualizar = estiloBoton("Actualizar", new Color(241, 196, 15), Color.BLACK);
btnEliminar = estiloBoton("Eliminar", new Color(231, 76, 60), Color.WHITE);
btnLimpiar = estiloBoton("Limpiar", new Color(155, 89, 182), Color.WHITE);
btnCerrarSesion = estiloBoton("Cerrar sesión", new Color(52, 73, 94), Color.WHITE);


        panelBotones.add(btnRegistrar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnCerrarSesion);

        add(panelBotones, BorderLayout.SOUTH);
        JButton btnRegresar = estiloBoton("Regresar", new Color(52, 73, 94), Color.WHITE);
panelBotones.add(btnRegresar);

        btnRegresar.addActionListener(e -> {
    dispose();
    new VentanaPrincipal().setVisible(true);
});


        // Eventos
        btnRegistrar.addActionListener(e -> registrarPlatillo());
        btnActualizar.addActionListener(e -> actualizarPlatillo());
        btnEliminar.addActionListener(e -> eliminarPlatillo());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnCerrarSesion.addActionListener(e -> {
            dispose();
            new FormularioInicioSesion().setVisible(true);
        });

        tablaPlatillos.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaPlatillos.getSelectedRow();
            if (fila >= 0) {
                txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
                txtDescripcion.setText(modeloTabla.getValueAt(fila, 2).toString());
                txtPrecio.setText(modeloTabla.getValueAt(fila, 3).toString());
                txtStock.setText(modeloTabla.getValueAt(fila, 4).toString());
            }
        });

        cargarPlatillos();
        setVisible(true);
    }

    private void cargarPlatillos() {
        modeloTabla.setRowCount(0);
        int offset = (paginaActual - 1) * registrosPorPagina;

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM platillos WHERE nombre LIKE ? LIMIT ? OFFSET ?")) {

            ps.setString(1, "%" + filtroBusqueda + "%");
            ps.setInt(2, registrosPorPagina);
            ps.setInt(3, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modeloTabla.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        rs.getInt("stock")
                });
            }

            generarBotonesPaginacion();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar platillos: " + e.getMessage());
        }
    }

    private void generarBotonesPaginacion() {
        panelPaginacion.removeAll();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM platillos WHERE nombre LIKE ?")) {

            ps.setString(1, "%" + filtroBusqueda + "%");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int totalRegistros = rs.getInt(1);
                int totalPaginas = (int) Math.ceil((double) totalRegistros / registrosPorPagina);

                for (int i = 1; i <= totalPaginas; i++) {
                    JButton btnPagina = new JButton(String.valueOf(i));
                    int numero = i;
                    btnPagina.addActionListener(e -> {
                        paginaActual = numero;
                        cargarPlatillos();
                    });

                    if (i == paginaActual) {
                        btnPagina.setEnabled(false);
                    }

                    panelPaginacion.add(btnPagina);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al generar paginación: " + e.getMessage());
        }
        panelPaginacion.revalidate();
        panelPaginacion.repaint();
    }

    private void registrarPlatillo() {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        double precio;
        int stock;

        try {
            precio = Double.parseDouble(txtPrecio.getText().trim());
            stock = Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Precio o stock inválido.");
            return;
        }

        try (Connection con = ConexionBD.conectar()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO platillos (nombre, descripcion, precio, stock) VALUES (?, ?, ?, ?)");
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setDouble(3, precio);
            ps.setInt(4, stock);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Platillo registrado.");
            cargarPlatillos();
            limpiarCampos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar: " + e.getMessage());
        }
    }

    private void actualizarPlatillo() {
        int fila = tablaPlatillos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un platillo para actualizar.");
            return;
        }

        int id = (int) modeloTabla.getValueAt(fila, 0);
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        double precio;
        int stock;

        try {
            precio = Double.parseDouble(txtPrecio.getText().trim());
            stock = Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Precio o stock inválido.");
            return;
        }

        try (Connection con = ConexionBD.conectar()) {
            PreparedStatement ps = con.prepareStatement("UPDATE platillos SET nombre=?, descripcion=?, precio=?, stock=? WHERE id=?");
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setDouble(3, precio);
            ps.setInt(4, stock);
            ps.setInt(5, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Platillo actualizado.");
            cargarPlatillos();
            limpiarCampos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage());
        }
    }

    private void eliminarPlatillo() {
        int fila = tablaPlatillos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un platillo para eliminar.");
            return;
        }

        int id = (int) modeloTabla.getValueAt(fila, 0);
        try (Connection con = ConexionBD.conectar()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM platillos WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Platillo eliminado.");
            cargarPlatillos();
            limpiarCampos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        tablaPlatillos.clearSelection();
    }

     private JButton estiloBoton(String texto, Color fondo, Color textoColor) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setBackground(fondo);
        boton.setForeground(textoColor);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        return boton;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(JFrameGerente2::new);
    }
}
