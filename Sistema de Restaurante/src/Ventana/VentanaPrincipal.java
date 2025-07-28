package Ventana;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal() {
        setTitle("Panel Principal");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panelFondo = new JPanel() {
            Image fondo = new ImageIcon(getClass().getResource("/Imagenes/Fondo1.jpg")).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panelFondo.setLayout(null);
        setContentPane(panelFondo);

        JLabel titulo = new JLabel("Bienvenido al Panel de Administrador", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        titulo.setBounds(100, 30, 500, 50);
        panelFondo.add(titulo);

        JPanel panelBtnUsuarios = crearBotonDifuminado("Usuarios");
        panelBtnUsuarios.setBounds(240, 120, 220, 60);
        panelFondo.add(panelBtnUsuarios);

        JPanel panelBtnPlatillos = crearBotonDifuminado("Platillos");
        panelBtnPlatillos.setBounds(240, 210, 220, 60);
        panelFondo.add(panelBtnPlatillos);

        JPanel panelBtnCerrarSesion = crearBotonDifuminado("Cerrar Sesión");
        panelBtnCerrarSesion.setBounds(240, 300, 220, 60);
        panelFondo.add(panelBtnCerrarSesion);
    }

    private JPanel crearBotonDifuminado(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setOpaque(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setForeground(new Color(255, 223, 0));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setForeground(Color.WHITE);
            }
        });

        boton.addActionListener(e -> {
            switch (texto) {
                case "Usuarios":
                    new JFrameGerente().setVisible(true);
                    dispose();
                    break;
                case "Platillos":
                    new JFrameGerente2().setVisible(true);
                    dispose();
                    break;
                case "Cerrar Sesión":
                    new FormularioInicioSesion().setVisible(true);
                    dispose();
                    break;
            }
        });

        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fondoDifuminado = new Color(0, 0, 0, 150);
                g2.setColor(fondoDifuminado);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
            }
        };

        panel.setOpaque(false);
        panel.setSize(220, 60);
        boton.setBounds(0, 0, 220, 60);
        panel.setLayout(null);
        panel.add(boton);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaPrincipal().setVisible(true);
        });
    }
}
