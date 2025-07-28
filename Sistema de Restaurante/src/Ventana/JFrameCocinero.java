package Ventana;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class JFrameCocinero extends JFrame {

    private JTable tablaPedidos, tablaDetallePedido;
    private DefaultTableModel modeloPedidos, modeloDetalle;
    private JButton btnActualizarEstado, btnCerrarSesion;
    private int pedidoSeleccionado = -1;

    private final String[] estadosValidos = {"Pendiente", "En preparación", "Servido"};

    public JFrameCocinero() {
        setTitle("Panel Cocinero - Gestión de Pedidos");
        setSize(850, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 15));
        UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 15));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 15));

        // Panel superior con tabla de pedidos
        modeloPedidos = new DefaultTableModel(new String[]{"ID Pedido", "Mesa", "Estado"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPedidos = new JTable(modeloPedidos);
        tablaPedidos.setRowHeight(28);
        estilizarTabla(tablaPedidos);

        JScrollPane scrollPedidos = new JScrollPane(tablaPedidos);
        scrollPedidos.setPreferredSize(new Dimension(820, 200));
        scrollPedidos.setBorder(BorderFactory.createTitledBorder("Pedidos actuales"));

        // Panel central con detalle de pedido
        modeloDetalle = new DefaultTableModel(new String[]{"Platillo", "Cantidad"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaDetallePedido = new JTable(modeloDetalle);
        tablaDetallePedido.setRowHeight(28);
        estilizarTabla(tablaDetallePedido);

        JScrollPane scrollDetalle = new JScrollPane(tablaDetallePedido);
        scrollDetalle.setPreferredSize(new Dimension(820, 300));
        scrollDetalle.setBorder(BorderFactory.createTitledBorder("Detalles del pedido"));

        // Botones
        btnActualizarEstado = crearBoton("Avanzar Estado");
        btnCerrarSesion = crearBoton("Cerrar Sesión");

        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        panelBotones.setBackground(Color.decode("#f2f2f2"));
        panelBotones.add(btnActualizarEstado);
        panelBotones.add(btnCerrarSesion);

        // Agregar componentes
        add(scrollPedidos, BorderLayout.NORTH);
        add(scrollDetalle, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        cargarPedidos();

        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaPedidos.getSelectedRow();
                if (fila >= 0) {
                    pedidoSeleccionado = (int) modeloPedidos.getValueAt(fila, 0);
                    cargarDetallePedido(pedidoSeleccionado);
                }
            }
        });

        btnActualizarEstado.addActionListener(e -> {
            if (pedidoSeleccionado == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un pedido primero.");
                return;
            }
            try {
                avanzarEstadoPedido(pedidoSeleccionado);
                cargarPedidos();
                cargarDetallePedido(pedidoSeleccionado);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al actualizar estado: " + ex.getMessage());
            }
        });

        btnCerrarSesion.addActionListener(e -> {
            dispose();
            new FormularioInicioSesion().setVisible(true);
        });
    }

    private void cargarPedidos() {
        modeloPedidos.setRowCount(0);
        String sql = "SELECT id, mesa, estado FROM pedidos WHERE estado IN ('Pendiente', 'En preparación', 'Servido') ORDER BY id DESC";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modeloPedidos.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("mesa"),
                        rs.getString("estado")
                });
            }
            pedidoSeleccionado = -1;
            modeloDetalle.setRowCount(0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar pedidos: " + e.getMessage());
        }
    }

    private void cargarDetallePedido(int idPedido) {
        modeloDetalle.setRowCount(0);
        String sql = "SELECT p.nombre, dp.cantidad FROM detalles_pedido dp JOIN platillos p ON dp.id_platillo = p.id WHERE dp.id_pedido = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modeloDetalle.addRow(new Object[]{
                        rs.getString("nombre"),
                        rs.getInt("cantidad")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar detalles del pedido: " + e.getMessage());
        }
    }

    private void avanzarEstadoPedido(int idPedido) throws SQLException {
        String estadoActual = null;
        String sqlEstado = "SELECT estado FROM pedidos WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sqlEstado)) {
            ps.setInt(1, idPedido);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                estadoActual = rs.getString("estado");
            } else {
                throw new SQLException("Pedido no encontrado.");
            }
        }

        int idx = -1;
        for (int i = 0; i < estadosValidos.length; i++) {
            if (estadosValidos[i].equalsIgnoreCase(estadoActual)) {
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            throw new SQLException("Estado actual inválido o no modificable.");
        }
        if (idx == estadosValidos.length - 1) {
            JOptionPane.showMessageDialog(this, "El pedido ya está en estado '" + estadosValidos[idx] + "' y no se puede avanzar más.");
            return;
        }

        String nuevoEstado = estadosValidos[idx + 1];
        try (Connection con = ConexionBD.conectar();
             PreparedStatement psUpd = con.prepareStatement("UPDATE pedidos SET estado = ? WHERE id = ?")) {
            psUpd.setString(1, nuevoEstado);
            psUpd.setInt(2, idPedido);
            psUpd.executeUpdate();
            JOptionPane.showMessageDialog(this, "Estado actualizado a: " + nuevoEstado);
        }
    }

    private void estilizarTabla(JTable tabla) {
        JTableHeader header = tabla.getTableHeader();
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tabla.setDefaultRenderer(Object.class, centerRenderer);
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        boton.setBackground(new Color(52, 152, 219));
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JFrameCocinero().setVisible(true));
    }
}
