package conexion;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

@WebServlet("/usuarios")
public class UsuarioServlet extends HttpServlet {

    private static final Set<String> ESTADOS_VALIDOS = Set.of("Active", "Suspended");
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        request.setAttribute("usuarios", usuarioDAO.consultar());
        request.setAttribute("fechaActual", LocalDate.now());
        request.setAttribute("mensaje", obtenerMensaje(request.getParameter("mensaje")));
        request.getRequestDispatcher("/WEB-INF/views/usuarios.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");
        String codigoMensaje;
        try {
            if (accion == null) {
                codigoMensaje = "accion-invalida";
            } else {
                codigoMensaje = switch (accion) {
                    case "crear" -> crearUsuario(request);
                    case "estado" -> actualizarEstado(request) ? "actualizado" : "error";
                    case "eliminar" -> eliminarUsuario(request) ? "eliminado" : "error";
                    default -> "accion-invalida";
                };
            }
        } catch (IllegalArgumentException e) {
            codigoMensaje = "datos-invalidos";
        }

        response.sendRedirect(response.encodeRedirectURL(
                request.getContextPath() + "/usuarios?mensaje=" + codigoMensaje));
    }

    private String crearUsuario(HttpServletRequest request) {
        String nickname = request.getParameter("nicknameUser");
        String email = request.getParameter("emailUser");
        String password = request.getParameter("passwordUser");
        String estado = request.getParameter("statusUserAccount");

        if (estaVacio(nickname) || estaVacio(email) || estaVacio(password)
                || nickname.trim().length() > 100 || email.trim().length() > 255
                || password.length() > 100) {
            return "datos-invalidos";
        }
        validarEstado(estado);

        Usuario usuario = new Usuario(
                0,
                nickname.trim(),
                email.trim(),
                password,
                LocalDate.now(),
                estado
        );
        return switch (usuarioDAO.insertarConResultado(usuario)) {
            case INSERTADO -> "creado";
            case DUPLICADO -> "duplicado";
            case DATOS_INVALIDOS -> "datos-invalidos";
            case ERROR_BASE_DATOS -> "error";
        };
    }

    private boolean actualizarEstado(HttpServletRequest request) {
        int idUsuario = Integer.parseInt(request.getParameter("idUser"));
        String estado = request.getParameter("statusUserAccount");
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("El identificador debe ser positivo.");
        }
        validarEstado(estado);
        return usuarioDAO.actualizarEstado(idUsuario, estado);
    }

    private boolean eliminarUsuario(HttpServletRequest request) {
        int idUsuario = Integer.parseInt(request.getParameter("idUser"));
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("El identificador debe ser positivo.");
        }
        return usuarioDAO.eliminar(idUsuario);
    }

    private void validarEstado(String estado) {
        if (estado == null || !ESTADOS_VALIDOS.contains(estado)) {
            throw new IllegalArgumentException("El estado indicado no es válido.");
        }
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String obtenerMensaje(String codigo) {
        if (codigo == null) {
            return "";
        }
        return switch (codigo) {
            case "creado" -> "Usuario registrado correctamente.";
            case "actualizado" -> "Estado del usuario actualizado.";
            case "eliminado" -> "Usuario eliminado correctamente.";
            case "datos-invalidos" -> "Verifica los campos: usuario hasta 100, correo hasta 255 y contraseña hasta 100 caracteres.";
            case "duplicado" -> "Ese nombre de usuario o correo ya está registrado.";
            case "accion-invalida" -> "La acción solicitada no es válida.";
            case "error" -> "No se pudo completar la operación.";
            default -> "";
        };
    }
}
