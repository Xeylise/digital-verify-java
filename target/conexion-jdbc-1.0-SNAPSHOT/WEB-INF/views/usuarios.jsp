<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Administración de usuarios</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 2rem auto; max-width: 1000px; padding: 0 1rem; color: #202124; }
        h1, h2 { color: #174a70; }
        form { margin: 0; }
        .registro { display: grid; gap: .8rem; max-width: 440px; }
        label { display: grid; gap: .3rem; }
        input, select, button { font: inherit; padding: .5rem; }
        button { cursor: pointer; }
        .mensaje { padding: .8rem; background: #edf6fc; border-left: 4px solid #174a70; }
        table { border-collapse: collapse; margin-top: 1rem; width: 100%; }
        th, td { border: 1px solid #d5d9dc; padding: .6rem; text-align: left; }
        th { background: #f1f4f6; }
        .acciones { display: flex; gap: .4rem; align-items: center; }
        .sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0, 0, 0, 0); }
        .eliminar { background: #a32626; color: white; border: 0; }
    </style>
</head>
<body>
    <c:url var="usuariosUrl" value="/usuarios"/>
    <h1>Administración de usuarios</h1>

    <c:if test="${not empty mensaje}">
        <p class="mensaje" role="status"><c:out value="${mensaje}"/></p>
    </c:if>

    <section>
        <h2>Registrar usuario</h2>
        <form class="registro" method="post" action="${usuariosUrl}">
            <input type="hidden" name="accion" value="crear">

            <label for="nicknameUser">Nombre de usuario</label>
            <input id="nicknameUser" name="nicknameUser" type="text" maxlength="100" required>

            <label for="emailUser">Correo electrónico</label>
            <input id="emailUser" name="emailUser" type="email" maxlength="255" required>

            <label for="passwordUser">Contraseña</label>
            <input id="passwordUser" name="passwordUser" type="password" maxlength="100" required>

            <label for="dateCreate">Fecha de creación</label>
            <input id="dateCreate" type="date" value="${fechaActual}" readonly>

            <label for="statusUserAccount">Estado</label>
            <select id="statusUserAccount" name="statusUserAccount" required>
                <option value="Active">Activo</option>
                <option value="Suspended">Suspendido</option>
            </select>

            <button type="submit">Guardar usuario</button>
        </form>
    </section>

    <section>
        <h2>Usuarios registrados</h2>
        <c:choose>
            <c:when test="${empty usuarios}">
                <p>No hay usuarios registrados.</p>
            </c:when>
            <c:otherwise>
                <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Usuario</th>
                        <th>Correo</th>
                        <th>Fecha de creación</th>
                        <th>Estado</th>
                        <th>Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="usuario" items="${usuarios}">
                        <tr>
                            <td><c:out value="${usuario.idUser}"/></td>
                            <td><c:out value="${usuario.nicknameUser}"/></td>
                            <td><c:out value="${usuario.emailUser}"/></td>
                            <td><c:out value="${usuario.dateCreate}"/></td>
                            <td><c:out value="${usuario.statusUserAccount}"/></td>
                            <td>
                                <div class="acciones">
                                    <form method="post" action="${usuariosUrl}">
                                        <input type="hidden" name="accion" value="estado">
                                        <input type="hidden" name="idUser" value="${usuario.idUser}">
                                        <label>
                                            <span class="sr-only">Estado de <c:out value="${usuario.nicknameUser}"/></span>
                                            <select name="statusUserAccount" aria-label="Cambiar estado">
                                                <c:choose>
                                                    <c:when test="${usuario.statusUserAccount eq 'Active'}">
                                                        <option value="Active" selected>Activo</option>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <option value="Active">Activo</option>
                                                    </c:otherwise>
                                                </c:choose>
                                                <c:choose>
                                                    <c:when test="${usuario.statusUserAccount eq 'Suspended'}">
                                                        <option value="Suspended" selected>Suspendido</option>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <option value="Suspended">Suspendido</option>
                                                    </c:otherwise>
                                                </c:choose>
                                            </select>
                                        </label>
                                        <button type="submit">Actualizar</button>
                                    </form>
                                    <form method="post" action="${usuariosUrl}"
                                          onsubmit="return confirm('¿Eliminar este usuario?');">
                                        <input type="hidden" name="accion" value="eliminar">
                                        <input type="hidden" name="idUser" value="${usuario.idUser}">
                                        <button class="eliminar" type="submit">Eliminar</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </section>
</body>
</html>
