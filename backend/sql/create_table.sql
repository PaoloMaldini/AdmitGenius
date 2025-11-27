-- 留学者指南 数据库初始化脚本

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;


DROP DATABASE IF EXISTS admitgenius_db;

-- 重新创建数据库
CREATE DATABASE admitgenius_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE admitgenius_db;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL, -- UNIQUE 会自动创建索引
    password VARCHAR(255) NOT NULL,
    profile_picture VARCHAR(255),
    role ENUM('USER', 'ADMIN', 'EXPERT') DEFAULT 'USER',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    current_school VARCHAR(255),
    major VARCHAR(255),
    gpa FLOAT,
    grade_scale VARCHAR(10),
    
    -- 专家用户特有字段
    institution VARCHAR(255),
    expertise_area VARCHAR(255),
    title VARCHAR(255),
    bio TEXT,
    years_of_experience INT,
    rating_avg DECIMAL(3,2) DEFAULT 0.00,
    review_count INT DEFAULT 0,
    is_verified BOOLEAN DEFAULT FALSE,
    verified_at TIMESTAMP NULL,
    
    -- 标准化考试成绩
    toefl_total INT,
    toefl_reading INT,
    toefl_listening INT,
    toefl_speaking INT,
    toefl_writing INT,
    
    ielts_total FLOAT,
    ielts_reading FLOAT,
    ielts_listening FLOAT,
    ielts_speaking FLOAT,
    ielts_writing FLOAT,
    
    gre_combined INT,
    gre_verbal INT,
    gre_quantitative INT,
    gre_analytical FLOAT,
    
    gmat_total INT,
    gmat_verbal INT,
    gmat_quantitative INT,
    gmat_integrated INT,
    gmat_analytical FLOAT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 用户教育经历表
CREATE TABLE IF NOT EXISTS user_education (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    school VARCHAR(255) NOT NULL,
    degree VARCHAR(50) NOT NULL,
    major VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    achievement TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 用户项目经历表
CREATE TABLE IF NOT EXISTS user_projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    content TEXT NOT NULL,
    is_leader BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 学校表
CREATE TABLE IF NOT EXISTS schools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    ranking INT,
    acceptance_rate FLOAT,
    average_gpa FLOAT,
    average_gre_verbal INT,
    average_gre_quantitative INT,
    average_gre_analytical FLOAT,
    average_gmat INT,
    average_toefl INT,
    average_ielts FLOAT,
    description TEXT,
    website VARCHAR(255),
    image_url VARCHAR(255),
    has_scholarship BOOLEAN DEFAULT FALSE,
    tuition_fee DECIMAL(10,2),
    school_type ENUM('PUBLIC', 'PRIVATE') DEFAULT 'PRIVATE',
    admission_requirements TEXT,
    top_programs JSON,
    research_focus JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_school_name (name) -- 确保学校名称唯一
);

-- 学校项目表
CREATE TABLE IF NOT EXISTS school_programs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    degree_type ENUM('BACHELOR', 'MASTER', 'PHD') NOT NULL,
    department VARCHAR(255),
    duration_years INT,
    tuition_fee VARCHAR(255),
    admission_requirements TEXT,
    description TEXT,
    website VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    UNIQUE KEY uk_school_program_name_deg (school_id, name, degree_type)
);

-- 文书要求表
CREATE TABLE IF NOT EXISTS essay_requirements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT,
    program_id BIGINT,
    essay_type VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    prompt TEXT NOT NULL,
    word_limit INT,
    is_required BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    FOREIGN KEY (program_id) REFERENCES school_programs(id) ON DELETE CASCADE,
    UNIQUE KEY uk_essay_req_school_program_title (school_id, program_id, title) -- 确保同一学校/项目下的文书标题唯一
);

-- 文书表
CREATE TABLE IF NOT EXISTS essays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    school_id BIGINT, 
    essay_requirement_id BIGINT,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    word_limit INT,
    essay_type VARCHAR(255),
    status ENUM('DRAFT', 'COMPLETED', 'REVIEWED') DEFAULT 'DRAFT',
    version INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE SET NULL,
    FOREIGN KEY (essay_requirement_id) REFERENCES essay_requirements(id) ON DELETE SET NULL
);

-- 推荐表
CREATE TABLE IF NOT EXISTS recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    request_data JSON,
    total_recommendations INT DEFAULT 0,
    recommendation_type ENUM('SCHOOL', 'PROGRAM') DEFAULT 'SCHOOL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 推荐项表
CREATE TABLE IF NOT EXISTS recommendation_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recommendation_id BIGINT NOT NULL,
    school_id BIGINT,
    program_id BIGINT,
    match_score FLOAT,
    `rank` INT,
    match_reason TEXT,
    feedback TEXT,
    is_applied BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recommendation_id) REFERENCES recommendations(id) ON DELETE CASCADE,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE, 
    FOREIGN KEY (program_id) REFERENCES school_programs(id) ON DELETE SET NULL
);

-- 申请统计表
CREATE TABLE IF NOT EXISTS application_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT NOT NULL,
    program_id BIGINT,
    admission_year INT,
    total_applications INT DEFAULT 0,
    total_admissions INT DEFAULT 0,
    admission_rate FLOAT,
    average_gpa FLOAT,
    average_gre_verbal INT,
    average_gre_quantitative INT,
    average_gmat INT,
    average_toefl INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    FOREIGN KEY (program_id) REFERENCES school_programs(id) ON DELETE CASCADE,
    UNIQUE KEY uk_app_stats_school_program_year (school_id, program_id, admission_year) -- 确保统计数据对于学校+项目+年份是唯一的
);

-- 论坛帖子表
CREATE TABLE IF NOT EXISTS forum_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    category VARCHAR(255),
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    is_expert_post BOOLEAN DEFAULT FALSE,
    expert_tag VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 评论表
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 帖子点赞表
CREATE TABLE IF NOT EXISTS post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_post_user_like (post_id, user_id),
    FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 文档表
CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    file_path VARCHAR(500),
    file_size BIGINT,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_filename (user_id, filename) -- 确保同一用户的文件名唯一
);

-- 专家评审表
CREATE TABLE IF NOT EXISTS expert_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    essay_id BIGINT NOT NULL,
    reviewer_id BIGINT, 
    review_content TEXT,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    suggestions TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (essay_id) REFERENCES essays(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY uk_expert_review_essay_reviewer (essay_id, reviewer_id) -- 防止同一评审员对同一文章多次评审
);

-- 创建索引以提高查询性能
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_essays_user_id ON essays(user_id);
CREATE INDEX idx_essays_school_id ON essays(school_id);
CREATE INDEX idx_essays_essay_requirement_id ON essays(essay_requirement_id);
CREATE INDEX idx_recommendations_user_id ON recommendations(user_id);
CREATE INDEX idx_recommendation_items_recommendation_id ON recommendation_items(recommendation_id);
CREATE INDEX idx_recommendation_items_school_id ON recommendation_items(school_id);
CREATE INDEX idx_recommendation_items_program_id ON recommendation_items(program_id);
CREATE INDEX idx_forum_posts_author_id ON forum_posts(author_id);
CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_author_id ON comments(author_id);
CREATE INDEX idx_post_likes_user_id ON post_likes(user_id);
CREATE INDEX idx_school_programs_school_id ON school_programs(school_id);
CREATE INDEX idx_essay_requirements_school_id ON essay_requirements(school_id);
CREATE INDEX idx_essay_requirements_program_id ON essay_requirements(program_id);
CREATE INDEX idx_application_statistics_school_id ON application_statistics(school_id);
CREATE INDEX idx_application_statistics_program_id ON application_statistics(program_id);
CREATE INDEX idx_documents_user_id ON documents(user_id);
CREATE INDEX idx_expert_reviews_essay_id ON expert_reviews(essay_id);
CREATE INDEX idx_expert_reviews_reviewer_id ON expert_reviews(reviewer_id);
CREATE INDEX idx_user_education_user_id ON user_education(user_id);
CREATE INDEX idx_user_projects_user_id ON user_projects(user_id);

-- 插入一些示例数据

-- 插入管理员用户 (密码: admin123)
INSERT INTO users (full_name, email, password, role) VALUES ('系统管理员', 'admin@admitgenius.com', '$2b$10$mkUNLd.OIhKhuoT9uJd3Ber/iqtyffzmFBVdHdkTlzUmtKSGmAizu', 'ADMIN') ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), password=VALUES(password), role=VALUES(role);

-- 插入测试用户数据

-- 1. 普通用户 (密码: admin123)
INSERT INTO users (
    full_name, email, password, role, status,
    current_school, gpa, gre_combined, toefl_total
) VALUES (
    '张小明', 
    'user@test.com', 
    '$2b$10$mkUNLd.OIhKhuoT9uJd3Ber/iqtyffzmFBVdHdkTlzUmtKSGmAizu',
    'USER', 
    'ACTIVE',
    '北京大学', 
    3.7, 
    320, 
    105
) ON DUPLICATE KEY UPDATE 
    full_name = VALUES(full_name), 
    password = VALUES(password), 
    role = VALUES(role),
    current_school = VALUES(current_school),
    gpa = VALUES(gpa),
    gre_combined = VALUES(gre_combined),
    toefl_total = VALUES(toefl_total);

-- 2. 专家用户 (密码: admin123)
INSERT INTO users (
    full_name, email, password, role, status,
    institution, expertise_area, title, bio, years_of_experience,
    is_verified, verified_at, rating_avg, review_count
) VALUES (
    '李教授', 
    'expert@test.com', 
    '$2b$10$mkUNLd.OIhKhuoT9uJd3Ber/iqtyffzmFBVdHdkTlzUmtKSGmAizu',
    'EXPERT', 
    'ACTIVE',
    '清华大学计算机学院', 
    '计算机科学', 
    '副教授', 
    '清华大学计算机学院副教授，专注于人工智能和机器学习领域研究。曾指导多名学生成功申请到MIT、Stanford等顶尖院校的计算机科学项目。在Nature、ICML等顶级期刊和会议发表论文30余篇。', 
    12,
    TRUE,
    CURRENT_TIMESTAMP,
    4.8,
    156
) ON DUPLICATE KEY UPDATE 
    full_name = VALUES(full_name), 
    password = VALUES(password), 
    role = VALUES(role),
    institution = VALUES(institution),
    expertise_area = VALUES(expertise_area),
    title = VALUES(title),
    bio = VALUES(bio),
    years_of_experience = VALUES(years_of_experience),
    is_verified = VALUES(is_verified),
    verified_at = VALUES(verified_at),
    rating_avg = VALUES(rating_avg),
    review_count = VALUES(review_count);

-- 3. 另一个普通用户 (密码: admin123)
INSERT INTO users (
    full_name, email, password, role, status,
    current_school, major, gpa, gre_combined, gre_verbal, gre_quantitative,
    toefl_total, toefl_reading, toefl_listening, toefl_speaking, toefl_writing
) VALUES (
    '王小红', 
    'student@test.com', 
    '$2b$10$mkUNLd.OIhKhuoT9uJd3Ber/iqtyffzmFBVdHdkTlzUmtKSGmAizu',
    'USER', 
    'ACTIVE',
    '上海交通大学', 
    '电子工程', 
    3.85, 
    325, 
    158, 
    167,
    108, 
    28, 
    27, 
    24, 
    29
) ON DUPLICATE KEY UPDATE 
    full_name = VALUES(full_name), 
    password = VALUES(password), 
    role = VALUES(role),
    current_school = VALUES(current_school),
    major = VALUES(major),
    gpa = VALUES(gpa),
    gre_combined = VALUES(gre_combined),
    gre_verbal = VALUES(gre_verbal),
    gre_quantitative = VALUES(gre_quantitative),
    toefl_total = VALUES(toefl_total),
    toefl_reading = VALUES(toefl_reading),
    toefl_listening = VALUES(toefl_listening),
    toefl_speaking = VALUES(toefl_speaking),
    toefl_writing = VALUES(toefl_writing);

-- 4. 第二个专家用户 (密码: admin123) - 留学顾问
INSERT INTO users (
    full_name, email, password, role, status,
    institution, expertise_area, title, bio, years_of_experience,
    is_verified, verified_at, rating_avg, review_count
) VALUES (
    '陈顾问', 
    'advisor@test.com', 
    '$2b$10$mkUNLd.OIhKhuoT9uJd3Ber/iqtyffzmFBVdHdkTlzUmtKSGmAizu',
    'EXPERT', 
    'ACTIVE',
    '新东方前途出国', 
    '商科', 
    '资深留学顾问', 
    '资深留学顾问，专注美国商科申请8年，成功帮助300+学生获得顶尖商学院录取，包括Wharton、Kellogg、Booth等MBA项目。精通商科申请策略和文书写作指导。', 
    8,
    TRUE,
    CURRENT_TIMESTAMP,
    4.6,
    89
) ON DUPLICATE KEY UPDATE 
    full_name = VALUES(full_name), 
    password = VALUES(password), 
    role = VALUES(role),
    institution = VALUES(institution),
    expertise_area = VALUES(expertise_area),
    title = VALUES(title),
    bio = VALUES(bio),
    years_of_experience = VALUES(years_of_experience),
    is_verified = VALUES(is_verified),
    verified_at = VALUES(verified_at),
    rating_avg = VALUES(rating_avg),
    review_count = VALUES(review_count);

-- 5. 第二个学生用户 (密码: admin123)
INSERT INTO users (
    full_name, email, password, role, status,
    current_school, major, gpa, gre_combined, toefl_total
) VALUES (
    '李同学', 
    'student2@test.com', 
    '$2b$10$mkUNLd.OIhKhuoT9uJd3Ber/iqtyffzmFBVdHdkTlzUmtKSGmAizu',
    'USER', 
    'ACTIVE',
    '复旦大学', 
    '计算机科学', 
    3.6, 
    315, 
    102
) ON DUPLICATE KEY UPDATE 
    full_name = VALUES(full_name), 
    password = VALUES(password), 
    role = VALUES(role),
    current_school = VALUES(current_school),
    major = VALUES(major),
    gpa = VALUES(gpa),
    gre_combined = VALUES(gre_combined),
    toefl_total = VALUES(toefl_total);

