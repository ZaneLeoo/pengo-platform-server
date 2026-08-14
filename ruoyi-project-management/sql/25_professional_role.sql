-- V1：全局项目专业角色配置。
-- 项目角色仍然属于具体项目；专业角色跨项目复用。

CREATE TABLE IF NOT EXISTS pm_professional_role (
  professional_role_id BIGINT NOT NULL AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL,
  role_name VARCHAR(100) NOT NULL,
  role_description VARCHAR(500) DEFAULT NULL,
  system_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '1系统预置 0自定义',
  status CHAR(1) NOT NULL DEFAULT '0' COMMENT '0启用 1停用',
  sort_order INT NOT NULL DEFAULT 0,
  create_by VARCHAR(64) DEFAULT '',
  create_time DATETIME,
  update_by VARCHAR(64) DEFAULT '',
  update_time DATETIME,
  PRIMARY KEY (professional_role_id),
  UNIQUE KEY uk_pm_professional_role_code (role_code)
) ENGINE=InnoDB COMMENT='项目专业角色配置';

-- 项目成员保留 specialty_role 快照，同时记录标准化角色ID。
SET @professional_role_column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'pm_project_member'
    AND column_name = 'professional_role_id'
);
SET @professional_role_alter_sql = IF(
  @professional_role_column_exists = 0,
  'ALTER TABLE pm_project_member ADD COLUMN professional_role_id BIGINT DEFAULT NULL AFTER role_id',
  'SELECT 1'
);
PREPARE professional_role_alter_stmt FROM @professional_role_alter_sql;
EXECUTE professional_role_alter_stmt;
DEALLOCATE PREPARE professional_role_alter_stmt;

INSERT INTO pm_professional_role
  (role_code, role_name, role_description, system_flag, status, sort_order, create_by, create_time)
SELECT 'PROJECT_MANAGER', '项目经理', '负责项目总体目标、计划与协调', '1', '0', 10, 'system', SYSDATE()
WHERE NOT EXISTS (SELECT 1 FROM pm_professional_role WHERE role_code = 'PROJECT_MANAGER');
INSERT INTO pm_professional_role
  (role_code, role_name, role_description, system_flag, status, sort_order, create_by, create_time)
SELECT 'FRONTEND_DEVELOPER', '前端开发', '负责前端页面、组件和交互实现', '1', '0', 20, 'system', SYSDATE()
WHERE NOT EXISTS (SELECT 1 FROM pm_professional_role WHERE role_code = 'FRONTEND_DEVELOPER');
INSERT INTO pm_professional_role
  (role_code, role_name, role_description, system_flag, status, sort_order, create_by, create_time)
SELECT 'BACKEND_DEVELOPER', '后端开发', '负责后端服务、接口和数据实现', '1', '0', 30, 'system', SYSDATE()
WHERE NOT EXISTS (SELECT 1 FROM pm_professional_role WHERE role_code = 'BACKEND_DEVELOPER');
INSERT INTO pm_professional_role
  (role_code, role_name, role_description, system_flag, status, sort_order, create_by, create_time)
SELECT 'TEST_ENGINEER', '测试工程师', '负责测试设计、执行和质量验证', '1', '0', 40, 'system', SYSDATE()
WHERE NOT EXISTS (SELECT 1 FROM pm_professional_role WHERE role_code = 'TEST_ENGINEER');
INSERT INTO pm_professional_role
  (role_code, role_name, role_description, system_flag, status, sort_order, create_by, create_time)
SELECT 'MECHANICAL_ENGINEER', '机械工程师', '负责机械方案、结构和图纸设计', '1', '0', 50, 'system', SYSDATE()
WHERE NOT EXISTS (SELECT 1 FROM pm_professional_role WHERE role_code = 'MECHANICAL_ENGINEER');
INSERT INTO pm_professional_role
  (role_code, role_name, role_description, system_flag, status, sort_order, create_by, create_time)
SELECT 'ELECTRICAL_ENGINEER', '电气工程师', '负责电气方案、控制和电气图纸设计', '1', '0', 60, 'system', SYSDATE()
WHERE NOT EXISTS (SELECT 1 FROM pm_professional_role WHERE role_code = 'ELECTRICAL_ENGINEER');
INSERT INTO pm_professional_role
  (role_code, role_name, role_description, system_flag, status, sort_order, create_by, create_time)
SELECT 'PROCESS_ENGINEER', '工艺工程师', '负责工艺方案、工艺文件和制造支持', '1', '0', 70, 'system', SYSDATE()
WHERE NOT EXISTS (SELECT 1 FROM pm_professional_role WHERE role_code = 'PROCESS_ENGINEER');
INSERT INTO pm_professional_role
  (role_code, role_name, role_description, system_flag, status, sort_order, create_by, create_time)
SELECT 'PROCUREMENT_ENGINEER', '采购工程师', '负责采购计划、供应商协同和物料交付', '1', '0', 80, 'system', SYSDATE()
WHERE NOT EXISTS (SELECT 1 FROM pm_professional_role WHERE role_code = 'PROCUREMENT_ENGINEER');

-- 兼容既有测试数据：按原有名称补齐标准化角色ID。
UPDATE pm_project_member m
JOIN pm_professional_role r ON r.role_name = m.specialty_role
SET m.professional_role_id = r.professional_role_id
WHERE m.professional_role_id IS NULL
  AND m.specialty_role IS NOT NULL
  AND m.specialty_role <> '';
