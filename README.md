# Digital Verify - Backend Spring Boot + JDBC (`app-web-java`)

Implementación completa en **Java 21** con **Spring Boot 3.2.5**, validaciones robustas (**Jakarta Bean Validation**) y persistencia relacional con **Spring JDBC (`JdbcTemplate`)** para la plataforma Digital Verify (KYC & Control Desk / Gaming).

---

## 🚀 Requisitos y Ejecución Rápida

El proyecto incluye el Maven Wrapper (`mvnw`), por lo que no requieres tener Maven instalado globalmente.

### Compilar y Ejecutar Pruebas
```bash
./mvnw clean test
```

### Ejecutar la Aplicación
```bash
./mvnw spring-boot:run
```

O ejecutando el JAR empaquetado:
```bash
java -jar target/app-web-java-1.0.0-SNAPSHOT.jar
```

El servidor iniciará en: **`http://localhost:8080`**

---

## 🗄️ Conexión con Base de Datos

- La aplicación usa **MySQL local** en `localhost:3306`, con la base `digitalverify` y el usuario `root` por defecto. Si no existe, el conector intenta crearla.
- Define la variable de entorno `DB_PASSWORD` con la contraseña de MySQL antes de iniciar la aplicación. No guardes esa contraseña en `application.properties` ni en el repositorio.
- Opcionalmente puedes configurar `DB_HOST`, `DB_PORT`, `DB_NAME` y `DB_USERNAME`.
- En PowerShell, establece la contraseña para la sesión actual y luego inicia el proyecto:
  ```powershell
  $env:DB_PASSWORD = "tu_contraseña"
  .\mvnw.cmd spring-boot:run
  ```
- El servicio local de MySQL debe estar iniciado antes de ejecutar la aplicación.
- Las pruebas automatizadas usan una base H2 en memoria en modo de compatibilidad MySQL; H2 no se usa en la aplicación ejecutable.
- La inicialización crea tablas faltantes y carga los datos de demostración sin borrar registros existentes en cada reinicio.

---

## 🛡️ Validaciones Implementadas

1. **Autenticación y Registro (`LoginRequest`, `RegisterRequest`)**:
   - Validación de formato de email (`@Email`).
   - Campos requeridos (`@NotBlank`).
   - Longitud mínima de contraseñas (`@Size(min = 6)`).
   - Verificación de duplicidad de correo en base de datos mediante `UserJdbcRepository`.
   - Mensajes de error claros e integrados en las vistas HTML/Thymeleaf.
2. **Subida de Documentos KYC (`FileUploadValidationService`)**:
   - Validación de presencia de archivo.
   - Restricción de extensiones permitidas: `.jpg`, `.jpeg`, `.png`, `.pdf`.
   - Validación de tipos MIME (`image/jpeg`, `image/png`, `application/pdf`).
   - Tamaño máximo por archivo de **10 MB**.
3. **Decisiones de Verificación (`CaseDecisionRequest`)**:
   - Estados válidos restringidos por patrón regex (`approved`, `rejected`, `in_review`, `escalated`, `info_requested`).
   - Registro obligatorio de bitácora en la tabla `audit_logs` con el operador responsable y fecha/hora.

---

## ⚡ Flujos Totalmente Funcionales

### 1. Jugador / Cliente
- **Login Real**: Inicia sesión con `carlos.mendoza@email.com` (`password123`) o `pcastrosarabia@gmail.com` (`password123`).
- **Registro Real**: Permite registrar nuevos usuarios con almacenamiento en base de datos e inicio de sesión automático.
- **Flujo KYC Dinámico**:
  1. `/start_kyc.html`: Inicio de verificación.
  2. `/upload_id.html`: Carga de documento de identidad con validación de tipo y tamaño. Crea o vincula el caso en la tabla `kyc_cases` y guarda el documento en `kyc_documents`.
  3. `/proof_of_address.html`: Carga de comprobante de domicilio. Cambia automáticamente el estado del caso a `in_review`.
  4. `/kyc_status_review.html`: Muestra el estado del caso consultado en tiempo real desde la base de datos con su referencia de caso y timeline.
- **Dashboard de Juego (`/home.html`)**: Refleja el nombre del usuario y el estado de su verificación KYC de forma dinámica.
- **Apuestas (`/my_bets.html`)**: Lista las apuestas activas e históricas desde la tabla `bets`.
- **Notificaciones (`/notifications.html`)**: Lista alertas reales con opción de marcar como leídas y persistencia en DB.

### 2. Operador / Empleado (Control Desk)
- **Login Operador**: Inicia sesión con `paola.castro@control-desk.com` o `emp90210@verify.com` (`operator123`).
- **Dashboard (`/dashboard.html`)**:
  - Métricas de KPIs (Casos pendientes, en revisión, aprobados, rechazados, alertas de fraude) calculadas mediante consultas JDBC agregadas.
  - Tabla de casos recientes renderizada desde la base de datos con filtros por pestañas.
- **Cola de Verificación (`/queue.html`)**:
  - Lista de casos en espera cargada dinámicamente desde `kyc_cases`.
  - Enlace directo a cada caso para inspección.
- **Detalle de Caso (`/case.html?caseId=X`)**:
  - Carga el caso por ID con datos del usuario, score de riesgo y documentos subidos.
  - **Panel de Decisión Funcional**: Permite aprobar o rechazar un caso con notas de decisión, actualizando el estado del caso en `kyc_cases`, el estado del jugador en `users` y registrando la acción en `audit_logs`.
- **Registro de Auditoría (`/activity_log.html`)**: Historial completo de acciones tomadas por los operadores.
- **Reportes (`/reports.html`)**: Resumen analítico de métricas y tasas de aprobación.

---

## 🧪 Pruebas Automatizadas

Se cuenta con **40 pruebas automatizadas** que cubren repositorios JDBC, controladores web, flujos de validación y endpoints REST:

```bash
./mvnw clean test
```
Resultados: **40 tests exitosos / 0 fallos / 0 errores**.