-- 6. 第三个学生用户 (密码: admin123)
INSERT INTO users (
    full_name, email, password, role, status,
    current_school, major, gpa, gre_combined, toefl_total
) VALUES (
    '赵同学', 
    'student3@test.com', 
    '$2b$10$mkUNLd.OIhKhuoT9uJd3Ber/iqtyffzmFBVdHdkTlzUmtKSGmAizu',
    'USER', 
    'ACTIVE',
    '中山大学', 
    '数学与应用数学', 
    3.8, 
    320, 
    107
) ON DUPLICATE KEY UPDATE 
    full_name = VALUES(full_name), 
    password = VALUES(password), 
    role = VALUES(role),
    current_school = VALUES(current_school),
    major = VALUES(major),
    gpa = VALUES(gpa),
    gre_combined = VALUES(gre_combined),
    toefl_total = VALUES(toefl_total);

-- 7. 第三个专家用户 (密码: admin123) - 院校分析专家
INSERT INTO users (
    full_name, email, password, role, status,
    institution, expertise_area, title, bio, years_of_experience,
    is_verified, verified_at, rating_avg, review_count
) VALUES (
    '张专家', 
    'expert2@test.com', 
    '$2b$10$mkUNLd.OIhKhuoT9uJd3Ber/iqtyffzmFBVdHdkTlzUmtKSGmAizu',
    'EXPERT', 
    'ACTIVE',
    '启德教育', 
    '院校分析', 
    '高级留学顾问', 
    '院校分析专家，专注英美澳加院校研究10年，对各国TOP大学录取要求和趋势有深入了解。帮助500+学生成功申请到理想院校。', 
    10,
    TRUE,
    CURRENT_TIMESTAMP,
    4.7,
    125
) ON DUPLICATE KEY UPDATE 
    full_name = VALUES(full_name), 
    password = VALUES(password), 
    role = VALUES(role),
    institution = VALUES(institution),
    expertise_area = VALUES(expertise_area),
    title = VALUES(title),
    bio = VALUES(bio),
    years_of_experience = VALUES(years_of_experience),
    is_verified = VALUES(is_verified),
    verified_at = VALUES(verified_at),
    rating_avg = VALUES(rating_avg),
    review_count = VALUES(review_count);

-- 8. 第四个专家用户 (密码: admin123) - 考试指导专家
INSERT INTO users (
    full_name, email, password, role, status,
    institution, expertise_area, title, bio, years_of_experience,
    is_verified, verified_at, rating_avg, review_count
) VALUES (
    '刘老师', 
    'expert3@test.com', 
    '$2b$10$mkUNLd.OIhKhuoT9uJd3Ber/iqtyffzmFBVdHdkTlzUmtKSGmAizu',
    'EXPERT', 
    'ACTIVE',
    '新东方教育', 
    '语言考试', 
    '托福金牌讲师', 
    '托福教学专家，10年托福培训经验，帮助3000+学生突破100分。独创高效备考方法，对托福考试趋势和技巧有深入研究。', 
    10,
    TRUE,
    CURRENT_TIMESTAMP,
    4.9,
    200
) ON DUPLICATE KEY UPDATE 
    full_name = VALUES(full_name), 
    password = VALUES(password), 
    role = VALUES(role),
    institution = VALUES(institution),
    expertise_area = VALUES(expertise_area),
    title = VALUES(title),
    bio = VALUES(bio),
    years_of_experience = VALUES(years_of_experience),
    is_verified = VALUES(is_verified),
    verified_at = VALUES(verified_at),
    rating_avg = VALUES(rating_avg),
    review_count = VALUES(review_count);

-- 插入一些示例专家帖子
INSERT INTO forum_posts (
    author_id, title, content, category, like_count, comment_count, 
    is_expert_post, expert_tag, created_at
) VALUES 

(
    (SELECT id FROM users WHERE email = 'advisor@test.com'), 
    '【专家分析】2024年MBA申请趋势解读', 
    '作为专业的留学顾问，我来分析一下2024年MBA申请的最新趋势...\n\n1. 申请竞争更加激烈\n- 申请人数持续增长\n- GMAT/GRE分数水涨船高\n- 工作经验质量要求提高\n\n2. 多元化背景受欢迎\n- 科技背景申请者增多\n- 非营利组织经验被看重\n- 创业经历成为加分项\n\n3. 文书要求更加个性化\n- 突出个人独特经历\n- 展现领导力和影响力\n- 明确职业规划\n\n如果大家有具体问题，欢迎私信咨询！', 
    'experience',
    32, 
    8, 
    TRUE, 
    '商科申请', 
    DATE_SUB(NOW(), INTERVAL 1 DAY)
);

-- 插入一些普通帖子
INSERT INTO forum_posts (
    author_id, title, content, category, like_count, comment_count, 
    is_expert_post, created_at
) VALUES 

(
    (SELECT id FROM users WHERE email = 'student@test.com'), 
    '武大申请美国研究生求建议', 
    '楼主背景：\n- 本科：武汉大学 电子工程\n- GPA：3.70/4.0\n- GRE：325 (V158 + Q167)\n- TOEFL：108\n- 研究经历：2段实习 + 1个项目\n\n目标学校：\n- 冲刺：Stanford, MIT, Berkeley\n- 匹配：UIUC, Gatech, CMU\n- 保底：UCSD, NYU\n\n请问各位大神：\n1. 我的背景申请这些学校有希望吗？\n2. 还需要补强哪些方面？\n3. 推荐信应该找哪些老师？\n\n谢谢大家！', 
    'experience',
    16, 
    9, 
    FALSE, 
    DATE_SUB(NOW(), INTERVAL 1 DAY)
);

