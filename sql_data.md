-- USE epqas_db;

-- =============================================
-- 1. Initialize Roles & Users
-- =============================================

-- Insert Roles
INSERT INTO role (role_id, role_name, description) VALUES 
(1, 'Administrator', 'System maintenance and global access'),
(2, 'Question Setter', 'Responsible for designing questions and papers'),
(3, 'Course Instructor', 'Classroom teacher, views reports'),
(4, 'Student', 'Takes exams and views own grades');

-- Insert Users (Password is '123456' hashed - dummy placeholder)
INSERT INTO user (user_id, username, password_hash, real_name, role_id, email) VALUES 
(1, 'admin', 'hash_secret', 'System Admin', 1, 'admin@school.edu'),
(2, 'setter_john', 'hash_secret', 'Dr. John Setter', 2, 'john@school.edu'),
(3, 'teacher_mary', 'hash_secret', 'Mary Instructor', 3, 'mary@school.edu'),
(4, 'student_alice', 'hash_secret', 'Alice Smith', 4, 'alice@school.edu'),
(5, 'student_bob', 'hash_secret', 'Bob Jones', 4, 'bob@school.edu'),
(6, 'student_charlie', 'hash_secret', 'Charlie Brown', 4, 'charlie@school.edu'),
(7, 'student_david', 'hash_secret', 'David White', 4, 'david@school.edu'),
(8, 'student_eve', 'hash_secret', 'Eve Black', 4, 'eve@school.edu');

-- =============================================
-- 2. Academic Data (Courses, Classes, Students)
-- =============================================

-- Insert Course
INSERT INTO course (course_id, course_name, course_code) VALUES 
(1, 'Data Structures & Algorithms', 'CS-201');

-- Insert Class
INSERT INTO school_class (class_id, class_name, department) VALUES 
(1, 'CS-2024-A', 'Computer Science');

-- Insert Students (Linking User to Class)
INSERT INTO student (student_id, class_id, student_number) VALUES 
(4, 1, 'S2024001'), -- Alice
(5, 1, 'S2024002'), -- Bob
(6, 1, 'S2024003'), -- Charlie
(7, 1, 'S2024004'), -- David
(8, 1, 'S2024005'); -- Eve

-- Insert Knowledge Points
INSERT INTO knowledge_point (point_id, course_id, point_name, description) VALUES 
(1, 1, 'Arrays', 'Basic linear data structures'),
(2, 1, 'Linked Lists', 'Nodes and pointers'),
(3, 1, 'Stacks', 'LIFO principles'),
(4, 1, 'Sorting Algorithms', 'Quick sort, Merge sort');

-- =============================================
-- 3. Question Bank
-- =============================================

INSERT INTO question (question_id, course_id, creator_id, question_content, question_type, options_json, correct_answer, initial_difficulty) VALUES 
-- Q1: Easy MCQ
(1, 1, 2, 'Which data structure follows LIFO?', 'MCQ', '{"A": "Queue", "B": "Stack", "C": "Array", "D": "Tree"}', 'B', 0.8),
-- Q2: Medium MCQ
(2, 1, 2, 'What is the time complexity of accessing an element in an array?', 'MCQ', '{"A": "O(1)", "B": "O(n)", "C": "O(log n)", "D": "O(n^2)"}', 'A', 0.6),
-- Q3: Hard True/False
(3, 1, 2, 'A linked list requires contiguous memory allocation.', 'TrueFalse', NULL, 'False', 0.4),
-- Q4: Medium FillBlank
(4, 1, 2, 'The worst-case time complexity of QuickSort is O(___).', 'FillBlank', NULL, 'n^2', 0.5),
-- Q5: Hard Essay
(5, 1, 2, 'Explain the difference between a Stack and a Queue.', 'Essay', NULL, 'Key points: LIFO vs FIFO', 0.3);

-- Map Questions to Knowledge Points
INSERT INTO question_knowledge_map (question_id, point_id) VALUES 
(1, 3), -- Q1 -> Stacks
(2, 1), -- Q2 -> Arrays
(3, 2), -- Q3 -> Linked Lists
(4, 4), -- Q4 -> Sorting
(5, 3); -- Q5 -> Stacks

-- =============================================
-- 4. Examination Paper Construction
-- =============================================

-- Create the Paper Template
INSERT INTO examination_paper (paper_id, title, course_id, setter_id, total_score, duration_minutes, target_difficulty, status) VALUES 
(1, 'CS-201 Midterm Examination', 1, 2, 50, 60, 0.6, 'Published');

-- Add Questions to Paper (Total 50 points)
INSERT INTO examination_paper_question (paper_id, question_id, score_value, question_order) VALUES 
(1, 1, 10.00, 1), -- Q1 (10 pts)
(1, 2, 10.00, 2), -- Q2 (10 pts)
(1, 3, 10.00, 3), -- Q3 (10 pts)
(1, 4, 10.00, 4), -- Q4 (10 pts)
(1, 5, 10.00, 5); -- Q5 (10 pts)

