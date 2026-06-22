-- 为配件表添加规格字段
ALTER TABLE accessory
ADD COLUMN square_number VARCHAR(20) COMMENT '平方数（接线端子）' AFTER unit,
ADD COLUMN pin_count INT COMMENT '针脚数（接线端子）' AFTER square_number,
ADD COLUMN width VARCHAR(20) COMMENT '宽度（线槽）' AFTER pin_count,
ADD COLUMN height VARCHAR(20) COMMENT '高度（线槽）' AFTER width,
ADD COLUMN diameter VARCHAR(20) COMMENT '直径（固定卡扣）' AFTER height;

-- 为现有示例数据填充规格字段
UPDATE accessory SET square_number = '2.5mm²' WHERE id = 1;
UPDATE accessory SET square_number = '2.5mm²' WHERE id = 2;
UPDATE accessory SET pin_count = 5 WHERE id = 3;
UPDATE accessory SET width = '25mm', height = '15mm' WHERE id = 4;
UPDATE accessory SET width = '100mm', height = '50mm' WHERE id = 5;
UPDATE accessory SET diameter = '20mm' WHERE id = 6;
UPDATE accessory SET diameter = '8mm' WHERE id = 7;
UPDATE accessory SET diameter = '3mm' WHERE id = 8;
UPDATE accessory SET width = '35mm' WHERE id = 9;
