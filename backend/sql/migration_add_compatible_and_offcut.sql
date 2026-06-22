USE weakcurrent_db;

CREATE TABLE IF NOT EXISTS compatible_model_group (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    group_name VARCHAR(100) NOT NULL COMMENT '兼容组名称',
    brand VARCHAR(100) COMMENT '品牌',
    model VARCHAR(100) NOT NULL COMMENT '型号',
    spec VARCHAR(100) COMMENT '规格参数',
    accessory_id BIGINT COMMENT '关联配件ID',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_model (model),
    INDEX idx_accessory_id (accessory_id),
    INDEX idx_brand (brand)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兼容型号库';

CREATE TABLE IF NOT EXISTS duct_offcut (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    accessory_id BIGINT NOT NULL COMMENT '线槽配件ID',
    accessory_name VARCHAR(100) COMMENT '线槽配件名称',
    accessory_model VARCHAR(100) COMMENT '线槽型号',
    stock_out_id BIGINT COMMENT '关联出库记录ID',
    original_length DECIMAL(10,2) NOT NULL COMMENT '领用原始长度(米)',
    used_length DECIMAL(10,2) NOT NULL COMMENT '已使用长度(米)',
    offcut_length DECIMAL(10,2) NOT NULL COMMENT '余料长度(米)',
    return_zone VARCHAR(50) COMMENT '回库货位',
    operator VARCHAR(50) COMMENT '操作人',
    status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0待回库 1已回库',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_accessory_id (accessory_id),
    INDEX idx_stock_out_id (stock_out_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='线槽裁切余料管理';
