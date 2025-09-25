-- Spring WebFlux Demo 初始化数据

-- 清空现有数据
DELETE FROM user_activities;
DELETE FROM users;

-- 插入示例用户数据
INSERT INTO users (id, name, email, avatar_url, account_type, create_time, update_time, is_active) VALUES
('u001', '张三', 'zhangsan@example.com', '/static/avatars/zhangsan.jpg', 'standard', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
('u002', '李四', 'lisi@example.com', '/static/avatars/lisi.jpg', 'premium', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
('u003', '王五', 'wangwu@example.com', NULL, 'standard', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
('u004', '赵六', 'zhaoliu@example.com', '/static/avatars/zhaoliu.jpg', 'premium', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('u005', '孙七', 'sunqi@example.com', NULL, 'standard', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

-- 插入用户活动记录
INSERT INTO user_activities (id, user_id, action, description, ip_address, user_agent, timestamp) VALUES
('a001', 'u001', 'LOGIN', '用户登录', '192.168.1.100', 'Chrome/Windows', CURRENT_TIMESTAMP),
('a002', 'u001', 'VIEW_PROFILE', '查看个人资料', '192.168.1.100', 'Chrome/Windows', CURRENT_TIMESTAMP),
('a003', 'u002', 'LOGIN', '用户登录', '192.168.1.101', 'Chrome/MacOS', CURRENT_TIMESTAMP),
('a004', 'u002', 'UPDATE_USER', '更新用户信息', '192.168.1.101', 'Chrome/MacOS', CURRENT_TIMESTAMP),
('a005', 'u003', 'LOGIN', '用户登录', '192.168.1.102', 'Chrome/Linux', CURRENT_TIMESTAMP),
('a006', 'u003', 'VIEW_PROFILE', '查看个人资料', '192.168.1.102', 'Chrome/Linux', CURRENT_TIMESTAMP),
('a007', 'u001', 'LOGOUT', '用户登出', '192.168.1.100', 'Chrome/Windows', CURRENT_TIMESTAMP),
('a008', 'u005', 'CREATE_USER', '用户注册', '192.168.1.105', 'Safari/iPhone', CURRENT_TIMESTAMP),
('a009', 'u005', 'LOGIN', '首次登录', '192.168.1.105', 'Safari/iPhone', CURRENT_TIMESTAMP),
('a010', 'u002', 'UPLOAD_AVATAR', '上传头像', '192.168.1.101', 'Chrome/MacOS', CURRENT_TIMESTAMP);