-- 插入示例学校数据
INSERT INTO schools (id, name, location, ranking, acceptance_rate, average_gpa, average_gre_verbal, average_gre_quantitative, average_gmat, description, website, has_scholarship, tuition_fee) VALUES
(1, '麻省理工学院', '美国马萨诸塞州剑桥市', 1, 0.020, 3.90, 163, 169, 730, '全球理工科顶尖学府，工程、计算机、物理和商科领域领先', 'https://www.mit.edu', TRUE, 58746.00),
(2, '帝国理工学院', '英国伦敦', 2, 0.060, 3.50, 156, 158, 600, '英国顶尖公立研究型大学，G5成员，科研商业化突出，交叉学科应对全球挑战。工科和商科顶尖，计算机、土木工程、MBA项目全球前列，与MIT合作密切。', 'https://www.imperial.ac.uk/', TRUE, 34400.00),
(3, '牛津大学', '英国牛津', 3, 0.100, 3.70, NULL, NULL, NULL, '英语世界最古老大学，以书院联邦制著称，科研实力顶尖，PPE、医学、法律等专业全球领先。', 'https://www.ox.ac.uk/', TRUE, 27840.00),
(4, '哈佛大学', '美国马萨诸塞州剑桥市', 4, 0.050, 3.90, 165, 166, 730, '美国历史最悠久的大学，以法学、医学、商学著称，校训"Veritas"。', 'https://www.harvard.edu/', TRUE, 55000.00),
(5, '剑桥大学', '英国剑桥', 5, 0.120, 3.70, NULL, NULL, NULL, '与牛津齐名，自然科学、工程学、数学领域全球顶尖，实行学院制，科研经费充足。', 'https://www.cam.ac.uk/', TRUE, 22227.00),
(6, '斯坦福大学', '美国加利福尼亚州斯坦福', 6, 0.040, 3.90, 165, 168, 730, '顶尖私立研究型大学，以创新和跨学科研究闻名，工程、商科、计算机科学领先。', 'https://www.stanford.edu', TRUE, 62000.00),
(7, '新加坡国立大学', '新加坡', 8, 0.070, 3.70, 160, 165, 700, '亚洲顶尖，工程、计算机、商科全球前十，竞争激烈。', 'https://www.nus.edu.sg', TRUE, 38000.00),
(8, '伦敦大学学院', '英国伦敦', 9, 0.110, 3.50, NULL, NULL, NULL, 'G5超级精英大学，建筑学、教育学、医学领先，国际化程度高，主校区位于伦敦市中心。', 'https://www.ucl.ac.uk', TRUE, 24000.00),
(9, '加州理工大学', '美国加利福尼亚州帕萨迪纳', 10, 0.040, 3.90, 165, 170, 680, '顶尖理工院校，物理、数学和航空航天工程全美领先，录取竞争极激烈。', 'https://www.caltech.edu', TRUE, 60000.00),
(10, '宾夕法尼亚大学', '美国宾夕法尼亚州费城', 11, 0.080, 3.80, 162, 165, 730, '沃顿商学院全球第一，医学、护理学领先，校训"Legessinemoribusvanae"。', 'https://www.upenn.edu', TRUE, 61000.00),
(11, '加州大学伯克利分校', '美国加利福尼亚州伯克利', 12, 0.110, 3.90, 158, 167, 700, '公立常春藤，工程、计算机科学、环境科学和经济学领域顶尖', 'https://www.berkeley.edu', TRUE, 44000.00),
(12, '墨尔本大学', '澳大利亚墨尔本', 13, 0.210, 3.50, NULL, NULL, 695, '澳洲八大之首，研究密集型大学，商科、教育、计算机科学顶尖。2024年起优先录取985/211学生。', 'https://www.unimelb.edu.au', TRUE, 40000.00),
(13, '南洋理工大学', '新加坡', 15, 0.100, 3.60, 158, 164, 690, '新加坡顶尖公立大学，亚洲顶尖工程与科技学府，产学研融合突出。以理工科见长，材料科学、人工智能领域顶尖。', 'https://www.ntu.edu.sg', TRUE, 35000.00),
(14, '康奈尔大学', '美国纽约州伊萨卡', 16, 0.110, 3.70, 160, 164, 690, '美国常春藤盟校，公私合营模式，学科覆盖面广。首创酒店管理/劳工关系学院，自由选课制度。工程学和酒店管理全美顶尖，校训"有教无类，无所不包"。', 'https://www.cornell.edu', TRUE, 56000.00),
(15, '香港大学', '中国香港', 17, 0.100, 3.60, NULL, NULL, 680, '亚洲国际化程度最高大学，医学、法律、金融强势，全英文授课。', 'https://www.hku.hk', TRUE, 171000.00),
(16, '悉尼大学', '澳大利亚悉尼', 18, 0.210, 3.30, 150, 160, 700, '世界顶尖研究型大学，商科、医学、法学突出，接受中国高考成绩直录本科。', 'https://www.sydney.edu.au', TRUE, 45000.00),
(17, '新南威尔士大学', '澳大利亚悉尼', 19, 0.220, 3.20, NULL, NULL, 670, '澳大利亚八大之一，以工程科技见长，QS排名19。工程全澳最大，量子计算与光伏技术领先。工程、商科、法律强势。', 'https://www.unsw.edu.au', TRUE, 45000.00),
(18, '芝加哥大学', '美国伊利诺伊州芝加哥', 21, 0.060, 3.90, 162, 165, 720, '美国顶尖研究型大学，学术自由与批判精神著称。Quarter学制灵活，经济学"芝加哥学派"发源地。以经济学、法律和社会科学著称，强调学术自由与跨学科研究', 'https://www.uchicago.edu', TRUE, 63000.00),
(19, '普林斯顿大学', '美国新泽西州普林斯顿', 22, 0.060, 3.90, 164, 165, 725, '美国常春藤盟校，历史最悠久的顶尖私立大学之一。"荣誉准则"制度与严谨本科教育闻名，注重跨学科研究。数学、物理、经济学顶尖，校训"Dei sub numine viget"。', 'https://www.princeton.edu', TRUE, 54000.00),
(20, '耶鲁大学', '美国康涅狄格州纽黑文', 23, 0.060, 3.80, 164, 163, 710, '常春藤盟校，重视本科通识教育，历史悠久的私立大学。无核心课程，学玩兼备，开放包容。以人文社科和戏剧艺术闻名，法学院全球顶尖，校训"Lux et veritas"。', 'https://www.yale.edu', TRUE, 58000.00),
(21, '多伦多大学', '加拿大多伦多', 25, 0.180, 3.60, 160, 165, 650, '加拿大顶尖研究型大学，医学与人工智能领域突出。', 'https://www.utoronto.ca', TRUE, 60000.00),
(22, '爱丁堡大学', '英国爱丁堡', 27, 0.120, 3.30, NULL, NULL, NULL, '苏格兰顶尖学府，人工智能、语言学、医学研究突出，与皇室关系密切。', 'https://www.ed.ac.uk', TRUE, 24500.00),
(23, '慕尼黑工业大学', '德国慕尼黑', 28, 0.100, 2.50, NULL, NULL, 650, '欧洲顶尖理工院校，18位诺奖得主，计算机科学全球第101', 'https://www.tum.de', TRUE, 0.00),
(24, '麦吉尔大学', '加拿大蒙特利尔', 29, 0.120, 3.80, 159, 164, 670, '加拿大"哈佛"，医学与生命科学顶尖，国际生比例超30%。', 'https://www.mcgill.ca', TRUE, 50000.00),
(25, '约翰霍普金斯大学', '美国马里兰州巴尔的摩', 32, 0.060, 3.90, 160, 164, 710, '美国顶尖私立研究型大学，GWU医学领域标杆。医学和公共卫生领域全球领先，生物医学工程和护理学顶尖。', 'https://www.jhu.edu', TRUE, 60000.00),
(26, '东京大学', '日本东京', 32, 0.100, 3.50, NULL, NULL, NULL, '日本最高学府，医学、工学、自然科学顶尖。', 'https://www.u-tokyo.ac.jp', TRUE, 535800.00),
(27, '哥伦比亚大学', '美国纽约州纽约市', 34, 0.060, 3.80, 163, 164, 720, '常春藤盟校，人文社科与商科全美顶尖，核心课程体系强调跨学科融合。位于全球金融中心。', 'https://www.columbia.edu', TRUE, 60000.00),
(28, '香港中文大学', '中国香港', 36, 0.100, 3.40, NULL, NULL, NULL, '书院制传统深厚，中文研究全球顶尖，跨学科教育模式创新。', 'https://www.cuhk.edu.hk', TRUE, 145000.00),
(29, '不列颠哥伦比亚大学', '加拿大温哥华', 38, 0.150, 3.50, 158, 162, 600, '加拿大公立顶尖学府，U15研究联盟成员。环境科学与林业全球前三，校园生态多样性突出。', 'https://www.ubc.ca', TRUE, 42000.00),
(30, '昆士兰大学', '澳大利亚布里斯班', 40, 0.180, 3.00, NULL, NULL, NULL, '澳大利亚八大名校，研究型大学全球TOP 50。科研实力强，环境科学、农业、生物技术领先，提供预科衔接课程。', 'https://www.uq.edu.au', TRUE, 35000.00),
(31, '加州大学洛杉矶分校', '美国加利福尼亚州洛杉矶', 42, 0.110, 3.90, 157, 165, 710, '公立研究型大学，电影、医学、工程和艺术领域突出', 'https://www.ucla.edu', TRUE, 43000.00),
(32, '纽约大学', '美国纽约州纽约市', 43, 0.120, 3.80, 159, 163, 730, '国际化都市大学，金融和艺术顶尖。', 'https://www.nyu.edu', TRUE, 58000.00),
(33, '密歇根大学', '美国密歇根州安娜堡', 44, 0.150, 3.80, 160, 164, 720, '公立常春藤，商科、工程、医学顶尖，校友网络强大，就业率全美前列。', 'https://www.umich.edu', TRUE, 68874.00),
(34, '香港科技大学', '中国香港', 47, 0.100, 3.50, NULL, NULL, 700, '理工科强校，工商管理亚洲第一，产学研结合紧密。', 'https://www.ust.hk', TRUE, 140000.00),
(35, '西北大学', '美国伊利诺伊州', 51, 0.070, 3.90, 162, 167, 730, '十大联盟名校，新闻和商学院顶尖。', 'https://www.northwestern.edu', TRUE, 62000.00),
(36, '伦敦政治经济学院', '英国伦敦', 52, 0.090, 3.70, NULL, NULL, 700, 'G5超级精英大学，社会科学领域全球顶级，经济学、金融学、国际关系领先，校友包括多位诺奖得主。', 'https://www.lse.ac.uk', TRUE, 27500.00),
(37, '京都大学', '日本京都', 50, 0.150, 3.40, NULL, NULL, NULL, '日本顶尖国立大学，亚洲诺奖最多，自由学风。学生自治，鼓励个性化科研。自由学术氛围，iPS细胞研究、防灾科学领先。', 'https://www.kyoto-u.ac.jp', TRUE, 535800.00),
(38, '布里斯托大学', '英国布里斯托', 54, 0.090, 3.30, NULL, NULL, NULL, '红砖大学之一，工程学、地球科学、戏剧研究强项，科研实力英国前十。', 'https://www.bris.ac.uk', TRUE, 21100.00),
(39, '慕尼黑大学', '德国慕尼黑', 59, 0.100, 2.30, NULL, NULL, NULL, '德国综合排名第一，35位诺奖得主，人文与自然科学并重', 'https://www.lmu.de', TRUE, 0.00),
(40, '香港理工大学', '中国香港', 57, 0.150, 3.30, NULL, NULL, 650, '实践型理工大学，酒店管理全球第1，土木工程、设计学亚洲领先', 'https://www.polyu.edu.hk', TRUE, 145000.00),
(41, '卡内基梅隆大学', '美国宾夕法尼亚州匹兹堡', 58, 0.050, 3.80, 160, 168, 690, '美国私立名校，计算机与戏剧学院全球顶尖。跨学科实践导向，计算机科学和工程领域顶尖，人机交互与机器学习研究领先。', 'https://www.cmu.edu', TRUE, 59000.00),
(42, '马来亚大学', '马来西亚吉隆坡', 60, 0.100, 3.00, NULL, NULL, NULL, '马来西亚排名第1，医学、工程、语言学亚洲前50，学费性价比高。', 'https://www.um.edu.my', TRUE, 35000.00),
(43, '杜克大学', '美国北卡罗来纳州达勒姆', 61, 0.130, 3.90, 162, 170, 730, '南方顶尖私立大学，生物工程和公共政策全美领先。', 'https://duke.edu', TRUE, 65000.00),
(44, '香港城市大学', '中国香港', 62, 0.150, 3.00, NULL, NULL, NULL, '创新型研究大学，传媒与材料科学突出，与业界合作紧密。', 'https://www.cityu.edu.hk', TRUE, 140000.00),
(45, '索邦大学', '法国巴黎', 63, 0.120, 3.00, NULL, NULL, NULL, '欧洲最古老大学之一，人文与自然科学并重，拥有法国最大科研网络。', 'https://www.sorbonne-universite.fr', TRUE, 2770.00),
(46, '德克萨斯大学奥斯汀分校', '美国德克萨斯州奥斯汀', 66, 0.120, 3.80, 160, 168, 710, '公立旗舰校，工程和计算机科学竞争激烈，2025年申请量增长24.3%。', 'https://www.utexas.edu', TRUE, 40000.00),
(47, '伊利诺伊大学香槟分校', '美国伊利诺伊州香槟市', 69, 0.140, 3.70, 153, 160, 650, '公立研究型大学，计算机科学、工程和会计学全美领先', 'https://illinois.edu', TRUE, 34000.00),
(48, '加州大学圣地亚哥分校', '美国加利福尼亚州圣地亚哥', 72, 0.150, 3.80, NULL, NULL, NULL, '公立研究型大学，生物科学和工程学顶尖，国际生比例高。', 'https://www.ucsd.edu/', TRUE, 45000.00),
(49, '华盛顿大学', '美国华盛顿州西雅图', 76, 0.120, 3.95, 160, 168, 710, '"新常春藤"，国际生比例13%，STEM专业标化要求严格，2027届91%录取者年级前10%。', 'http://www.washington.edu/', TRUE, 62000.00),
(50, '布朗大学', '美国罗德岛州普罗维登斯', 79, 0.080, 3.80, 161, 165, 700, '常春藤中最自由的大学，开放式课程设计。S/NC评分制，可自设专业，无竞争压力。以开放课程和灵活学术政策著称。', 'https://www.brown.edu', TRUE, 58404.00),
(51, '南安普敦大学', '英国南安普敦', 81, 0.180, 3.20, NULL, NULL, NULL, '英国罗素集团成员，理工科实力强劲。计算机/电子工程全英顶尖，SES-5联盟校。工程与计算机科学强校，光电子、海洋学研究全球前10', 'https://www.soton.ac.uk', TRUE, 23000.00),
(52, '伯明翰大学', '英国伯明翰', 80, 0.150, 3.30, NULL, NULL, 650, '英国罗素集团成员，红砖大学鼻祖。商科、材料科学、牙医学科全球领先，工业合作紧密', 'https://www.bham.ac.uk', TRUE, 24000.00),
(53, '东京工业大学', '日本东京', 84, 0.100, 3.80, NULL, NULL, NULL, '日本顶尖理工院校，材料科学、机械工程、人工智能研究领先', 'https://www.titech.ac.jp', TRUE, 12000000.00),
(54, '悉尼科技大学', '澳大利亚悉尼', 88, 0.200, 3.00, NULL, NULL, 600, '实践导向型大学，护理学、艺术设计、数据科学全澳前三', 'https://www.uts.edu.au', TRUE, 40000.00),
(55, '杜伦大学', '英国杜伦', 89, 0.150, 3.50, NULL, NULL, 680, '学院制古典大学，法学、神学、地理学全球前20，校友网络精英化', 'https://www.durham.ac.uk', TRUE, 25000.00),
(56, '宾夕法尼亚州立大学', '美国宾夕法尼亚州大学城', 89, 0.120, 3.80, 158, 165, 700, '公立常春藤，工程、地球科学、供应链管理顶尖，产学研结合突出', 'https://www.psu.edu', TRUE, 38000.00),
(57, '圣保罗大学', '巴西圣保罗', 92, 0.150, 3.50, NULL, NULL, NULL, '拉丁美洲顶尖学府，医学、农林科学、社会科学研究领先', 'https://www.usp.br', TRUE, 5000.00),
(58, '阿尔伯塔大学', '加拿大埃德蒙顿', 96, 0.180, 3.20, NULL, NULL, NULL, '加拿大顶尖研究型大学，能源工程与纳米技术领先，拥有加拿大国家纳米研究所，石油工程全球前十。', 'https://www.ualberta.ca', TRUE, 30000.00),
(59, '柏林自由大学', '德国柏林', 97, 0.120, 2.70, NULL, NULL, NULL, '德国公立综合大学，人文社科与自然科学并重。国际化程度最高德校之一，研究自由度突出。文科社科强校，国际化程度高。', 'https://www.fu-berlin.de', TRUE, 0.00),
(60, '亚琛工业大学', '德国亚琛', 99, 0.100, 2.00, NULL, NULL, 650, '欧洲"麻省理工"，机械工程顶尖，TIME联盟成员', 'https://www.rwth-aachen.de', TRUE, 0.00),
(61, '哥本哈根大学', '丹麦哥本哈根', 100, 0.080, 3.60, NULL, NULL, NULL, '北欧学术中心，生命科学、药学、物理学全球前50，免学费政策（欧盟/EEA）', 'https://www.ku.dk', TRUE, 15000.00),
(62, '谢菲尔德大学', '英国谢菲尔德', 105, 0.150, 3.00, NULL, NULL, NULL, '英国罗素集团成员，工科与材料科学强校。"城市大学"产学研典范，航空航天领域全球领先，研究经费充足。', 'https://www.sheffield.ac.uk', TRUE, 20400.00),
(63, '南加州大学', '美国加利福尼亚州洛杉矶', 125, 0.100, 3.80, 155, 162, 700, '私立大学，电影艺术、传媒、工程和商科领域领先', 'https://www.usc.edu', TRUE, 63000.00),
(64, '早稻田大学', '日本东京', 181, 0.200, 3.00, NULL, NULL, NULL, '私立名校，政治经济、商科、文化研究突出，校友网络强大。', 'https://www.waseda.jp', TRUE, 1200000.00),
(65, '里昂高等师范学院', '法国里昂', 187, 0.080, 3.60, NULL, NULL, NULL, '法国高师体系成员，自然科学与人文社科均衡发展，强调跨学科研究。', 'https://www.ens-lyon.fr', TRUE, 0.00),
(66, '渥太华大学', '加拿大渥太华', 189, 0.150, 3.30, NULL, NULL, 650, '全球最大英法双语大学，毗邻政府机构，法学与公共政策项目强势。', 'https://www.uottawa.ca', TRUE, 33000.00),
(67, '苏黎世联邦理工大学', '瑞士苏黎世', 7, 0.150, 3.00, NULL, NULL, NULL, '欧洲顶尖理工院校，诺贝尔奖得主辈出，工程与自然科学领域全球领先', 'https://www.ethz.ch', TRUE, 1500.00),
(68, '澳大利亚国立大学', '澳大利亚堪培拉', 30, 0.210, 3.30, NULL, NULL, 650, '澳大利亚唯一国立大学，研究实力顶尖，人文社科与自然科学并重', 'https://www.anu.edu.au', TRUE, 39793.00),
(69, '首尔国立大学', '韩国首尔', 31, 0.200, 3.30, NULL, NULL, NULL, '韩国顶尖学府，工科与亚洲研究突出，国际化程度高', 'https://www.snu.ac.kr', TRUE, 6000.00),
(70, '曼彻斯特大学', '英国曼彻斯特', 34, 0.100, 3.30, NULL, NULL, 680, '英国最大单一校址大学，工程与社会科学领域卓越', 'https://www.manchester.ac.uk', TRUE, 21840.00),
(71, '蒙纳士大学', '澳大利亚墨尔本', 37, 0.150, 3.00, NULL, NULL, 650, '澳大利亚八校联盟成员，药剂学与教育学全球领先', 'https://www.monash.edu', TRUE, 39450.00),
(72, '伦敦国王学院', '英国伦敦', 40, 0.110, 3.50, NULL, NULL, 680, '伦敦大学创始学院之一，医学与法律领域享誉全球', 'https://www.kcl.ac.uk', TRUE, 19740.00),
(73, '巴黎理工学院', '法国巴黎', 46, 0.120, 3.50, NULL, NULL, NULL, '法国顶尖工程师院校联盟，整合巴黎高科等顶尖资源，欧洲最大科技创新集群，数学与工程交叉学科领先。', 'https://www.ip-paris.fr', TRUE, 3770.00)


ON DUPLICATE KEY UPDATE 
    name=VALUES(name), location=VALUES(location), ranking=VALUES(ranking), acceptance_rate=VALUES(acceptance_rate), average_gpa=VALUES(average_gpa), 
    average_gre_verbal=VALUES(average_gre_verbal), average_gre_quantitative=VALUES(average_gre_quantitative), average_gmat=VALUES(average_gmat), 
    description=VALUES(description), website=VALUES(website), has_scholarship=VALUES(has_scholarship), tuition_fee=VALUES(tuition_fee);

-- 插入示例项目数据

