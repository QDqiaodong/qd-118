-- 施工项目表
CREATE TABLE IF NOT EXISTS construction_project (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    project_no VARCHAR(64) NOT NULL COMMENT '施工项目编号',
    project_name VARCHAR(200) NOT NULL COMMENT '施工项目名称',
    factory_area VARCHAR(100) COMMENT '厂区',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '项目状态：1进行中 0已结束',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_no (project_no),
    INDEX idx_factory_area (factory_area),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='施工项目表';

-- 给领用出库表增加施工项目关联字段
ALTER TABLE stock_out ADD COLUMN project_id BIGINT COMMENT '施工项目ID' AFTER usage_name;
ALTER TABLE stock_out ADD COLUMN project_no VARCHAR(64) COMMENT '施工项目编号' AFTER project_id;
ALTER TABLE stock_out ADD COLUMN purpose VARCHAR(500) COMMENT '用途说明' AFTER project_no;

ALTER TABLE stock_out ADD INDEX idx_project_id (project_id);
