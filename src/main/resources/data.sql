-- Seed data for Digital Verify

-- 1. Users (Players and Employees - 2FA disabled for employees per specs)
INSERT INTO users (id, email, password_hash, full_name, role, country, phone, account_number, kyc_status, avatar_url, two_factor_enabled, language, timezone, queue_alerts_enabled)
VALUES
  (1, 'carlos.mendoza@email.com', 'password123', 'Carlos Mendoza', 'PLAYER', 'CO', '+57 300 123 4567', '80948', 'PENDING', '/img/user_avatar.jpg', false, 'es', 'utc-5', true),
  (2, 'pcastrosarabia@gmail.com', 'password123', 'Paola Castro Player', 'PLAYER', 'CO', '+57 311 987 6543', '78125', 'PENDING', '/img/user_avatar.jpg', true, 'es', 'utc-5', true),
  (3, 'lucas.silva@email.com', 'password123', 'Lucas Silva', 'PLAYER', 'BR', '+55 11 98765 4321', '66531', 'PENDING', '/img/user_avatar.jpg', true, 'pt', 'utc-3', true),
  (4, 'elena.rostova@email.com', 'password123', 'Elena Rostova', 'PLAYER', 'RU', '+7 903 123 4567', '57209', 'IN_REVIEW', '/img/user_avatar.jpg', false, 'en', 'utc+1', true),
  (5, 'mateo.rossi@email.com', 'password123', 'Mateo Rossi', 'PLAYER', 'IT', '+39 02 1234567', '43901', 'PENDING', '/img/user_avatar.jpg', false, 'en', 'utc+1', true),
  (6, 'chloe.bennet@email.com', 'password123', 'Chloe Bennet', 'PLAYER', 'UK', '+44 20 7946 0919', '38190', 'APPROVED', '/img/user_avatar.jpg', true, 'en', 'utc+0', true),
  (7, 'paola.castro@control-desk.com', 'operator123', 'Paola Castro', 'EMPLOYEE', 'ES', '+34 600 000 001', 'EMP001', 'APPROVED', '/img/employee_avatar.png', false, 'es', 'utc-5', true),
  (8, 'emp90210@verify.com', 'operator123', 'Paola Castro', 'EMPLOYEE', 'ES', '+34 600 000 002', 'EMP002', 'APPROVED', '/img/employee_avatar.png', false, 'en', 'utc-5', true),
  (9, 'marco.silva@control-desk.com', 'operator123', 'Marco Silva', 'EMPLOYEE', 'ES', '+34 600 000 003', 'EMP003', 'APPROVED', '/img/employee_avatar.png', false, 'en', 'utc-5', true)
ON DUPLICATE KEY UPDATE id = id;

-- 2. KYC Cases with priority, assigned_at, in_review_at, resolved_at and multi-month distribution
INSERT INTO kyc_cases (id, case_number, user_id, account_number, player_name, country, account_status, status, priority, risk_score, reason, assigned_to, decision_notes, assigned_at, in_review_at, resolved_at, submitted_at)
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
  (11, 'DV-1014', 4, '57209', 'Elena Rostova', 'RU', 'Active', 'approved', 'low', 22, 'withdrawal_threshold', 'Marco Silva', 'Address verified.', TIMESTAMP '2026-06-18 12:00:00', TIMESTAMP '2026-06-18 12:20:00', TIMESTAMP '2026-06-18 12:40:00', TIMESTAMP '2026-06-18 11:00:00')
ON DUPLICATE KEY UPDATE id = id;

-- 3. KYC Documents
INSERT INTO kyc_documents (id, case_id, document_type, file_name, file_path, file_size, mime_type, status)
VALUES
  (1, 1, 'NATIONAL_ID', 'carlos_cedula_front.jpg', '/uploads/carlos_cedula_front.jpg', 2450000, 'image/jpeg', 'VERIFIED'),
  (2, 1, 'UTILITY_BILL', 'carlos_recibo_publico.pdf', '/uploads/carlos_recibo_publico.pdf', 1230000, 'application/pdf', 'PENDING'),
  (3, 2, 'PASSPORT', 'paola_passport.jpg', '/uploads/paola_passport.jpg', 3100000, 'image/jpeg', 'PENDING'),
  (4, 3, 'PASSPORT', 'lucas_silva_passport.jpg', '/uploads/lucas_silva_passport.jpg', 1850000, 'image/jpeg', 'PENDING'),
  (5, 4, 'NATIONAL_ID', 'elena_rostova_id.jpg', '/uploads/elena_rostova_id.jpg', 2100000, 'image/jpeg', 'VERIFIED'),
  (6, 5, 'PASSPORT', 'mateo_rossi_passport.jpg', '/uploads/mateo_rossi_passport.jpg', 2900000, 'image/jpeg', 'PENDING'),
  (7, 6, 'DRIVERS_LICENSE', 'chloe_bennet_dl.jpg', '/uploads/chloe_bennet_dl.jpg', 1950000, 'image/jpeg', 'VERIFIED')
ON DUPLICATE KEY UPDATE id = id;