INSERT INTO school_programs (id, school_id, name, degree_type, department, duration_years, tuition_fee, description) VALUES
-- Entry 1: 计算机科学与人工智能
(1, 1, '计算机科学与人工智能', 'BACHELOR', '苏世民计算机学院', 4, '$60,000', '本科需国际奥赛金牌或发表SCI论文'),
(2, 1, '计算机科学与人工智能', 'MASTER', '苏世民计算机学院', 2, '$65,000', '硕士需提交3个开源项目代码'),
(3, 1, '计算机科学与人工智能', 'PHD', '苏世民计算机学院', 5, '$0', '博士需在NeurIPS/ICML发表2篇顶会论文'),
-- Entry 2: 电子工程与计算机科学（EECS）
(4, 1, '电子工程与计算机科学（EECS）', 'MASTER', '工程学院', 2, '$63,000', '硕士需Verilog语言设计RISC-V处理器'),
(5, 1, '电子工程与计算机科学（EECS）', 'PHD', '工程学院', 6, '$0', '博士需持有神经形态芯片专利'),
-- Entry 3: 机械工程
(6, 1, '机械工程', 'BACHELOR', '工程学院', 4, '$58,000', '本科需物理/数学AP成绩5分'),
(7, 1, '机械工程', 'MASTER', '工程学院', 2, '$62,000', '硕士需机器人竞赛获奖'),
(8, 1, '机械工程', 'PHD', '工程学院', 5, '$0', ''), -- 博士描述缺失，根据源数据留空
-- Entry 4: 建筑学
(9, 1, '建筑学', 'MASTER', '建筑与规划学院', 3, '$68,000', '硕士需提交2个国际竞赛获奖方案'),
(10, 1, '建筑学', 'PHD', '建筑与规划学院', 6, '$0', '博士需TOEFL 105+'),
-- Entry 5: 化学工程
(11, 1, '化学工程', 'BACHELOR', '理学院', 4, '$46,400', '本科需化学AP成绩5分'),
(12, 1, '化学工程', 'MASTER', '理学院', 2, '$49,000', ''), -- 硕士描述缺失，根据源数据留空
(13, 1, '化学工程', 'PHD', '理学院', 5, '$0', '博士需发表3篇《JACS》论文'),
-- Entry 6: 物理学
(14, 1, '物理学', 'MASTER', '理学院', 2, '$55,000', '硕士需GRE Physics 95%'),
(15, 1, '物理学', 'PHD', '理学院', 7, '$0', '博士需参与过CERN项目'),
-- Entry 7: 哲学、政治与经济学（PPE）
(16, 3, '哲学、政治与经济学（PPE）', 'BACHELOR', '社会科学部', 3, '£48,620', '本科：A-Level A*AA+TSA'),
(17, 3, '哲学、政治与经济学（PPE）', 'MASTER', '社会科学部', 2, '£32,000', '硕士：一等学位+研究提案'),
(18, 3, '哲学、政治与经济学（PPE）', 'PHD', '社会科学部', 4, '£25,000', '博士：硕士学位+面试'),
-- Entry 8: 医学
(19, 3, '医学', 'BACHELOR', '医学部', 6, '£52,490', '本科：A-Level A*AA+BMAT'),
(20, 3, '医学', 'MASTER', '医学部', 1, '£38,000', ''), -- 硕士描述缺失，根据源数据留空
(21, 3, '医学', 'PHD', '医学部', 4, '£26,000', '博士：发表过SCI论文'),
-- Entry 9: 法律
(22, 3, '法律', 'BACHELOR', '法学院', 3, '£48,620', '本科：A-Level A*AA+LNAT'),
(23, 3, '法律', 'MASTER', '法学院', 1, '£40,000', ''), -- 硕士描述缺失，根据源数据留空
(24, 3, '法律', 'PHD', '法学院', 4, '£24,000', '博士需法律实务经验'),
-- Entry 10: 英语语言与文学
(25, 3, '英语语言与文学', 'BACHELOR', '英语学院', 3, '£48,620', ''), -- 本科描述缺失，根据源数据留空
(26, 3, '英语语言与文学', 'MASTER', '英语学院', 1, '£36,000', '硕士需提交文学分析样本'),
(27, 3, '英语语言与文学', 'PHD', '英语学院', 4, '£22,000', '博士需掌握古英语'),
-- Entry 11: 计算机科学
(28, 3, '计算机科学', 'BACHELOR', '计算机科学系', 4, '£48,620', '本科：A-Level A*AA+MAT'),
(29, 3, '计算机科学', 'MASTER', '计算机科学系', 1, '£41,750', ''), -- 硕士描述缺失，根据源数据留空
(30, 3, '计算机科学', 'PHD', '计算机科学系', 4, '£26,000', '博士需AI领域研究成果'),
-- Entry 12: 人类学
(31, 3, '人类学', 'BACHELOR', '社会与文化人类学学院', 3, '£48,620', ''), -- 本科描述缺失，根据源数据留空
(32, 3, '人类学', 'MASTER', '社会与文化人类学学院', 1, '£34,000', '硕士需田野调查经历'),
(33, 3, '人类学', 'PHD', '社会与文化人类学学院', 4, '£23,000', '博士需掌握民族志研究方法'),

(34, 3, '数学', 'BACHELOR', '数学学院', 4, '£48620', '本科：A-Level AAA+STEP'),
(35, 3, '数学', 'MASTER', '数学学院', 1, '£39000', '硕士项目未列明'),
(36, 3, '数学', 'PHD', '数学学院', 4, '£25000', '博士需数学竞赛奖项'),

(37, 4, '经济学', 'BACHELOR', '社会科学学院', 4, '$37576', '本科：SAT 1800+或ACT 32+'),
(38, 4, '经济学', 'MASTER', '社会科学学院', 2, '$50469', '硕士：一等学位+研究提案'),
(39, 4, '经济学', 'PHD', '社会科学学院', 4, '$25000', '博士：硕士学位+面试'),

(40, 4, '心理学', 'MASTER', '心理学系', 2, '$50469', '硕士：GRE 325+，托福100+'),
(41, 4, '心理学', 'PHD', '心理学系', 4, '$26000', '博士：发表过SCI论文+研究计划'),

(42, 4, '计算机科学', 'BACHELOR', '计算机科学系', 4, '$48620', '本科：SAT数学800+，编程项目经历'),
(43, 4, '计算机科学', 'MASTER', '计算机科学系', 1, '$41750', '硕士项目未列明'),
(44, 4, '计算机科学', 'PHD', '计算机科学系', 4, '$26000', '博士需AI研究成果'),

(45, 4, '法学', 'BACHELOR', '法学院', 3, '$48620', '本科：LSAT 170+'),
(46, 4, '法学', 'MASTER', '法学院', 1, '$40000', '硕士项目未列明'),
(47, 4, '法学', 'PHD', '法学院', 4, '$24000', 'SJD需法律实务经验'),

(48, 4, '医学', 'BACHELOR', '医学院', 6, '$52490', '本科：BMAT 6.5+'),
(49, 4, '医学', 'MASTER', '医学院', 1, '$38000', '硕士项目未列明'),
(50, 4, '医学', 'PHD', '医学院', 4, '$26000', '博士需发表SCI论文'),

(51, 4, '政治学（政府学）', 'BACHELOR', '肯尼迪政治学院', 4, '$48620', '本科：SAT写作700+'),
(52, 4, '政治学（政府学）', 'MASTER', '肯尼迪政治学院', 1, '$36000', '硕士项目未列明'),
(53, 4, '政治学（政府学）', 'PHD', '肯尼迪政治学院', 4, '$22000', '博士需政策分析经验'),

(54, 4, '人类学', 'BACHELOR', '人类学系', 3, '$48620', '本科需提交民族志分析论文'),
(55, 4, '人类学', 'MASTER', '人类学系', 1, '$34000', '硕士项目未列明'),
(56, 4, '人类学', 'PHD', '人类学系', 4, '$23000', '博士需田野调查计划'),

(57, 4, '数学', 'BACHELOR', '数学学院', 4, '$48620', '本科：SAT数学800+，数学竞赛奖项'),
(58, 4, '数学', 'MASTER', '数学学院', 1, '$39000', '硕士项目未列明'),
(59, 4, '数学', 'PHD', '数学学院', 4, '$25000', '博士需解决千禧年难题分支'),

(60, 5, '数学', 'BACHELOR', '数学学院', 4, '£37000', '本科：A-Level AAA* + STEP考试两门Grade 1'),
(61, 5, '数学', 'MASTER', '数学学院', 1, '£29000', '硕士项目未列明'),
(62, 5, '数学', 'PHD', '数学学院', 4, '£24000', '博士需数学竞赛奖项'),

(63, 5, '自然科学', 'BACHELOR', '自然科学学院', 3, '£39000', '本科：A-Level AAA（理科组合）'),
(64, 5, '自然科学', 'MASTER', '自然科学学院', 1, '£33000', '硕士项目未列明'),
(65, 5, '自然科学', 'PHD', '自然科学学院', 4, '£26000', '博士需发表SCI论文'),

(66, 5, '工程学', 'BACHELOR', '工程学部', 4, '£42000', '本科：ESAT笔试数学模块85%+'),
(67, 5, '工程学', 'MASTER', '工程学部', 1, '£36000', '硕士项目未列明'),
(68, 5, '工程学', 'PHD', '工程学部', 4, '£28000', '博士需工程领域研究成果'),

(69, 5, '计算机科学', 'BACHELOR', '计算机科学系', 4, '£44000', '本科：A-Level数学A* + 编程项目经历'),
(70, 5, '计算机科学', 'MASTER', '计算机科学系', 1, '£38000', '硕士项目未列明'),
(71, 5, '计算机科学', 'PHD', '计算机科学系', 4, '£27000', '博士需AI领域顶会论文'),

(72, 5, '人类学与考古学', 'BACHELOR', '人类学系/考古系', 3, '£36000', '本科：A-Level A*AA + 面试写作测试'),
(73, 5, '人类学与考古学', 'MASTER', '人类学系/考古系', 1, '£32000', '硕士项目未列明'),
(74, 5, '人类学与考古学', 'PHD', '人类学系/考古系', 4, '£23000', '博士需18个月田野调查计划'),

(75, 6, '计算机科学', 'MASTER', '计算机科学系', 2, '$50469', '硕士：本科不限专业，GRE均分325+，托福100+'),
(76, 6, '计算机科学', 'PHD', '计算机科学系', 4, '$26000', '博士项目未列明'),

(77, 6, '电子工程', 'BACHELOR', '工程学院', 4, '$48620', '本科：SAT数学800+，ESAT笔试85%+'),
(78, 6, '电子工程', 'MASTER', '工程学院', 2, '$41750', '硕士项目未列明'),
(79, 6, '电子工程', 'PHD', '工程学院', 4, '$26000', '博士需AI/芯片设计领域成果'),

(80, 6, '工商管理硕士', 'MASTER', '商学院', 2, '$74475', '本科GPA 3.5+，GMAT均分738，两年以上工作经验'),

(81, 6, '生物学', 'BACHELOR', '文理学院/生物工程系', 4, '$52490', '本科：A-Level理科AAA*'),
(82, 6, '生物学', 'MASTER', '文理学院/生物工程系', 1, '$38000', '硕士项目未列明'),
(83, 6, '生物学', 'PHD', '文理学院/生物工程系', 5, '$26000', '博士需发表SCI论文+18个月实验室经历')

ON DUPLICATE KEY UPDATE  
school_id=VALUES(school_id), name=VALUES(name), degree_type=VALUES(degree_type), department=VALUES(department),  
duration_years=VALUES(duration_years), tuition_fee=VALUES(tuition_fee), description=VALUES(description);

INSERT INTO school_programs (id, school_id, name, degree_type, department, duration_years, tuition_fee, description) VALUES

(84, 7, '石油工程', 'BACHELOR', '土木与环境工程系/机械工程系', 4, 'S$39000', '高考一本线+100分或A-Level AAA（数理）'),
(85, 7, '石油工程', 'MASTER', '土木与环境工程系/机械工程系', 1, 'S$30800', '相关背景+雅思6.5'),
(86, 7, '石油工程', 'PHD', '土木与环境工程系/机械工程系', 4, 'S$26000', ''),

(87, 7, '化学工程', 'MASTER', '化学与生物分子工程系', 1, 'S$49000', '化学/生物制药学位+雅思6.5'),
(88, 7, '化学工程', 'PHD', '化学与生物分子工程系', 4, 'S$38000', '2篇SCI论文'),

(89, 7, '土木工程', 'BACHELOR', '土木与环境工程系', 4, 'S$36500', 'SAT数学800+或A-Level AAA'),
(90, 7, '土木工程', 'MASTER', '土木与环境工程系', 2, 'S$55000', 'GRE320+雅思7.0'),
(91, 7, '土木工程', 'PHD', '土木与环境工程系', 4, 'S$28000', ''),

(92, 7, '计算机科学与人工智能', 'MASTER', '计算机学院', 2, 'S$52500', '计算机背景+雅思6.5+GRE320'),
(93, 7, '计算机科学与人工智能', 'PHD', '计算机学院', 4, 'S$26000', '顶会论文'),

(94, 7, '电子电气工程', 'BACHELOR', '工程学院', 4, 'S$17900', '高考数学140+/物理A+'),
(95, 7, '电子电气工程', 'MASTER', '工程学院', 1, 'S$41000', '微电子背景+雅思6.5'),
(96, 7, '电子电气工程', 'PHD', '工程学院', 4, 'S$26000', ''),

(97, 7, '商科与管理学（MBA）', 'MASTER', '商学院', 2, 'S$74400', 'GMAT738+雅思7.0+2年经验'),

(98, 7, '法学', 'MASTER', '法学院', 1, 'S$35000', '法学学士+雅思7.0'),
(99, 7, '法学', 'PHD', '法学院', 3, 'S$27000', 'SSCI论文'),

(100, 8, '教育学', 'BACHELOR', '教育学院（IOE）', 3, '£26600', 'A-Level A*AA（含社会科学科目）+雅思7.0（6.5）'),
(101, 8, '教育学', 'MASTER', '教育学院（IOE）', 1, '£32100', ''),  
(102, 8, '教育学', 'PHD', '教育学院（IOE）', 4, '£24000', '博士需发表SSCI论文'),

(103, 8, '建筑学', 'BACHELOR', '巴特莱特建筑学院', 3, '£39400', '提交作品集+面试（需展示空间句法设计能力）'),
(104, 8, '建筑学', 'MASTER', '巴特莱特建筑学院', 2, '£44900', '相关专业背景+学术论文样本'),
(105, 8, '建筑学', 'PHD', '巴特莱特建筑学院', 4, '£28000', ''),

(106, 8, '药剂学与药理学', 'MASTER', '药学院', 1, '£33200', '药学/化学相关学科二等学位+雅思6.5（单项6.0）'),
(107, 8, '药剂学与药理学', 'PHD', '药学院', 4, '£26000', '发表SCI论文+实验室经历'),

(108, 9, '物理学', 'BACHELOR', '物理、数学与天文学学院', 4, '$63402', 'SAT 1550+, AP物理C 5分'),
(109, 9, '物理学', 'MASTER', '物理、数学与天文学学院', 2, '$58000', ''),  
(110, 9, '物理学', 'PHD', '物理、数学与天文学学院', 6, '$0', '发表《Nature Physics》级别论文'),

