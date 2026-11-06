-- ============================================================================
-- SCRIPT DE SINCRONIZACIÓN DE DATOS DE PRUEBA Y ESQUEMA (IDEMPOTENTE / UPSERT)
-- Base de Datos: H2 (Motor de la aplicación) / Compatible ANSI SQL
-- Propósito: Sincronizar esquema y datos de prueba existentes sin duplicar registros
--            ni fallar por violaciones de claves primarias o foráneas.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. ACTUALIZACIÓN IDEMPOTENTE DEL ESQUEMA (DDL)
-- ----------------------------------------------------------------------------

-- Columnas nuevas en tabla users
ALTER TABLE users ADD COLUMN IF NOT EXISTS language VARCHAR(20) DEFAULT 'es';
ALTER TABLE users ADD COLUMN IF NOT EXISTS timezone VARCHAR(50) DEFAULT 'utc-5';
ALTER TABLE users ADD COLUMN IF NOT EXISTS queue_alerts_enabled BOOLEAN DEFAULT TRUE;

-- Columnas de ciclo de vida en kyc_cases
ALTER TABLE kyc_cases ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP;
ALTER TABLE kyc_cases ADD COLUMN IF NOT EXISTS in_review_at TIMESTAMP;
ALTER TABLE kyc_cases ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP;

