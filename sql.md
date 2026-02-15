-- =============================================
-- 1. 数据库创建
-- =============================================
-- CREATE DATABASE IF NOT EXISTS epqas_db
--   CHARACTER SET utf8mb4
--   COLLATE utf8mb4_unicode_ci;
--------------------------------

-- USE epqas_db;

-- =============================================
-- 2. 用户与权限管理（目标1 & 7）
-- =============================================

-- 角色表
CREATE TABLE IF NOT EXISTS roles (
    role_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色唯一标识',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称（例如：管理员、出题人）',
    description VARCHAR(255) COMMENT '该角色关联的权限描述'
) COMMENT='存储权限管理所需的不同用户角色';

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户系统唯一标识',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '加密密码字符串（安全目标7）',
    real_name VARCHAR(100) COMMENT '用户显示用真实姓名',
    role_id INT COMMENT '关联用户角色的外键',
    email VARCHAR(100) COMMENT '联系邮箱',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建时间',
    is_active BOOLEAN DEFAULT TRUE COMMENT '用户软删除标识',
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
) COMMENT='存储所有用户的登录凭证和基础个人资料信息';

-- 审计日志表
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志唯一标识',
    user_id BIGINT COMMENT '执行操作的用户',
    action_type VARCHAR(50) COMMENT '操作类型（例如：登录、导出成绩）',
    target_table VARCHAR(50) COMMENT '受影响的数据库表',
    target_id BIGINT COMMENT '被访问/修改的记录ID',
    ip_address VARCHAR(45) COMMENT '安全追踪用IP地址',
    action_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间戳',
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) COMMENT='安全与可追溯性日志（目标7）';

-- =============================================
-- 3. 学业数据与题库（目标1）
-- =============================================

-- 课程表
CREATE TABLE IF NOT EXISTS courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '课程唯一标识',
    course_name VARCHAR(100) NOT NULL COMMENT '课程全称',
    course_code VARCHAR(50) COMMENT '课程的学校编码'
) COMMENT='存储课程元数据';

-- 班级表
CREATE TABLE IF NOT EXISTS classes (
    class_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '班级唯一标识',
    class_name VARCHAR(100) COMMENT '班级名称（例如：计算机2024级A班）',
    department VARCHAR(100) COMMENT '班级所属院系'
) COMMENT='学生和考试的组织单元';

-- 学生表
CREATE TABLE IF NOT EXISTS students (
    student_id BIGINT PRIMARY KEY COMMENT '关联users.user_id',
    class_id INT COMMENT '所属行政班级',
    student_number VARCHAR(50) UNIQUE COMMENT '官方学号',
    FOREIGN KEY (student_id) REFERENCES users(user_id),
    FOREIGN KEY (class_id) REFERENCES classes(class_id)
) COMMENT='用户表的扩展，存储学生专属属性';

-- 知识点表
CREATE TABLE IF NOT EXISTS knowledge_points (
    point_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '知识点唯一标识',
    course_id INT COMMENT '所属课程',
    point_name VARCHAR(200) NOT NULL COMMENT '知识点名称',
    description TEXT COMMENT '详细说明',
    FOREIGN KEY (course_id) REFERENCES courses(course_id)
) COMMENT='用于覆盖率分析的细粒度知识点';

-- 试题表
CREATE TABLE IF NOT EXISTS questions (
    question_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '试题唯一标识',
    course_id INT COMMENT '所属课程',
    creator_id BIGINT COMMENT '出题教师',
    question_content TEXT NOT NULL COMMENT '题干正文',
    question_type ENUM('选择题', '判断题', '填空题', '简答题') NOT NULL COMMENT '题型',
    options_json JSON COMMENT '客观题选项（例如 {"A":"..."}）',
    correct_answer TEXT COMMENT '标准答案',
    initial_difficulty FLOAT COMMENT '教师预估难度（0-1）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (course_id) REFERENCES courses(course_id),
    FOREIGN KEY (creator_id) REFERENCES users(user_id)
) COMMENT='核心题库表';

-- 试题-知识点关联表
CREATE TABLE IF NOT EXISTS question_knowledge_map (
    question_id BIGINT COMMENT '试题ID',
    point_id INT COMMENT '知识点ID',
    PRIMARY KEY (question_id, point_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id),
    FOREIGN KEY (point_id) REFERENCES knowledge_points(point_id)
) COMMENT='试题与知识点的关联映射';

-- =============================================
-- 4. 试卷与考试编制（目标1）
-- =============================================

-- 试卷表（模板）
-- 从test_papers重命名为examination_papers
CREATE TABLE IF NOT EXISTS examination_papers (
    paper_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '试卷唯一标识',
    title VARCHAR(200) NOT NULL COMMENT '试卷标题',
    course_id INT COMMENT '所属课程',
    setter_id BIGINT COMMENT '命题教师',
    total_score INT DEFAULT 100 COMMENT '试卷总分',
    duration_minutes INT COMMENT '考试时长（分钟）',
    target_difficulty FLOAT COMMENT '预期难度系数',
    status ENUM('草稿', '已发布', '已归档') DEFAULT '草稿' COMMENT '生命周期状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间戳',
    FOREIGN KEY (setter_id) REFERENCES users(user_id)
) COMMENT='存储试卷模板的结构/元数据';

