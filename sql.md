-- =============================================
-- 1. Database Creation
-- =============================================
-- CREATE DATABASE IF NOT EXISTS epqas_db
--   CHARACTER SET utf8mb4 
--   COLLATE utf8mb4_unicode_ci;
-- 
-- USE epqas_db;

-- =============================================
-- 2. User & Permission Management (Obj 1 & 7)
-- =============================================

-- Roles Table
CREATE TABLE IF NOT EXISTS roles (
    role_id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique identifier for the role',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT 'Name of the role (e.g., Administrator, Question Setter)',
    description VARCHAR(255) COMMENT 'Description of permissions associated with this role'
) COMMENT='Stores the different user roles for Permission Management';

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique system identifier for the user',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Login username',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Encrypted password string (Security Obj 7)',
    real_name VARCHAR(100) COMMENT 'Real name of the user for display',
    role_id INT COMMENT 'Foreign key linking to the user role',
    email VARCHAR(100) COMMENT 'Contact email',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Account creation time',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Flag to soft-delete users',
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
) COMMENT='Stores login credentials and basic profile info for all users';

-- Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique log identifier',
    user_id BIGINT COMMENT 'The user who performed the action',
    action_type VARCHAR(50) COMMENT 'Type of action (e.g., LOGIN, EXPORT_GRADES)',
    target_table VARCHAR(50) COMMENT 'The database table affected',
    target_id BIGINT COMMENT 'The ID of the record accessed/modified',
    ip_address VARCHAR(45) COMMENT 'IP address for security tracking',
    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp of the event',
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) COMMENT='Security and traceability log (Obj 7)';

-- =============================================
-- 3. Academic Data & Question Bank (Obj 1)
-- =============================================

-- Courses Table
CREATE TABLE IF NOT EXISTS courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique course identifier',
    course_name VARCHAR(100) NOT NULL COMMENT 'Full name of the course',
    course_code VARCHAR(50) COMMENT 'University code for the course'
) COMMENT='Stores course metadata';

-- Classes Table
CREATE TABLE IF NOT EXISTS classes (
    class_id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique class identifier',
    class_name VARCHAR(100) COMMENT 'Name of the class (e.g., CS-2024-A)',
    department VARCHAR(100) COMMENT 'Department the class belongs to'
) COMMENT='Organizational unit for students and examinations';

-- Students Table
CREATE TABLE IF NOT EXISTS students (
    student_id BIGINT PRIMARY KEY COMMENT 'Links to users.user_id',
    class_id INT COMMENT 'The administrative class',
    student_number VARCHAR(50) UNIQUE COMMENT 'Official student roll number',
    FOREIGN KEY (student_id) REFERENCES users(user_id),
    FOREIGN KEY (class_id) REFERENCES classes(class_id)
) COMMENT='Extension of User table for Student attributes';

-- Knowledge Points Table
CREATE TABLE IF NOT EXISTS knowledge_points (
    point_id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique KP identifier',
    course_id INT COMMENT 'The course context',
    point_name VARCHAR(200) NOT NULL COMMENT 'Name of the concept',
    description TEXT COMMENT 'Detailed description',
    FOREIGN KEY (course_id) REFERENCES courses(course_id)
) COMMENT='Granular knowledge points for Coverage analysis';

-- Questions Table
CREATE TABLE IF NOT EXISTS questions (
    question_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique question identifier',
    course_id INT COMMENT 'Course context',
    creator_id BIGINT COMMENT 'Teacher who set the question',
    question_content TEXT NOT NULL COMMENT 'The main text/stem',
    question_type ENUM('MCQ', 'TrueFalse', 'FillBlank', 'Essay') NOT NULL COMMENT 'Format',
    options_json JSON COMMENT 'JSON options (e.g. {"A":".."}) for objective questions',
    correct_answer TEXT COMMENT 'The correct answer key',
    initial_difficulty FLOAT COMMENT 'Estimated difficulty (0-1) set by teacher',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    FOREIGN KEY (course_id) REFERENCES courses(course_id),
    FOREIGN KEY (creator_id) REFERENCES users(user_id)
) COMMENT='The central repository of questions (Question Bank)';

-- Question-Knowledge Map
CREATE TABLE IF NOT EXISTS question_knowledge_map (
    question_id BIGINT COMMENT 'ID of the question',
    point_id INT COMMENT 'ID of the knowledge point',
    PRIMARY KEY (question_id, point_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id),
    FOREIGN KEY (point_id) REFERENCES knowledge_points(point_id)
) COMMENT='Maps questions to knowledge points';

-- =============================================
-- 4. Examination Paper & Exam Construction (Obj 1)
-- =============================================

-- Examination Papers (Templates)
-- RENAMED from test_papers to examination_papers
CREATE TABLE IF NOT EXISTS examination_papers (
    paper_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique paper identifier',
    title VARCHAR(200) NOT NULL COMMENT 'Title of the examination paper',
    course_id INT COMMENT 'Course subject',
    setter_id BIGINT COMMENT 'Teacher who designed it',
    total_score INT DEFAULT 100 COMMENT 'Total marks available',
    duration_minutes INT COMMENT 'Allowed time',
    target_difficulty FLOAT COMMENT 'Intended difficulty level',
    status ENUM('Draft', 'Published', 'Archived') DEFAULT 'Draft' COMMENT 'Lifecycle status',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    FOREIGN KEY (setter_id) REFERENCES users(user_id)
) COMMENT='Stores structure/metadata of an examination paper template';