(111, 9, '航空航天工程', 'MASTER', '工程与应用科学学院', 2, '$62000', 'GRE 330+, 微积分/流体力学课程A'),
(112, 9, '航空航天工程', 'PHD', '工程与应用科学学院', 6, '$0', 'JPL项目经历'),

(113, 9, '计算机科学与人工智能', 'MASTER', '计算机学院', 2, '$65000', '托福105+, NeurIPS/ICML论文'),
(114, 9, '计算机科学与人工智能', 'PHD', '计算机学院', 5, '$0', '开发开源AI工具'),

(115, 9, '化学工程', 'BACHELOR', '化学与化学工程学院', 4, '$63402', 'A-Level化学/数学A*'),
(116, 9, '化学工程', 'MASTER', '化学与化学工程学院', 1, '$58500', ''),  
(117, 9, '化学工程', 'PHD', '化学与化学工程学院', 6, '$0', '纳米材料合成专利'),

(118, 9, '地球与行星科学', 'MASTER', '地质与行星科学学院', 2, '$55000', '地质学/物理背景+GRE 325'),
(119, 9, '地球与行星科学', 'PHD', '地质与行星科学学院', 6, '$0', '参与NASA项目'),

(120, 10, '护理学', 'BACHELOR', '护理学院', 4, '$68000', 'SAT 1500+或ACT 34+, 高中生物/化学A'),
(121, 10, '护理学', 'MASTER', '护理学院', 2, '$74400', '护理学士+注册护士执照'),
(122, 10, '护理学', 'PHD', '护理学院', 5, '$0', '发表2篇SCI论文'),

(123, 10, '商科与管理学（MBA）', 'MASTER', '沃顿商学院', 2, '$87370', 'GMAT均分738+雅思7.5+两年管理经验；需提交区块链金融案例分析报告'),

(124, 10, '医学（MD）', 'PHD', '佩雷尔曼医学院', 4, '$0', 'MCAT 521+, 生物化学/生理学课程A；500小时临床实习+3封医学专家推荐信'),

(125, 10, '法学（LLM）', 'MASTER', '凯里法学院', 1, '$75000', '法学学士+雅思7.5（写作7.0）；需提交国际仲裁或知识产权法案例分析'),

(126, 11, '计算机科学（CS）', 'BACHELOR', '工程学院/CDSS学院', 4, '$60766', 'SAT 1500+, AP微积分BC 5分'),
(127, 11, '计算机科学（CS）', 'MASTER', '工程学院/CDSS学院', 2, '$60766', '编程/算法证书'),
(128, 11, '计算机科学（CS）', 'PHD', '工程学院/CDSS学院', 6, '$0', '顶会论文[5,6]'),

(129, 11, '电气工程与计算机科学（EECS）', 'BACHELOR', '工程学院', 4, '$60766', '本科直申（录取率4.5%）'),
(130, 11, '电气工程与计算机科学（EECS）', 'MASTER', '工程学院', 1, '$62000', 'GRE 330+'),
(131, 11, '电气工程与计算机科学（EECS）', 'PHD', '工程学院', 6, '$0', '芯片设计/机器人项目经历[5,6]'),

(132, 11, '化学工程', 'BACHELOR', '化学工程学院', 4, '$63402', 'A-Level化学/数学A*'),
(133, 11, '化学工程', 'MASTER', '化学工程学院', 1, '$58500', '纳米材料专利'),
(134, 11, '化学工程', 'PHD', '化学工程学院', 7, '$0', '催化研究成果[3,4]'),

(135, 11, '土木工程', 'MASTER', '土木与环境工程系', 1, '$55000', 'GRE 325+'),
(136, 11, '土木工程', 'PHD', '土木与环境工程系', 5, '$0', '参与抗震项目（如金门大桥案例）[1,2]'),

(137, 11, '经济学', 'BACHELOR', '文理学院', 4, '$37775', '高中GPA 3.9+, SAT 1540+'),
(138, 11, '经济学', 'PHD', '文理学院', 6, '$0', '田野调查经历[4,6]'),

(139, 11, '公共政策分析', 'MASTER', '高盛公共政策学院', 1, '$58000', '公共事务背景+GRE 328'),
(140, 11, '公共政策分析', 'PHD', '高盛公共政策学院', 5, '$0', '联合国/世卫组织实习经历[2,6]'),

(141, 2, '计算机科学（Computer Science）', 'BACHELOR', '工程学院/计算机系', 4, '£37900', 'A-Level A*A*A（数学A*+物理A*）'),
(142, 2, '计算机科学（Computer Science）', 'MASTER', '工程学院/计算机系', 2, '£44000', '编程竞赛获奖'),
(143, 2, '计算机科学（Computer Science）', 'PHD', '工程学院/计算机系', 6, '£0', 'NeurIPS/ICML论文[6,8]'),

(144, 2, '土木工程（Civil Engineering）', 'BACHELOR', '土木与环境工程系', 4, '£37900', 'A-Level A*AA（数学A*+物理A）'),
(145, 2, '土木工程（Civil Engineering）', 'MASTER', '土木与环境工程系', 1, '£44000', '参与抗震项目（如金门大桥案例）[9]'),
(146, 2, '土木工程（Civil Engineering）', 'PHD', '土木与环境工程系', 5, '£0', ''),

(147, 2, '医学（Medicine）', 'BACHELOR', '医学院', 6, '£46650', 'A-Level AAA（生物A+化学A）+UCAT笔试+面试'),
(148, 2, '医学（Medicine）', 'PHD', '医学院', 4, '£0', '发表《Cell》或《NEJM》论文[4,14]'),

(149, 2, '化学工程（Chemical Engineering）', 'BACHELOR', '化学工程学院', 4, '£37900', 'A-Level A*A*A（化学A*+数学A*）'),
(150, 2, '化学工程（Chemical Engineering）', 'MASTER', '化学工程学院', 1, '£44000', '纳米材料专利[15]'),
(151, 2, '化学工程（Chemical Engineering）', 'PHD', '化学工程学院', 7, '£0', '')


ON DUPLICATE KEY UPDATE 
    school_id=VALUES(school_id), name=VALUES(name), degree_type=VALUES(degree_type), department=VALUES(department), 
    duration_years=VALUES(duration_years), tuition_fee=VALUES(tuition_fee), description=VALUES(description);

INSERT INTO school_programs (id, school_id, name, degree_type, department, duration_years, tuition_fee, description) VALUES
(152, 2, '电子电气工程（EECS）', 'BACHELOR', '工程学院', 4, '£37900', 'A-Level A*A*A（数学A*+物理A*）+ESAT笔试'),
(153, 2, '电子电气工程（EECS）', 'MASTER', '工程学院', 1, '£44000', ''),
(154, 2, '电子电气工程（EECS）', 'PHD', '工程学院', 6, '0', '需芯片设计项目经历'),
(155, 2, '金融与工商管理（MBA）', 'MASTER', '商学院', 2, '£65000/£44000', 'GMAT均分738+雅思7.5+两年管理经验；需提交区块链金融案例分析'),
(156, 12, '教育学（Education）', 'BACHELOR', '墨尔本教育研究生院（MGSE）', 3, '$46650', 'A-Level AAA（需生物/化学）+UCAT笔试+面试'),
(157, 12, '教育学（Education）', 'MASTER', '墨尔本教育研究生院（MGSE）', 2, '$48284', '需1年教育相关经验+雅思7.0（写作7.0）'),
(158, 12, '教育学（Education）', 'PHD', '墨尔本教育研究生院（MGSE）', 5, '0', '需发表《Journal of Educational Psycholoy》论文'),
(159, 12, '法律（Law）', 'MASTER', '墨尔本法学院（Melbourne Law School）', 1, '$56000', '需法学本科背景+GPA 3.7/4.0+雅思7.0（单项6.5）'),
(160, 12, '法律（Law）', 'PHD', '墨尔本法学院（Melbourne Law School）', 4, '0', '需2年法律从业经验+研究计划书'),
(161, 12, '医学（Medicine）', 'BACHELOR', '医学院（Faculty of Medicine）', 6, '$46650', 'A-Level A*A*A（生物A*+化学A*）+UCAT笔试前10%'),
(162, 12, '医学（Medicine）', 'PHD', '医学院（Faculty of Medicine）', 4, '0（含临床津贴）', '需《Nature Medicine》或《NEJM》发表论文'),
(163, 12, '会计与金融（Accounting & Finance）', 'MASTER', '墨尔本商学院（MBS）', 2, '$44000', '需本科相关专业+GPA 3.5/4.0+雅思6.5（单项6.0）'),
(164, 12, '会计与金融（Accounting & Finance）', 'PHD', '墨尔本商学院（MBS）', 5, '0', '需CFA/CPA证书+2篇SSCI期刊论文'),
(165, 13, '材料科学与工程（Materials Science & Engineering）', 'MASTER', '材料科学与工程学院', 1, 'S$55000', '需课程项目报告（如石墨烯合成技术）'),
(166, 13, '材料科学与工程（Materials Science & Engineering）', 'PHD', '材料科学与工程学院', 6, '0', '需发表《Science》级别论文'),
(167, 13, '计算机科学与信息系统（Computer Science & Information Systems）', 'MASTER', '计算机科学与工程学院', 2, 'S$44000', '需顶会论文（如NeurIPS算法优化）'),
(168, 13, '计算机科学与信息系统（Computer Science & Information Systems）', 'PHD', '计算机科学与工程学院', 6, '0', '需开发开源工具（如量子计算框架）'),
(169, 13, '传播与媒体研究（Communication & Media Studies）', 'MASTER', '黄金辉传播与信息学院', 1, 'S$37800', '需媒体案例分析（如VR新闻制作）'),
(170, 13, '传播与媒体研究（Communication & Media Studies）', 'PHD', '黄金辉传播与信息学院', 5, '0', '需15万字专著（含数字传播模型）'),
(171, 14, '农业科学与工程（Agricultural Science & Engineering）', 'BACHELOR', '农业与生命科学学院', 4, '$46650', 'A-Level A*A*A（生物A*+化学A*）+SAT 1500+'),
(172, 14, '农业科学与工程（Agricultural Science & Engineering）', 'MASTER', '农业与生命科学学院', 2, '$48284', '需1年农业相关经验+托福100'),
(173, 14, '农业科学与工程（Agricultural Science & Engineering）', 'PHD', '农业与生命科学学院', 5, '0', '需发表《Nature》子刊论文'),
(174, 14, '酒店管理（Hotel Management）', 'BACHELOR', '诺兰酒店管理学院', 4, '$46650', '高中GPA 3.8+托福100+酒店业实习证明'),
(175, 14, '酒店管理（Hotel Management）', 'MASTER', '诺兰酒店管理学院', 2, '$56000', 'GMAT 640+3封行业推荐信'),
(176, 14, '工程学（Engineering）', 'BACHELOR', '工程学院', 4, '$46650', 'SAT 1550+物理/数学竞赛奖项'),
(177, 14, '工程学（Engineering）', 'MASTER', '工程学院', 2, '$44000', '3篇EI会议论文+托福100'),
(178, 14, '工程学（Engineering）', 'PHD', '工程学院', 5, '0', '提交智能材料研发计划书'),
(179, 15, '牙医学', 'BACHELOR', '牙医学院', 6, 'HK$218000/HK$44500', '高考700分+/英语140分+；A-Level需3A*+生物/化学；雅思7.0'),
(180, 15, '牙医学', 'MASTER', '牙医学院', 2, '', ''),
(181, 15, '牙医学', 'PHD', '牙医学院', 6, '', ''),
(182, 15, '教育学', 'MASTER', '教育学院', 1, 'HK$198000', '需学士学位+雅思6.5'),
(183, 15, '教育学', 'PHD', '教育学院', 5, '0', '需发表2篇SSCI论文'),
(184, 15, '法律学', 'BACHELOR', '法学院', 4, 'HK$198000', '高考需全省前0.5%；雅思7.0（写作≥7.0）'),
(185, 15, '法律学', 'MASTER', '法学院', 1, 'HK$220000', ''),
(186, 15, '医学（生物医学方向）', 'BACHELOR', '李嘉诚医学院', 6, 'HK$218000', '高考生物/化学满分；雅思7.5（单项≥7.0）'),
(187, 15, '医学（生物医学方向）', 'PHD', '李嘉诚医学院', 5, '0', ''),
(188, 16, '医学（含生物医学、护理学）', 'BACHELOR', '医学院', 6, 'A$218000/A$44500', '高考生物/化学满分；雅思7.0（单项≥7.0）'),
(189, 16, '医学（含生物医学、护理学）', 'MASTER', '医学院', 2, '', ''),
(190, 16, '医学（含生物医学、护理学）', 'PHD', '医学院', 4, '', ''),
(191, 16, '法学', 'BACHELOR', '法学院', 4, 'A$198000/A$42000', '高考全省前0.5%；雅思7.0（写作≥7.0）'),
(192, 16, '法学', 'MASTER', '法学院', 1, '', ''),
(193, 16, '工程学（含土木、机械、计算机工程）', 'BACHELOR', '工程学院', 4, 'A$50000', 'SAT 1550+物理/数学竞赛奖项'),
(194, 16, '工程学（含土木、机械、计算机工程）', 'MASTER', '工程学院', 2, 'A$45000', '需3篇EI会议论文'),
(195, 16, '工程学（含土木、机械、计算机工程）', 'PHD', '工程学院', 4, '', ''),
(196, 16, '会计与金融', 'MASTER', '商学院', 2, 'A$55000', 'GPA 3.0/4.0+；雅思7.0（单项≥6.0）'),
(197, 19, '数学', 'BACHELOR', '数学学院', 4, '$62400', 'SAT 1540+或ACT 34+；数学竞赛金奖；雅思7.5（写作≥7.0）'),
(198, 19, '数学', 'PHD', '数学学院', 6, '0', ''),
(199, 19, '物理学', 'BACHELOR', '物理系', 4, '$62400', 'AP物理/微积分满分；托福110（口语≥26）'),
(200, 19, '物理学', 'PHD', '物理系', 5, '0', ''),
(201, 19, '经济学', 'BACHELOR', '经济系', 4, '$62400', 'GPA 3.9/4.0+；GRE数学170'),
(202, 19, '经济学', 'PHD', '经济系', 4, '0', ''),
(203, 19, '公共与国际事务', 'MASTER', '伍德罗·威尔逊学院', 2, '$54000', '需3年政策领域工作经验；托福114（写作≥28）'),
(204, 19, '公共与国际事务', 'PHD', '伍德罗·威尔逊学院', 5, '0', ''),
(205, 20, '法学', 'MASTER', '法学院', 1, '$74044', '本科GPA 3.9+/SAT 1550+；LSAT 170+；法学硕士需博士学历或法律领域工作经验'),
(206, 20, '法学', 'PHD', '法学院', 4, '0', ''),
(207, 20, '历史学', 'BACHELOR', '历史学系', 4, '$62400', 'SAT 1540+/ACT 34+；AP历史满分；雅思7.5（写作≥7.0）'),
(208, 20, '历史学', 'PHD', '历史学系', 6, '0', ''),
(209, 23, '机械工程', 'BACHELOR', '机械工程学院', 4, '€4000', 'Abitur或国际同等学历+TestDaF 16'),
(210, 23, '机械工程', 'MASTER', '机械工程学院', 2, '€6000', '机械工程相关本科背景+雅思6.5'),
(211, 23, '电气工程与信息技术', 'MASTER', '电气与信息工程学院', 2, '€6000', '本科均分85+/4.0；需通过专业匹配度评估（数学32学分+物理24学分）'),
(212, 23, '计算机科学与工程', 'BACHELOR', '计算、信息与技术学院', 3, '€2000', '数学竞赛金奖'),
(213, 23, '计算机科学与工程', 'MASTER', '计算、信息与技术学院', 2, '€6000', 'GRE数学170+雅思7.0'),
(214, 25, '医学', 'PHD', '医学院', 7, '0', '')
ON DUPLICATE KEY UPDATE 
    school_id=VALUES(school_id), name=VALUES(name), degree_type=VALUES(degree_type), department=VALUES(department), 
    duration_years=VALUES(duration_years), tuition_fee=VALUES(tuition_fee), description=VALUES(description);


