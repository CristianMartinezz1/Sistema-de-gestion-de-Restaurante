package Ventana;

import ProyectoTap.ValidadorTexto;
import captchacadenas.CaptchaPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class FormularioInicioSesion extends JFrame {

    private JPanel panelPrincipal;
    private JTextField campoCorreo;
    private JPasswordField campoContrasena;
    private JLabel etiquetaErrorCorreo;
    private JLabel etiquetaErrorContrasena;
    private JLabel etiquetaErrorCaptcha;
    private CaptchaPanel captchaPanel;
    private JTextField campoCaptchaUsuario;
    private JPanel panelCorreo;
    private JPanel panelContrasena;

    private BufferedImage imagenFondo;

public FormularioInicioSesion() {
    try {
        imagenFondo = ImageIO.read(getClass().getResource("/Imagenes/FondoRest.jpeg"));
        setSize(450, 630);
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "No se pudo cargar la imagen de fondo.");
        setSize(450, 630);
    }

    setTitle("Iniciar Sesión");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);

    panelPrincipal = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagenFondo != null) {
                g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
            }
        }
    };
    panelPrincipal.setLayout(null);
    setContentPane(panelPrincipal);

    int anchoCentro = 320;
    int altoCentro = 460;

    JPanel panelContenedorDifuminado = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagenFondo != null) {
                int x = (getWidth() - anchoCentro) / 2;
                int y = (getHeight() - altoCentro) / 2;
                BufferedImage sub = imagenFondo.getSubimage(
                    Math.max(0, x), Math.max(0, y),
                    Math.min(anchoCentro, imagenFondo.getWidth()),
                    Math.min(altoCentro, imagenFondo.getHeight())
                );
                BufferedImage blur = aplicarDesenfoque(sub);
                g.drawImage(blur, 0, 0, getWidth(), getHeight(), null);
            }
        }
    };
    panelContenedorDifuminado.setOpaque(false);
    panelContenedorDifuminado.setBounds(
        (getWidth() - anchoCentro) / 2 - 10,
        (getHeight() - altoCentro) / 2 - 10,
        anchoCentro + 20,
        altoCentro + 20
    );
    panelPrincipal.add(panelContenedorDifuminado);

    JPanel panelCentro = new JPanel(null);
    panelCentro.setOpaque(false);
    panelCentro.setBounds(10, 10, anchoCentro, altoCentro);
    panelContenedorDifuminado.add(panelCentro);

    // Icono perfil.png en lugar de texto
    JLabel iconoPerfil = new JLabel();
    iconoPerfil.setBounds(110, 0, 100, 100);
    try {
        ImageIcon icono = new ImageIcon(getClass().getResource("/Imagenes/perfil.png"));
        Image imagen = icono.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
        iconoPerfil.setIcon(new ImageIcon(imagen));
    } catch (Exception e) {
        System.out.println("No se pudo cargar el icono perfil.png");
    }
    panelCentro.add(iconoPerfil);

    panelCorreo = crearPanelCampoTextoDifuminado("Correo", "/Imagenes/emails.png");
    panelCorreo.setBounds(10, 100, 300, 40);
    panelCentro.add(panelCorreo);

    panelContrasena = crearPanelCampoContrasenaDifuminado("Contraseña", "/Imagenes/llave.png");
    panelContrasena.setBounds(10, 152, 300, 40);
    panelCentro.add(panelContrasena);

    campoCorreo = (JTextField) panelCorreo.getComponent(0);
    campoContrasena = (JPasswordField) panelContrasena.getComponent(0);

    etiquetaErrorCorreo = crearEtiquetaError();
    etiquetaErrorCorreo.setBounds(10, 137, 300, 15);
    panelCentro.add(etiquetaErrorCorreo);

    etiquetaErrorContrasena = crearEtiquetaError();
    etiquetaErrorContrasena.setBounds(10, 190, 300, 15);
    panelCentro.add(etiquetaErrorContrasena);

    JCheckBox mostrarContrasena = new JCheckBox("Mostrar contraseña");
    mostrarContrasena.setBounds(10, 205, 150, 25);
    mostrarContrasena.setForeground(Color.BLACK);
    mostrarContrasena.setOpaque(false);
    panelCentro.add(mostrarContrasena);

    mostrarContrasena.addItemListener(e -> {
        campoContrasena.setEchoChar(mostrarContrasena.isSelected() ? (char) 0 : '●');
    });

    // CAPTCHA (imagen) con tamaño original y centrado un poco más
    captchaPanel = new CaptchaPanel();
    captchaPanel.setBounds(40, 240, 180, 60);  // Se movió 10 px a la derecha para centrar
    panelCentro.add(captchaPanel);

    // Botón refrescar (icono) pequeño y cuadrado a la derecha del captchaPanel
    JButton botonRefrescarCaptcha = new JButton();
    botonRefrescarCaptcha.setBounds(225, 250, 40, 40); // ajustado a nuevo captchaPanel
    botonRefrescarCaptcha.setFocusPainted(false);
    botonRefrescarCaptcha.setBackground(new Color(200, 200, 200, 180));
    botonRefrescarCaptcha.setContentAreaFilled(false);
    botonRefrescarCaptcha.setBorderPainted(false);
    botonRefrescarCaptcha.setFocusPainted(false);
    botonRefrescarCaptcha.setOpaque(false);
    try {
        ImageIcon iconoCargando = new ImageIcon(getClass().getResource("/Imagenes/Cargando.png"));
        Image imagenEscalada = iconoCargando.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        botonRefrescarCaptcha.setIcon(new ImageIcon(imagenEscalada));
    } catch (Exception e) {
        System.out.println("No se pudo cargar el icono Cargando.png");
    }
    panelCentro.add(botonRefrescarCaptcha);

    botonRefrescarCaptcha.addActionListener(e -> captchaPanel.generarCaptcha());

    // Campo para ingresar captcha (debajo, ancho igual al captchaPanel + botón)
    campoCaptchaUsuario = new JTextField();
    JPanel panelCaptchaDifuminado = crearPanelCaptchaDifuminado(campoCaptchaUsuario);
    panelCaptchaDifuminado.setBounds(30, 310, 270, 30);
    panelCentro.add(panelCaptchaDifuminado);

    
    // Etiqueta error captcha
    etiquetaErrorCaptcha = crearEtiquetaError();
    etiquetaErrorCaptcha.setBounds(60, 350, 200, 15);
    etiquetaErrorCaptcha.setHorizontalAlignment(SwingConstants.CENTER);
    panelCentro.add(etiquetaErrorCaptcha);

    JButton botonIngresar = new JButton("INGRESAR");
    botonIngresar.setBounds(60, 380, 200, 45);
    botonIngresar.setBackground(new Color(10, 30, 60, 200));
    botonIngresar.setForeground(Color.WHITE);
    botonIngresar.setFont(new Font("Segoe UI", Font.BOLD, 16));
    botonIngresar.setFocusPainted(false);
    botonIngresar.setBorder(BorderFactory.createEmptyBorder());
    panelCentro.add(botonIngresar);

    botonIngresar.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            botonIngresar.setBackground(new Color(0, 168, 107, 220));
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
            botonIngresar.setBackground(new Color(10, 30, 60, 200));
        }
    });

    botonIngresar.addActionListener(e -> validarCampos());
}

    private JPanel crearPanelCampoTextoDifuminado(String textoInicial, String rutaIcono) {
        JTextField campo = new JTextField(textoInicial);
        campo.setForeground(Color.BLACK);
        campo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        campo.setHorizontalAlignment(JTextField.LEFT);
        campo.setOpaque(false);
        campo.setBorder(BorderFactory.createEmptyBorder(5, 35, 5, 5));

        JLabel icono = new JLabel(redimensionarIcono(rutaIcono, 20, 20));
        icono.setBounds(8, 5, 20, 20);

        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fondoDifuminado = new Color(245, 245, 245, 160);
                g2.setColor(fondoDifuminado);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBounds(0, 0, 300, 40);

        campo.setBounds(0, 0, 300, 40);
        panel.add(campo);
        panel.add(icono);

        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (campo.getText().equals(textoInicial)) {
                    campo.setText("");
                    campo.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (campo.getText().isEmpty()) {
                    campo.setText(textoInicial);
                    campo.setForeground(Color.BLACK);
                }
            }
        });

        return panel;
    }

    private JPanel crearPanelCampoContrasenaDifuminado(String textoInicial, String rutaIcono) {
        JPasswordField campo = new JPasswordField(textoInicial);
        campo.setEchoChar((char) 0);
        campo.setForeground(Color.BLACK);
        campo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        campo.setHorizontalAlignment(JTextField.LEFT);
        campo.setOpaque(false);
        campo.setBorder(BorderFactory.createEmptyBorder(5, 35, 5, 5));

        JLabel icono = new JLabel(redimensionarIcono(rutaIcono, 20, 20));
        icono.setBounds(8, 5, 20, 20);

        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fondoDifuminado = new Color(245, 245, 245, 160);
                g2.setColor(fondoDifuminado);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBounds(0, 0, 300, 40);

        campo.setBounds(0, 0, 300, 40);
        panel.add(campo);
        panel.add(icono);

        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                String texto = new String(campo.getPassword());
                if (texto.equals(textoInicial)) {
                    campo.setText("");
                    campo.setEchoChar('●');
                    campo.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (new String(campo.getPassword()).isEmpty()) {
                    campo.setText(textoInicial);
                    campo.setEchoChar((char) 0);
                    campo.setForeground(Color.BLACK);
                }
            }
        });

        return panel;
    }

    private JLabel crearEtiquetaError() {
        JLabel etiqueta = new JLabel();
        etiqueta.setForeground(new Color(255, 80, 80));
        etiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        return etiqueta;
    }

    private ImageIcon redimensionarIcono(String ruta, int ancho, int alto) {
        ImageIcon icono = new ImageIcon(getClass().getResource(ruta));
        Image imagen = icono.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new ImageIcon(imagen);
    }

    private void validarCampos() {
        String correo = campoCorreo.getText().trim();
        String contrasena = new String(campoContrasena.getPassword());
        String captchaUsuario = campoCaptchaUsuario.getText().trim();
        String captchaGenerado = captchaPanel.getCodigo();

        boolean correoValido = ValidadorTexto.esCorreoValido(correo);
        boolean contrasenaValida = ValidadorTexto.esContrasenaSegura(contrasena);
        boolean captchaValido = captchaUsuario.equalsIgnoreCase(captchaGenerado);

        etiquetaErrorCorreo.setText(correoValido ? "" : "Correo inválido. Usa Gmail o Hotmail.");
        etiquetaErrorContrasena.setText(contrasenaValida ? "" : "Contraseña insegura: 8+, mayúscula, número y símbolo.");
        etiquetaErrorCaptcha.setText(captchaValido ? "" : "Captcha incorrecto. Intenta nuevamente.");

        if (correoValido && contrasenaValida && captchaValido) {
            String nivel = LoginDAO.obtenerNivelUsuario(correo, contrasena);
            if (nivel != null) {
                switch (nivel.toLowerCase()) {
                    case "gerente":
                        new VentanaPrincipal().setVisible(true);
                        break;
                    case "mesero":
                        new JFrameMesero().setVisible(true);
                        break;
                    case "cocinero":
                        new JFrameCocinero().setVisible(true);
                        break;
                    case "cajero":
                        new JFrameCajero().setVisible(true);
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "Nivel no reconocido.");
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Credenciales incorrectas o usuario no registrado.");
                captchaPanel.generarCaptcha();
                campoCaptchaUsuario.setText("");
            }
        } else {
            captchaPanel.generarCaptcha();
            campoCaptchaUsuario.setText("");
        }
    }

