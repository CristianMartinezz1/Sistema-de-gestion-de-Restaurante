package Ventana;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.image.BufferedImage;


public class JFrameMesero extends JFrame {
    private JComboBox<String> comboMesa;
    private JTextField txtCantidad;
    private JTextField txtBuscarPlatillo;
    private JButton  btnEliminarPlatillo;
    private JTable tablaDetallePedido, tablaPedidos;
    private DefaultTableModel modeloDetallePedido,modeloPedidos;
    private JLabel lblTotalPedido;
    private int pedidoActual = -1; // -1 significa que aún no se ha creado el pedido
    private List<String> todosLosPlatillos = new ArrayList<>();
int filaAnteriorSeleccionada = -1;

private JTable tablaMenu, tablaCarrito;
private DefaultTableModel modeloMenu;
private JButton btnAgregarAlCarrito, btnCrearPedidoDesdeCarrito;


   public JFrameMesero() {
    setTitle("Mesero - Sistema de Pedidos");
    setSize(1350, 650);
    setLayout(null);
    JButton btnEliminarPedido = new JButton("Eliminar Pedido");
btnEliminarPedido.setBounds(860, 330, 180, 30);
add(btnEliminarPedido);
btnEliminarPedido.addActionListener(e -> {
    if (pedidoActual == -1) {
        JOptionPane.showMessageDialog(this, "Selecciona un pedido primero.");
        return;
    }

    // Confirmación del usuario
    int confirmacion = JOptionPane.showConfirmDialog(this,
        "¿Estás seguro de que deseas eliminar este pedido?",
        "Confirmar eliminación",
        JOptionPane.YES_NO_OPTION);

    if (confirmacion != JOptionPane.YES_OPTION) {
        return;
    }

    try (Connection con = ConexionBD.conectar()) {
        // Verificar si el pedido está pagado
        PreparedStatement psEstado = con.prepareStatement("SELECT estado FROM pedidos WHERE id = ?");
        psEstado.setInt(1, pedidoActual);
        ResultSet rsEstado = psEstado.executeQuery();

        if (rsEstado.next()) {
            String estado = rsEstado.getString("estado");
            if (estado.equalsIgnoreCase("Pagado")) {
                JOptionPane.showMessageDialog(this, "No se puede eliminar un pedido pagado.");
                return;
            }
        }

        // Recuperar platillos del pedido y devolver al stock
        PreparedStatement psDetalles = con.prepareStatement("SELECT id_platillo, cantidad FROM detalles_pedido WHERE id_pedido = ?");
        psDetalles.setInt(1, pedidoActual);
        ResultSet rsDetalles = psDetalles.executeQuery();

        while (rsDetalles.next()) {
            int idPlatillo = rsDetalles.getInt("id_platillo");
            int cantidad = rsDetalles.getInt("cantidad");

            PreparedStatement psActualizarStock = con.prepareStatement(
                "UPDATE platillos SET stock = stock + ? WHERE id = ?");
            psActualizarStock.setInt(1, cantidad);
            psActualizarStock.setInt(2, idPlatillo);
            psActualizarStock.executeUpdate();
        }

        // Eliminar los detalles del pedido
        PreparedStatement psEliminarDetalles = con.prepareStatement("DELETE FROM detalles_pedido WHERE id_pedido = ?");
        psEliminarDetalles.setInt(1, pedidoActual);
        psEliminarDetalles.executeUpdate();

        // Eliminar el pedido
        PreparedStatement psEliminarPedido = con.prepareStatement("DELETE FROM pedidos WHERE id = ?");
        psEliminarPedido.setInt(1, pedidoActual);
        int filasAfectadas = psEliminarPedido.executeUpdate();

        if (filasAfectadas > 0) {
            JOptionPane.showMessageDialog(this, "Pedido eliminado correctamente.");
            pedidoActual = -1;
            modeloDetallePedido.setRowCount(0);
            cargarPedidos();
            cargarMenuDisponible();
            cargarMesasDisponibles();
            lblTotalPedido.setText("Total del pedido: $0.00");
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar el pedido.");
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error al eliminar el pedido: " + ex.getMessage());
    }
});



    // Icono buscador
    try {
        BufferedImage imgBuscador = ImageIO.read(getClass().getResource("/Imagenes/Buscador.png"));
        ImageIcon iconoBuscador = new ImageIcon(imgBuscador.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        JLabel lblIconoBuscador = new JLabel(iconoBuscador);
        lblIconoBuscador.setBounds(30, 15, 32, 32);
        add(lblIconoBuscador);
    } catch (IOException e) {
        System.err.println("Error al cargar la imagen Buscador.png: " + e.getMessage());
    }

    JButton btnActualizarPedido = new JButton("Actualizar Pedido");
btnActualizarPedido.setBounds(660, 430, 180, 30);
add(btnActualizarPedido);

btnActualizarPedido.addActionListener(e -> {
    if (pedidoActual == -1) {
        JOptionPane.showMessageDialog(this, "Selecciona un pedido primero.");
        return;
    }

    String estado = null;
    for (int x = 0; x < modeloPedidos.getRowCount(); x++) {
        int id = (int) modeloPedidos.getValueAt(x, 0);
        if (id == pedidoActual) {
            estado = (String) modeloPedidos.getValueAt(x, 2);
            break;
        }
    }

    if (estado == null || estado.equalsIgnoreCase("Pagado")) {
        JOptionPane.showMessageDialog(this, "No puedes actualizar un pedido pagado.");
        return;
    }

    if (modeloDetallePedido.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "No hay platillos en el pedido para actualizar.");
        return;
    }

    try (Connection con = ConexionBD.conectar()) {
        con.setAutoCommit(false);

        for (int x = 0; x < modeloDetallePedido.getRowCount(); x++) {
            Object objIdDetalle = modeloDetallePedido.getValueAt(x, 0);
            String nombre = (String) modeloDetallePedido.getValueAt(x, 1);
            int nuevaCantidad = (int) modeloDetallePedido.getValueAt(x, 2);

            PreparedStatement psPlatillo = con.prepareStatement("SELECT id, stock FROM platillos WHERE nombre = ?");
            psPlatillo.setString(1, nombre);
            ResultSet rsPlatillo = psPlatillo.executeQuery();

            if (rsPlatillo.next()) {
                int idPlatillo = rsPlatillo.getInt("id");
                int stockActual = rsPlatillo.getInt("stock");

                if (objIdDetalle == null) {
                    // Platillo nuevo
                    PreparedStatement insert = con.prepareStatement(
                        "INSERT INTO detalles_pedido (id_pedido, id_platillo, cantidad) VALUES (?, ?, ?)");
                    insert.setInt(1, pedidoActual);
                    insert.setInt(2, idPlatillo);
                    insert.setInt(3, nuevaCantidad);
                    insert.executeUpdate();

                    PreparedStatement actualizarStock = con.prepareStatement(
                        "UPDATE platillos SET stock = stock - ? WHERE id = ?");
                    actualizarStock.setInt(1, nuevaCantidad);
                    actualizarStock.setInt(2, idPlatillo);
                    actualizarStock.executeUpdate();
                } else {
                    // Platillo ya existente → actualizar cantidad
                    int idDetalle = (int) objIdDetalle;

                    PreparedStatement psCantidadActual = con.prepareStatement(
                        "SELECT cantidad FROM detalles_pedido WHERE id = ?");
                    psCantidadActual.setInt(1, idDetalle);
                    ResultSet rsCantidad = psCantidadActual.executeQuery();

                    if (rsCantidad.next()) {
                        int cantidadBD = rsCantidad.getInt("cantidad");
                        int diferencia = nuevaCantidad - cantidadBD;

                        if (diferencia != 0) {
                            // Actualiza la cantidad en detalles_pedido
                            PreparedStatement psUpdateCantidad = con.prepareStatement(
                                "UPDATE detalles_pedido SET cantidad = ? WHERE id = ?");
                            psUpdateCantidad.setInt(1, nuevaCantidad);
                            psUpdateCantidad.setInt(2, idDetalle);
                            psUpdateCantidad.executeUpdate();

                            // Actualiza el stock del platillo
                            PreparedStatement psUpdateStock = con.prepareStatement(
                                "UPDATE platillos SET stock = stock - ? WHERE id = ?");
                            psUpdateStock.setInt(1, diferencia); // puede ser negativo
                            psUpdateStock.setInt(2, idPlatillo);
                            psUpdateStock.executeUpdate();
                        }
                    }
                }
            }
        }

        con.commit();
        JOptionPane.showMessageDialog(this, "Pedido actualizado correctamente.");
        cargarDetallePedido(pedidoActual);
        cargarMenuDisponible();

    } catch (SQLException ex) {
        try {
            JOptionPane.showMessageDialog(this, "Error: se hará rollback.");
            ex.printStackTrace();
        } catch (Exception rollbackEx) {
            rollbackEx.printStackTrace();
        }
    }
});


    // Etiqueta Buscar Platillo
    JLabel lblBuscarPlatillo = new JLabel("Buscar Platillo:");
    lblBuscarPlatillo.setBounds(70, 0, 150, 20);
    add(lblBuscarPlatillo);

    // Campo Buscar Platillo
    txtBuscarPlatillo = new JTextField();
    txtBuscarPlatillo.setBounds(70, 20, 470, 25);
    add(txtBuscarPlatillo);

    // Etiqueta Cantidad
  

    // Campo Cantidad
    txtCantidad = new JTextField();
   
    add(txtCantidad);

    // Botón Cerrar Sesión
    JButton btnCerrarSesion = new JButton("Cerrar Sesión");
    btnCerrarSesion.setBounds(660, 20, 120, 25);
    add(btnCerrarSesion);
    btnCerrarSesion.addActionListener(e -> {
        dispose();
        new FormularioInicioSesion().setVisible(true);
    });

    // Tabla Menú
    modeloMenu = new DefaultTableModel(new Object[]{"ID", "Platillo", "Precio", "Stock"}, 0);
    tablaMenu = new JTable(modeloMenu);
    JScrollPane scrollMenu = new JScrollPane(tablaMenu);
    scrollMenu.setBounds(30, 60, 750, 200);
    add(scrollMenu);

    // Tabla Carrito
    

    // Botón Agregar al Carrito
   btnAgregarAlCarrito = new JButton("Agregar al Carrito");
btnAgregarAlCarrito.setBounds(30, 430, 180, 30);
add(btnAgregarAlCarrito);

    // Botón Crear Pedido desde Carrito
    btnCrearPedidoDesdeCarrito = new JButton("Crear Pedido (Carrito)");
  btnCrearPedidoDesdeCarrito.setBounds(450, 430, 200, 30);
    add(btnCrearPedidoDesdeCarrito);

    // Combo Mesas
    comboMesa = new JComboBox<>();
    cargarMesasDisponibles();
    comboMesa.setBounds(30, 480, 100, 25);
    add(comboMesa);

    // Label Total Pedido
    lblTotalPedido = new JLabel("Total del pedido: $0.00");
    lblTotalPedido.setBounds(150, 480, 300, 25);
    lblTotalPedido.setFont(new Font("Arial", Font.BOLD, 16));
    add(lblTotalPedido);

    // Botones para agregar/crear/eliminar platillo
    
btnEliminarPlatillo = new JButton("Eliminar Platillo");
btnEliminarPlatillo.setBounds(240, 430, 180, 30); // ← alineado al medio
add(btnEliminarPlatillo);
btnEliminarPlatillo.addActionListener(e -> eliminarPlatilloDelPedido());


    // Tabla Historial de Pedidos
    modeloPedidos = new DefaultTableModel(new String[]{"ID Pedido", "Mesa", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    tablaPedidos = new JTable(modeloPedidos);
    JScrollPane scrollPedidos = new JScrollPane(tablaPedidos);
    scrollPedidos.setBounds(800, 60, 500, 260);
    add(scrollPedidos);
tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        int fila = tablaPedidos.getSelectedRow();
        if (fila >= 0) {
            int idPedidoSeleccionado = (int) modeloPedidos.getValueAt(fila, 0);
            pedidoActual = idPedidoSeleccionado; // actualizar variable global
            cargarDetallePedido(idPedidoSeleccionado);
        }
    }
});
tablaPedidos.addMouseListener(new java.awt.event.MouseAdapter() {
    @Override
    public void mousePressed(java.awt.event.MouseEvent evt) {
        int filaClic = tablaPedidos.rowAtPoint(evt.getPoint());

        if (filaClic == filaAnteriorSeleccionada) {
            // Si haces clic en la misma fila, deselecciona
            tablaPedidos.clearSelection();
            pedidoActual = -1;
            modeloDetallePedido.setRowCount(0);
            lblTotalPedido.setText("Total del pedido: $0.00");
            filaAnteriorSeleccionada = -1;
        } else {
            // Si haces clic en otra fila, actualiza la filaAnteriorSeleccionada
            filaAnteriorSeleccionada = filaClic;
        }
    }
});
    
    
    // Tabla Detalle del Pedido seleccionado (lado derecho debajo de pedidos)
    modeloDetallePedido = new DefaultTableModel(
            new String[]{"ID Detalle", "Platillo", "Cantidad", "Precio Unitario", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    tablaDetallePedido = new JTable(modeloDetallePedido);
    JScrollPane scrollDetalle = new JScrollPane(tablaDetallePedido);
    scrollDetalle.setBounds(30, 270, 750, 150);
    add(scrollDetalle);

   // Elimina cualquier listener anterior
for (ActionListener al : btnAgregarAlCarrito.getActionListeners()) {
    btnAgregarAlCarrito.removeActionListener(al);
}

// Agrega solo uno
btnAgregarAlCarrito.addActionListener(e -> {
    int fila = tablaMenu.getSelectedRow();
    if (fila >= 0) {
        String nombre = (String) modeloMenu.getValueAt(fila, 1);
        double precioUnitario = (double) modeloMenu.getValueAt(fila, 2);
        int stockActual = (int) modeloMenu.getValueAt(fila, 3);

        String cantidadStr = JOptionPane.showInputDialog(this, "Cantidad de " + nombre + ":");
        if (cantidadStr != null) {
            try {
                int cantidadNueva = Integer.parseInt(cantidadStr);
                if (cantidadNueva <= 0) {
                    JOptionPane.showMessageDialog(this, "Cantidad inválida.");
                    return;
                }

                if (cantidadNueva > stockActual) {
                    JOptionPane.showMessageDialog(this, "No hay suficiente stock. Disponible: " + stockActual);
                    return;
                }

                // Verificar si ya está en el carrito
                boolean encontrado = false;
                for (int x = 0; x < modeloDetallePedido.getRowCount(); x++) {
                    String platilloCarrito = (String) modeloDetallePedido.getValueAt(x, 1);
                    if (platilloCarrito.equals(nombre)) {
                        int cantidadExistente = (int) modeloDetallePedido.getValueAt(x, 2);
                        int nuevaCantidad = cantidadExistente + cantidadNueva;

                        if (nuevaCantidad > stockActual) {
                            JOptionPane.showMessageDialog(this, "No puedes agregar esa cantidad.\nYa en carrito: " + cantidadExistente + "\nStock disponible: " + stockActual);
                            return;
                        }

                        modeloDetallePedido.setValueAt(nuevaCantidad, x, 2); // Actualiza cantidad
                        double nuevoTotal = nuevaCantidad * precioUnitario;
                        modeloDetallePedido.setValueAt(nuevoTotal, x, 4); // Actualiza total
                        encontrado = true;
                        break;
                    }
                }

                if (!encontrado) {
                    double total = cantidadNueva * precioUnitario;
                    modeloDetallePedido.addRow(new Object[]{
                        null, nombre, cantidadNueva, precioUnitario, total
                    });
                }

                // Actualizar stock visualmente en tablaMenu
                modeloMenu.setValueAt(stockActual - cantidadNueva, fila, 3);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingresa un número válido.");
            }
        }
    } else {
        JOptionPane.showMessageDialog(this, "Selecciona un platillo del menú.");
    }
});

  

   btnCrearPedidoDesdeCarrito.addActionListener(e -> {
    if (modeloDetallePedido.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "No hay platillos agregados.");
        return;
    }

    String mesaSeleccionada = (String) comboMesa.getSelectedItem();
    if (mesaSeleccionada == null) {
        JOptionPane.showMessageDialog(this, "Selecciona una mesa.");
        return;
    }

    int mesa = Integer.parseInt(mesaSeleccionada.replace("Mesa ", "").trim());

    try (Connection conn = ConexionBD.conectar()) {
        conn.setAutoCommit(false);

        // Insertar nuevo pedido
        PreparedStatement stmtPedido = conn.prepareStatement(
            "INSERT INTO pedidos (mesa, estado) VALUES (?, 'Pendiente')",
            Statement.RETURN_GENERATED_KEYS
        );
        stmtPedido.setInt(1, mesa);
        stmtPedido.executeUpdate();

        ResultSet generatedKeys = stmtPedido.getGeneratedKeys();
        if (generatedKeys.next()) {
            int idPedido = generatedKeys.getInt(1);

            // Preparar sentencias para insertar detalle y actualizar stock
            PreparedStatement stmtDetalle = conn.prepareStatement(
                "INSERT INTO detalles_pedido (id_pedido, id_platillo, cantidad) VALUES (?, ?, ?)"
            );

            PreparedStatement stmtStock = conn.prepareStatement(
                "UPDATE platillos SET stock = stock - ? WHERE id = ?"
            );

            for (int x = 0; x < modeloDetallePedido.getRowCount(); x++) {
                String nombre = (String) modeloDetallePedido.getValueAt(x, 1); // Columna "Platillo"
                int cantidad = (int) modeloDetallePedido.getValueAt(x, 2);     // Columna "Cantidad"

                // Obtener ID del platillo
                PreparedStatement stmtPlatillo = conn.prepareStatement("SELECT id FROM platillos WHERE nombre = ?");
                stmtPlatillo.setString(1, nombre);
                ResultSet rsPlatillo = stmtPlatillo.executeQuery();

                if (rsPlatillo.next()) {
                    int idPlatillo = rsPlatillo.getInt("id");

                    // Insertar detalle del pedido
                    stmtDetalle.setInt(1, idPedido);
                    stmtDetalle.setInt(2, idPlatillo);
                    stmtDetalle.setInt(3, cantidad);
                    stmtDetalle.executeUpdate();

                    // Actualizar stock del platillo
                    stmtStock.setInt(1, cantidad);
                    stmtStock.setInt(2, idPlatillo);
                    stmtStock.executeUpdate();
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Pedido creado exitosamente.");

            modeloDetallePedido.setRowCount(0); // Limpiar la tabla usada como carrito
            cargarPedidos();
            cargarMenuDisponible();
            cargarMesasDisponibles();
        }

    } catch (SQLException ex) {
        ex.printStackTrace();
    }
});


   

  txtBuscarPlatillo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
    private void actualizarTabla() {
        String texto = txtBuscarPlatillo.getText().toLowerCase();
        filtrarMenu(texto);  // Solo filtra la tabla
    }

    @Override
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        actualizarTabla();
    }

    @Override
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        actualizarTabla();
    }

    @Override
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        actualizarTabla();
    }
});


    cargarPlatillos();
    cargarPedidos();
    cargarMenuDisponible();

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null); // Centrar ventana
    setVisible(true);
}
private void filtrarMenu(String texto) {
    modeloMenu.setRowCount(0); // Limpiar tabla
    try (Connection con = ConexionBD.conectar();
         PreparedStatement ps = con.prepareStatement(
             "SELECT id, nombre, precio, stock FROM platillos WHERE LOWER(nombre) LIKE ?")) {
        ps.setString(1, "%" + texto.toLowerCase() + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            modeloMenu.addRow(new Object[]{
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getDouble("precio"),
                rs.getInt("stock")
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al filtrar platillos: " + e.getMessage());
    }
}


private void cargarMenuDisponible() {
    modeloMenu.setRowCount(0);
    try (Connection conn = ConexionBD.conectar();
         PreparedStatement stmt = conn.prepareStatement("SELECT id, nombre, precio, stock FROM platillos");
         ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            int id = rs.getInt("id");
            String nombre = rs.getString("nombre");
            double precio = rs.getDouble("precio");
            int stock = rs.getInt("stock");
            modeloMenu.addRow(new Object[]{id, nombre, precio, stock});
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}




    private void cargarPlatillos() {
        todosLosPlatillos.clear();
        try (Connection con = ConexionBD.conectar();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nombre FROM platillos")) {
            while (rs.next()) {
                todosLosPlatillos.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar platillos: " + e.getMessage());
        }
    }

    private void cargarPedidos() {
        modeloPedidos.setRowCount(0);
        try (Connection con = ConexionBD.conectar();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, mesa, estado FROM pedidos ORDER BY id DESC")) {
            while (rs.next()) {
                modeloPedidos.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("mesa"),
                        rs.getString("estado")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar pedidos: " + e.getMessage());
        }
    }

    private void cargarDetallePedido(int idPedido) {
        modeloDetallePedido.setRowCount(0);
        double totalPedido = 0.0;
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT dp.id, p.nombre, dp.cantidad, p.precio " +
                             "FROM detalles_pedido dp " +
                             "JOIN platillos p ON dp.id_platillo = p.id WHERE dp.id_pedido = ?")) {
            ps.setInt(1, idPedido);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int idDetalle = rs.getInt(1);
                String nombrePlatillo = rs.getString(2);
                int cantidad = rs.getInt(3);
                double precioUnitario = rs.getDouble(4);
                double total = cantidad * precioUnitario;
                totalPedido += total;

                modeloDetallePedido.addRow(new Object[]{
                        idDetalle,
                        nombrePlatillo,
                        cantidad,
                        String.format("$%.2f", precioUnitario),
                        String.format("$%.2f", total)
                });
            }
            lblTotalPedido.setText("Total del pedido: $" + String.format("%.2f", totalPedido));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar detalles del pedido: " + e.getMessage());
        }
    }

    private void crearNuevoPedido() {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO pedidos(mesa, estado) VALUES (?, 'Pendiente')", Statement.RETURN_GENERATED_KEYS)) {

            String seleccion = (String) comboMesa.getSelectedItem();
            int mesaSeleccionada = Integer.parseInt(seleccion.replace("Mesa ", ""));

            ps.setInt(1, mesaSeleccionada);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                pedidoActual = rs.getInt(1);
                modeloDetallePedido.setRowCount(0);
                cargarPedidos();
                cargarMesasDisponibles();

                lblTotalPedido.setText("Total del pedido: $0.00");
                JOptionPane.showMessageDialog(this, "Nuevo pedido creado. ID: " + pedidoActual);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al crear pedido: " + e.getMessage());
        }
    }

   private void agregarPlatilloAlPedido() {
    if (pedidoActual == -1) {
        JOptionPane.showMessageDialog(this, "Primero crea o selecciona un pedido.");
        return;
    }
    // Validar que el pedido no esté pagado
    int estadoColumna = 2; // columna "Estado"
    for (int i = 0; i < modeloPedidos.getRowCount(); i++) {
        int id = (int) modeloPedidos.getValueAt(i, 0);
        if (id == pedidoActual) {
            String estado = (String) modeloPedidos.getValueAt(i, estadoColumna);
            if (estado.equalsIgnoreCase("Pagado")) {
                JOptionPane.showMessageDialog(this, "No puedes agregar platillos a un pedido pagado.");
                return;
            }
            break;
        }
    }

    String platillo = txtBuscarPlatillo.getText().trim();
    int cantidad;

    try {
        cantidad = Integer.parseInt(txtCantidad.getText());
        if (cantidad <= 0) throw new NumberFormatException();
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Cantidad inválida. Debe ser un número mayor que cero.");
        return;
    }

    try (Connection con = ConexionBD.conectar()) {
        // Obtener id y stock actual del platillo
        PreparedStatement psPlatillo = con.prepareStatement("SELECT id, stock FROM platillos WHERE nombre = ?");
        psPlatillo.setString(1, platillo);
        ResultSet rs = psPlatillo.executeQuery();

        if (rs.next()) {
            int idPlatillo = rs.getInt("id");
            int stockActual = rs.getInt("stock");

            if (stockActual < cantidad) {
                JOptionPane.showMessageDialog(this, "No hay suficiente stock disponible. Stock actual: " + stockActual);
                return;
            }

            // Ya hay suficiente stock, descontar
            int nuevoStock = stockActual - cantidad;

            // Actualizar stock en tabla platillos
            PreparedStatement psUpdateStock = con.prepareStatement("UPDATE platillos SET stock = ? WHERE id = ?");
            psUpdateStock.setInt(1, nuevoStock);
            psUpdateStock.setInt(2, idPlatillo);
            psUpdateStock.executeUpdate();

            // Verificar si el platillo ya está en detalles_pedido
            PreparedStatement psCheck = con.prepareStatement(
                    "SELECT id, cantidad FROM detalles_pedido WHERE id_pedido = ? AND id_platillo = ?");
            psCheck.setInt(1, pedidoActual);
            psCheck.setInt(2, idPlatillo);
            ResultSet rsCheck = psCheck.executeQuery();

            if (rsCheck.next()) {
                int idDetalle = rsCheck.getInt("id");
                int cantidadExistente = rsCheck.getInt("cantidad");
                int nuevaCantidad = cantidadExistente + cantidad;

                PreparedStatement psUpdate = con.prepareStatement(
                        "UPDATE detalles_pedido SET cantidad = ? WHERE id = ?");
                psUpdate.setInt(1, nuevaCantidad);
                psUpdate.setInt(2, idDetalle);
                psUpdate.executeUpdate();
            } else {
                PreparedStatement psInsert = con.prepareStatement(
                        "INSERT INTO detalles_pedido(id_pedido, id_platillo, cantidad) VALUES (?, ?, ?)");
                psInsert.setInt(1, pedidoActual);
                psInsert.setInt(2, idPlatillo);
                psInsert.setInt(3, cantidad);
                psInsert.executeUpdate();
            }

            cargarDetallePedido(pedidoActual);
            txtCantidad.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Platillo no encontrado.");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al agregar platillo: " + e.getMessage());
    }
}

 private void eliminarPlatilloDelPedido() {
    int filaSeleccionada = tablaDetallePedido.getSelectedRow();
    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(this, "Selecciona un platillo para eliminar.");
        return;
    }

    Object valorIdDetalle = modeloDetallePedido.getValueAt(filaSeleccionada, 0);
    String nombrePlatillo = (String) modeloDetallePedido.getValueAt(filaSeleccionada, 1);
    int cantidadActual = (int) modeloDetallePedido.getValueAt(filaSeleccionada, 2);

    if (valorIdDetalle == null) {
        // Platillo no guardado en BD, solo eliminar fila localmente
        
        if (cantidadActual > 1) {
            String input = JOptionPane.showInputDialog(this, "Cantidad actual: " + cantidadActual + 
                "\n¿Cuántos platillos deseas eliminar?");
            if (input == null) return; // Canceló
            int cantidadEliminar;
            try {
                cantidadEliminar = Integer.parseInt(input);
                for (int i = 0; i < modeloMenu.getRowCount(); i++) {
    String nombreEnMenu = (String) modeloMenu.getValueAt(i, 1);
    if (nombreEnMenu.equals(nombrePlatillo)) {
        int stockActualMenu = (int) modeloMenu.getValueAt(i, 3);
        modeloMenu.setValueAt(stockActualMenu + cantidadEliminar, i, 3);
        break;
    }
}

                if (cantidadEliminar <= 0 || cantidadEliminar > cantidadActual) {
                    JOptionPane.showMessageDialog(this, "Cantidad inválida.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingresa un número válido.");
                return;
            }
            if (cantidadEliminar == cantidadActual) {
                modeloDetallePedido.removeRow(filaSeleccionada);
            } else {
                int nuevaCantidad = cantidadActual - cantidadEliminar;
                modeloDetallePedido.setValueAt(nuevaCantidad, filaSeleccionada, 2);
                // Puedes actualizar también el total si quieres
            }
            JOptionPane.showMessageDialog(this, "Cantidad actualizada.");
        } else {
            modeloDetallePedido.removeRow(filaSeleccionada);
            JOptionPane.showMessageDialog(this, "Platillo eliminado del carrito.");
        }
        return;
    }

    int idDetalle = (int) valorIdDetalle;

    try (Connection con = ConexionBD.conectar()) {
        int idPlatillo = -1;
        int stockActual = 0;

        PreparedStatement psSelectPlatillo = con.prepareStatement("SELECT id, stock FROM platillos WHERE nombre = ?");
        psSelectPlatillo.setString(1, nombrePlatillo);
        ResultSet rs = psSelectPlatillo.executeQuery();
        if (rs.next()) {
            idPlatillo = rs.getInt("id");
            stockActual = rs.getInt("stock");
        }

        if (idPlatillo == -1) {
            JOptionPane.showMessageDialog(this, "Error: platillo no encontrado.");
            return;
        }

        // Preguntar cantidad a eliminar si hay más de 1
        int cantidadEliminar = cantidadActual;
        if (cantidadActual > 1) {
            String input = JOptionPane.showInputDialog(this, "Cantidad actual: " + cantidadActual + 
                "\n¿Cuántos platillos deseas eliminar?");
            if (input == null) return; // Canceló
            try {
                cantidadEliminar = Integer.parseInt(input);
                if (cantidadEliminar <= 0 || cantidadEliminar > cantidadActual) {
                    JOptionPane.showMessageDialog(this, "Cantidad inválida.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingresa un número válido.");
                return;
            }
        }

        if (cantidadEliminar == cantidadActual) {
            // Eliminar fila y registro en BD
            PreparedStatement psDelete = con.prepareStatement("DELETE FROM detalles_pedido WHERE id = ?");
            psDelete.setInt(1, idDetalle);
            int eliminado = psDelete.executeUpdate();

            if (eliminado > 0) {
                int nuevoStock = stockActual + cantidadEliminar;
                PreparedStatement psUpdateStock = con.prepareStatement("UPDATE platillos SET stock = ? WHERE id = ?");
                psUpdateStock.setInt(1, nuevoStock);
                psUpdateStock.setInt(2, idPlatillo);
                psUpdateStock.executeUpdate();

                cargarDetallePedido(pedidoActual);
                cargarMenuDisponible();
                JOptionPane.showMessageDialog(this, "Platillo eliminado y stock restaurado.");
            }
        } else {
            // Actualizar cantidad en BD y stock
            int nuevaCantidad = cantidadActual - cantidadEliminar;

            PreparedStatement psUpdateCantidad = con.prepareStatement("UPDATE detalles_pedido SET cantidad = ? WHERE id = ?");
            psUpdateCantidad.setInt(1, nuevaCantidad);
            psUpdateCantidad.setInt(2, idDetalle);
            psUpdateCantidad.executeUpdate();

            int nuevoStock = stockActual + cantidadEliminar;
            PreparedStatement psUpdateStock = con.prepareStatement("UPDATE platillos SET stock = ? WHERE id = ?");
            psUpdateStock.setInt(1, nuevoStock);
            psUpdateStock.setInt(2, idPlatillo);
            psUpdateStock.executeUpdate();

            cargarDetallePedido(pedidoActual);
            cargarMenuDisponible();
            JOptionPane.showMessageDialog(this, "Cantidad actualizada y stock restaurado.");
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al eliminar platillo: " + e.getMessage());
    }
}

    private void cargarMesasDisponibles() {
        comboMesa.removeAllItems();
        boolean[] mesasOcupadas = new boolean[11];

        try (Connection con = ConexionBD.conectar();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT mesa FROM pedidos WHERE estado != 'Pagado'")) {
            while (rs.next()) {
                int mesa = rs.getInt("mesa");
                mesasOcupadas[mesa] = true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar mesas ocupadas: " + e.getMessage());
        }

        for (int i = 1; i <= 10; i++) {
            if (!mesasOcupadas[i]) {
                comboMesa.addItem("Mesa " + i);
            }
        }
    }

    public static void main(String[] args) {
        new JFrameMesero();
    }
}