INSERT INTO school_programs (id, school_id, name, degree_type, department, duration_years, tuition_fee, description) VALUES
(215, 25, '公共卫生', 'MASTER', '布隆伯格公共卫生学院', 2, '$45,000', 'GRE 325+/雅思7.5（写作≥7.0）；需2年疾控中心经验'),
(216, 25, '公共卫生', 'PHD', '布隆伯格公共卫生学院', 6, '$0', 'GRE 325+/雅思7.5（写作≥7.0）；需2年疾控中心经验'),
(217, 25, '生物医学工程', 'MASTER', '怀廷工程学院', 2, '$58,557', 'GRE数学170+；需机械设计/生物材料课程证书'),
(218, 25, '生物医学工程', 'PHD', '怀廷工程学院', 5, '$0', 'GRE数学170+；需机械设计/生物材料课程证书'),
(219, 25, '国际关系', 'MASTER', '高级国际研究学院(SAIS)', 2, '$71,535', 'GMAT 720+/托福110；需掌握2门外语（含中/法/阿语）'),
(220, 26, '现代语言学', 'BACHELOR', '文学部言语学学科', 4, '¥535,800', '日语N1证书+英语托福85+'),
(221, 26, '现代语言学', 'MASTER', '文学部言语学学科', 2, '¥535,800', '日语N1证书+英语托福85+；需提交《源氏物语》语言演变分析报告'),
(222, 26, '现代语言学', 'PHD', '文学部言语学学科', 5, '¥535,800', '日语N1证书+英语托福85+'),
(223, 26, '物理学与天文学', 'BACHELOR', '理学部物理学科', 4, '¥535,800', '物理国际竞赛金奖+托福90+'),
(224, 26, '物理学与天文学', 'MASTER', '理学部物理学科', 2, '¥535,800', '物理国际竞赛金奖+托福90+'),
(225, 26, '物理学与天文学', 'PHD', '理学部物理学科', 5, '¥0', '物理国际竞赛金奖+托福90+；需量子物理研究经历'),
(226, 26, '医学', 'BACHELOR', '医学部医学科', 6, '¥4,818,000', '留考730+/日语N1+生物化学实验室实习证明'),
(227, 26, '医学', 'MASTER', '医学部医学科', 2, '¥2,000,000', '留考730+/日语N1+生物化学实验室实习证明'),
(228, 26, '医学', 'PHD', '医学部医学科', 6, '¥2,000,000', '留考730+/日语N1+生物化学实验室实习证明'),
(229, 26, '机械工程与电子工程', 'BACHELOR', '工学部机械工学科', 4, '¥535,800', '数学/物理竞赛全国前10%+机械设计作品集'),
(230, 26, '机械工程与电子工程', 'MASTER', '工学部机械工学科', 2, '¥535,800', '数学/物理竞赛全国前10%+机械设计作品集'),
(231, 26, '机械工程与电子工程', 'PHD', '工学部机械工学科', 5, '¥0', '数学/物理竞赛全国前10%+机械设计作品集'),
(232, 27, '新闻学', 'MASTER', '新闻学院', 1, '$84,496', '• 本科GPA 3.7+ • 托福114+或雅思8.0 • 需提交3篇新闻作品（含数据新闻报道）'),
(233, 27, '金融经济学', 'MASTER', '哥伦比亚商学院', 2, '$71,544', '• GRE数学170/定量169+ • 需2年投行/对冲基金实习经验 • 掌握Python/R编程'),
(234, 27, '国际关系与公共事务（SIPA）', 'MASTER', '国际与公共事务学院', 2, '$82,818', '• GMAT 720+/托福110 • 需掌握2门外语（含中/法/阿语）'),
(235, 27, '生物医学信息学', 'MASTER', '文理学院生物医学信息学系', 2, '$68,400', '• 生物/计算机相关本科背景 • GRE 325+（数学≥168） • 需CRISPR或医疗AI项目经历'),
(236, 28, '护理学', 'BACHELOR', '那打素护理学院', 5, 'HK$145,000', '• 护理学相关本科背景 • 雅思6.5（写作≥6.0）或托福80+ • 需40小时临床实习证明'),
(237, 28, '护理学', 'MASTER', '那打素护理学院', 2, '¥44,000', '• 护理学相关本科背景 • 雅思6.5（写作≥6.0）或托福80+ • 需40小时临床实习证明'),
(238, 28, '护理学', 'PHD', '那打素护理学院', 5, '¥0', '• 护理学相关本科背景 • 雅思6.5（写作≥6.0）或托福80+ • 需40小时临床实习证明'),
(239, 28, '传媒学（传播及媒体研究）', 'MASTER', '传播与媒体学院', 1, 'HK$148,500', '• 本科GPA 3.3/4.0+ • 雅思6.5（单项≥6.0）或托福79+ • 需提交3篇新闻作品'),
(240, 28, '语言学', 'MASTER', '文学院语言学系', 1, 'HK$135,000', '• 中文/英语相关背景 • 雅思6.5或托福79 • 需精通中文及一门外语'),
(241, 34, '数据科学与人工智能', 'MASTER', '工学院/信息枢纽', 1, 'HK$148,500', '• 计算机/数学/统计本科背景 • 雅思6.5或托福80 • 需掌握Python/Java编程'),
(242, 34, '计算机科学与工程', 'BACHELOR', '计算机科学与工程系', 4, 'HK$182,000', '• GRE数学168+ • 需2年科技企业实习经历 • 掌握TensorFlow/PyTorch框架'),
(243, 34, '计算机科学与工程', 'MASTER', '计算机科学与工程系', 1, 'HK$186,900', '• GRE数学168+ • 需2年科技企业实习经历 • 掌握TensorFlow/PyTorch框架'),
(244, 34, '计算机科学与工程', 'PHD', '计算机科学与工程系', 5, 'HK$0', '• GRE数学168+ • 需2年科技企业实习经历 • 掌握TensorFlow/PyTorch框架'),
(245, 34, '土木与结构工程', 'MASTER', '土木及环境工程系', 1, 'HK$186,900', '• 土木工程本科背景 • 雅思6.5（单项≥6.0） • 需BIM建模作品集'),
(246, 34, '商业与管理（MBA）', 'MASTER', '商学院', 2, 'HK$685,000', '• GMAT 720+/托福100 • 需5年管理层工作经验'),
(247, 39, '哲学', 'BACHELOR', '哲学、科学理论及宗教学院', 3, '€0', '• 高中均分70%+或本科哲学相关背景 • 德语授课需德福4×4或DSH-2 • 英语授课需雅思6.5/托福90'),
(248, 39, '哲学', 'MASTER', '哲学、科学理论及宗教学院', 2, '€0', '• 高中均分70%+或本科哲学相关背景 • 德语授课需德福4×4或DSH-2 • 英语授课需雅思6.5/托福90'),
(249, 39, '哲学', 'PHD', '哲学、科学理论及宗教学院', 5, '€0', '• 高中均分70%+或本科哲学相关背景 • 德语授课需德福4×4或DSH-2 • 英语授课需雅思6.5/托福90'),
(250, 39, '古典文学与古代史', 'MASTER', '文学院古代历史系', 2, '€0', '• 古典学/历史学本科背景 • 拉丁语B2+希腊语B1水平 • 提交中世纪手稿分析报告'),
(251, 39, '医学', 'BACHELOR', '医学院', 6, '€0', '• 需通过TestAS医学能力测试 • 德语DSH-2+英语B2 • 本科需提供40小时医院实习证明'),
(252, 39, '医学', 'MASTER', '医学院', 2, '€0', '• 需通过TestAS医学能力测试 • 德语DSH-2+英语B2'),
(253, 39, '医学', 'PHD', '医学院', 6, '€0', '• 需通过TestAS医学能力测试 • 德语DSH-2+英语B2'),
(254, 39, '法学', 'BACHELOR', '法学院', 4, '€0', '• 法学本科背景GPA3.0+/4.0 • 德语DSH-2+英语雅思7.0（国际法方向）'),
(255, 39, '法学', 'MASTER', '法学院', 1, '€0', '• 法学本科背景GPA3.0+/4.0 • 德语DSH-2+英语雅思7.0（国际法方向）'),
(256, 39, '法学', 'PHD', '法学院', 5, '€0', '• 法学本科背景GPA3.0+/4.0 • 德语DSH-2+英语雅思7.0（国际法方向）'),
(257, 42, '图书馆与信息管理', 'MASTER', '艺术与社会科学学院', 2, 'RM36,000', '• 图书馆/信息管理本科背景（CGPA≥3.0） • 雅思5.5或托福80 • 需提交中世纪手稿分析报告'),
(258, 42, '数据科学与人工智能', 'MASTER', '计算机科学与信息技术学院', 2, 'RM36,000', '• 数学/统计/计算机本科背景（GPA≥3.0） • 掌握Python/R编程 • 雅思6.0或托福80'),
(259, 42, '医学（临床医学）', 'BACHELOR', '医学院', 5, 'RM81,100', '• 本科需通过TestAS医学测试 • 雅思6.0（单项≥5.5）'),
(260, 42, '医学（临床医学）', 'MASTER', '医学院', 3, 'RM42,000', '• 本科需通过TestAS医学测试 • 雅思6.0（单项≥5.5） • 需40小时医院实习证明'),
(261, 42, '医学（临床医学）', 'PHD', '医学院', 6, 'RM0', '• 本科需通过TestAS医学测试 • 雅思6.0（单项≥5.5）'),
(262, 50, '计算机科学', 'BACHELOR', '工程学院', 4, '$68,230', '• 本科需完成5门核心编程课程（GPA≥3.5）'),
(263, 50, '计算机科学', 'MASTER', '工程学院', 2, '$72,500', '• 本科需完成5门核心编程课程（GPA≥3.5） • 硕士需掌握Python/Julia/C++'),
(264, 50, '计算机科学', 'PHD', '工程学院', 5, '$0', '• 本科需完成5门核心编程课程（GPA≥3.5） • 硕士需掌握Python/Julia/C++ • 博士需发表顶会论文（NeurIPS/ICML）'),
(265, 50, '经济学', 'BACHELOR', '文理学院', 4, '$68,230', '• 本科需宏观/微观经济学A-'),
(266, 50, '经济学', 'MASTER', '文理学院', 2, '$61,200', '• 本科需宏观/微观经济学A- • 硕士需线性代数+计量经济学基础'),
(267, 50, '经济学', 'PHD', '文理学院', 6, '$0', '• 本科需宏观/微观经济学A- • 硕士需线性代数+计量经济学基础 • 博士需SSCI期刊论文预发表'),
(268, 50, '生物学/医学预科', 'BACHELOR', '医学院', 4, '$68,230', '• 本科需AP生物5分+化学实验报告'),
(269, 50, '生物学/医学预科', 'MASTER', '医学院', 2, '$75,000', '• 本科需AP生物5分+化学实验报告 • 硕士需40小时临床实习证明'),
(270, 50, '生物学/医学预科', 'PHD', '医学院', 5, '$0', '• 本科需AP生物5分+化学实验报告 • 硕士需40小时临床实习证明 • 博士需SCI论文（IF≥5.0）'),
(271, 50, '工程学（生物医学工程）', 'MASTER', '工程学院', 2, '$72,500', '• 硕士需生物/材料工程本科背景'),
(272, 50, '工程学（生物医学工程）', 'PHD', '工程学院', 5, '$0', '• 博士需专利或医疗器械设计作品'),
(273, 56, '地球与矿物科学（地理学/地质学/气象学）', 'BACHELOR', '地球与矿产科学学院', 4, '$33,734', '• 本科需GPA≥3.5'),
(274, 56, '地球与矿物科学（地理学/地质学/气象学）', 'MASTER', '地球与矿产科学学院', 2, '$36,000', '• 本科需GPA≥3.5 • 硕士需提交GIS/遥感技术研究报告'),
(275, 56, '地球与矿物科学（地理学/地质学/气象学）', 'PHD', '地球与矿产科学学院', 5, '$0', '• 本科需GPA≥3.5 • 硕士需提交GIS/遥感技术研究报告 • 博士需发表SCI论文（IF≥5.0）')
ON DUPLICATE KEY UPDATE 
    school_id=VALUES(school_id), name=VALUES(name), degree_type=VALUES(degree_type), department=VALUES(department), 
    duration_years=VALUES(duration_years), tuition_fee=VALUES(tuition_fee), description=VALUES(description);