-- 试卷结构表（试卷中的试题）
CREATE TABLE IF NOT EXISTS examination_paper_questions (
    paper_id BIGINT COMMENT '试卷ID',
    question_id BIGINT COMMENT '试题ID',
    score_value DECIMAL(5,2) COMMENT '该试题在本试卷中的分值',
    question_order INT COMMENT '试题序号',
    PRIMARY KEY (paper_id, question_id),
    FOREIGN KEY (paper_id) REFERENCES examination_papers(paper_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
) COMMENT='将试题关联到试卷，并指定具体分值';

-- 考试表（实例）
-- 班级参加某份试卷考试的实际事件
CREATE TABLE IF NOT EXISTS examinations (
    exam_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '考试实例唯一标识',
    paper_id BIGINT COMMENT '使用的试卷模板',
    class_id INT COMMENT '参加考试的班级',
    exam_date DATETIME COMMENT '考试日期/时间',
    total_candidates INT COMMENT '预计参考人数',
    actual_examinees INT COMMENT '实际提交人数',
    FOREIGN KEY (paper_id) REFERENCES examination_papers(paper_id),
    FOREIGN KEY (class_id) REFERENCES classes(class_id)
) COMMENT='记录考试的实际执行情况';

-- =============================================
-- 5. 学生成绩与答题记录（目标6）
-- =============================================

-- 考试成绩表
CREATE TABLE IF NOT EXISTS student_exam_results (
    result_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成绩唯一标识',
    exam_id BIGINT COMMENT '考试实例',
    student_id BIGINT COMMENT '学生',
    total_score DECIMAL(5,2) COMMENT '最终得分',
    is_absent BOOLEAN DEFAULT FALSE COMMENT '缺考标识',
    submitted_at DATETIME COMMENT '提交时间',
    FOREIGN KEY (exam_id) REFERENCES examinations(exam_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id)
) COMMENT='学生单次考试的总成绩汇总';

-- 学生答题记录表（明细）
CREATE TABLE IF NOT EXISTS student_answers (
    answer_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '答题记录唯一标识',
    result_id BIGINT COMMENT '关联成绩主记录',
    question_id BIGINT COMMENT '作答的试题',
    student_choice TEXT COMMENT '学生实际选择/填写的内容',
    score_obtained DECIMAL(5,2) COMMENT '该题得分',
    is_correct BOOLEAN COMMENT '是否正确的二元标识',
    FOREIGN KEY (result_id) REFERENCES student_exam_results(result_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
) COMMENT='用于分析的详细答题日志';

-- =============================================
-- 6. 质量分析与指标（目标2, 3, 5）
-- =============================================

-- 试卷质量分析表
CREATE TABLE IF NOT EXISTS examination_paper_quality_analysis (
    analysis_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分析记录唯一标识',
    exam_id BIGINT UNIQUE COMMENT '关联考试实例',
    average_score DECIMAL(5,2) COMMENT '平均分',
    std_deviation DECIMAL(5,2) COMMENT '标准差',
    highest_score DECIMAL(5,2) COMMENT '最高分',
    lowest_score DECIMAL(5,2) COMMENT '最低分',
    reliability_coefficient FLOAT COMMENT '克朗巴哈系数（信度）',
    validity_coefficient FLOAT COMMENT '效度系数',
    knowledge_coverage_rate FLOAT COMMENT '知识点覆盖率',
    overall_difficulty FLOAT COMMENT '计算得出的实际难度',
    overall_discrimination FLOAT COMMENT '计算得出的区分度',
    is_abnormal BOOLEAN DEFAULT FALSE COMMENT '指标超出阈值的异常标识',
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '计算时间',
    FOREIGN KEY (exam_id) REFERENCES examinations(exam_id)
) COMMENT='整份试卷的计算质量指标';

-- 试题质量分析表
CREATE TABLE IF NOT EXISTS question_quality_analysis (
    q_analysis_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分析记录唯一标识',
    exam_id BIGINT COMMENT '所属考试',
    question_id BIGINT COMMENT '分析的试题',
    correct_response_rate FLOAT COMMENT '实际难度（通过率/P值）',
    discrimination_index FLOAT COMMENT '区分度（高低分组差异）',
    selection_distribution_json JSON COMMENT '选项选择分布统计',
    is_too_easy BOOLEAN DEFAULT FALSE COMMENT '易题标识（正确率>90%）',
    is_low_discrimination BOOLEAN DEFAULT FALSE COMMENT '低区分度标识',
    diagnosis_tag VARCHAR(100) COMMENT '异常原因说明',
    FOREIGN KEY (exam_id) REFERENCES examinations(exam_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
) COMMENT='单个试题的表现指标';

-- =============================================
-- 7. 智能建议（目标4）
-- =============================================

-- 改进建议表
CREATE TABLE IF NOT EXISTS improvement_suggestions (
    suggestion_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '建议唯一标识',
    exam_id BIGINT COMMENT '分析的考试',
    question_id BIGINT NULL COMMENT '特定试题（NULL表示针对整份试卷）',
    suggestion_type ENUM('试卷结构', '试题内容', '难度调整') COMMENT '建议类别',
    suggestion_text TEXT NOT NULL COMMENT '可执行的改进建议',
    generated_rule VARCHAR(100) COMMENT '触发的规则',
    is_implemented BOOLEAN DEFAULT FALSE COMMENT '反馈闭环：是否已实施',
    FOREIGN KEY (exam_id) REFERENCES examinations(exam_id),
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
) COMMENT='基于AI/规则的改进建议';
