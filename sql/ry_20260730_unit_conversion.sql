-- ============================================================
-- 多计量单位公式换算系统 - 数据库迁移脚本 (RuoYi-MES)
-- 创建日期：2026-07-30
-- ============================================================

-- 一、扩展 material 表 — 新增物理属性和计量单位组字段
ALTER TABLE material
    ADD COLUMN length         DECIMAL(18,4) NULL COMMENT '长(m)',
    ADD COLUMN width          DECIMAL(18,4) NULL COMMENT '宽(m)',
    ADD COLUMN height         DECIMAL(18,4) NULL COMMENT '高(m)',
    ADD COLUMN weight         DECIMAL(18,4) NULL COMMENT '重量(kg)',
    ADD COLUMN yards          DECIMAL(18,4) NULL COMMENT '码数',
    ADD COLUMN standard_weight_per_sqm DECIMAL(18,4) NULL COMMENT '每平方标准重量(kg/m2)';

-- 二、新建表：unit_group（计量单位组）
CREATE TABLE IF NOT EXISTS unit_group (
    id          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    group_code  VARCHAR(64)   NOT NULL COMMENT '单位组编码',
    group_name  VARCHAR(128)  NOT NULL COMMENT '单位组名称',
    group_type  VARCHAR(32)   NULL     COMMENT '单位组类别',
    status      CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    create_by   VARCHAR(64)   NULL     COMMENT '创建者',
    create_time DATETIME      NULL     COMMENT '创建时间',
    update_by   VARCHAR(64)   NULL     COMMENT '更新者',
    update_time DATETIME      NULL     COMMENT '更新时间',
    remark      VARCHAR(500)  NULL     COMMENT '备注',
    UNIQUE KEY uk_group_code (group_code)
) COMMENT '计量单位组';

-- 三、新建表：unit_group_detail（计量单位组明细，含公式支持）
CREATE TABLE IF NOT EXISTS unit_group_detail (
    id                BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    group_id          BIGINT        NOT NULL COMMENT '单位组ID',
    unit_code         VARCHAR(64)   NOT NULL COMMENT '单位编码',
    unit_name         VARCHAR(64)   NOT NULL COMMENT '单位名称',
    formula_id        BIGINT        NULL     COMMENT '该单位作为源单位时关联的换算公式ID',
    sort_order        INT           NOT NULL DEFAULT 0 COMMENT '排序号',
    create_by         VARCHAR(64)   NULL,
    create_time       DATETIME      NULL,
    update_by         VARCHAR(64)   NULL,
    update_time       DATETIME      NULL,
    remark            VARCHAR(500)  NULL,
    INDEX idx_group_id (group_id),
    UNIQUE KEY uk_group_unit (group_id, unit_code)
) COMMENT '计量单位组明细';

-- 四、新建表：unit_conversion_formula（换算公式定义，支持三级作用域）
CREATE TABLE IF NOT EXISTS unit_conversion_formula (
    id                  BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    formula_code        VARCHAR(64)   NOT NULL COMMENT '公式编码',
    formula_name        VARCHAR(128)  NOT NULL COMMENT '公式名称',
    formula_type        VARCHAR(32)   NOT NULL COMMENT '公式类型: FIXED_RATE/AREA_TIMES_YARDS/WEIGHT_FROM_DIMS/CARTON_PRICE/CARDBOARD_PRICE/CUSTOM',
    expression          VARCHAR(512)  NOT NULL COMMENT '表达式模板, 如: ${length} * ${width} * ${yards}',
    unit_group_id       BIGINT        NOT NULL COMMENT '所属计量单位组ID',
    input_unit          VARCHAR(64)   NOT NULL COMMENT '源单位编码',
    output_unit         VARCHAR(64)   NOT NULL COMMENT '目标单位编码',
    scope_type          VARCHAR(16)   NOT NULL DEFAULT 'UNIT_GROUP' COMMENT '作用域: UNIT_GROUP/CLASSIFICATION/MATERIAL',
    scope_id            BIGINT        NULL     COMMENT '作用域ID(分类ID或物料ID)',
    reverse_mode        VARCHAR(16)   NOT NULL DEFAULT 'DIVIDE' COMMENT '反向模式: DIVIDE/MULTIPLY/CUSTOM',
    decimal_scale       INT           NOT NULL DEFAULT 4 COMMENT '公式结果小数位数(0-6)',
    rounding_mode       VARCHAR(16)   NOT NULL DEFAULT 'HALF_UP' COMMENT '公式结果舍入模式',
    reverse_expression  VARCHAR(512)  NULL     COMMENT '反向公式(CUSTOM时使用)',
    param1_name         VARCHAR(64)   NULL     COMMENT '参数1变量名',
    param1_field        VARCHAR(64)   NULL     COMMENT '参数1映射字段: length/width/height/weight/yards/standard_weight_per_sqm',
    param1_default      DECIMAL(20,8) NULL     COMMENT '参数1默认常量(field为空时使用)',
    param2_name         VARCHAR(64)   NULL,    param2_field VARCHAR(64) NULL,    param2_default DECIMAL(20,8) NULL,
    param3_name         VARCHAR(64)   NULL,    param3_field VARCHAR(64) NULL,    param3_default DECIMAL(20,8) NULL,
    param4_name         VARCHAR(64)   NULL,    param4_field VARCHAR(64) NULL,    param4_default DECIMAL(20,8) NULL,
    param5_name         VARCHAR(64)   NULL,    param5_field VARCHAR(64) NULL,    param5_default DECIMAL(20,8) NULL,
    is_active           CHAR(1)       NOT NULL DEFAULT 'Y' COMMENT '是否启用',
    sort_order          INT           NOT NULL DEFAULT 0,
    status              CHAR(1)       NOT NULL DEFAULT '0' COMMENT '状态',
    create_by           VARCHAR(64)   NULL,
    create_time         DATETIME      NULL,
    update_by           VARCHAR(64)   NULL,
    update_time         DATETIME      NULL,
    remark              VARCHAR(500)  NULL,
    INDEX idx_group_scope (unit_group_id, scope_type, scope_id),
    UNIQUE KEY uk_formula_code (formula_code)
) COMMENT '计量单位换算公式配置';

