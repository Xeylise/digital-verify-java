-- Schema for Digital Verify KYC & Gaming Platform

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'PLAYER',
    country VARCHAR(10) DEFAULT 'CO',
    phone VARCHAR(50),
    account_number VARCHAR(50) DEFAULT '80948',
    kyc_status VARCHAR(50) DEFAULT 'PENDING',
    avatar_url VARCHAR(255) DEFAULT '/img/user_avatar.jpg',
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    language VARCHAR(20) DEFAULT 'en',
    timezone VARCHAR(50) DEFAULT 'utc-5',
    queue_alerts_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS kyc_cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    account_number VARCHAR(50) DEFAULT '80948',
    player_name VARCHAR(255) NOT NULL,
    country VARCHAR(10) DEFAULT 'CO',
    account_status VARCHAR(50) DEFAULT 'Active',
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    priority VARCHAR(50) DEFAULT 'medium',
    risk_score INT DEFAULT 50,
    reason VARCHAR(100) NOT NULL,
    assigned_to VARCHAR(255) DEFAULT 'Unassigned',
    decision_notes TEXT,
    assigned_at TIMESTAMP,
    in_review_at TIMESTAMP,
    resolved_at TIMESTAMP,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_case_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

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

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_name VARCHAR(255) NOT NULL,
    case_number VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS kyc_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT DEFAULT 0,
    mime_type VARCHAR(100),
    status VARCHAR(50) DEFAULT 'PENDING',
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doc_case FOREIGN KEY (case_id) REFERENCES kyc_cases(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bet_reference VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    selection VARCHAR(255) NOT NULL,
    odds DECIMAL(10, 2) NOT NULL,
    stake DECIMAL(12, 2) NOT NULL,
    payout DECIMAL(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    placed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    category VARCHAR(50) DEFAULT 'General',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