INSERT INTO school_programs (id, school_id, name, degree_type, department, duration_years, tuition_fee, description) VALUES
(276, 56, '工程学（工业工程/材料工程/石油工程）', 'BACHELOR', '工程学院', 4, '$33,734/年', '• 本科需数学建模竞赛奖项'),
(277, 56, '工程学（工业工程/材料工程/石油工程）', 'MASTER', '工程学院', 2, '$36,000/全程', '• 硕士需掌握SolidWorks/ANSYS软件'),
(278, 56, '工程学（工业工程/材料工程/石油工程）', 'PHD', '工程学院', 5, '全免', '• 博士需持有材料学专利'),
(279, 56, '大众传媒与新闻学', 'BACHELOR', '传媒学院', 4, '$33,734/年', '• 本科需提交新闻作品集'),
(280, 56, '大众传媒与新闻学', 'MASTER', '传媒学院', 2, '$28,874/年', '• 硕士需雅思7.0（单项≥6.5）'),
(281, 56, '农业科学', 'BACHELOR', '农学院', 4, '$33,734/年', '• 本科需AP生物4分以上'),
(282, 56, '农业科学', 'MASTER', '农学院', 2, '$15,072/学期', '• 硕士需提交农业经济模型'),
(283, 56, '农业科学', 'PHD', '农学院', 5, '全免', '• 博士需SCI论文（IF≥3.0）'),
(284, 57, '牙科', 'BACHELOR', '牙医学院（FO）', 4, '全免（仅注册费200雷亚尔/学期）', '• 本科需通过FUVEST考试（录取率<5%）[2,9](@ref)'),
(285, 57, '牙科', 'MASTER', '牙医学院（FO）', 2, '全免（仅注册费200雷亚尔/学期）', '• 硕士需提交数字化诊疗技术报告[7](@ref)'),
(286, 57, '牙科', 'PHD', '牙医学院（FO）', 5, '全免（仅注册费200雷亚尔/学期）', '• 博士需持有牙科专利或设备设计'),
(287, 57, '石油工程', 'BACHELOR', '理工学院（POLI）', 5, '全免（仅注册费200雷亚尔/学期）', '• 本科需数学建模竞赛奖项[15](@ref)'),
(288, 57, '石油工程', 'MASTER', '理工学院（POLI）', 2, '全免（仅注册费200雷亚尔/学期）', '• 硕士需掌握Petrel/CMG软件[5](@ref)'),
(289, 57, '石油工程', 'PHD', '理工学院（POLI）', 4, '全免（仅注册费200雷亚尔/学期）', '• 博士需发表2篇SCI论文（IF≥3.0）'),
(290, 57, '动物科学', 'BACHELOR', '兽医和动物科学学院（FMVZ）', 4, '全免（仅注册费200雷亚尔/学期）', '• 本科需AP生物5分+化学实验报告[9](@ref)'),
(291, 57, '动物科学', 'MASTER', '兽医和动物科学学院（FMVZ）', 2, '全免（仅注册费200雷亚尔/学期）', '• 硕士需40小时雨林科考经历[2](@ref)'),
(292, 57, '动物科学', 'PHD', '兽医和动物科学学院（FMVZ）', 5, '全免（仅注册费200雷亚尔/学期）', '• 博士需SCI论文（IF≥5.0）[4](@ref)'),
(293, 57, '人类学', 'MASTER', '哲学、文学及人文科学学院', 2, '全免（仅注册费200雷亚尔/学期）', '• 硕士需提交全球化社会变迁分析报告[4](@ref)'),
(294, 57, '人类学', 'PHD', '哲学、文学及人文科学学院', 4, '全免（仅注册费200雷亚尔/学期）', '• 博士需掌握葡萄牙语+土著语言[2,7](@ref)'),
(295, 58, '石油工程', 'BACHELOR', '工程学院', 4, '$33,734/年', '• 本科需高中理科均分≥85%且通过FUVEST考试[1,2](@ref)'),
(296, 58, '石油工程', 'MASTER', '工程学院', 2, '$36,000/全程', '• 硕士需掌握Petrel/CMG软件[4,5](@ref)'),
(297, 58, '石油工程', 'PHD', '工程学院', 5, '全免', '• 博士需发表2篇SCI论文（IF≥3.0）[3,13](@ref)'),
(298, 58, '人工智能与机器学习', 'BACHELOR', '理学院（计算机科学系）', 4, '$33,734/年', '• 本科需数学建模竞赛奖项[14,15](@ref)'),
(299, 58, '人工智能与机器学习', 'MASTER', '理学院（计算机科学系）', 2, '$38,500/年', '• 硕士需Python/C++高级编程能力[7,8](@ref)'),
(300, 58, '人工智能与机器学习', 'PHD', '理学院（计算机科学系）', 5, '全免', '• 博士需持有机器学习专利[6,14](@ref)'),
(301, 58, '农业科学', 'BACHELOR', '农业、生命与环境科学学院', 4, '$33,734/年', '• 本科需AP生物4分+化学实验报告[11,16](@ref)'),
(302, 58, '农业科学', 'MASTER', '农业、生命与环境科学学院', 2, '$15,072/学期', '• 硕士需40小时农场实践经历[10,11](@ref)'),
(303, 58, '农业科学', 'PHD', '农业、生命与环境科学学院', 5, '全免', '• 博士需SCI论文（IF≥3.0）[10,16](@ref)'),
(304, 60, '机械工程', 'BACHELOR', '机械学院', 3, '全免（仅注册费300-400欧元/学期）', '• 本科需高中理科均分≥85%且通过APS审核'),
(305, 60, '机械工程', 'MASTER', '机械学院', 2, '全免（仅注册费300-400欧元/学期）', '• 硕士需课程匹配度≥80%'),
(306, 60, '机械工程', 'PHD', '机械学院', 5, '全免（仅注册费300-400欧元/学期）', '• 博士需发表2篇SCI论文（IF≥3.0）'),
(307, 60, '计算机科学', 'BACHELOR', '理学院（计算机科学系）', 3, '全免（仅注册费300-400欧元/学期）', '• 本科需数学建模竞赛奖项'),
(308, 60, '计算机科学', 'MASTER', '理学院（计算机科学系）', 2, '全免（仅注册费300-400欧元/学期）', '• 硕士需掌握Python/C++/Java'),
(309, 60, '计算机科学', 'PHD', '理学院（计算机科学系）', 5, '全免（仅注册费300-400欧元/学期）', '• 博士需持有机器学习专利'),
(310, 61, '药学与药理学', 'BACHELOR', '药学院', 3, '全免（仅注册费300-400欧元/学期）', '• 本科需高中化学/生物成绩≥85%且通过APS审核'),
(311, 61, '药学与药理学', 'MASTER', '药学院', 2, '100000丹麦克朗/年', '• 硕士需GRE 320+或课程匹配度≥80%'),
(312, 61, '药学与药理学', 'PHD', '药学院', 5, '全免', '• 博士需发表2篇SCI论文（IF≥3.0）'),
(313, 61, '生物科学', 'BACHELOR', '理学院', 3, '全免（仅注册费300-400欧元/学期）', '• 本科需AP生物4分+化学实验报告'),
(314, 61, '生物科学', 'MASTER', '理学院', 2, '98000丹麦克朗/年', '• 硕士需提交研究计划书（含分子生物学方向）'),
(315, 61, '生物科学', 'PHD', '理学院', 5, '全免', '• 博士需发表3篇SCI论文（IF≥4.0）'),
(316, 65, '物理学（材料科学）', 'MASTER', '材料科学-物理化学系', 2, '全免（仅注册费）', '• 硕士需物理/化学均分≥80%且法语B2'),
(317, 65, '物理学（材料科学）', 'PHD', '材料科学-物理化学系', 5, '全免（仅注册费）', '• 博士需持有纳米材料相关专利'),
(318, 65, '化学与分子生物学', 'BACHELOR', '化学学院', 3, '全免（仅注册费）', '• 本科需AP化学4分+实验报告'),
(319, 65, '化学与分子生物学', 'MASTER', '化学学院', 2, '全免（仅注册费）', '• 硕士需求描述缺失'),
(320, 65, '化学与分子生物学', 'PHD', '化学学院', 5, '全免（仅注册费）', '• 博士需掌握CRISPR-Cas9技术'),
(321, 65, '生命科学（生物学）', 'BACHELOR', '生命与地球科学系', 3, '全免（仅注册费）', '• 本科需生物/化学双学科背景'),
(322, 65, '生命科学（生物学）', 'MASTER', '生命与地球科学系', 2, '全免（仅注册费）', '• 硕士需求描述缺失'),
(323, 65, '生命科学（生物学）', 'PHD', '生命与地球科学系', 5, '全免（仅注册费）', '• 博士需发表3篇SCI论文（IF≥4.0）'),
(324, 66, '公共管理与政治学', 'BACHELOR', '社会科学学院', 4, '全免（仅注册费400-500加元/学期）', '• 本科需高中政治/经济成绩≥85%'),
(325, 66, '公共管理与政治学', 'MASTER', '社会科学学院', 2, '9,800加元/年', '• 硕士需提交政策分析报告（含3个加拿大本土案例）'),
(326, 66, '公共管理与政治学', 'PHD', '社会科学学院', 5, '全免', '• 博士需发表2篇SSCI论文'),
(327, 66, '法学', 'BACHELOR', '法学院', 3, '全免', '• 本科需法学预科课程证书'),
(328, 66, '法学', 'MASTER', '法学院', 2, '12,500加元/年', '• 硕士需法律职业资格考试成绩≥75%'),
(329, 66, '法学', 'PHD', '法学院', 4, '免学费', '• 博士需持有2项法律实务案例'),
(330, 66, '医学与护理学', 'BACHELOR', '医学院', 4, '全免', '• 本科需生物/化学双A-Level成绩'),
(331, 66, '医学与护理学', 'MASTER', '医学院', 2, '14,200加元/年', '• 硕士需持有注册护士执照'),
(332, 66, '医学与护理学', 'PHD', '医学院', 5, '免学费', '• 博士需发表3篇SCI论文（IF≥3.5）'),
(333, 67, '地球与海洋科学', 'BACHELOR', '地球科学系', 3, '全免（仅注册费660瑞士法郎/学期）[4,5](@ref)', '• 本科需地理/物理A-Level成绩≥90%'),
(334, 67, '地球与海洋科学', 'MASTER', '地球科学系', 2, '全免（仅注册费660瑞士法郎/学期）[4,5](@ref)', '• 硕士需提交极地生态系统研究报告'),
(335, 67, '地球与海洋科学', 'PHD', '地球科学系', 5, '全免（仅注册费660瑞士法郎/学期）[4,5](@ref)', '• 博士需发表3篇SCI论文（IF≥4.0）[1,3](@ref)'),
(336, 67, '建筑学', 'MASTER', '建筑系', 2, '全免（仅注册费）', '• 硕士需提交3个国际竞赛设计方案'),
(337, 67, '建筑学', 'PHD', '建筑系', 4, '全免（仅注册费）', '• 博士需持有建筑专利或获奖作品[7](@ref)'),
(338, 67, '电子电气工程', 'BACHELOR', '信息科技与电气工程系', 3, '全免（仅注册费）', '• 本科需数学竞赛金牌或物理奥赛证书'),
(339, 67, '电子电气工程', 'MASTER', '信息科技与电气工程系', 2, '全免（仅注册费）', '• 硕士需GRE数学≥168分'),
(340, 67, '电子电气工程', 'PHD', '信息科技与电气工程系', 5, '全免（仅注册费）', '• 博士需参与过5G通信研发项目[9](@ref)'),
(341, 67, '计算机科学', 'MASTER', '计算机科学与信息技术学院', 2, '730瑞郎/学期', '• 硕士需算法设计竞赛奖项'),
(342, 67, '计算机科学', 'PHD', '计算机科学与信息技术学院', 5, '全免（非欧盟硕士730瑞郎/学期）[4,5](@ref)', '• 博士需发表2篇顶会论文（如NeurIPS/ICML）[10](@ref)'),
(343, 67, '机械工程', 'BACHELOR', '机械与过程工程系', 3, '全免（仅注册费）', '• 本科需机械制图作品集'),
(344, 67, '机械工程', 'MASTER', '机械与过程工程系', 2, '全免（仅注册费）', '• 硕士需提交微纳米工程研究计划'),
(345, 67, '机械工程', 'PHD', '机械与过程工程系', 4, '全免（仅注册费）', '• 博士需持有3项发明专利[10](@ref)'),
(346, 68, '政治学与国际研究', 'BACHELOR', '社会科学学院', 3, '53,110澳元/年', '• 本科需高考成绩75%-79%（各省份具体分数见网页1）'),
(347, 68, '政治学与国际研究', 'MASTER', '社会科学学院', 2, '56,120澳元/年', '• 硕士需提交3个政策案例分析'),
(348, 68, '政治学与国际研究', 'PHD', '社会科学学院', 5, '全免', '• 博士需发表2篇SSCI论文'),
(349, 68, '考古学', 'BACHELOR', '地球科学学院', 3, '56,120澳元/年', '• 本科需地理/历史成绩≥85%'),
(350, 68, '考古学', 'MASTER', '地球科学学院', 2, '56,120澳元/年', '• 硕士需提交田野考古报告'),
(351, 68, '考古学', 'PHD', '地球科学学院', 4, '免学费', '• 博士需发表3篇SCI论文（IF≥3.0）'),
(352, 68, '精算学', 'BACHELOR', '商业与经济学院', 3, '43,680澳元/年', '• 本科需高考数学全省前0.5%'),
(353, 68, '精算学', 'MASTER', '商业与经济学院', 2, '44,032澳元/年', '• 硕士需GRE数学≥168分'),
(354, 68, '精算学', 'PHD', '商业与经济学院', 5, '免学费', '• 博士需持有精算模型专利'),
(355, 69, '材料科学', 'BACHELOR', '材料科学研究中心', 4, '38000人民币/年', '• 本科需物理/化学高考成绩≥90%'),
(356, 69, '材料科学', 'MASTER', '材料科学研究中心', 2, '42000人民币/年', '• 硕士需提交量子点材料研究报告'),
(357, 69, '材料科学', 'PHD', '材料科学研究中心', 5, '全免', '• 博士需发表3篇SCI论文（IF≥5.0）'),
(358, 69, '计算机科学与技术', 'MASTER', '信息科技学院', 2, '730万韩元/年', '• 硕士需算法设计竞赛金奖'),
(359, 69, '计算机科学与技术', 'PHD', '信息科技学院', 5, '免学费', '• 博士需发表2篇顶会论文（NeurIPS/ICML）'),
(360, 69, '法学', 'MASTER', '法学院', 2, '45000人民币/年', '• 硕士需提交3个国际法案例分析'),
(361, 69, '法学', 'PHD', '法学院', 4, '免学费', '• 博士需持有法律专利或获奖论文'),
(362, 69, '经济学', 'BACHELOR', '商业与经济学院', 4, '35000人民币/年', '• 本科需高考数学全省前1%'),
(363, 69, '经济学', 'MASTER', '商业与经济学院', 2, '40000人民币/年', '• 硕士需GRE数学≥165分'),
(364, 69, '经济学', 'PHD', '商业与经济学院', 5, '免学费', '• 博士需参与过国家经济政策制定')
ON DUPLICATE KEY UPDATE
    school_id=VALUES(school_id), name=VALUES(name), degree_type=VALUES(degree_type), department=VALUES(department),
    duration_years=VALUES(duration_years), tuition_fee=VALUES(tuition_fee), description=VALUES(description);

