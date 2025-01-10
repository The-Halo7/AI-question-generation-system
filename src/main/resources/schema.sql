-- 创建题目集合表（如果不存在）
CREATE TABLE IF NOT EXISTS question_set (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL COMMENT '标题',
    description TEXT COMMENT '文档描述',
    question_count INT NOT NULL COMMENT '题目数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目集合表';

-- 创建题目表（如果不存在）
CREATE TABLE IF NOT EXISTS question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    set_id BIGINT NOT NULL COMMENT '关联的题目集合ID',
    question_number INT NOT NULL COMMENT '题号',
    type VARCHAR(50) NOT NULL COMMENT '题目类型：选择题/判断题/简答题',
    content TEXT NOT NULL COMMENT '题目内容',
    answer TEXT NOT NULL COMMENT '答案',
    analysis TEXT COMMENT '解析',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (set_id) REFERENCES question_set(id) ON DELETE CASCADE,
    INDEX idx_set_id (set_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表'; 