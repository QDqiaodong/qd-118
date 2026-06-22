USE weakcurrent_db;

-- 老化预检登记表
CREATE TABLE IF NOT EXISTS aging_pre_check (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    accessory_id BIGINT NOT NULL COMMENT '配件ID',
    accessory_name VARCHAR(100) COMMENT '配件名称',
    accessory_model VARCHAR(100) COMMENT '配件型号',
    warehouse_zone VARCHAR(50) COMMENT '库区',
    category_name VARCHAR(100) COMMENT '分类名称',
    unit VARCHAR(20) COMMENT '单位',
    stock_quantity INT NOT NULL COMMENT '库存数量',
    yellowing_level INT NOT NULL DEFAULT 0 COMMENT '发黄等级 0-5',
    cracking_level INT NOT NULL DEFAULT 0 COMMENT '脆裂等级 0-5',
    oxidation_level INT NOT NULL DEFAULT 0 COMMENT '金属氧化等级 0-5',
    overall_level INT NOT NULL DEFAULT 0 COMMENT '综合老化等级 0-5',
    threshold_reached TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否达到报废阈值',
    operator VARCHAR(50) COMMENT '操作人',
    check_time DATETIME NOT NULL COMMENT '检查时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    remark VARCHAR(500) COMMENT '备注',
    scrap_record_id BIGINT COMMENT '关联报废记录ID',
    PRIMARY KEY (id),
    INDEX idx_accessory_id (accessory_id),
    INDEX idx_threshold_reached (threshold_reached),
    INDEX idx_check_time (check_time),
    INDEX idx_scrap_record_id (scrap_record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老化预检登记表';

-- 报废附件表
CREATE TABLE IF NOT EXISTS scrap_attachment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    scrap_record_id BIGINT NOT NULL COMMENT '报废记录ID',
    file_url VARCHAR(500) NOT NULL COMMENT '附件地址',
    file_name VARCHAR(200) COMMENT '文件名称',
    file_type VARCHAR(50) COMMENT '文件类型',
    description VARCHAR(500) COMMENT '附件说明',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_scrap_record_id (scrap_record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报废附件表';
