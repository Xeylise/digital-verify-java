
package conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // CREATE: Registrar un nuevo usuario
    public boolean insertar(Usuario usuario) {

        String sql = "INSERT INTO users "
                + "(nickname_user, email_user, password_user, date_create, status_user_account) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNicknameUser());
            stmt.setString(2, usuario.getEmailUser());
            stmt.setString(3, usuario.getPasswordUser());
            stmt.setDate(4, Date.valueOf(usuario.getDateCreate()));
            stmt.setString(5, usuario.getStatusUserAccount());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al insertar usuario: " + e.getMessage());
            return false;
        }
    }

    // READ: Consultar todos los usuarios
    public List<Usuario> consultar() {

        List<Usuario> usuarios = new ArrayList<>();

        String sql = "SELECT * FROM users";

        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Usuario usuario = new Usuario(
                        rs.getInt("id_user"),
                        rs.getString("nickname_user"),
                        rs.getString("email_user"),
                        rs.getString("password_user"),
                        rs.getDate("date_create").toLocalDate(),
                        rs.getString("status_user_account")
                );

                usuarios.add(usuario);
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar usuarios: " + e.getMessage());
        }

        return usuarios;
    }

    // UPDATE: Actualizar el estado de un usuario
    public boolean actualizarEstado(int idUser, String nuevoEstado) {

        String sql = "UPDATE users SET status_user_account = ? "
                + "WHERE id_user = ?";

        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, idUser);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    // DELETE: Eliminar un usuario
    public boolean eliminar(int idUser) {

        String sql = "DELETE FROM users WHERE id_user = ?";

        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUser);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }
}