-- 插入示例文书要求
INSERT INTO essay_requirements (id, school_id, program_id, essay_type, title, prompt, word_limit, is_required) VALUES
(1, 50, 262, '个人陈述', 'Statement of Purpose', 'Please describe your motivation for pursuing a Bachelor''s degree in Computer Science and your career goals.', 500, TRUE),
(2, 50, 263, '个人陈述', 'Statement of Purpose', 'Describe your research interests and how they align with our Computer Science program.', 600, TRUE),
(3, 50, 264, '研究陈述', 'Research Statement', 'Outline your research interests and how they align with our research strengths.', 1000, TRUE),
(4, 66, 327, '个人陈述', 'Personal Statement', 'Explain why you want to study law and how it fits with your career aspirations.', 750, TRUE),
(5, 66, 328, '学术兴趣', 'Statement of Academic Interest', 'Describe your interest in law and relevant experience.', 500, TRUE)
ON DUPLICATE KEY UPDATE 
    school_id=VALUES(school_id), program_id=VALUES(program_id), essay_type=VALUES(essay_type), 
    title=VALUES(title), prompt=VALUES(prompt), word_limit=VALUES(word_limit), is_required=VALUES(is_required);

UPDATE schools SET image_url = 'https://www.logoids.com/upload/image/202007/15948826907117430.jpg' WHERE id = 1;
UPDATE schools SET image_url = 'https://lximg.eiceducation.com.cn/img/b9a699af7a9944e9b09dc78e9a4bb5c4' WHERE id = 2;
UPDATE schools SET image_url = 'https://img.ixintu.com/download/jpg/201912/21358f0f2afb5e38f5709c872c1cc531.jpg!con' WHERE id = 3;
UPDATE schools SET image_url = 'https://www.harvard.edu/wp-content/themes/core/assets/img/theme/branding-assets/footer-logo.svg' WHERE id = 4;
UPDATE schools SET image_url = 'https://www.cam.ac.uk/sites/all/themes/fresh/images/interface/cambridge_university2.svg' WHERE id = 5;
UPDATE schools SET image_url = 'https://vipyidiancom.oss-cn-beijing.aliyuncs.com/vipyidian.com/article/15948839313456098.jpg' WHERE id = 6;
UPDATE schools SET image_url = 'https://nus.edu.sg/images/default-source/base/logo.png' WHERE id = 7;
UPDATE schools SET image_url = 'https://cdn.ucl.ac.uk/indigo/images/ucl-logo.svg' WHERE id = 8;
UPDATE schools SET image_url = 'https://www.caltech.edu/static/core/img/caltech-new-logo.png' WHERE id = 9;
UPDATE schools SET image_url = 'https://p1.ssl.qhimg.com/t019dbc44918d931171.jpg' WHERE id = 10;
UPDATE schools SET image_url = 'https://www.berkeley.edu/wp-content/themes/berkeleygateway/img/logo-berkeley.svg?v=3' WHERE id = 11;
UPDATE schools SET image_url = 'https://www.unimelb.edu.au/__data/assets/image/0007/3901660/UoM_square-logo_cmyk_black.png' WHERE id = 12;
UPDATE schools SET image_url = 'https://www.ntu.edu.sg/ResourcePackages/NTU/assets/images/NTU_Logo.png' WHERE id = 13;
UPDATE schools SET image_url = 'https://brand.cornell.edu/assets/images/downloads/logos/bold_cornell_logo/bold_cornell_logo.svg' WHERE id = 14;
UPDATE schools SET image_url = 'https://www.hku.hk/assets/img/hku-logo.svg?t=1678891777' WHERE id = 15;
UPDATE schools SET image_url = 'https://www.sydney.edu.au/content/dam/corporate/ex/images/web-uplift/175-black.svg' WHERE id = 16;
UPDATE schools SET image_url = 'https://www.unsw.edu.au/content/dam/images/graphics/logos/unsw/unsw_0.png' WHERE id = 17;
UPDATE schools SET image_url = 'https://mc-1b49d921-43a2-4264-88fd-647979-cdn-endpoint.azureedge.net/-/jssmedia/project/uchicago-tenant/intranet/test-images/footerlogo.jpg?h=92&iar=0&w=403&rev=11451ae47c60411ab5ea940240e6b383&hash=5F4C623BD87D5F2A9F26E21530BB4694' WHERE id = 18;
UPDATE schools SET image_url = 'https://www.princeton.edu/themes/custom/hobbes/images/logo-white.svg' WHERE id = 19;
UPDATE schools SET image_url = 'https://www.yale.edu/sites/all/themes/yale_blue/images/logo-print.png' WHERE id = 20;
UPDATE schools SET image_url = 'https://www.utoronto.ca/themes/custom/bootstrap_uoft/logo.svg' WHERE id = 21;
UPDATE schools SET image_url = 'https://www.ed.ac.uk/themes/upstream/wpp_theme/images/logo.png' WHERE id = 22;
UPDATE schools SET image_url = 'https://upload.wikimedia.org/wikipedia/commons/c/c8/Logo_of_the_Technical_University_of_Munich.svg' WHERE id = 23;
UPDATE schools SET image_url = 'https://www.mcgill.ca/visual-identity/files/visual-identity/mcgill_logo_red_black.svg' WHERE id = 24;
UPDATE schools SET image_url = 'https://brand.jhu.edu/assets/uploads/sites/2/2020/08/university.logo_.horizontal.blue_.svg' WHERE id = 25;
UPDATE schools SET image_url = 'https://www.u-tokyo.ac.jp/content/400244322.svg' WHERE id = 26;
UPDATE schools SET image_url = 'https://www.columbia.edu/themes/columbia/images/cu-logo-hoz-tag.svg' WHERE id = 27;
UPDATE schools SET image_url = 'https://www.cuhk.edu.hk/english/images/cuhk_logo_2x.png?20221027' WHERE id = 28;
UPDATE schools SET image_url = 'https://brand.ubc.ca/files/2020/04/UBC-logo-2019-fullsig-blue72-rgb-web_rev.svg' WHERE id = 29;
UPDATE schools SET image_url = 'https://static.uq.net.au/v11/logos/corporate/uq-logo--reversed.svg' WHERE id = 30;
UPDATE schools SET image_url = 'https://www.ucla.edu/img/logo-ucla.svg' WHERE id = 31;
UPDATE schools SET image_url = 'https://www.nyu.edu/etc.clientlibs/nyu/clientlibs/clientlib-main/resources/images/nyu-logo.svg' WHERE id = 32;
UPDATE schools SET image_url = 'https://umich.edu/assets/images/UM-Logo-Block-M_Maize-Body-Blue.svg' WHERE id = 33;
UPDATE schools SET image_url = 'https://hkust.edu.hk/sites/default/files/2024-04/ust%20logo_c.svg' WHERE id = 34;
UPDATE schools SET image_url = 'https://common.northwestern.edu/v9/css/images/northwestern.svg' WHERE id = 35;
UPDATE schools SET image_url = 'https://www.lse.ac.uk/Content/images/lse-logo.svg' WHERE id = 36;
UPDATE schools SET image_url = 'https://www.kyoto-u.ac.jp/themes/custom/camphor/images/header-logo-horizontal.svg' WHERE id = 37;
UPDATE schools SET image_url = 'https://www.bristol.ac.uk/assets/responsive-web-project/2.6.0/images/logos/uob-logo.svg' WHERE id = 38;
UPDATE schools SET image_url = 'https://cms-cdn.lmu.de/assets/img/Logo_LMU.svg' WHERE id = 39;
UPDATE schools SET image_url = 'https://www.polyu.edu.hk/assets/img/main-logo-1x.png' WHERE id = 40;
UPDATE schools SET image_url = 'https://www.cmu.edu/brand/brand-assets/images/wordmarksmasterbrand-cmu-edu-900x200.svg' WHERE id = 41;
UPDATE schools SET image_url = 'https://um.edu.my/images/img-logo-UM.png' WHERE id = 42;
UPDATE schools SET image_url = 'https://styleguide.duke.edu/assets/img/logos/duke_wordmark_horizontal_RGB.svg' WHERE id = 43;
UPDATE schools SET image_url = 'https://www.cityu.edu.hk/themes/custom/cityu/logo.svg' WHERE id = 44;
UPDATE schools SET image_url = 'https://www.sorbonne-universite.fr/sites/default/files/media/2019-10/logo_4.svg' WHERE id = 45;
UPDATE schools SET image_url = 'https://www.utexas.edu/sites/all/themes/utexas_zen/logo.svg' WHERE id = 46;
UPDATE schools SET image_url = 'https://illinois.edu/assets/img/branding/illinois_primary_wordmark_blue.svg' WHERE id = 47;
UPDATE schools SET image_url = 'https://ucsd.edu/_resources/img/logo_UCSD.png' WHERE id = 48;
UPDATE schools SET image_url = 'https://www.washington.edu/brand/files/2020/08/UW-logo_mark_c160_stacked_purple_RGB.svg' WHERE id = 49;
UPDATE schools SET image_url = 'https://www.brown.edu/themes/custom/brown_base/logo.svg' WHERE id = 50;
UPDATE schools SET image_url = 'https://www.southampton.ac.uk/assets/site/images/uos-logo.svg' WHERE id = 51;
UPDATE schools SET image_url = 'https://www.birmingham.ac.uk/css/images/logo.svg' WHERE id = 52;
UPDATE schools SET image_url = 'https://www.titech.ac.jp/img/common/logo_titech.svg' WHERE id = 53;
UPDATE schools SET image_url = 'https://www.uts.edu.au/sites/default/files/UTS_logo_horizontal_black_RGB.svg' WHERE id = 54;
UPDATE schools SET image_url = 'https://www.durham.ac.uk/images/durham-logo.svg' WHERE id = 55;
UPDATE schools SET image_url = 'https://www.psu.edu/scripts/build/assets/psu-mark.9c2120c9.svg' WHERE id = 56;
UPDATE schools SET image_url = 'https://www5.usp.br/wp-content/themes/usp-em-dia/assets/src/images/usp-logo.png' WHERE id = 57;
UPDATE schools SET image_url = 'https://www.ualberta.ca/media-library/ualberta/uadodo/images/global-ualberta-logo.svg' WHERE id = 58;
UPDATE schools SET image_url = 'https://www.fu-berlin.de/assets_core_camaleon/images/fu_logo_svg/fu_logo_de_gruen_rgb.svg' WHERE id = 59;
UPDATE schools SET image_url = 'https://www.rwth-aachen.de/global/images/rwth_logo_standard_rwth_blau_rgb.svg' WHERE id = 60;
UPDATE schools SET image_url = 'https://www.ku.dk/images/top/ku_logo_en.svg' WHERE id = 61;
UPDATE schools SET image_url = 'https://www.sheffield.ac.uk/themes/custom/uos_theme/images/logo.svg' WHERE id = 62;
UPDATE schools SET image_url = 'https://brand.usc.edu/files/2020/07/PrimaryMonogram_Cardinal.svg' WHERE id = 63;
UPDATE schools SET image_url = 'https://www.waseda.jp/top/assets/img/common/logo-header.png' WHERE id = 64;
UPDATE schools SET image_url = 'https://www.ens-lyon.fr/themes/custom/ensl_theme/logo.svg' WHERE id = 65;
UPDATE schools SET image_url = 'https://www.uottawa.ca/themes/custom/uottawa/logo-en.svg' WHERE id = 66;
UPDATE schools SET image_url = 'https://tse2-mm.cn.bing.net/th/id/OIP-C.G9cj5Iu3fqCou2QNpoA3qwAAAA?rs=1&pid=ImgDetMain' WHERE id = 67;
UPDATE schools SET image_url = 'https://www.anu.edu.au/themes/anu_subtheme/logo.svg' WHERE id = 68;
UPDATE schools SET image_url = 'https://en.snu.ac.kr/web/images/common/logo-snu-main.png' WHERE id = 69;
UPDATE schools SET image_url = 'https://www.manchester.ac.uk/images/uom-logo.svg' WHERE id = 70;
UPDATE schools SET image_url = 'https://www.monash.edu/__data/assets/git_bridge/0006/3093459/Monash-Logo-SVG-Hor-Blue-Text-for-LightBG.svg' WHERE id = 71;
UPDATE schools SET image_url = 'https://www.kcl.ac.uk/newstyle/assets/image/kings-logo.svg' WHERE id = 72;
UPDATE schools SET image_url = 'https://www.ip-paris.fr/wp-content/themes/ip-paris/assets/images/logo-ip-paris.svg' WHERE id = 73; 
COMMIT;