-- 五、业务单据明细表 — 新增录入单位和三个对称单位快照

-- 采购订单明细
ALTER TABLE purchase_order_line
    ADD COLUMN input_unit_code  VARCHAR(64)  NULL COMMENT '录入单位编码',
    ADD COLUMN input_unit_name  VARCHAR(64)  NULL COMMENT '录入单位名称',
    ADD COLUMN input_qty        DECIMAL(20,6) NULL COMMENT '录入数量',
    ADD COLUMN unit1_code       VARCHAR(64)  NULL COMMENT '单位组成员1编码',
    ADD COLUMN unit1_name       VARCHAR(64)  NULL COMMENT '单位组成员1名称',
    ADD COLUMN unit1_qty        DECIMAL(20,6) NULL COMMENT '单位组成员1数量',
    ADD COLUMN unit2_code       VARCHAR(64)  NULL COMMENT '单位组成员2编码',
    ADD COLUMN unit2_name       VARCHAR(64)  NULL COMMENT '单位组成员2名称',
    ADD COLUMN unit2_qty        DECIMAL(20,6) NULL COMMENT '单位组成员2数量',
    ADD COLUMN unit3_code       VARCHAR(64)  NULL COMMENT '单位组成员3编码',
    ADD COLUMN unit3_name       VARCHAR(64)  NULL COMMENT '单位组成员3名称',
    ADD COLUMN unit3_qty        DECIMAL(20,6) NULL COMMENT '单位组成员3数量';

-- ============================================================
-- 六、种子数据：平方-卷-箱 单位组配置示例
-- ============================================================

-- 插入单位组
INSERT INTO unit_group (group_code, group_name, group_type, status) VALUES
('SQ_ROLL_BOX', '平方-卷-箱', 'FINISHED', '0');

SET @group_id = LAST_INSERT_ID();

-- 插入单位组成员；三个成员无主次，material.unit 仅保留作历史库存兼容字段
INSERT INTO unit_group_detail (group_id, unit_code, unit_name) VALUES
(@group_id, 'SQ',  '平方米'),
(@group_id, 'ROL', '卷'),
(@group_id, 'BOX', '箱');

-- 插入换算公式1：卷→平方米（单位组级别）
INSERT INTO unit_conversion_formula (
    formula_code, formula_name, formula_type,
    expression, unit_group_id, input_unit, output_unit,
    scope_type, scope_id, reverse_mode, decimal_scale, rounding_mode,
    param1_name, param1_field,
    param2_name, param2_field,
    param3_name, param3_field,
    is_active
) VALUES (
    'ROLL_TO_SQM', '卷转平方米', 'AREA_TIMES_YARDS',
    '${length} * ${width} * ${yards}', @group_id, 'ROL', 'SQ',
    'UNIT_GROUP', NULL, 'DIVIDE', 4, 'HALF_UP',
    'length', 'length',
    'width', 'width',
    'yards', 'yards',
    'Y'
);

SET @roll_formula_id = LAST_INSERT_ID();
UPDATE unit_group_detail SET formula_id = @roll_formula_id WHERE group_id = @group_id AND unit_code = 'ROL';

-- 插入换算公式2：箱→平方米（含常量 rollsPerBox=4）
INSERT INTO unit_conversion_formula (
    formula_code, formula_name, formula_type,
    expression, unit_group_id, input_unit, output_unit,
    scope_type, scope_id, reverse_mode, decimal_scale, rounding_mode,
    param1_name, param1_field,
    param2_name, param2_field,
    param3_name, param3_field,
    param4_name, param4_field, param4_default,
    is_active
) VALUES (
    'BOX_TO_SQM', '箱转平方米', 'AREA_TIMES_YARDS',
    '${length} * ${width} * ${yards} * ${rollsPerBox}', @group_id, 'BOX', 'SQ',
    'UNIT_GROUP', NULL, 'DIVIDE', 4, 'HALF_UP',
    'length', 'length',
    'width', 'width',
    'yards', 'yards',
    'rollsPerBox', NULL, 4,
    'Y'
);

SET @box_formula_id = LAST_INSERT_ID();
UPDATE unit_group_detail SET formula_id = @box_formula_id WHERE group_id = @group_id AND unit_code = 'BOX';
