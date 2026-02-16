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

-- Table: role (Renamed from roles)
CREATE TABLE IF NOT EXISTS role (
    role_id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique identifier for the role',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT 'Name of the role (e.g., Administrator, Question Setter)',
    description VARCHAR(255) COMMENT 'Description of permissions associated with this role'
) COMMENT='Stores the different user roles';

-- Table: user (Renamed from users)
CREATE TABLE IF NOT EXISTS user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique system identifier for the user',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Login username',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Encrypted password string',
    real_name VARCHAR(100) COMMENT 'Real name of the user for display',
    role_id INT COMMENT 'Foreign key linking to the role',
    email VARCHAR(100) COMMENT 'Contact email',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Account creation time',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Flag to soft-delete users',
    FOREIGN KEY (role_id) REFERENCES role(role_id)
) COMMENT='Stores login credentials and basic profile info';

-- Table: audit_log (Renamed from audit_logs)
CREATE TABLE IF NOT EXISTS audit_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique log identifier',
    user_id BIGINT COMMENT 'The user who performed the action',
    action_type VARCHAR(50) COMMENT 'Type of action (e.g., LOGIN, EXPORT_GRADES)',
    target_table VARCHAR(50) COMMENT 'The database table affected',
    target_id BIGINT COMMENT 'The ID of the record accessed/modified',
    ip_address VARCHAR(45) COMMENT 'IP address for security tracking',
    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp of the event',
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) COMMENT='Security and traceability log';

-- =============================================
-- 3. Academic Data & Question Bank (Obj 1)
-- =============================================

-- Table: course (Renamed from courses)
-- Note: This represents the "Subject" (e.g., Mathematics, Java Programming)
CREATE TABLE IF NOT EXISTS course (
    course_id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique course identifier',
    course_name VARCHAR(100) NOT NULL COMMENT 'Full name of the course/subject',
    course_code VARCHAR(50) COMMENT 'University code for the course'
) COMMENT='Stores course metadata';

-- Table: school_class (Renamed from classes)
-- Changed to avoid Java "class" keyword conflict
CREATE TABLE IF NOT EXISTS school_class (
    class_id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique class identifier',
    class_name VARCHAR(100) COMMENT 'Name of the class (e.g., CS-2024-A)',
    department VARCHAR(100) COMMENT 'Department the class belongs to'
) COMMENT='Organizational unit for a group of students';

-- Table: student (Renamed from students)
CREATE TABLE IF NOT EXISTS student (
    student_id BIGINT PRIMARY KEY COMMENT 'Links to user.user_id',
    class_id INT COMMENT 'The administrative school_class',
    student_number VARCHAR(50) UNIQUE COMMENT 'Official student roll number',
    FOREIGN KEY (student_id) REFERENCES user(user_id),
    FOREIGN KEY (class_id) REFERENCES school_class(class_id)
) COMMENT='Extension of User table for Student attributes';

-- Table: knowledge_point (Renamed from knowledge_points)
CREATE TABLE IF NOT EXISTS knowledge_point (
    point_id INT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique KP identifier',
    course_id INT COMMENT 'The course context',
    point_name VARCHAR(200) NOT NULL COMMENT 'Name of the concept',
    description TEXT COMMENT 'Detailed description',
    FOREIGN KEY (course_id) REFERENCES course(course_id)
) COMMENT='Granular knowledge points for Coverage analysis';

-- Table: question (Renamed from questions)
CREATE TABLE IF NOT EXISTS question (
    question_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique question identifier',
    course_id INT COMMENT 'Course context',
    creator_id BIGINT COMMENT 'Teacher who set the question',
    question_content TEXT NOT NULL COMMENT 'The main text/stem',
    question_type ENUM('MCQ', 'TrueFalse', 'FillBlank', 'Essay') NOT NULL COMMENT 'Format',
    options_json JSON COMMENT 'JSON options (e.g. {"A":".."}) for objective questions',
    correct_answer TEXT COMMENT 'The correct answer key',
    initial_difficulty FLOAT COMMENT 'Estimated difficulty (0-1) set by teacher',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    FOREIGN KEY (course_id) REFERENCES course(course_id),
    FOREIGN KEY (creator_id) REFERENCES user(user_id)
) COMMENT='The central repository of questions';

-- Table: question_knowledge_map (Already singular concept)
CREATE TABLE IF NOT EXISTS question_knowledge_map (
    question_id BIGINT COMMENT 'ID of the question',
    point_id INT COMMENT 'ID of the knowledge point',
    PRIMARY KEY (question_id, point_id),
    FOREIGN KEY (question_id) REFERENCES question(question_id),
    FOREIGN KEY (point_id) REFERENCES knowledge_point(point_id)
) COMMENT='Maps questions to knowledge points';

-- =============================================
-- 4. Examination Paper & Exam Construction (Obj 1)
-- =============================================

