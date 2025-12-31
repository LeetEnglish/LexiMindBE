-- V4: Create chat session and message tables for AI tutoring

-- Chat Sessions table
CREATE TABLE chat_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    session_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    context TEXT,
    message_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP
);

-- Chat Messages table
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    chat_session_id BIGINT NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    tokens_used INTEGER,
    processing_time_ms INTEGER,
    grammar_issues TEXT,
    vocabulary_suggestions TEXT,
    corrected_text TEXT,
    improvement_score DECIMAL(3,1),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_chat_sessions_user ON chat_sessions(user_id);
CREATE INDEX idx_chat_sessions_status ON chat_sessions(status);
CREATE INDEX idx_chat_sessions_type ON chat_sessions(session_type);
CREATE INDEX idx_chat_sessions_last_message ON chat_sessions(last_message_at DESC);
CREATE INDEX idx_chat_messages_session ON chat_messages(chat_session_id);
CREATE INDEX idx_chat_messages_role ON chat_messages(role);