-- 4. Bets
INSERT INTO bets (bet_reference, user_id, event_name, selection, odds, stake, payout, status, placed_at)
VALUES
  ('BET-89421', 1, 'Real Madrid vs Manchester City', 'Real Madrid (Win)', 2.45, 50.00, 122.50, 'WON', CURRENT_TIMESTAMP),
  ('BET-89422', 1, 'Lakers vs Warriors', 'Warriors +4.5', 1.91, 100.00, 191.00, 'PENDING', CURRENT_TIMESTAMP),
  ('BET-89423', 1, 'Alcaraz vs Djokovic', 'Over 3.5 Sets', 1.75, 75.00, 131.25, 'LOST', CURRENT_TIMESTAMP),
  ('BET-89424', 1, 'Arsenal vs Chelsea', 'Both Teams to Score', 1.80, 60.00, 108.00, 'WON', CURRENT_TIMESTAMP),
  ('BET-89425', 1, 'Inter Miami vs Orlando City', 'Messi to score anytime', 1.65, 120.00, 198.00, 'PENDING', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE id = id;

-- 5. Notifications
INSERT INTO notifications (id, user_id, title, message, category, is_read, created_at)
VALUES
  (1, 1, 'KYC Verification Pending', 'Your identity verification documents have been received and are in the review queue.', 'Verification', false, CURRENT_TIMESTAMP),
  (2, 1, 'Deposit Successful', 'Your deposit of $200.00 via Credit Card has been confirmed.', 'Wallet', false, CURRENT_TIMESTAMP),
  (3, 1, 'Security Alert', 'New login detected from Bogota, Colombia.', 'Security', true, CURRENT_TIMESTAMP),
  (4, 7, 'High Risk Case Flagged', 'Case DV-1023 has been flagged for Suspended Fraud Review with risk score 95.', 'Alert', false, CURRENT_TIMESTAMP),
  (5, 7, 'Queue SLA Warning', '3 cases have been waiting in queue for over 45 minutes.', 'SLA', false, CURRENT_TIMESTAMP),
  (6, 8, 'High Risk Case Flagged', 'Case DV-1023 has been flagged for Suspended Fraud Review with risk score 95.', 'Alert', false, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE id = id;

-- 6. Seguimiento (Tracking decisions with verification and account status decisions)
INSERT INTO seguimiento (id, case_id, internal_user_id, verification_decision, account_status_decision, comments, created_at)
VALUES
  (1, 4, 7, 'Approve Verification', 'Reactivate Account', 'Identity confirmed from valid passport and facial biometrics.', TIMESTAMP '2026-09-06 09:45:00'),
  (2, 7, 7, 'Approve Verification', 'Reactivate Account', 'Annual re-verification cleared successfully.', TIMESTAMP '2026-08-15 11:45:00'),
  (3, 8, 7, 'Approve Verification', 'Reactivate Account', 'Proof of address within 90 days validity window.', TIMESTAMP '2026-08-20 15:30:00'),
  (4, 9, 9, 'Reject Verification (Needs Reupload)', 'Permanently Close Account', 'Sanctions list match confirmed, account permanently restricted.', TIMESTAMP '2026-08-22 09:30:00'),
  (5, 10, 7, 'Approve Verification', 'Reactivate Account', 'Passport verified without anomalies.', TIMESTAMP '2026-07-10 10:30:00'),
  (6, 11, 9, 'Approve Verification', 'Reactivate Account', 'Address utility bill validated.', TIMESTAMP '2026-06-18 12:40:00')
ON DUPLICATE KEY UPDATE id = id;

-- 7. Activity Logs (fecha, usuario_interno_id, tipo_evento, id_verification, detalles)
INSERT INTO activity_logs (id, fecha, usuario_interno_id, tipo_evento, id_verification, detalles)
VALUES
  (1, TIMESTAMP '2026-09-02 09:30:00', 7, 'Assigned Case', 1, 'Operator assigned case DV-1024 to herself'),
  (2, TIMESTAMP '2026-09-06 09:45:00', 7, 'Approved Case', 4, 'Case DV-1021 cleared and approved after document inspection'),
  (3, TIMESTAMP '2026-09-04 11:20:00', 7, 'Risk Flagged', 2, 'Suspicious IP pattern detected score 95'),
  (4, TIMESTAMP '2026-09-05 14:10:00', 7, 'Status Changed', 3, 'Moved case DV-1022 to Pending Review'),
  (5, TIMESTAMP '2026-08-22 09:30:00', 9, 'Rejected Case', 9, 'Sanctions list match confirmed, account suspended')
ON DUPLICATE KEY UPDATE id = id;

-- 8. Audit Logs
INSERT INTO audit_logs (id, operator_name, case_number, action, details, created_at)
VALUES
  (1, 'Paola Castro', 'DV-1024', 'Assigned Case', 'Operator assigned case DV-1024 to herself', TIMESTAMP '2026-09-02 09:30:00'),
  (2, 'Paola Castro', 'DV-1021', 'Approved Case', 'Case DV-1021 cleared and approved after document inspection', TIMESTAMP '2026-09-06 09:45:00'),
  (3, 'Paola Castro', 'DV-1023', 'Risk Flagged', 'Suspicious IP pattern detected score 95', TIMESTAMP '2026-09-04 11:20:00'),
  (4, 'Paola Castro', 'DV-1022', 'Status Changed', 'Moved case DV-1022 to Pending Review', TIMESTAMP '2026-09-05 14:10:00'),
  (5, 'Marco Silva', 'DV-1016', 'Rejected Case', 'Sanctions list match confirmed, account suspended', TIMESTAMP '2026-08-22 09:30:00')
ON DUPLICATE KEY UPDATE id = id;
