package Ventana;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import elementos.Correo;
import ProyectoTap.ValidadorTexto;
import java.io.File;
import java.util.ArrayList;
import javax.swing.table.TableRowSorter;

public class JFrameGerente extends JFrame {
    private JTextField txtNombre, txtCorreo, txtTelefono, txtUsuario;
    private JPasswordField txtContrasena;
    private JComboBox<String> comboNivel;
    private JButton btnRegistrar, btnEliminar, btnActualizar, btnCerrarSesion, btnLimpiar;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JLabel errorNombre, errorCorreo, errorTelefono, errorUsuario, errorContrasena;
    private JTextField txtBuscar;
private JPanel panelPaginacion;
private java.util.List<Object[]> listaUsuarios = new ArrayList<>();
private final int USUARIOS_POR_PAGINA = 7;
private int paginaActual = 1;

    private Map<String, Integer> rolesMap = new HashMap<>();

    public JFrameGerente() {
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
panelBusqueda.setBackground(Color.WHITE);
panelBusqueda.setBorder(BorderFactory.createTitledBorder("Buscar usuario por nombre"));
        
        
        Color colorFondo = new Color(245, 247, 250);
        Color colorPanel = Color.WHITE;
        Color colorHeader = new Color(46, 134, 222);
        Color colorBoton = new Color(72, 201, 176);
        Color colorTexto = new Color(44, 62, 80);

        setTitle("Panel de Gerente");
        setSize(950, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(colorFondo);
         btnCerrarSesion = new JButton("Cerrar Sesión");
         
        JPanel header = new JPanel();
        header.setBackground(colorHeader);
        header.setPreferredSize(new Dimension(getWidth(), 60));
        JLabel titulo = new JLabel("Panel de Gerente");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        header.add(titulo);
        add(header, BorderLayout.NORTH);

        // Panel formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(colorPanel);
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Gestión de Usuarios"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNombre = estiloCampo(new JTextField(15));
        txtCorreo = estiloCampo(new JTextField(15));
        txtTelefono = estiloCampo(new JTextField(15));
        txtUsuario = estiloCampo(new JTextField(15));
        txtContrasena = (JPasswordField) estiloCampo(new JPasswordField(15));
        comboNivel = new JComboBox<>();
        comboNivel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboNivel.setBackground(Color.WHITE);

        cargarRoles();

        errorNombre = crearEtiquetaError();
        errorCorreo = crearEtiquetaError();
        errorTelefono = crearEtiquetaError();
        errorUsuario = crearEtiquetaError();
        errorContrasena = crearEtiquetaError();

        String[] etiquetas = {"Nombre:", "Correo:", "Teléfono:", "Usuario:", "Contraseña:", "Rol:"};
        Component[] campos = {txtNombre, txtCorreo, txtTelefono, txtUsuario, txtContrasena, comboNivel};
        JLabel[] errores = {errorNombre, errorCorreo, errorTelefono, errorUsuario, errorContrasena};

        int fila = 0;
        for (int i = 0; i < etiquetas.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = fila;
            panelFormulario.add(estiloLabel(etiquetas[i]), gbc);
            gbc.gridx = 1;
            panelFormulario.add(campos[i], gbc);
            if (i < errores.length) {
                fila++;
                gbc.gridy = fila;
                gbc.gridx = 1;
                panelFormulario.add(errores[i], gbc);
            }
            fila++;
        }

        add(panelFormulario, BorderLayout.WEST);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(colorFondo);
        btnRegistrar = estiloBoton("Registrar", colorBoton, Color.WHITE);
        btnEliminar = estiloBoton("Eliminar", new Color(231, 76, 60), Color.WHITE);
        btnActualizar = estiloBoton("Actualizar", new Color(241, 196, 15), Color.WHITE);
        btnLimpiar = estiloBoton("Limpiar", new Color(127, 140, 141), Color.WHITE);
        btnCerrarSesion = estiloBoton("Cerrar sesión", new Color(149, 165, 166), Color.WHITE);

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnCerrarSesion);
        add(panelBotones, BorderLayout.SOUTH);

        // Tabla
        // Tabla
modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre", "Correo", "Teléfono", "Usuario", "Rol"}, 0);
tablaUsuarios = new JTable(modeloTabla);
tablaUsuarios.setRowHeight(25);
tablaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 14));
tablaUsuarios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
tablaUsuarios.getTableHeader().setBackground(colorHeader);
tablaUsuarios.getTableHeader().setForeground(Color.WHITE);
JButton btnRegresar = estiloBoton("Regresar", new Color(52, 73, 94), Color.WHITE);
panelBotones.add(btnRegresar);



JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
scrollPane.setBorder(BorderFactory.createTitledBorder("Usuarios Registrados"));


JPanel panelTablaYPaginacion = new JPanel(new BorderLayout());
panelTablaYPaginacion.add(scrollPane, BorderLayout.CENTER);

panelPaginacion = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
panelTablaYPaginacion.add(panelPaginacion, BorderLayout.SOUTH);

add(panelTablaYPaginacion, BorderLayout.CENTER);

        txtBuscar = new JTextField(50);
txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));

panelBusqueda.add(txtBuscar);
txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        filtrarTabla(txtBuscar.getText().trim());
    }
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        filtrarTabla(txtBuscar.getText().trim());
    }
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        filtrarTabla(txtBuscar.getText().trim());
    }
});

        cargarUsuarios();
panelTablaYPaginacion.add(panelBusqueda, BorderLayout.NORTH);

        btnRegresar.addActionListener(e -> {
    dispose();
    new VentanaPrincipal().setVisible(true);
});

        
        btnRegistrar.addActionListener(e -> registrarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnActualizar.addActionListener(e -> actualizarUsuario());
        btnCerrarSesion.addActionListener(e -> {
            dispose();
            new FormularioInicioSesion().setVisible(true);
        });
        btnLimpiar.addActionListener(e -> limpiarCampos());

        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            int filaSel = tablaUsuarios.getSelectedRow();
            if (filaSel >= 0) {
                txtNombre.setText(modeloTabla.getValueAt(filaSel, 1).toString());
                txtCorreo.setText(modeloTabla.getValueAt(filaSel, 2).toString());
                txtTelefono.setText(modeloTabla.getValueAt(filaSel, 3).toString());
                txtUsuario.setText(modeloTabla.getValueAt(filaSel, 4).toString());
                comboNivel.setSelectedItem(modeloTabla.getValueAt(filaSel, 5).toString());
                txtContrasena.setText("");
                limpiarErrores();
            }
        });

        setVisible(true);
    }

    private void cargarRoles() {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement("SELECT id, nombre FROM roles");
             ResultSet rs = ps.executeQuery()) {
            comboNivel.removeAllItems();
            rolesMap.clear();
            while (rs.next()) {
                String rol = rs.getString("nombre");
                int id = rs.getInt("id");
                rolesMap.put(rol, id);
                comboNivel.addItem(rol);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar roles: " + e.getMessage());
        }
    }