-- Table: examination_paper (Renamed from examination_papers)
CREATE TABLE IF NOT EXISTS examination_paper (
    paper_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique paper identifier',
    title VARCHAR(200) NOT NULL COMMENT 'Title of the examination paper',
    course_id INT COMMENT 'Course subject',
    setter_id BIGINT COMMENT 'Teacher who designed it',
    total_score INT DEFAULT 100 COMMENT 'Total marks available',
    duration_minutes INT COMMENT 'Allowed time',
    target_difficulty FLOAT COMMENT 'Intended difficulty level',
    status ENUM('Draft', 'Published', 'Archived') DEFAULT 'Draft' COMMENT 'Lifecycle status',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    FOREIGN KEY (setter_id) REFERENCES user(user_id)
) COMMENT='Stores structure of an examination paper template';

-- Table: examination_paper_question (Renamed from examination_paper_questions)
CREATE TABLE IF NOT EXISTS examination_paper_question (
    paper_id BIGINT COMMENT 'ID of the examination paper',
    question_id BIGINT COMMENT 'ID of the question',
    score_value DECIMAL(5,2) COMMENT 'Points assigned in this paper',
    question_order INT COMMENT 'Sequence number',
    PRIMARY KEY (paper_id, question_id),
    FOREIGN KEY (paper_id) REFERENCES examination_paper(paper_id),
    FOREIGN KEY (question_id) REFERENCES question(question_id)
) COMMENT='Links questions to examination papers with specific scores';

-- Table: examination (Renamed from examinations)
CREATE TABLE IF NOT EXISTS examination (
    exam_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique examination instance ID',
    paper_id BIGINT COMMENT 'The examination paper template used',
    class_id INT COMMENT 'The class taking the examination',
    exam_date DATETIME COMMENT 'Date/Time of examination',
    total_candidates INT COMMENT 'Expected student count',
    actual_examinees INT COMMENT 'Actual submission count',
    FOREIGN KEY (paper_id) REFERENCES examination_paper(paper_id),
    FOREIGN KEY (class_id) REFERENCES school_class(class_id)
) COMMENT='Records actual administration of an examination';

-- =============================================
-- 5. Student Grades & Answers (Obj 6)
-- =============================================

-- Table: student_exam_result (Renamed from student_exam_results)
CREATE TABLE IF NOT EXISTS student_exam_result (
    result_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique result identifier',
    exam_id BIGINT COMMENT 'The examination instance',
    student_id BIGINT COMMENT 'The student',
    total_score DECIMAL(5,2) COMMENT 'Final score obtained',
    is_absent BOOLEAN DEFAULT FALSE COMMENT 'Flag if absent',
    submitted_at DATETIME COMMENT 'Time of submission',
    FOREIGN KEY (exam_id) REFERENCES examination(exam_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id)
) COMMENT='Summary results of a student for a specific examination';

-- Table: student_answer (Renamed from student_answers)
CREATE TABLE IF NOT EXISTS student_answer (
    answer_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique answer identifier',
    result_id BIGINT COMMENT 'Link to result header',
    question_id BIGINT COMMENT 'The question answered',
    student_choice TEXT COMMENT 'Actual option selected or text written',
    score_obtained DECIMAL(5,2) COMMENT 'Score awarded',
    is_correct BOOLEAN COMMENT 'Binary correct/incorrect flag',
    FOREIGN KEY (result_id) REFERENCES student_exam_result(result_id),
    FOREIGN KEY (question_id) REFERENCES question(question_id)
) COMMENT='Detailed answer logs for analysis';

-- =============================================
-- 6. Quality Analysis & Metrics (Obj 2, 3, 5)
-- =============================================

-- Table: examination_paper_quality_analysis (Singular)
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
    FOREIGN KEY (exam_id) REFERENCES examination(exam_id)
) COMMENT='Computed quality indicators for the whole examination paper';

-- Table: question_quality_analysis (Singular)
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
    FOREIGN KEY (exam_id) REFERENCES examination(exam_id),
    FOREIGN KEY (question_id) REFERENCES question(question_id)
) COMMENT='Performance metrics for individual questions';

-- =============================================
-- 7. Intelligent Suggestions (Obj 4)
-- =============================================

-- Table: improvement_suggestion (Renamed from improvement_suggestions)
CREATE TABLE IF NOT EXISTS improvement_suggestion (
    suggestion_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Unique suggestion identifier',
    exam_id BIGINT COMMENT 'The examination analyzed',
    question_id BIGINT NULL COMMENT 'Specific question (NULL if for whole paper)',
    suggestion_type ENUM('Paper_Structure', 'Question_Content', 'Difficulty_Adj') COMMENT 'Category',
    suggestion_text TEXT NOT NULL COMMENT 'Actionable advice',
    generated_rule VARCHAR(100) COMMENT 'Rule triggered',
    is_implemented BOOLEAN DEFAULT FALSE COMMENT 'Feedback loop',
    FOREIGN KEY (exam_id) REFERENCES examination(exam_id),
    FOREIGN KEY (question_id) REFERENCES question(question_id)
) COMMENT='AI/Rule-based recommendations';