-- Crear tabla de seguimiento de decisiones si no existe
CREATE TABLE IF NOT EXISTS seguimiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_id BIGINT NOT NULL,
    internal_user_id BIGINT NOT NULL,
    verification_decision VARCHAR(100) NOT NULL,
    account_status_decision VARCHAR(100) NOT NULL,
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seg_case FOREIGN KEY (case_id) REFERENCES kyc_cases(id) ON DELETE CASCADE,
    CONSTRAINT fk_seg_user FOREIGN KEY (internal_user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Crear tabla de bitácora de actividad de operadores si no existe
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_interno_id BIGINT NOT NULL,
    tipo_evento VARCHAR(100) NOT NULL,
    id_verification BIGINT,
    detalles VARCHAR(500),
    CONSTRAINT fk_act_user FOREIGN KEY (usuario_interno_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_act_case FOREIGN KEY (id_verification) REFERENCES kyc_cases(id) ON DELETE SET NULL
);

-- ----------------------------------------------------------------------------
-- 2. SINCRONIZACIÓN DE USUARIOS (MERGE INTO KEY(id))
--    Actualiza los atributos si el usuario ya existe; lo inserta si no existe.
-- ----------------------------------------------------------------------------
MERGE INTO users (id, email, password_hash, full_name, role, country, phone, account_number, kyc_status, avatar_url, two_factor_enabled, language, timezone, queue_alerts_enabled)
KEY (id)
VALUES
  (1, 'carlos.mendoza@email.com', 'password123', 'Carlos Mendoza', 'PLAYER', 'CO', '+57 300 123 4567', '80948', 'PENDING', '/img/user_avatar.jpg', false, 'es', 'utc-5', true),
  (2, 'pcastrosarabia@gmail.com', 'password123', 'Paola Castro Player', 'PLAYER', 'CO', '+57 311 987 6543', '78125', 'PENDING', '/img/user_avatar.jpg', true, 'es', 'utc-5', true),
  (3, 'lucas.silva@email.com', 'password123', 'Lucas Silva', 'PLAYER', 'BR', '+55 11 98765 4321', '66531', 'PENDING', '/img/user_avatar.jpg', true, 'pt', 'utc-3', true),
  (4, 'elena.rostova@email.com', 'password123', 'Elena Rostova', 'PLAYER', 'RU', '+7 903 123 4567', '57209', 'IN_REVIEW', '/img/user_avatar.jpg', false, 'en', 'utc+1', true),
  (5, 'mateo.rossi@email.com', 'password123', 'Mateo Rossi', 'PLAYER', 'IT', '+39 02 1234567', '43901', 'PENDING', '/img/user_avatar.jpg', false, 'en', 'utc+1', true),
  (6, 'chloe.bennet@email.com', 'password123', 'Chloe Bennet', 'PLAYER', 'UK', '+44 20 7946 0919', '38190', 'APPROVED', '/img/user_avatar.jpg', true, 'en', 'utc+0', true),
  (7, 'paola.castro@control-desk.com', 'operator123', 'Paola Castro', 'EMPLOYEE', 'ES', '+34 600 000 001', 'EMP001', 'APPROVED', '/img/employee_avatar.png', false, 'es', 'utc-5', true),
  (8, 'emp90210@verify.com', 'operator123', 'Paola Castro', 'EMPLOYEE', 'ES', '+34 600 000 002', 'EMP002', 'APPROVED', '/img/employee_avatar.png', false, 'en', 'utc-5', true),
  (9, 'marco.silva@control-desk.com', 'operator123', 'Marco Silva', 'EMPLOYEE', 'ES', '+34 600 000 003', 'EMP003', 'APPROVED', '/img/employee_avatar.png', false, 'en', 'utc-5', true);

-- ----------------------------------------------------------------------------
-- 3. SINCRONIZACIÓN DE CASOS KYC (MERGE INTO KEY(id))
--    Sincroniza estados, timestamps de revisión, operadores y métricas de riesgo.
-- ----------------------------------------------------------------------------
MERGE INTO kyc_cases (id, case_number, user_id, account_number, player_name, country, account_status, status, priority, risk_score, reason, assigned_to, decision_notes, assigned_at, in_review_at, resolved_at, submitted_at)
KEY (id)
VALUES
  (1, 'DV-1024', 1, '80948', 'Carlos Mendoza', 'CO', 'Active', 'in_review', 'high', 88, 'proactive_kyc', 'Paola Castro', 'Tier 1 high roller volume trigger. Document verification ongoing.', TIMESTAMP '2026-09-02 09:30:00', TIMESTAMP '2026-09-02 10:00:00', NULL, TIMESTAMP '2026-09-02 08:15:00'),
  (2, 'DV-1023', 2, '78125', 'Paola Castro Player', 'CO', 'Suspended', 'pending', 'critical', 95, 'fraud_review', 'Unassigned', 'Suspicious login pattern detected from different IP subnets.', NULL, NULL, NULL, TIMESTAMP '2026-09-04 11:20:00'),
  (3, 'DV-1022', 3, '66531', 'Lucas Silva', 'BR', 'Active', 'pending', 'medium', 42, 'proactive_kyc', 'Paola Castro', 'Proactive onboarding KYC check.', TIMESTAMP '2026-09-05 14:10:00', NULL, NULL, TIMESTAMP '2026-09-05 13:00:00'),
  (4, 'DV-1021', 4, '57209', 'Elena Rostova', 'RU', 'Active', 'approved', 'low', 18, 'fraud_review', 'Paola Castro', 'Secondary verification cleared. Identity verified.', TIMESTAMP '2026-09-06 09:00:00', TIMESTAMP '2026-09-06 09:15:00', TIMESTAMP '2026-09-06 09:45:00', TIMESTAMP '2026-09-06 08:30:00'),
  (5, 'DV-1020', 5, '43901', 'Mateo Rossi', 'IT', 'Active', 'pending', 'high', 79, 'withdrawal_threshold', 'Unassigned', 'Withdrawal request of $12,500 exceeds verification threshold.', NULL, NULL, NULL, TIMESTAMP '2026-09-07 16:45:00'),
  (6, 'DV-1019', 6, '38190', 'Chloe Bennet', 'UK', 'Active', 'in_review', 'medium', 55, 'high_frequency', 'Marco Silva', 'High frequency betting alert on Premier League.', TIMESTAMP '2026-09-08 10:00:00', TIMESTAMP '2026-09-08 10:30:00', NULL, TIMESTAMP '2026-09-08 09:00:00'),
  (7, 'DV-1018', 1, '80948', 'Carlos Mendoza', 'CO', 'Active', 'approved', 'low', 25, 'proactive_kyc', 'Paola Castro', 'Annual account re-verification cleared.', TIMESTAMP '2026-08-15 11:00:00', TIMESTAMP '2026-08-15 11:20:00', TIMESTAMP '2026-08-15 11:45:00', TIMESTAMP '2026-08-15 10:00:00'),
  (8, 'DV-1017', 3, '66531', 'Lucas Silva', 'BR', 'Active', 'approved', 'low', 15, 'withdrawal_threshold', 'Paola Castro', 'Documents approved.', TIMESTAMP '2026-08-20 15:00:00', TIMESTAMP '2026-08-20 15:15:00', TIMESTAMP '2026-08-20 15:30:00', TIMESTAMP '2026-08-20 14:00:00'),
  (9, 'DV-1016', 5, '43901', 'Mateo Rossi', 'IT', 'Suspended', 'rejected', 'critical', 98, 'pep_sanctions', 'Marco Silva', 'Sanctions list match confirmed.', TIMESTAMP '2026-08-22 08:30:00', TIMESTAMP '2026-08-22 09:00:00', TIMESTAMP '2026-08-22 09:30:00', TIMESTAMP '2026-08-22 08:00:00'),
  (10, 'DV-1015', 6, '38190', 'Chloe Bennet', 'UK', 'Active', 'approved', 'low', 20, 'proactive_kyc', 'Paola Castro', 'Passport verified.', TIMESTAMP '2026-07-10 10:00:00', TIMESTAMP '2026-07-10 10:15:00', TIMESTAMP '2026-07-10 10:30:00', TIMESTAMP '2026-07-10 09:30:00'),
  (11, 'DV-1014', 4, '57209', 'Elena Rostova', 'RU', 'Active', 'approved', 'low', 22, 'withdrawal_threshold', 'Marco Silva', 'Address verified.', TIMESTAMP '2026-06-18 12:00:00', TIMESTAMP '2026-06-18 12:20:00', TIMESTAMP '2026-06-18 12:40:00', TIMESTAMP '2026-06-18 11:00:00');

-- ----------------------------------------------------------------------------
-- 4. SINCRONIZACIÓN DE DOCUMENTOS DE KYC (MERGE INTO KEY(id))
-- ----------------------------------------------------------------------------
MERGE INTO kyc_documents (id, case_id, document_type, file_name, file_path, file_size, mime_type, status)
KEY (id)
VALUES
  (1, 1, 'NATIONAL_ID', 'carlos_cedula_front.jpg', '/uploads/carlos_cedula_front.jpg', 2450000, 'image/jpeg', 'VERIFIED'),
  (2, 1, 'UTILITY_BILL', 'carlos_recibo_publico.pdf', '/uploads/carlos_recibo_publico.pdf', 1230000, 'application/pdf', 'PENDING'),
  (3, 2, 'PASSPORT', 'paola_passport.jpg', '/uploads/paola_passport.jpg', 3100000, 'image/jpeg', 'PENDING'),
  (4, 3, 'PASSPORT', 'lucas_silva_passport.jpg', '/uploads/lucas_silva_passport.jpg', 1850000, 'image/jpeg', 'PENDING'),
  (5, 4, 'NATIONAL_ID', 'elena_rostova_id.jpg', '/uploads/elena_rostova_id.jpg', 2100000, 'image/jpeg', 'VERIFIED'),
  (6, 5, 'PASSPORT', 'mateo_rossi_passport.jpg', '/uploads/mateo_rossi_passport.jpg', 2900000, 'image/jpeg', 'PENDING'),
  (7, 6, 'DRIVERS_LICENSE', 'chloe_bennet_dl.jpg', '/uploads/chloe_bennet_dl.jpg', 1950000, 'image/jpeg', 'VERIFIED');

-- ----------------------------------------------------------------------------
-- 5. SINCRONIZACIÓN DE REGISTROS DE SEGUIMIENTO (MERGE INTO KEY(id))
-- ----------------------------------------------------------------------------
MERGE INTO seguimiento (id, case_id, internal_user_id, verification_decision, account_status_decision, comments, created_at)
KEY (id)
VALUES
  (1, 4, 7, 'Approve Verification', 'Reactivate Account', 'Identity confirmed from valid passport and facial biometrics.', TIMESTAMP '2026-09-06 09:45:00'),
  (2, 7, 7, 'Approve Verification', 'Reactivate Account', 'Annual re-verification cleared successfully.', TIMESTAMP '2026-08-15 11:45:00'),
  (3, 8, 7, 'Approve Verification', 'Reactivate Account', 'Proof of address within 90 days validity window.', TIMESTAMP '2026-08-20 15:30:00'),
  (4, 9, 9, 'Reject Verification (Needs Reupload)', 'Permanently Close Account', 'Sanctions list match confirmed, account permanently restricted.', TIMESTAMP '2026-08-22 09:30:00'),
  (5, 10, 7, 'Approve Verification', 'Reactivate Account', 'Passport verified without anomalies.', TIMESTAMP '2026-07-10 10:30:00'),
  (6, 11, 9, 'Approve Verification', 'Reactivate Account', 'Address utility bill validated.', TIMESTAMP '2026-06-18 12:40:00');

-- ----------------------------------------------------------------------------
-- 6. SINCRONIZACIÓN DE BITÁCORA DE ACTIVIDAD (MERGE INTO KEY(id))
-- ----------------------------------------------------------------------------
MERGE INTO activity_logs (id, fecha, usuario_interno_id, tipo_evento, id_verification, detalles)
KEY (id)
VALUES
  (1, TIMESTAMP '2026-09-02 09:30:00', 7, 'Assigned Case', 1, 'Operator assigned case DV-1024 to herself'),
  (2, TIMESTAMP '2026-09-06 09:45:00', 7, 'Approved Case', 4, 'Case DV-1021 cleared and approved after document inspection'),
  (3, TIMESTAMP '2026-09-04 11:20:00', 7, 'Risk Flagged', 2, 'Suspicious IP pattern detected score 95'),
  (4, TIMESTAMP '2026-09-05 14:10:00', 7, 'Status Changed', 3, 'Moved case DV-1022 to Pending Review'),
  (5, TIMESTAMP '2026-08-22 09:30:00', 9, 'Rejected Case', 9, 'Sanctions list match confirmed, account suspended');

-- ----------------------------------------------------------------------------
-- 7. SINCRONIZACIÓN DE AUDIT LOGS (MERGE INTO KEY(id))
-- ----------------------------------------------------------------------------
MERGE INTO audit_logs (id, operator_name, case_number, action, details, created_at)
KEY (id)
VALUES
  (1, 'Paola Castro', 'DV-1024', 'Assigned Case', 'Operator assigned case DV-1024 to herself', TIMESTAMP '2026-09-02 09:30:00'),
  (2, 'Paola Castro', 'DV-1021', 'Approved Case', 'Case DV-1021 cleared and approved after document inspection', TIMESTAMP '2026-09-06 09:45:00'),
  (3, 'Paola Castro', 'DV-1023', 'Risk Flagged', 'Suspicious IP pattern detected score 95', TIMESTAMP '2026-09-04 11:20:00'),
  (4, 'Paola Castro', 'DV-1022', 'Status Changed', 'Moved case DV-1022 to Pending Review', TIMESTAMP '2026-09-05 14:10:00'),
  (5, 'Marco Silva', 'DV-1016', 'Rejected Case', 'Sanctions list match confirmed, account suspended', TIMESTAMP '2026-08-22 09:30:00');

-- ----------------------------------------------------------------------------
-- 8. REAJUSTE SEGURO DE SECUENCIAS
-- ----------------------------------------------------------------------------
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE kyc_cases ALTER COLUMN id RESTART WITH 100;
ALTER TABLE kyc_documents ALTER COLUMN id RESTART WITH 100;
ALTER TABLE seguimiento ALTER COLUMN id RESTART WITH 100;
ALTER TABLE activity_logs ALTER COLUMN id RESTART WITH 100;
ALTER TABLE audit_logs ALTER COLUMN id RESTART WITH 100;