private void filtrarTabla(String texto) {
    modeloTabla.setRowCount(0);
    if (texto.isEmpty()) {
        // Restaurar paginación si el campo está vacío
        actualizarTabla(); // vuelve a mostrar la página actual de listaUsuarios
    } else {
        for (Object[] usuario : listaUsuarios) {
            String nombre = usuario[1].toString().toLowerCase();
            if (nombre.contains(texto.toLowerCase())) {
                modeloTabla.addRow(usuario);
            }
        }
        panelPaginacion.removeAll(); // Ocultar la paginación mientras se filtra
        panelPaginacion.revalidate();
        panelPaginacion.repaint();
    }
}


    private void registrarUsuario() {
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        String rol = (String) comboNivel.getSelectedItem();

          if (!validarCampos(nombre, correo, telefono, usuario, contrasena, !contrasena.isEmpty())) return;

       try (Connection con = ConexionBD.conectar()) {
    PreparedStatement ps = con.prepareStatement("INSERT INTO usuarios (nombre, correo, telefono, usuario, contrasena, id_rol) VALUES (?, ?, ?, ?, ?, ?)");
    ps.setString(1, nombre);
    ps.setString(2, correo);
    ps.setString(3, telefono);
    ps.setString(4, usuario);
    ps.setString(5, contrasena);
    ps.setInt(6, rolesMap.get(rol));
    ps.executeUpdate();

            File pdf = GeneradorPDF.generarPdfRegistro(nombre, correo, usuario, contrasena);
            Correo correoEnvio = new Correo();
            correoEnvio.setRemitente("la.crila.comida.mexicana@gmail.com", "sbkm ylbn rfwq bwnm");
            correoEnvio.setDestinatario(correo);
            correoEnvio.setContenido("Bienvenido", "Hola " + nombre + ", se te ha registrado como " + rol + ".");
            correoEnvio.agregarArchivo(pdf.getAbsolutePath());
            correoEnvio.enviarCorreo();

            JOptionPane.showMessageDialog(this, "Usuario registrado.");
            cargarUsuarios();
            limpiarCampos();
       } catch (SQLIntegrityConstraintViolationException e) {
    JOptionPane.showMessageDialog(this, "El correo ya está registrado.");
} catch (Exception e) {
    e.printStackTrace();
    JOptionPane.showMessageDialog(this, "Error al registrar: " + e.getMessage());
}}

   private boolean validarCampos(String nombre, String correo, String telefono, String usuario, String contrasena, boolean validarContrasena) {
    boolean valido = true;
    
    if (nombre.isEmpty()) {
        errorNombre.setText("Ingrese nombre");
        valido = false;
    } else errorNombre.setText(" ");

    if (!ValidadorTexto.esCorreoValido(correo)) {
        errorCorreo.setText("Correo inválido");
        valido = false;
    } else errorCorreo.setText(" ");

    if (!telefono.matches("\\d{10}")) {
        errorTelefono.setText("Teléfono inválido");
        valido = false;
    } else errorTelefono.setText(" ");

    if (usuario.isEmpty()) {
        errorUsuario.setText("Usuario vacío");
        valido = false;
    } else errorUsuario.setText(" ");

    if (validarContrasena) {
        if (!ValidadorTexto.esContrasenaSegura(contrasena)) {
            errorContrasena.setText("Contraseña insegura");
            valido = false;
        } else errorContrasena.setText(" ");
    } else {
        errorContrasena.setText(" "); // limpiar si no se valida
    }

    return valido;
}



