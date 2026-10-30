
package conexion;

public class App {

    public static void main(String[] args) {

        UsuarioDAO usuarioDAO = new UsuarioDAO();

        // CREATE
        System.out.println("--- REGISTRAR USUARIO ---");

        Usuario usuario = new Usuario(
                0,
                "john_doe",
                "john.doe@example.com",
                "password123",
                java.time.LocalDate.now(),
                "Suspended"
        );

        usuarioDAO.insertar(usuario);

        // READ
        System.out.println("\n--- CONSULTAR USUARIOS ---");

        mostrarUsuarios(usuarioDAO);

        // UPDATE
        System.out.println("\n--- ACTUALIZAR USUARIO ---");

        usuarioDAO.actualizarEstado(1, "Active");

        // READ nuevamente
        mostrarUsuarios(usuarioDAO);

        // DELETE
        System.out.println("\n--- ELIMINAR USUARIO ---");

        usuarioDAO.eliminar(1);

        // READ final
        System.out.println("\n--- CONSULTA FINAL ---");

        mostrarUsuarios(usuarioDAO);
    }

    private static void mostrarUsuarios(UsuarioDAO usuarioDAO) {
        System.out.println("Usuarios en la base de datos:");
        for (Usuario usuario : usuarioDAO.consultar()) {
            System.out.println("ID: " + usuario.getIdUser() +
                    ", Nickname: " + usuario.getNicknameUser() +
                    ", Email: " + usuario.getEmailUser() +
                    ", Estado: " + usuario.getStatusUserAccount());
        }
    }
}


