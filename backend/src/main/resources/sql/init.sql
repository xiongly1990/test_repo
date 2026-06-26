-- =============================================
-- 亲子学习打卡系统 - 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 创建日期: 2026-06-25
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS parent_kid_checkin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE parent_kid_checkin;

-- =============================================
-- 1. 管理员表
-- =============================================
DROP TABLE IF EXISTS sys_admin;
CREATE TABLE sys_admin (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- =============================================
-- 2. 孩子表
-- =============================================
DROP TABLE IF EXISTS child;
CREATE TABLE child (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    birthday DATE DEFAULT NULL COMMENT '生日',
    grade VARCHAR(50) DEFAULT NULL COMMENT '年级/班级',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    points INT DEFAULT 0 COMMENT '当前积分',
    streak_days INT DEFAULT 0 COMMENT '连续打卡天数',
    total_days INT DEFAULT 0 COMMENT '累计打卡天数',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除 0否 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='孩子表';

-- =============================================
-- 3. 任务表
-- =============================================
DROP TABLE IF EXISTS task;
CREATE TABLE task (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    child_id BIGINT NOT NULL COMMENT '孩子ID',
    name VARCHAR(100) NOT NULL COMMENT '任务名称',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标',
    cycle_type TINYINT DEFAULT 1 COMMENT '周期类型：1每日 2工作日 3周末',
    start_time TIME DEFAULT NULL COMMENT '开始时间',
    end_time TIME DEFAULT NULL COMMENT '结束时间',
    need_photo TINYINT DEFAULT 1 COMMENT '是否需要拍照：0否 1是',
    points INT DEFAULT 10 COMMENT '奖励积分',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0停用 1启用',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除 0否 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_child_id (child_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- =============================================
-- 4. 打卡记录表
-- =============================================
DROP TABLE IF EXISTS checkin_record;
CREATE TABLE checkin_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    child_id BIGINT NOT NULL COMMENT '孩子ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称快照',
    points INT DEFAULT 0 COMMENT '获得积分',
    photo_url VARCHAR(500) DEFAULT NULL COMMENT '打卡照片URL',
    status TINYINT DEFAULT 0 COMMENT '状态：0待审核 1通过 2不通过',
    checkin_date DATE NOT NULL COMMENT '打卡日期',
    checkin_time DATETIME NOT NULL COMMENT '打卡时间',
    is_supplement TINYINT DEFAULT 0 COMMENT '是否补卡：0否 1是',
    supplement_reason VARCHAR(255) DEFAULT NULL COMMENT '补卡原因',
    auditor_id BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    audit_remark VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_child_date (child_id, checkin_date),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- =============================================
-- 5. 奖励分类表
-- =============================================
DROP TABLE IF EXISTS reward_category;
CREATE TABLE reward_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖励分类表';

-- =============================================
-- 6. 奖励表
-- =============================================
DROP TABLE IF EXISTS reward;
CREATE TABLE reward (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '奖励名称',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标',
    points INT NOT NULL COMMENT '所需积分',
    stock INT DEFAULT -1 COMMENT '库存，-1为无限',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0下架 1上架',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除 0否 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖励表';

-- =============================================
-- 7. 兑换记录表
-- =============================================
DROP TABLE IF EXISTS exchange_record;
CREATE TABLE exchange_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    child_id BIGINT NOT NULL COMMENT '孩子ID',
    reward_id BIGINT NOT NULL COMMENT '奖励ID',
    reward_name VARCHAR(100) NOT NULL COMMENT '奖励名称快照',
    points INT NOT NULL COMMENT '消耗积分',
    status TINYINT DEFAULT 0 COMMENT '状态：0待发放 1已发放 2已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deliver_time DATETIME DEFAULT NULL COMMENT '发放时间',
    PRIMARY KEY (id),
    KEY idx_child_id (child_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换记录表';

-- =============================================
-- 8. 成就节点表
-- =============================================
DROP TABLE IF EXISTS achievement;
CREATE TABLE achievement (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '成就名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标',
    type TINYINT DEFAULT 1 COMMENT '类型：1连续打卡天数',
    target_value INT DEFAULT 0 COMMENT '目标值',
    points INT DEFAULT 0 COMMENT '奖励积分',
    level INT DEFAULT 1 COMMENT '层级（任务树层级）',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就节点表';

-- =============================================
-- 9. 孩子成就表
-- =============================================
DROP TABLE IF EXISTS child_achievement;
CREATE TABLE child_achievement (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    child_id BIGINT NOT NULL COMMENT '孩子ID',
    achievement_id BIGINT NOT NULL COMMENT '成就ID',
    unlock_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '解锁时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_child_achievement (child_id, achievement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='孩子成就表';

-- =============================================
-- 初始化数据
-- =============================================

-- 初始管理员账号: admin / 123456
-- BCrypt加密后的密码: $2b$10$cHqwWIpvv/33Tr7yH0HMH./SfM2TVm4striTEF9VpB4Kho5y.FyEq (由bcryptjs生成)
INSERT INTO sys_admin (username, password, nickname) VALUES
('admin', '$2b$10$cHqwWIpvv/33Tr7yH0HMH./SfM2TVm4striTEF9VpB4Kho5y.FyEq', '超级管理员');

-- 初始奖励分类
INSERT INTO reward_category (name, icon, sort) VALUES
('美食', 'ice_cream', 1),
('特权', 'game_pad', 2),
('物品', 'book', 3),
('成就徽章', 'trophy', 4);

-- 初始成就节点
INSERT INTO achievement (name, description, icon, type, target_value, points, level, sort) VALUES
('初次打卡', '完成第一次打卡', 'star', 1, 1, 1, 1),
('坚持一周', '连续打卡7天', 'star', 1, 7, 50, 2, 2),
('半月达人', '连续打卡15天', 'star', 1, 15, 100, 3, 3),
('月度之星', '连续打卡30天', 'star', 1, 30, 200, 4, 4),
('百日辉煌', '连续打卡100天', 'trophy', 1, 100, 500, 5, 5),
('年度冠军', '连续打卡365天', 'trophy', 1, 365, 1000, 6, 6);

-- 初始示例孩子（可选）
INSERT INTO child (name, birthday, grade, points, streak_days, total_days) VALUES
('小明', '2018-05-20', '小学二年级', 850, 7, 45),
('小红', '2020-03-15', '幼儿园大班', 320, 0, 20);

-- 初始示例任务（可选）
INSERT INTO task (child_id, name, icon, cycle_type, need_photo, points, sort) VALUES
(1, '阅读绘本', 'book', 1, 1, 10, 1),
(1, '写字练习', 'pencil', 2, 1, 15, 2),
(1, '运动20分钟', 'run', 1, 0, 20, 3),
(2, '阅读绘本', 'book', 1, 1, 10, 1),
(2, '运动20分钟', 'run', 1, 0, 20, 2);

-- 初始示例奖励
INSERT INTO reward (category_id, name, icon, points, stock, description, sort) VALUES
(1, '冰淇淋', 'ice_cream', 100, -1, '奖励冰淇淋一个', 1),
(1, '麦当劳', 'hamburger', 500, 10, '麦当劳套餐一份', 2),
(1, '必胜客', 'pizza', 800, -1, '必胜客披萨一份', 3),
(1, '蛋糕', 'cake', 300, -1, '小蛋糕一个', 4),
(2, '游戏时间30分钟', 'game_pad', 200, -1, '增加30分钟游戏时间', 1),
(2, '电影之夜', 'movie', 500, -1, '周末看一场电影', 2),
(3, '新绘本', 'book', 300, -1, '一本新的绘本', 1),
(3, '毛绒玩具', 'teddy_bear', 500, 5, '毛绒玩具一个', 2);