private void cargarUsuarios() {
    listaUsuarios.clear();
    try (Connection con = ConexionBD.conectar();
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery("SELECT u.id, u.nombre, u.correo, u.telefono, u.usuario, r.nombre AS rol FROM usuarios u JOIN roles r ON u.id_rol = r.id")) {
        while (rs.next()) {
            listaUsuarios.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("correo"),
                    rs.getString("telefono"),
                    rs.getString("usuario"),
                    rs.getString("rol")
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + e.getMessage());
    }
    paginaActual = 1;
    actualizarTabla();
}
private void actualizarTabla() {
    modeloTabla.setRowCount(0);
    int inicio = (paginaActual - 1) * USUARIOS_POR_PAGINA;
    int fin = Math.min(inicio + USUARIOS_POR_PAGINA, listaUsuarios.size());

    for (int i = inicio; i < fin; i++) {
        modeloTabla.addRow(listaUsuarios.get(i));
    }

    actualizarPaginacion();
}
private void actualizarPaginacion() {
    panelPaginacion.removeAll();
    int totalPaginas = (int) Math.ceil((double) listaUsuarios.size() / USUARIOS_POR_PAGINA);

    for (int i = 1; i <= totalPaginas; i++) {
        JButton btnPagina = new JButton(String.valueOf(i));
        btnPagina.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnPagina.setBackground(i == paginaActual ? new Color(52, 152, 219) : Color.LIGHT_GRAY);
        btnPagina.setForeground(i == paginaActual ? Color.WHITE : Color.BLACK);

        int pagina = i;
        btnPagina.addActionListener(e -> {
            paginaActual = pagina;
            actualizarTabla();
        });

        panelPaginacion.add(btnPagina);
    }

    panelPaginacion.revalidate();
    panelPaginacion.repaint();
}

    private void eliminarUsuario() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario para eliminar.");
            return;
        }
        String usuario = modeloTabla.getValueAt(fila, 4).toString();
        try (Connection con = ConexionBD.conectar()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM usuarios WHERE usuario = ?");
            ps.setString(1, usuario);
            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Usuario eliminado");
                cargarUsuarios();
                limpiarCampos();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
        }
    }

  private void actualizarUsuario() {
    int fila = tablaUsuarios.getSelectedRow();
    if (fila == -1) {
        JOptionPane.showMessageDialog(this, "Seleccione un usuario para actualizar.");
        return;
    }

    int id = (int) modeloTabla.getValueAt(fila, 0); // ID del usuario
    String nombre = txtNombre.getText().trim();
    String correo = txtCorreo.getText().trim();
    String telefono = txtTelefono.getText().trim();
    String usuario = txtUsuario.getText().trim();
    String contrasena = new String(txtContrasena.getPassword()).trim();
    String rol = (String) comboNivel.getSelectedItem();

    // Validación sin contraseña obligatoria (solo si se modifica)
    if (!validarCampos(nombre, correo, telefono, usuario, contrasena, !contrasena.isEmpty())) return;


    try (Connection con = ConexionBD.conectar()) {
        PreparedStatement ps;

        if (!contrasena.isEmpty()) {
            ps = con.prepareStatement(
                "UPDATE usuarios SET nombre=?, correo=?, telefono=?, usuario=?, contrasena=?, id_rol=? WHERE id=?"
            );
            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.setString(3, telefono);
            ps.setString(4, usuario);
            ps.setString(5, contrasena);
            ps.setInt(6, rolesMap.get(rol));
            ps.setInt(7, id);
        } else {
            ps = con.prepareStatement(
                "UPDATE usuarios SET nombre=?, correo=?, telefono=?, usuario=?, id_rol=? WHERE id=?"
            );
            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.setString(3, telefono);
            ps.setString(4, usuario);
            ps.setInt(5, rolesMap.get(rol));
            ps.setInt(6, id);
        }

        int filasAfectadas = ps.executeUpdate();
        if (filasAfectadas > 0) {
            JOptionPane.showMessageDialog(this, "Usuario actualizado correctamente.");
            cargarUsuarios();
            limpiarCampos();
        } else {
            JOptionPane.showMessageDialog(this, "No se encontró el usuario para actualizar.");
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage());
    }
}


    private JLabel crearEtiquetaError() {
        JLabel label = new JLabel(" ");
        label.setForeground(Color.RED);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        return label;
    }

    private void limpiarErrores() {
        errorNombre.setText(" ");
        errorCorreo.setText(" ");
        errorTelefono.setText(" ");
        errorUsuario.setText(" ");
        errorContrasena.setText(" ");
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtCorreo.setText("");
        txtTelefono.setText("");
        txtUsuario.setText("");
        txtContrasena.setText("");
        comboNivel.setSelectedIndex(0);
        limpiarErrores();
        tablaUsuarios.clearSelection();
    }

    private JLabel estiloLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(44, 62, 80));
        return label;
    }

    private JTextField estiloCampo(JTextField campo) {
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(189, 195, 199)));
        campo.setBackground(Color.WHITE);
        return campo;
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
        SwingUtilities.invokeLater(JFrameGerente::new);
    }
}
