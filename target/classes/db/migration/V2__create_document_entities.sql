-- V2__create_document_entities.sql
-- Document intelligence tables

-- Documents table (uploaded files)
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    raw_content TEXT,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_lessons INTEGER DEFAULT 0,
    completed_lessons INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Lessons table (parsed chapters from documents)
CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    summary TEXT,
    content TEXT,
    order_index INTEGER NOT NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    difficulty_level VARCHAR(10) DEFAULT 'B1',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Flashcards table (SRS vocabulary cards)
CREATE TABLE flashcards (
    id BIGSERIAL PRIMARY KEY,
    front TEXT NOT NULL,
    back TEXT NOT NULL,
    example TEXT,
    phonetic VARCHAR(255),
    card_type VARCHAR(50) NOT NULL DEFAULT 'VOCABULARY',
    document_id BIGINT REFERENCES documents(id) ON DELETE SET NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    -- SRS fields (SM-2 algorithm)
    ease_factor DECIMAL(4,2) NOT NULL DEFAULT 2.50,
    interval INTEGER NOT NULL DEFAULT 0,
    repetitions INTEGER NOT NULL DEFAULT 0,
    next_review_date TIMESTAMP,
    last_reviewed_date TIMESTAMP,
    is_mastered BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Exercises table (generated exercises from lessons)
CREATE TABLE exercises (
    id BIGSERIAL PRIMARY KEY,
    question TEXT NOT NULL,
    exercise_type VARCHAR(50) NOT NULL,
    correct_answer VARCHAR(500) NOT NULL,
    explanation TEXT,
    lesson_id BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Exercise options table (for MCQ)
CREATE TABLE exercise_options (
    exercise_id BIGINT NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    option_text VARCHAR(500) NOT NULL
);

-- Indexes for performance
CREATE INDEX idx_documents_user ON documents(user_id);
CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_lessons_document ON lessons(document_id);
CREATE INDEX idx_lessons_order ON lessons(document_id, order_index);
CREATE INDEX idx_flashcards_user ON flashcards(user_id);
CREATE INDEX idx_flashcards_document ON flashcards(document_id);
CREATE INDEX idx_flashcards_next_review ON flashcards(user_id, next_review_date);
CREATE INDEX idx_flashcards_mastered ON flashcards(user_id, is_mastered);
CREATE INDEX idx_exercises_lesson ON exercises(lesson_id);
