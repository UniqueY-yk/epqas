-- 试卷质量分析修改

-- 试卷表添加字段
ALTER TABLE examination_paper_quality_analysis
  ADD COLUMN skewness FLOAT DEFAULT NULL COMMENT '偏度',
  ADD COLUMN kurtosis FLOAT DEFAULT NULL COMMENT '峰度',
  ADD COLUMN reliability_evaluation VARCHAR(100) DEFAULT NULL COMMENT '信度定性评价',
  ADD COLUMN difficulty_evaluation VARCHAR(50) DEFAULT NULL COMMENT '难度定性评价',
  ADD COLUMN discrimination_evaluation VARCHAR(50) DEFAULT NULL COMMENT '区分度定性评价';

-- 题目表添加字段
ALTER TABLE question_quality_analysis
  ADD COLUMN difficulty_index FLOAT DEFAULT NULL COMMENT '难度系数P (极端组法)',
  ADD COLUMN validity_index FLOAT DEFAULT NULL COMMENT '效度 Pearson r(i,T)',
  ADD COLUMN difficulty_evaluation VARCHAR(50) DEFAULT NULL COMMENT '难度定性评价',
  ADD COLUMN discrimination_evaluation VARCHAR(50) DEFAULT NULL COMMENT '区分度定性评价';

	
-- 考试记录修改

-- 级联删除：允许删除考试自动删除其质量分析记录
-- 1. 试卷质量分析表
ALTER TABLE examination_paper_quality_analysis
    DROP FOREIGN KEY examination_paper_quality_analysis_ibfk_1;
ALTER TABLE examination_paper_quality_analysis
    ADD CONSTRAINT examination_paper_quality_analysis_ibfk_1 
    FOREIGN KEY (exam_id) REFERENCES examination(exam_id) ON DELETE CASCADE;

-- 2. 题目质量分析表
ALTER TABLE question_quality_analysis
    DROP FOREIGN KEY question_quality_analysis_ibfk_1;
ALTER TABLE question_quality_analysis
    ADD CONSTRAINT question_quality_analysis_ibfk_1 
    FOREIGN KEY (exam_id) REFERENCES examination(exam_id) ON DELETE CASCADE;
		
-- 3. 给学生考试成绩表也加上级联删除
ALTER TABLE student_exam_result
    DROP FOREIGN KEY student_exam_result_ibfk_1;
ALTER TABLE student_exam_result
    ADD CONSTRAINT student_exam_result_ibfk_1 
    FOREIGN KEY (exam_id) REFERENCES examination(exam_id) ON DELETE CASCADE;

-- 4. 学生答题表：级联删除（关联成绩表）
ALTER TABLE student_answer
    DROP FOREIGN KEY student_answer_ibfk_1;
ALTER TABLE student_answer
    ADD CONSTRAINT student_answer_ibfk_1 
    FOREIGN KEY (result_id) REFERENCES student_exam_result(result_id) ON DELETE CASCADE;

-- 试卷模板修改
-- 1. 试卷题目关系表
ALTER TABLE examination_paper_question
    DROP FOREIGN KEY examination_paper_question_ibfk_1;
ALTER TABLE examination_paper_question
    ADD CONSTRAINT examination_paper_question_ibfk_1 
    FOREIGN KEY (paper_id) REFERENCES examination_paper(paper_id) ON DELETE CASCADE;

