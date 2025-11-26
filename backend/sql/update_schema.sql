-- 删除旧的数据库
DROP DATABASE IF EXISTS admitgenius_db;

-- 创建新的数据库
CREATE DATABASE admitgenius_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE admitgenius_db;

-- 创建用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    profile_picture VARCHAR(255),
    last_login_at TIMESTAMP NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 学生用户特有属性
    current_school VARCHAR(100),
    target_major VARCHAR(100),
    gpa DOUBLE,
    sat_score INT,
    act_score INT,
    toefl_score INT,
    ielts_score DOUBLE,
    gre_verbal INT,
    gre_quant INT,
    gre_writing DOUBLE,
    gre_combined INT,
    gmat_total INT,
    graduation_year INT,
    
    -- 专家用户特有属性
    institution VARCHAR(100),
    expertise_area VARCHAR(100),
    title VARCHAR(100),
    bio TEXT,
    years_of_experience INT,
    rating_avg DOUBLE,
    review_count INT,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMP NULL,
    
    -- 管理员特有属性
    admin_level INT,
    department VARCHAR(100),
    permissions TEXT
);

-- 创建学校表
CREATE TABLE schools (
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
    admission_requirements TEXT,
    top_programs JSON,
    research_focus JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_ivy_league BOOLEAN DEFAULT FALSE
);

-- 创建学校项目表
CREATE TABLE school_programs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    degree_type VARCHAR(20) NOT NULL,
    department VARCHAR(255),
    duration_years INT,
    tuition_fee DECIMAL(10,2),
    admission_requirements TEXT,
    description TEXT,
    website VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);

-- 创建学校热门项目表
CREATE TABLE school_top_programs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT NOT NULL,
    program_name VARCHAR(255) NOT NULL,
    ranking INT,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);

-- 创建项目关键词表
CREATE TABLE program_keywords (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    program_id BIGINT NOT NULL,
    keyword VARCHAR(100) NOT NULL,
    FOREIGN KEY (program_id) REFERENCES school_programs(id) ON DELETE CASCADE
);

-- 创建文书表
CREATE TABLE essays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    word_limit INT,
    essay_type VARCHAR(255),
    status VARCHAR(20) DEFAULT 'DRAFT',
    version INT DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建文书要求表
CREATE TABLE essay_requirements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT,
    program_id BIGINT,
    essay_type VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    prompt TEXT NOT NULL,
    word_limit INT,
    is_required BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    FOREIGN KEY (program_id) REFERENCES school_programs(id) ON DELETE CASCADE
);

-- 创建专家评审表
CREATE TABLE expert_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    essay_id BIGINT NOT NULL,
    expert_id BIGINT,
    review_content TEXT,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    suggestions TEXT,
    review_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (essay_id) REFERENCES essays(id) ON DELETE CASCADE,
    FOREIGN KEY (expert_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 创建文档表
CREATE TABLE documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    file_path VARCHAR(500),
    file_size BIGINT,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建论坛帖子表
CREATE TABLE forum_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建评论表
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建帖子点赞表
CREATE TABLE post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_post_user_like (user_id, post_id),
    FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建推荐表
CREATE TABLE recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    request_data JSON,
    total_recommendations INT DEFAULT 0,
    recommendation_type VARCHAR(20) DEFAULT 'SCHOOL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建推荐项表
CREATE TABLE recommendation_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recommendation_id BIGINT NOT NULL,
    school_id BIGINT,
    program_id BIGINT,
    match_score FLOAT,
    rank_order INT,
    match_reason TEXT,
    feedback TEXT,
    is_applied BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recommendation_id) REFERENCES recommendations(id) ON DELETE CASCADE,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    FOREIGN KEY (program_id) REFERENCES school_programs(id) ON DELETE SET NULL
);

-- 创建申请统计表
CREATE TABLE application_statistics (
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
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    FOREIGN KEY (program_id) REFERENCES school_programs(id) ON DELETE CASCADE
);

-- 创建用户教育经历表
CREATE TABLE user_education (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    school VARCHAR(255) NOT NULL,
    degree VARCHAR(50) NOT NULL,
    major VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    achievement TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建用户项目经历表
CREATE TABLE user_projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    content TEXT NOT NULL,
    is_leader BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 插入管理员用户
INSERT INTO users (email, password, full_name, role, status) VALUES 
('admin@zhinan.com', '$2a$10$rH4hD8iC5cxrCm.9YpMXvOq8LJe8AyaBF5nYDbGNM/UX/tKiL6LYy', '系统管理员', 'ADMIN', 'ACTIVE');

-- 插入示例学校数据
INSERT INTO schools (name, location, ranking, acceptance_rate, average_gpa, average_gre_verbal, average_gre_quantitative, average_gmat, description, website, has_scholarship, tuition_fee, is_ivy_league) VALUES
('斯坦福大学', '美国加利福尼亚州', 2, 0.042, 3.96, 165, 168, 732, '斯坦福大学是一所位于美国加利福尼亚州的私立研究型大学，以其在科技和创新领域的卓越成就而闻名。', 'https://www.stanford.edu', TRUE, 60000.00, FALSE),
('麻省理工学院', '美国马萨诸塞州', 1, 0.067, 3.97, 162, 167, 730, 'MIT是世界领先的科学技术研究和教育机构，在工程、计算机科学和商业领域享有盛誉。', 'https://www.mit.edu', TRUE, 58000.00, FALSE);

COMMIT; 