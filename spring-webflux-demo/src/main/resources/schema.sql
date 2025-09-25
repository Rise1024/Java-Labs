-- Spring WebFlux Demo 数据库表结构




-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY COMMENT '用户ID',
    name VARCHAR(100) NOT NULL COMMENT '用户名',
    email VARCHAR(200) NOT NULL UNIQUE COMMENT '邮箱',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    account_type VARCHAR(20) DEFAULT 'standard' COMMENT '账户类型',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否激活'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户活动记录表
CREATE TABLE IF NOT EXISTS user_activities (
    id VARCHAR(36) PRIMARY KEY COMMENT '活动ID',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    action VARCHAR(50) NOT NULL COMMENT '动作类型',
    description VARCHAR(500) COMMENT '描述',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '时间戳',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户活动记录表';

SET FOREIGN_KEY_CHECKS = 1;

-- 创建索引优化查询性能
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_account_type ON users(account_type);
CREATE INDEX idx_users_create_time ON users(create_time);
CREATE INDEX idx_users_is_active ON users(is_active);

CREATE INDEX idx_user_activities_user_id ON user_activities(user_id);
CREATE INDEX idx_user_activities_action ON user_activities(action);
CREATE INDEX idx_user_activities_timestamp ON user_activities(timestamp);
CREATE INDEX idx_user_activities_user_action ON user_activities(user_id, action);
