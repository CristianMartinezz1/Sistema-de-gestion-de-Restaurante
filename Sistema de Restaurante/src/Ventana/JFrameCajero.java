package Ventana;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

import elementos.Correo;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class JFrameCajero extends JFrame {
    private JTable tablaPedidos;
    private DefaultTableModel modeloTabla;
    private JTextArea ticketArea;
    private int pedidoActual = -1;
    private double cantidadRecibida = 0;

    public JFrameCajero() {
        setTitle("Cajero - Visualización de Pedidos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setPreferredSize(new Dimension(300, 600));

        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String textoFecha = "Pedidos de hoy (" + fechaActual.format(formatter) + ")";

        JLabel labelPedidos = new JLabel(textoFecha);
        labelPedidos.setHorizontalAlignment(JLabel.CENTER);
        labelPedidos.setFont(new Font("Arial", Font.BOLD, 16));
        panelIzquierdo.add(labelPedidos, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new String[]{"ID", "Mesa", "Fecha"}, 0);
        tablaPedidos = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaPedidos);
        panelIzquierdo.add(scrollTabla, BorderLayout.CENTER);

        add(panelIzquierdo, BorderLayout.WEST);

        JPanel panelDerecho = new JPanel(new BorderLayout());
        ticketArea = new JTextArea();
        ticketArea.setEditable(false);
        ticketArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollTicket = new JScrollPane(ticketArea);
        panelDerecho.add(scrollTicket, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();

  
        JButton botonPagar = new JButton("Realizar Pago");

        panelBotones.add(botonPagar);
       
        panelDerecho.add(panelBotones, BorderLayout.SOUTH);

        add(panelDerecho, BorderLayout.CENTER);

        cargarPedidosPendientes();

        tablaPedidos.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int fila = tablaPedidos.getSelectedRow();
                if (fila != -1) {
                    pedidoActual = (int) modeloTabla.getValueAt(fila, 0);
                    generarTicket();
                }
            }
        });

        

        botonPagar.addActionListener(e -> {
            if (pedidoActual == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un pedido para pagar.");
                return;
            }
            mostrarDialogoPago();
        });
    }

    private void cargarPedidosPendientes() {
        modeloTabla.setRowCount(0);
        String consulta = "SELECT id, mesa, fecha FROM pedidos WHERE estado = 'Servido' AND DATE(fecha) = CURDATE()";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(consulta);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int mesa = rs.getInt("mesa");
                String fecha = rs.getString("fecha");
                modeloTabla.addRow(new Object[]{id, mesa, fecha});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar pedidos: " + e.getMessage());
        }
    }

    private void generarTicket() {
        ticketArea.setText("");
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement stmtPedido = conn.prepareStatement("SELECT mesa, fecha FROM pedidos WHERE id = ?");
             PreparedStatement stmtPlatillos = conn.prepareStatement(
                     "SELECT p.nombre, p.precio, dp.cantidad " +
                     "FROM detalles_pedido dp JOIN platillos p ON dp.id_platillo = p.id " +
                     "WHERE dp.id_pedido = ?")) {

            stmtPedido.setInt(1, pedidoActual);
            ResultSet rsPedido = stmtPedido.executeQuery();
            if (rsPedido.next()) {
                int mesa = rsPedido.getInt("mesa");
                String fecha = rsPedido.getString("fecha");

                ticketArea.append("===== TICKET DE PEDIDO #" + pedidoActual + " =====\n");
                ticketArea.append("Mesa: " + mesa + "\n");
                ticketArea.append("Fecha: " + fecha + "\n");
                ticketArea.append("====================================\n");
                ticketArea.append(String.format("%-20s %5s %8s\n", "Platillo", "Cant", "Subtotal"));
                ticketArea.append("------------------------------------\n");
            }

            stmtPlatillos.setInt(1, pedidoActual);
            ResultSet rsPlatillos = stmtPlatillos.executeQuery();
            double total = 0;

            while (rsPlatillos.next()) {
                String nombre = rsPlatillos.getString("nombre");
                double precio = rsPlatillos.getDouble("precio");
                int cantidad = rsPlatillos.getInt("cantidad");
                double subtotal = precio * cantidad;

                ticketArea.append(String.format("%-20s %5d %8.2f\n", nombre, cantidad, subtotal));
                total += subtotal;
            }

            ticketArea.append("====================================\n");
            ticketArea.append(String.format("TOTAL: %30.2f\n", total));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al generar el ticket: " + e.getMessage());
        }
    }

    private double obtenerTotalPedido(int idPedido) {
    double total = 0.0;

    String sql = "SELECT p.precio, dp.cantidad " +
                 "FROM detalles_pedido dp " +
                 "JOIN platillos p ON dp.id_platillo = p.id " +
                 "WHERE dp.id_pedido = ?";

    try (Connection conn = ConexionBD.conectar();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, idPedido);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            double precio = rs.getDouble("precio");
            int cantidad = rs.getInt("cantidad");
            total += precio * cantidad;
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error al obtener el total del pedido: " + e.getMessage());
    }

    return total;
}

    private String generarContenidoTicket(int idPedido) {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement stmtPedido = conn.prepareStatement("SELECT mesa, fecha FROM pedidos WHERE id = ?");
             PreparedStatement stmtPlatillos = conn.prepareStatement(
                     "SELECT p.nombre, p.precio, dp.cantidad " +
                     "FROM detalles_pedido dp JOIN platillos p ON dp.id_platillo = p.id " +
                     "WHERE dp.id_pedido = ?")) {

            stmtPedido.setInt(1, idPedido);
            ResultSet rsPedido = stmtPedido.executeQuery();
            if (rsPedido.next()) {
                int mesa = rsPedido.getInt("mesa");
                String fecha = rsPedido.getString("fecha");

                sb.append("===== TICKET DE PEDIDO #" + idPedido + " =====\n");
                sb.append("Mesa: " + mesa + "\n");
                sb.append("Fecha: " + fecha + "\n");
                sb.append("====================================\n");
                sb.append(String.format("%-20s %5s %8s\n", "Platillo", "Cant", "Subtotal"));
                sb.append("------------------------------------\n");
            }

            stmtPlatillos.setInt(1, idPedido);
            ResultSet rsPlatillos = stmtPlatillos.executeQuery();
            double total = 0;

            while (rsPlatillos.next()) {
                String nombre = rsPlatillos.getString("nombre");
                double precio = rsPlatillos.getDouble("precio");
                int cantidad = rsPlatillos.getInt("cantidad");
                double subtotal = precio * cantidad;

                sb.append(String.format("%-20s %5d %8.2f\n", nombre, cantidad, subtotal));
                total += subtotal;
            }

            sb.append("====================================\n");
            sb.append(String.format("TOTAL: %30.2f\n", total));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al generar contenido del ticket: " + e.getMessage());
        }
        return sb.toString();
    }