-- Examination Paper Structure (Questions in a Paper)
CREATE TABLE IF NOT EXISTS examination_paper_questions (
    paper_id BIGINT COMMENT 'ID of the examination paper',
    question_id BIGINT COMMENT 'ID of the question',
    score_value DECIMAL(5,2) COMMENT 'Points assigned in this paper',
    question_order INT COMMENT 'Sequence number',
    PRIMARY KEY (paper_id, question_id),
    FOREIGN KEY (paper_id) REFERENCES examination_papers(paper_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
) COMMENT='Links questions to examination papers with specific scores';

-- Examinations (Instances)
-- The actual event where a class takes an examination paper
CREATE TABLE IF NOT EXISTS examinations (
    exam_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique examination instance ID',
    paper_id BIGINT COMMENT 'The examination paper template used',
    class_id INT COMMENT 'The class taking the examination',
    exam_date DATETIME COMMENT 'Date/Time of examination',
    total_candidates INT COMMENT 'Expected student count',
    actual_examinees INT COMMENT 'Actual submission count',
    FOREIGN KEY (paper_id) REFERENCES examination_papers(paper_id),
    FOREIGN KEY (class_id) REFERENCES classes(class_id)
) COMMENT='Records actual administration of an examination';

-- =============================================
-- 5. Student Grades & Answers (Obj 6)
-- =============================================

-- Examination Results
CREATE TABLE IF NOT EXISTS student_exam_results (
    result_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique result identifier',
    exam_id BIGINT COMMENT 'The examination instance',
    student_id BIGINT COMMENT 'The student',
    total_score DECIMAL(5,2) COMMENT 'Final score obtained',
    is_absent BOOLEAN DEFAULT FALSE COMMENT 'Flag if absent',
    submitted_at DATETIME COMMENT 'Time of submission',
    FOREIGN KEY (exam_id) REFERENCES examinations(exam_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id)
) COMMENT='Summary results of a student for a specific examination';

-- Student Answers (Detailed)
CREATE TABLE IF NOT EXISTS student_answers (
    answer_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique answer identifier',
    result_id BIGINT COMMENT 'Link to result header',
    question_id BIGINT COMMENT 'The question answered',
    student_choice TEXT COMMENT 'Actual option selected or text written',
    score_obtained DECIMAL(5,2) COMMENT 'Score awarded',
    is_correct BOOLEAN COMMENT 'Binary correct/incorrect flag',
    FOREIGN KEY (result_id) REFERENCES student_exam_results(result_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
) COMMENT='Detailed answer logs for analysis';

-- =============================================
-- 6. Quality Analysis & Metrics (Obj 2, 3, 5)
-- =============================================

-- Examination Paper Quality Analysis
CREATE TABLE IF NOT EXISTS examination_paper_quality_analysis (
    analysis_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique analysis identifier',
    exam_id BIGINT UNIQUE COMMENT 'Link to examination instance',
    average_score DECIMAL(5,2) COMMENT 'Mean score',
    std_deviation DECIMAL(5,2) COMMENT 'Standard deviation',
    highest_score DECIMAL(5,2) COMMENT 'Max score',
    lowest_score DECIMAL(5,2) COMMENT 'Min score',
    reliability_coefficient FLOAT COMMENT 'Cronbachs Alpha',
    validity_coefficient FLOAT COMMENT 'Validity coeff',
    knowledge_coverage_rate FLOAT COMMENT 'Percentage of syllabus covered',
    overall_difficulty FLOAT COMMENT 'Calculated actual difficulty',
    overall_discrimination FLOAT COMMENT 'Calculated discrimination',
    is_abnormal BOOLEAN DEFAULT FALSE COMMENT 'Flag if metrics breach thresholds',
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Calculation time',
    FOREIGN KEY (exam_id) REFERENCES examinations(exam_id)
) COMMENT='Computed quality indicators for the whole examination paper';

-- Question Quality Analysis
CREATE TABLE IF NOT EXISTS question_quality_analysis (
    q_analysis_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique identifier',
    exam_id BIGINT COMMENT 'The examination context',
    question_id BIGINT COMMENT 'The question analyzed',
    correct_response_rate FLOAT COMMENT 'Actual Difficulty (P-Value)',
    discrimination_index FLOAT COMMENT 'Difference between high/low groups',
    selection_distribution_json JSON COMMENT 'Stats on options chosen',
    is_too_easy BOOLEAN DEFAULT FALSE COMMENT 'Flag if Correct Rate > 90%',
    is_low_discrimination BOOLEAN DEFAULT FALSE COMMENT 'Flag if D-Index is low',
    diagnosis_tag VARCHAR(100) COMMENT 'Text reason for anomaly',
    FOREIGN KEY (exam_id) REFERENCES examinations(exam_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
) COMMENT='Performance metrics for individual questions';

-- =============================================
-- 7. Intelligent Suggestions (Obj 4)
-- =============================================

-- Improvement Suggestions
CREATE TABLE IF NOT EXISTS improvement_suggestions (
    suggestion_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique suggestion identifier',
    exam_id BIGINT COMMENT 'The examination analyzed',
    question_id BIGINT NULL COMMENT 'Specific question (NULL if for whole paper)',
    suggestion_type ENUM('Paper_Structure', 'Question_Content', 'Difficulty_Adj') COMMENT 'Category',
    suggestion_text TEXT NOT NULL COMMENT 'Actionable advice',
    generated_rule VARCHAR(100) COMMENT 'Rule triggered',
    is_implemented BOOLEAN DEFAULT FALSE COMMENT 'Feedback loop',
    FOREIGN KEY (exam_id) REFERENCES examinations(exam_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
) COMMENT='AI/Rule-based recommendations';