private JPanel crearPanelCaptchaDifuminado(JTextField campo) {
    campo.setFont(new Font("Segoe UI", Font.BOLD, 14));
    campo.setHorizontalAlignment(JTextField.CENTER);
    campo.setOpaque(false);
    campo.setForeground(Color.BLACK);
    campo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JPanel panel = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(245, 245, 245, 160)); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.dispose();
        }
    };
    panel.setOpaque(false);
    panel.setBounds(70, 260, 180, 30); 
    campo.setBounds(0, 0, 180, 30);
    panel.add(campo);

    return panel;
}

    private BufferedImage aplicarDesenfoque(BufferedImage imagenOriginal) {
    int ancho = imagenOriginal.getWidth();
    int alto = imagenOriginal.getHeight();

    // Crear imagen con fondo blanco
    BufferedImage imagenFondoBlanco = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = imagenFondoBlanco.createGraphics();
    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, ancho, alto);
    g2d.drawImage(imagenOriginal, 0, 0, null);
    g2d.dispose();

    // Imagen desenfocada
    BufferedImage desenfocada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

    for (int x = 2; x < ancho - 2; x++) {
        for (int y = 2; y < alto - 2; y++) {
            int r = 0, g = 0, b = 0;

            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    Color color = new Color(imagenFondoBlanco.getRGB(x + dx, y + dy));
                    r += color.getRed();
                    g += color.getGreen();
                    b += color.getBlue();
                }
            }

            r /= 25;
            g /= 25;
            b /= 25;

            Color nuevo = new Color(r, g, b, 170); // Más desenfoque con transparencia
            desenfocada.setRGB(x, y, nuevo.getRGB());
        }
    }

    return desenfocada;
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormularioInicioSesion().setVisible(true));
    }
}



