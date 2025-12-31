-- V3__create_mock_test_entities.sql
-- Mock test and assessment tables

-- Mock Tests table (test templates)
CREATE TABLE mock_tests (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    test_type VARCHAR(50) NOT NULL,
    skill_type VARCHAR(50) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    total_questions INTEGER NOT NULL,
    passing_score INTEGER,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    difficulty_level VARCHAR(50) DEFAULT 'INTERMEDIATE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Questions table
CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    correct_answer TEXT,
    explanation TEXT,
    passage TEXT,
    audio_url VARCHAR(500),
    image_url VARCHAR(500),
    points INTEGER NOT NULL DEFAULT 1,
    order_index INTEGER NOT NULL,
    mock_test_id BIGINT NOT NULL REFERENCES mock_tests(id) ON DELETE CASCADE,
    time_limit_seconds INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Question options (for MCQ)
CREATE TABLE question_options (
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_text VARCHAR(500) NOT NULL
);

-- Test Attempts table (user sessions)
CREATE TABLE test_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mock_test_id BIGINT NOT NULL REFERENCES mock_tests(id) ON DELETE CASCADE,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    total_score DECIMAL(10,2),
    max_score INTEGER,
    percentage_score DECIMAL(5,2),
    time_spent_seconds INTEGER,
    reading_score DECIMAL(5,2),
    listening_score DECIMAL(5,2),
    writing_score DECIMAL(5,2),
    speaking_score DECIMAL(5,2),
    feedback TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- User Responses table
CREATE TABLE user_responses (
    id BIGSERIAL PRIMARY KEY,
    test_attempt_id BIGINT NOT NULL REFERENCES test_attempts(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    user_answer TEXT,
    is_correct BOOLEAN,
    score DECIMAL(5,2),
    time_spent_seconds INTEGER,
    answered_at TIMESTAMP,
    -- AI scoring fields
    ai_feedback TEXT,
    grammar_score DECIMAL(5,2),
    vocabulary_score DECIMAL(5,2),
    coherence_score DECIMAL(5,2),
    task_achievement_score DECIMAL(5,2),
    audio_response_url VARCHAR(500),
    pronunciation_score DECIMAL(5,2),
    fluency_score DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_mock_tests_type ON mock_tests(test_type);
CREATE INDEX idx_mock_tests_published ON mock_tests(is_published);
CREATE INDEX idx_questions_mock_test ON questions(mock_test_id);
CREATE INDEX idx_test_attempts_user ON test_attempts(user_id);
CREATE INDEX idx_test_attempts_test ON test_attempts(mock_test_id);
CREATE INDEX idx_test_attempts_status ON test_attempts(status);
CREATE INDEX idx_user_responses_attempt ON user_responses(test_attempt_id);
CREATE INDEX idx_user_responses_question ON user_responses(question_id);
