-- 创建数据库
CREATE DATABASE IF NOT EXISTS weakcurrent_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE weakcurrent_db;

-- Ensure MySQL client decodes this UTF-8 seed file correctly during docker init.
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 配件分类表
CREATE TABLE IF NOT EXISTS accessory_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID，0表示顶级',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '启用状态：1启用 0停用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配件分类表';

-- 配件表
CREATE TABLE IF NOT EXISTS accessory (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '配件名称',
    model VARCHAR(100) COMMENT '型号',
    spec VARCHAR(100) COMMENT '规格',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    category_name VARCHAR(100) COMMENT '分类名称',
    stock_quantity INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    warehouse_zone VARCHAR(50) COMMENT '库区',
    unit VARCHAR(20) COMMENT '单位',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_category_id (category_id),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配件表';

-- 领用出库表
CREATE TABLE IF NOT EXISTS stock_out (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    accessory_id BIGINT NOT NULL COMMENT '配件ID',
    accessory_name VARCHAR(100) COMMENT '配件名称',
    workshop VARCHAR(100) COMMENT '领用车间',
    quantity INT NOT NULL COMMENT '出库数量',
    operator VARCHAR(50) COMMENT '操作人',
    out_time DATETIME NOT NULL COMMENT '出库时间',
    remark VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (id),
    INDEX idx_accessory_id (accessory_id),
    INDEX idx_out_time (out_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='领用出库表';

-- 库房清点表
CREATE TABLE IF NOT EXISTS inventory_check (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    accessory_id BIGINT NOT NULL COMMENT '配件ID',
    accessory_name VARCHAR(100) COMMENT '配件名称',
    physical_quantity INT NOT NULL COMMENT '实际数量',
    system_quantity INT NOT NULL COMMENT '系统数量',
    difference INT NOT NULL COMMENT '差异数量',
    check_person VARCHAR(50) COMMENT '清点人',
    check_time DATETIME NOT NULL COMMENT '清点时间',
    remark VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (id),
    INDEX idx_accessory_id (accessory_id),
    INDEX idx_check_time (check_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库房清点表';

-- 报废归档表
CREATE TABLE IF NOT EXISTS scrap_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    accessory_id BIGINT NOT NULL COMMENT '配件ID',
    accessory_name VARCHAR(100) COMMENT '配件名称',
    quantity INT NOT NULL COMMENT '报废数量',
    reason VARCHAR(500) COMMENT '报废原因',
    operator VARCHAR(50) COMMENT '操作人',
    scrap_time DATETIME NOT NULL COMMENT '报废时间',
    remark VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (id),
    INDEX idx_accessory_id (accessory_id),
    INDEX idx_scrap_time (scrap_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报废归档表';

-- 初始化分类数据
INSERT IGNORE INTO accessory_category (id, name, parent_id, sort) VALUES
(1, '接线端子', 0, 1),
(2, '线槽', 0, 2),
(3, '固定卡扣', 0, 3),
-- 接线端子子分类
(4, '螺钉式接线端子', 1, 1),
(5, '弹簧式接线端子', 1, 2),
(6, '插拔式接线端子', 1, 3),
-- 线槽子分类
(7, 'PVC方形线槽', 2, 1),
(8, '金属桥架线槽', 2, 2),
(9, '圆形波纹管', 2, 3),
-- 固定卡扣子分类
(10, '线卡卡扣', 3, 1),
(11, '扎带卡扣', 3, 2),
(12, '导轨卡扣', 3, 3);

-- 初始化示例配件数据
INSERT INTO accessory (name, model, spec, category_id, category_name, stock_quantity, warehouse_zone, unit) VALUES
('UK接线端子', 'UK2.5B', '2.5mm²', 4, '螺钉式接线端子', 500, 'A区-01', '个'),
('菲尼克斯接线端子', 'ST2.5', '2.5mm²', 5, '弹簧式接线端子', 300, 'A区-02', '个'),
('插拔端子排', 'PCB-5P', '5Pin', 6, '插拔式接线端子', 200, 'A区-03', '条'),
('PVC线槽', 'PVC-2515', '25*15mm', 7, 'PVC方形线槽', 150, 'B区-01', '米'),
('镀锌桥架', 'QJ-10050', '100*50mm', 8, '金属桥架线槽', 80, 'B区-02', '米'),
('塑料波纹管', 'PP-20', 'Φ20mm', 9, '圆形波纹管', 500, 'B区-03', '米'),
('钢钉线卡', 'SK-8', 'Φ8mm', 10, '线卡卡扣', 1000, 'C区-01', '包'),
('自锁式扎带', 'NT-3*150', '3*150mm', 11, '扎带卡扣', 2000, 'C区-02', '包'),
('C45导轨卡扣', 'DK-1', '35mm导轨', 12, '导轨卡扣', 800, 'C区-03', '个');
