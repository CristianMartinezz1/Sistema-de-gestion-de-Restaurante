package Ventana;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginDAO {

    public static boolean validarUsuario(String usuario, String correo, String contrasena) {
        String sql = "SELECT * FROM usuarios WHERE usuario = ? AND correo = ? AND contrasena = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, correo);
            ps.setString(3, contrasena);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("Error al validar usuario: " + e.getMessage());
            return false;
        }
    }

    public static boolean registrarUsuario(String nombre, String correo, String telefono, String usuario, String contrasena, int idRol) {
        String sql = "INSERT INTO usuarios(nombre, correo, telefono, usuario, contrasena, id_rol) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.setString(3, telefono);
            ps.setString(4, usuario);
            ps.setString(5, contrasena);
            ps.setInt(6, idRol);  // ID del rol (por ejemplo: 1 = gerente, 2 = repartidor, etc.)

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    public static String obtenerNivelUsuario(String correo, String contrasena) {
        String nivel = null;
        String sql = "SELECT r.nombre FROM usuarios u " +
                     "JOIN roles r ON u.id_rol = r.id " +
                     "WHERE u.correo = ? AND u.contrasena = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo);
            ps.setString(2, contrasena);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nivel = rs.getString("nombre");
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener nivel del usuario: " + e.getMessage());
        }

        return nivel;
    }
}
