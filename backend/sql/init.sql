-- 创建数据库
CREATE DATABASE IF NOT EXISTS weakcurrent_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE weakcurrent_db;

-- Ensure MySQL client decodes this UTF-8 seed file correctly during docker init.
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 配件分类表
CREATE TABLE IF NOT EXISTS accessory_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    code VARCHAR(32) COMMENT '分类编码',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID，0表示顶级',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '启用状态：1启用 0停用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_parent_id (parent_id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配件分类表';

-- 配件表
CREATE TABLE IF NOT EXISTS accessory (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '配件名称',
    model VARCHAR(100) COMMENT '型号',
    spec VARCHAR(100) COMMENT '规格',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    category_name VARCHAR(100) COMMENT '分类名称',
    category_path VARCHAR(500) COMMENT '分类完整路径，如接线端子/弹簧式接线端子',
    stock_quantity INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    warehouse_zone VARCHAR(50) COMMENT '库区',
    unit VARCHAR(20) COMMENT '单位',
    square_number VARCHAR(20) COMMENT '平方数（接线端子）',
    pin_count INT COMMENT '针脚数（接线端子）',
    width VARCHAR(20) COMMENT '宽度（线槽）',
    height VARCHAR(20) COMMENT '高度（线槽）',
    diameter VARCHAR(20) COMMENT '直径（固定卡扣）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_category_id (category_id),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配件表';

-- 车间领用用途字典表
CREATE TABLE IF NOT EXISTS workshop_usage (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '用途名称',
    code VARCHAR(32) COMMENT '用途编码',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '启用状态：1启用 0停用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车间领用用途字典表';

-- 领用出库表
CREATE TABLE IF NOT EXISTS stock_out (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    accessory_id BIGINT NOT NULL COMMENT '配件ID',
    accessory_name VARCHAR(100) COMMENT '配件名称',
    workshop VARCHAR(100) COMMENT '领用车间',
    usage_id BIGINT NOT NULL COMMENT '领用用途ID',
    usage_name VARCHAR(100) COMMENT '领用用途名称',
    quantity INT NOT NULL COMMENT '出库数量',
    operator VARCHAR(50) COMMENT '操作人',
    out_time DATETIME NOT NULL COMMENT '出库时间',
    remark VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (id),
    INDEX idx_accessory_id (accessory_id),
    INDEX idx_usage_id (usage_id),
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
    accessory_model VARCHAR(100) COMMENT '配件型号',
    unit VARCHAR(20) COMMENT '单位',
    quantity INT NOT NULL COMMENT '报废数量',
    reason VARCHAR(500) COMMENT '报废原因',
    operator VARCHAR(50) COMMENT '操作人',
    scrap_time DATETIME NOT NULL COMMENT '报废时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    remark VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (id),
    INDEX idx_accessory_id (accessory_id),
    INDEX idx_scrap_time (scrap_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报废归档表';

-- 老化批次归档表
CREATE TABLE IF NOT EXISTS aging_batch (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    batch_no VARCHAR(50) NOT NULL COMMENT '批次号',
    category_id BIGINT COMMENT '配件类别ID',
    category_name VARCHAR(100) COMMENT '配件类别名称',
    warehouse_zone VARCHAR(50) COMMENT '库区',
    inbound_start DATETIME COMMENT '入库时间范围-起始',
    inbound_end DATETIME COMMENT '入库时间范围-截止',
    total_quantity INT NOT NULL DEFAULT 0 COMMENT '批次总数量',
    item_count INT NOT NULL DEFAULT 0 COMMENT '批次配件项数',
    reason VARCHAR(500) COMMENT '报废原因',
    operator VARCHAR(50) COMMENT '归档人',
    archive_time DATETIME NOT NULL COMMENT '归档时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    remark VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_batch_no (batch_no),
    INDEX idx_category_id (category_id),
    INDEX idx_warehouse_zone (warehouse_zone),
    INDEX idx_archive_time (archive_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老化批次归档表';

-- 老化批次归档明细表
CREATE TABLE IF NOT EXISTS aging_batch_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    batch_id BIGINT NOT NULL COMMENT '批次ID',
    accessory_id BIGINT NOT NULL COMMENT '配件ID',
    accessory_name VARCHAR(100) COMMENT '配件名称',
    accessory_model VARCHAR(100) COMMENT '配件型号',
    unit VARCHAR(20) COMMENT '单位',
    warehouse_zone VARCHAR(50) COMMENT '库区',
    category_id BIGINT COMMENT '配件类别ID',
    category_name VARCHAR(100) COMMENT '配件类别名称',
    stock_quantity INT NOT NULL COMMENT '归档时库存数量',
    scrap_quantity INT NOT NULL COMMENT '报废数量',
    inbound_time DATETIME COMMENT '入库时间',
    reason VARCHAR(500) COMMENT '报废原因',
    remark VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (id),
    INDEX idx_batch_id (batch_id),
    INDEX idx_accessory_id (accessory_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老化批次归档明细表';

-- 初始化分类数据
INSERT IGNORE INTO accessory_category (id, name, code, parent_id, sort, remark) VALUES
(1, '接线端子', 'TERMINAL', 0, 1, '各类接线端子产品'),
(2, '线槽', 'DUCT', 0, 2, '各类线槽产品'),
(3, '固定卡扣', 'CLIP', 0, 3, '各类固定卡扣产品'),
-- 接线端子子分类
(4, '螺钉式接线端子', 'TERMINAL-SCREW', 1, 1, '螺钉连接式接线端子'),
(5, '弹簧式接线端子', 'TERMINAL-SPRING', 1, 2, '弹簧连接式接线端子'),
(6, '插拔式接线端子', 'TERMINAL-PLUG', 1, 3, '插拔式接线端子'),
-- 线槽子分类
(7, 'PVC方形线槽', 'DUCT-PVC', 2, 1, 'PVC材质方形线槽'),
(8, '金属桥架线槽', 'DUCT-METAL', 2, 2, '金属材质桥架线槽'),
(9, '圆形波纹管', 'DUCT-CORRUGATED', 2, 3, '圆形塑料波纹管'),
-- 固定卡扣子分类
(10, '线卡卡扣', 'CLIP-WIRE', 3, 1, '钢钉线卡类卡扣'),
(11, '扎带卡扣', 'CLIP-TIE', 3, 2, '尼龙扎带类卡扣'),
(12, '导轨卡扣', 'CLIP-RAIL', 3, 3, 'C45导轨安装卡扣');

-- 初始化示例配件数据
INSERT INTO accessory (name, model, spec, category_id, category_name, category_path, stock_quantity, warehouse_zone, unit, square_number, pin_count, width, height, diameter) VALUES
('UK接线端子', 'UK2.5B', '2.5mm²', 4, '螺钉式接线端子', '接线端子/螺钉式接线端子', 500, 'A区-01', '个', '2.5mm²', NULL, NULL, NULL, NULL),
('菲尼克斯接线端子', 'ST2.5', '2.5mm²', 5, '弹簧式接线端子', '接线端子/弹簧式接线端子', 300, 'A区-02', '个', '2.5mm²', NULL, NULL, NULL, NULL),
('插拔端子排', 'PCB-5P', '5Pin', 6, '插拔式接线端子', '接线端子/插拔式接线端子', 200, 'A区-03', '条', NULL, 5, NULL, NULL, NULL),
('PVC线槽', 'PVC-2515', '25*15mm', 7, 'PVC方形线槽', '线槽/PVC方形线槽', 150, 'B区-01', '米', NULL, NULL, '25mm', '15mm', NULL),
('镀锌桥架', 'QJ-10050', '100*50mm', 8, '金属桥架线槽', '线槽/金属桥架线槽', 80, 'B区-02', '米', NULL, NULL, '100mm', '50mm', NULL),
('塑料波纹管', 'PP-20', 'Φ20mm', 9, '圆形波纹管', '线槽/圆形波纹管', 500, 'B区-03', '米', NULL, NULL, NULL, NULL, '20mm'),
('钢钉线卡', 'SK-8', 'Φ8mm', 10, '线卡卡扣', '固定卡扣/线卡卡扣', 1000, 'C区-01', '包', NULL, NULL, NULL, NULL, '8mm'),
('自锁式扎带', 'NT-3*150', '3*150mm', 11, '扎带卡扣', '固定卡扣/扎带卡扣', 2000, 'C区-02', '包', NULL, NULL, NULL, NULL, '3mm'),
('C45导轨卡扣', 'DK-1', '35mm导轨', 12, '导轨卡扣', '固定卡扣/导轨卡扣', 800, 'C区-03', '个', NULL, NULL, '35mm', NULL, NULL);

-- 初始化车间领用用途字典数据
INSERT IGNORE INTO workshop_usage (id, name, code, sort, remark) VALUES
(1, '设备改线', 'EQUIPMENT_REWIRING', 1, '车间设备接线改造、重新布线等场景'),
(2, '网络维护', 'NETWORK_MAINTENANCE', 2, '网络线路维护、检修、故障排查等场景'),
(3, '传感器更换', 'SENSOR_REPLACEMENT', 3, '各类传感器更换、安装、调试场景'),
(4, '临时布线', 'TEMPORARY_WIRING', 4, '临时布线、临时接线等场景'),
(5, '设备安装', 'EQUIPMENT_INSTALL', 5, '新设备安装、部署接线场景'),
(6, '日常维修', 'DAILY_REPAIR', 6, '日常设备维修、配件更换场景');