private void enviarCorreoTicket(String correoCliente) {
    try {
        File pdfTicket = generarPdfTicket(pedidoActual);

        Correo correoEnvio = new Correo();
        correoEnvio.setRemitente("la.crila.comida.mexicana@gmail.com", "sbkm ylbn rfwq bwnm");
        correoEnvio.setDestinatario(correoCliente);
        correoEnvio.setContenido("Ticket de Pedido #" + pedidoActual,
                "Adjunto encontrarás el ticket de tu pedido #" + pedidoActual + ".");
        correoEnvio.agregarArchivo(pdfTicket.getAbsolutePath()); // <-- aquí se pasa la ruta como String
        correoEnvio.enviarCorreo();

        marcarPedidoComoPagado(pedidoActual);
        JOptionPane.showMessageDialog(this, "Ticket PDF enviado a " + correoCliente);
        ticketArea.setText("");

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error al enviar el PDF: " + e.getMessage());
    }
}



    private void marcarPedidoComoPagado(int idPedido) {
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement("UPDATE pedidos SET estado = 'Pagado' WHERE id = ?")) {
            ps.setInt(1, idPedido);
            ps.executeUpdate();
            cargarPedidosPendientes();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar estado del pedido: " + e.getMessage());
        }
    }

 private void mostrarDialogoPago() {
    JDialog dialogo = new JDialog(this, "Pago", true);
    dialogo.setSize(500, 500);
    dialogo.setLocationRelativeTo(this);
    dialogo.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Componentes comunes
    JLabel lblMetodo = new JLabel("Método de pago:");
    String[] opciones = {"Efectivo", "Tarjeta Débito", "Tarjeta Crédito"};
    JComboBox<String> comboMetodo = new JComboBox<>(opciones);

    double total = obtenerTotalPedido(pedidoActual);
    JLabel lblTotal = new JLabel("Total a pagar:");
    JLabel lblTotalValor = new JLabel(String.format("$ %.2f", total));

    JLabel lblCorreo = new JLabel("Correo del cliente:");
    JTextField txtCorreo = new JTextField();
    txtCorreo.setPreferredSize(new Dimension(300, 25));

    // Campos tarjeta
    JLabel lblNumero = new JLabel("Número de tarjeta:");
    JTextField txtNumero = new JTextField();
    JLabel lblCVV = new JLabel("CVV:");
    JTextField txtCVV = new JTextField();
    JLabel lblVencimiento = new JLabel("Fecha Venc. (MM/AA):");
    JTextField txtVencimiento = new JTextField();

    // Campos efectivo
    JLabel lblRecibido = new JLabel("Cantidad recibida:");
    JTextField txtRecibido = new JTextField();
    JLabel lblCambio = new JLabel("Cambio:");
    JLabel lblCambioValor = new JLabel("$ 0.00");

    // Botones
    JButton btnConfirmar = new JButton("Confirmar");
    JButton btnCancelar = new JButton("Cancelar");

    // Agregar componentes al layout
    int y = 0;

    gbc.gridx = 0; gbc.gridy = y; dialogo.add(lblMetodo, gbc);
    gbc.gridx = 1; dialogo.add(comboMetodo, gbc); y++;

    gbc.gridx = 0; gbc.gridy = y; dialogo.add(lblTotal, gbc);
    gbc.gridx = 1; dialogo.add(lblTotalValor, gbc); y++;

    gbc.gridx = 0; gbc.gridy = y; dialogo.add(lblNumero, gbc);
    gbc.gridx = 1; dialogo.add(txtNumero, gbc); y++;

    gbc.gridx = 0; gbc.gridy = y; dialogo.add(lblCVV, gbc);
    gbc.gridx = 1; dialogo.add(txtCVV, gbc); y++;

    gbc.gridx = 0; gbc.gridy = y; dialogo.add(lblVencimiento, gbc);
    gbc.gridx = 1; dialogo.add(txtVencimiento, gbc); y++;

    gbc.gridx = 0; gbc.gridy = y; dialogo.add(lblRecibido, gbc);
    gbc.gridx = 1; dialogo.add(txtRecibido, gbc); y++;

    gbc.gridx = 0; gbc.gridy = y; dialogo.add(lblCambio, gbc);
    gbc.gridx = 1; dialogo.add(lblCambioValor, gbc); y++;

    gbc.gridx = 0; gbc.gridy = y; dialogo.add(lblCorreo, gbc);
    gbc.gridx = 1; dialogo.add(txtCorreo, gbc); y++;

    gbc.gridx = 0; gbc.gridy = y; dialogo.add(btnConfirmar, gbc);
    gbc.gridx = 1; dialogo.add(btnCancelar, gbc);

    // Mostrar/ocultar según método
    comboMetodo.addActionListener(e -> {
        boolean esTarjeta = !comboMetodo.getSelectedItem().equals("Efectivo");

        lblNumero.setVisible(esTarjeta);
        txtNumero.setVisible(esTarjeta);
        lblCVV.setVisible(esTarjeta);
        txtCVV.setVisible(esTarjeta);
        lblVencimiento.setVisible(esTarjeta);
        txtVencimiento.setVisible(esTarjeta);

        lblRecibido.setVisible(!esTarjeta);
        txtRecibido.setVisible(!esTarjeta);
        lblCambio.setVisible(!esTarjeta);
        lblCambioValor.setVisible(!esTarjeta);
    });
    comboMetodo.setSelectedIndex(0); // activar visibilidad

    // Calcular cambio
    txtRecibido.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        public void insertUpdate(javax.swing.event.DocumentEvent e) { calcularCambio(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { calcularCambio(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { calcularCambio(); }

        private void calcularCambio() {
            try {
                double recibido = Double.parseDouble(txtRecibido.getText());
                double cambio = recibido - total;
                lblCambioValor.setText(cambio >= 0 ? String.format("$ %.2f", cambio) : "Insuficiente");
            } catch (NumberFormatException ex) {
                lblCambioValor.setText("$ 0.00");
            }
        }
    });

    // Cancelar
    btnCancelar.addActionListener(e -> dialogo.dispose());

    // Confirmar
    btnConfirmar.addActionListener(e -> {
        String metodo = (String) comboMetodo.getSelectedItem();
        String correo = txtCorreo.getText().trim();

        if (metodo.equals("Efectivo")) {
            try {
                double recibido = Double.parseDouble(txtRecibido.getText());
                if (recibido >= total) {
                     cantidadRecibida = recibido; 
                    if (!correo.isEmpty()) {
                        enviarCorreoTicket(correo);
                    }
                    marcarPedidoComoPagado(pedidoActual);
                    JOptionPane.showMessageDialog(this,
                            String.format("Pago en efectivo registrado.\nTotal: $ %.2f\nCambio: %s", total, lblCambioValor.getText()));
                    dialogo.dispose();
                    ticketArea.setText("");
                } else {
                    JOptionPane.showMessageDialog(dialogo, "Cantidad insuficiente.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialogo, "Ingresa una cantidad válida.");
            }

        } else {
            String numero = txtNumero.getText().trim();
            String cvv = txtCVV.getText().trim();
            String vencimiento = txtVencimiento.getText().trim();

            if (numero.matches("\\d{16}") && cvv.matches("\\d{3}") && vencimiento.matches("\\d{2}/\\d{2}")) {
                if (!correo.isEmpty()) {
                    enviarCorreoTicket(correo);
                }
                marcarPedidoComoPagado(pedidoActual);
                JOptionPane.showMessageDialog(this, String.format("Pago con tarjeta registrado.\nMonto: $ %.2f", total));
                dialogo.dispose();
                ticketArea.setText("");
            } else {
                JOptionPane.showMessageDialog(dialogo, "Datos de tarjeta inválidos.");
            }
        }
    });

    dialogo.setVisible(true);
}

 
private File generarPdfTicket(int idPedido) throws IOException {
    try (PDDocument document = new PDDocument()) {
        PDPage pagina = new PDPage(PDRectangle.LETTER);
        document.addPage(pagina);
        PDPageContentStream contenido = new PDPageContentStream(document, pagina);

        float pageWidth = pagina.getMediaBox().getWidth();
        float y = 720;
        float interlineado = 16;

        // Logo centrado
        try {
            InputStream logoStream = getClass().getResourceAsStream("/Imagenes/logo.png");
            if (logoStream != null) {
                PDImageXObject logo = PDImageXObject.createFromByteArray(document, logoStream.readAllBytes(), "logo");
                float logoWidth = 100;
                float logoHeight = 100;
                float xLogo = (pageWidth - logoWidth) / 2;
                contenido.drawImage(logo, xLogo, y, logoWidth, logoHeight);
                y -= logoHeight + 20;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No se pudo cargar el logo.");
        }

        // Título centrado
        String titulo = "TICKET DE PEDIDO #" + idPedido;
        contenido.setFont(PDType1Font.COURIER_BOLD, 14);
        float textWidth = PDType1Font.COURIER_BOLD.getStringWidth(titulo) / 1000 * 14;
        contenido.beginText();
        contenido.newLineAtOffset((pageWidth - textWidth) / 2, y);
        contenido.showText(titulo);
        contenido.endText();
        y -= interlineado * 2;

        double total = 0;

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement stmtPedido = conn.prepareStatement("SELECT mesa, fecha FROM pedidos WHERE id = ?");
             PreparedStatement stmtPlatillos = conn.prepareStatement(
                     "SELECT p.nombre, p.precio, dp.cantidad FROM detalles_pedido dp JOIN platillos p ON dp.id_platillo = p.id WHERE dp.id_pedido = ?")) {

            stmtPedido.setInt(1, idPedido);
            ResultSet rsPedido = stmtPedido.executeQuery();
            if (rsPedido.next()) {
                String mesa = "Mesa: " + rsPedido.getInt("mesa");
                String fecha = "Fecha: " + rsPedido.getString("fecha");

                contenido.setFont(PDType1Font.COURIER, 12);
                contenido.beginText();
                contenido.newLineAtOffset(80, y);
                contenido.showText(mesa);
                contenido.endText();
                y -= interlineado;
                contenido.beginText();
                contenido.newLineAtOffset(80, y);
                contenido.showText(fecha);
                contenido.endText();
                y -= interlineado * 2;
            }

            // Encabezado de tabla
            contenido.setFont(PDType1Font.COURIER_BOLD, 12);
            contenido.beginText();
            contenido.newLineAtOffset(80, y);
            contenido.showText(String.format("%-20s %6s %10s", "Platillo", "Cant", "Subtotal"));
            contenido.endText();
            y -= interlineado;

            contenido.setFont(PDType1Font.COURIER, 12);
            stmtPlatillos.setInt(1, idPedido);
            ResultSet rsPlatillos = stmtPlatillos.executeQuery();

            while (rsPlatillos.next()) {
                String nombre = rsPlatillos.getString("nombre");
                double precio = rsPlatillos.getDouble("precio");
                int cantidad = rsPlatillos.getInt("cantidad");
                double subtotal = precio * cantidad;
                total += subtotal;

                contenido.beginText();
                contenido.newLineAtOffset(80, y);
                contenido.showText(String.format("%-20s %6d %10.2f", nombre, cantidad, subtotal));
                contenido.endText();
                y -= interlineado;
            }

            y -= interlineado;

            // Si no se capturó cantidadRecibida, asumimos igual al total (por ejemplo para tarjeta)
            double recibido = cantidadRecibida > 0 ? cantidadRecibida : total;
            double cambio = recibido - total;

            contenido.setFont(PDType1Font.COURIER_BOLD, 12);
            String[] totales = {
                String.format("TOTAL: $ %.2f", total),
                String.format("RECIBIDO: $ %.2f", recibido),
                String.format("CAMBIO: $ %.2f", cambio)
            };

            for (String linea : totales) {
                float anchoLinea = PDType1Font.COURIER_BOLD.getStringWidth(linea) / 1000 * 12;
                contenido.beginText();
                contenido.newLineAtOffset((pageWidth - anchoLinea) / 2, y);
                contenido.showText(linea);
                contenido.endText();
                y -= interlineado;
            }

            y -= interlineado;

            contenido.setFont(PDType1Font.COURIER, 10);
            contenido.beginText();
            contenido.newLineAtOffset((pageWidth - 200) / 2, y);
            contenido.showText("----------------------------------------");
            contenido.endText();
            y -= interlineado;
            contenido.beginText();
            contenido.newLineAtOffset((pageWidth - 180) / 2, y);
            contenido.showText("   ¡Gracias por su compra!  ");
            contenido.endText();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al generar PDF del ticket: " + e.getMessage());
        }

        contenido.close();
        File archivo = new File("ticket_pedido_" + idPedido + ".pdf");
        document.save(archivo);
        return archivo;
    }
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrameCajero ventana = new JFrameCajero();
            ventana.setVisible(true);
        });
    }
}


