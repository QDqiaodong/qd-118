-- 为报废记录表添加老化批次ID字段，用于关联老化批次
ALTER TABLE scrap_record
ADD COLUMN aging_batch_id BIGINT COMMENT '关联的老化批次ID' AFTER remark;

-- 添加索引
CREATE INDEX idx_scrap_record_aging_batch_id ON scrap_record(aging_batch_id);