-- =============================================
-- 5. Exam Execution & Results
-- =============================================

-- Create an Exam Instance
INSERT INTO examination (exam_id, paper_id, class_id, exam_date, total_candidates, actual_examinees) VALUES 
(1, 1, 1, NOW(), 5, 5);

-- 1. Alice (High Achiever - Score: 50/50)
INSERT INTO student_exam_result (result_id, exam_id, student_id, total_score, submitted_at) VALUES (1, 1, 4, 50.00, NOW());
INSERT INTO student_answer (result_id, question_id, student_choice, score_obtained, is_correct) VALUES 
(1, 1, 'B', 10.00, 1), (1, 2, 'A', 10.00, 1), (1, 3, 'False', 10.00, 1), (1, 4, 'n^2', 10.00, 1), (1, 5, 'Perfect essay', 10.00, 1);

-- 2. Bob (Average - Score: 30/50)
INSERT INTO student_exam_result (result_id, exam_id, student_id, total_score, submitted_at) VALUES (2, 1, 5, 30.00, NOW());
INSERT INTO student_answer (result_id, question_id, student_choice, score_obtained, is_correct) VALUES 
(2, 1, 'B', 10.00, 1), (2, 2, 'B', 0.00, 0), (2, 3, 'False', 10.00, 1), (2, 4, 'n', 0.00, 0), (2, 5, 'Okay essay', 10.00, 1);

-- 3. Charlie (Low Achiever - Score: 10/50)
INSERT INTO student_exam_result (result_id, exam_id, student_id, total_score, submitted_at) VALUES (3, 1, 6, 10.00, NOW());
INSERT INTO student_answer (result_id, question_id, student_choice, score_obtained, is_correct) VALUES 
(3, 1, 'A', 0.00, 0), (3, 2, 'D', 0.00, 0), (3, 3, 'True', 0.00, 0), (3, 4, 'nlogn', 0.00, 0), (3, 5, 'Good essay', 10.00, 1);

-- 4. David (Average - Score: 40/50)
INSERT INTO student_exam_result (result_id, exam_id, student_id, total_score, submitted_at) VALUES (4, 1, 7, 40.00, NOW());
INSERT INTO student_answer (result_id, question_id, student_choice, score_obtained, is_correct) VALUES 
(4, 1, 'B', 10.00, 1), (4, 2, 'A', 10.00, 1), (4, 3, 'False', 10.00, 1), (4, 4, 'n^2', 10.00, 1), (4, 5, 'Blank', 0.00, 0);

-- 5. Eve (Low - Score: 20/50)
INSERT INTO student_exam_result (result_id, exam_id, student_id, total_score, submitted_at) VALUES (5, 1, 8, 20.00, NOW());
INSERT INTO student_answer (result_id, question_id, student_choice, score_obtained, is_correct) VALUES 
(5, 1, 'B', 10.00, 1), (5, 2, 'C', 0.00, 0), (5, 3, 'True', 0.00, 0), (5, 4, 'n', 0.00, 0), (5, 5, 'Short text', 10.00, 1);

-- =============================================
-- 6. Quality Analysis Results (Simulated)
-- =============================================

-- Analysis for the Whole Paper
INSERT INTO examination_paper_quality_analysis 
(exam_id, average_score, std_deviation, highest_score, lowest_score, reliability_coefficient, overall_difficulty, overall_discrimination, is_abnormal) VALUES 
(1, 30.00, 14.14, 50.00, 10.00, 0.85, 0.60, 0.55, 0);

-- Analysis for Question 1 (Everyone got it right except Charlie)
INSERT INTO question_quality_analysis 
(exam_id, question_id, correct_response_rate, discrimination_index, selection_distribution_json, is_too_easy, diagnosis_tag) VALUES 
(1, 1, 0.80, 0.20, '{"A": "20%", "B": "80%", "C": "0%", "D": "0%"}', 0, 'Slightly Easy');

-- Analysis for Question 2 (Good discrimination)
INSERT INTO question_quality_analysis 
(exam_id, question_id, correct_response_rate, discrimination_index, selection_distribution_json, is_too_easy, diagnosis_tag) VALUES 
(1, 2, 0.40, 0.60, '{"A": "40%", "B": "20%", "C": "20%", "D": "20%"}', 0, 'Good Quality');

-- =============================================
-- 7. Intelligent Suggestions (Simulated)
-- =============================================

INSERT INTO improvement_suggestion 
(exam_id, question_id, suggestion_type, suggestion_text, generated_rule, is_implemented) VALUES 
(1, NULL, 'Paper_Structure', 'Reliability is high (0.85). This paper is suitable for final exams.', 'Reliability > 0.8', 0),
(1, 1, 'Difficulty_Adj', 'This question was answered correctly by 80% of students. Consider removing option hints.', 'Difficulty < 0.3', 0);

