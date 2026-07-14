-- 物料基础资料字典：物料类型、来源类型
-- 字典编码与 MaterialType、SourceType Java 枚举保持一致。

insert into sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
select '物料类型', 'mes_material_type', '0', 'admin', now(), 'MES物料类型'
where not exists (select 1 from sys_dict_type where dict_type = 'mes_material_type');

insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '成品', 'FINISHED', 'mes_material_type', '', 'primary', 'N', '0', 'admin', now(), 'MaterialType.FINISHED'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_material_type' and dict_value = 'FINISHED');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '半成品', 'SEMI_FINISHED', 'mes_material_type', '', 'warning', 'N', '0', 'admin', now(), 'MaterialType.SEMI_FINISHED'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_material_type' and dict_value = 'SEMI_FINISHED');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '原材料', 'RAW', 'mes_material_type', '', 'success', 'N', '0', 'admin', now(), 'MaterialType.RAW'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_material_type' and dict_value = 'RAW');

insert into sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
select '物料来源类型', 'mes_material_source_type', '0', 'admin', now(), 'MES物料来源类型'
where not exists (select 1 from sys_dict_type where dict_type = 'mes_material_source_type');

insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '自制', 'MAKE', 'mes_material_source_type', '', 'primary', 'N', '0', 'admin', now(), 'SourceType.MAKE'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_material_source_type' and dict_value = 'MAKE');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '委外', 'OUTSOURCE', 'mes_material_source_type', '', 'warning', 'N', '0', 'admin', now(), 'SourceType.OUTSOURCE'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_material_source_type' and dict_value = 'OUTSOURCE');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '外购', 'PURCHASE', 'mes_material_source_type', '', 'success', 'N', '0', 'admin', now(), 'SourceType.PURCHASE'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_material_source_type' and dict_value = 'PURCHASE');

insert into sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
select 'BOM类型', 'mes_bom_type', '0', 'admin', now(), 'MES BOM类型'
where not exists (select 1 from sys_dict_type where dict_type = 'mes_bom_type');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 1, '生产BOM', 'MANUFACTURING', 'mes_bom_type', 'primary', '0', 'admin', now(), 'BomType.MANUFACTURING'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_type' and dict_value = 'MANUFACTURING');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 2, '试制BOM', 'TRIAL', 'mes_bom_type', 'warning', '0', 'admin', now(), 'BomType.TRIAL'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_type' and dict_value = 'TRIAL');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 3, '返工BOM', 'REWORK', 'mes_bom_type', 'danger', '0', 'admin', now(), 'BomType.REWORK'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_type' and dict_value = 'REWORK');

insert into sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
select 'BOM主表状态', 'mes_bom_master_status', '0', 'admin', now(), 'MES BOM主表状态'
where not exists (select 1 from sys_dict_type where dict_type = 'mes_bom_master_status');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 1, '启用', 'ENABLED', 'mes_bom_master_status', 'success', '0', 'admin', now(), 'BomMasterStatus.ENABLED'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_master_status' and dict_value = 'ENABLED');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 2, '停用', 'DISABLED', 'mes_bom_master_status', 'danger', '0', 'admin', now(), 'BomMasterStatus.DISABLED'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_master_status' and dict_value = 'DISABLED');

insert into sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
select 'BOM版本用途', 'mes_bom_usage_type', '0', 'admin', now(), 'MES BOM版本用途'
where not exists (select 1 from sys_dict_type where dict_type = 'mes_bom_usage_type');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 1, '通用', 'GENERAL', 'mes_bom_usage_type', 'default', '0', 'admin', now(), 'BomUsageType.GENERAL'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_usage_type' and dict_value = 'GENERAL');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 2, '量产', 'MASS', 'mes_bom_usage_type', 'success', '0', 'admin', now(), 'BomUsageType.MASS'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_usage_type' and dict_value = 'MASS');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 3, '试制', 'TRIAL', 'mes_bom_usage_type', 'warning', '0', 'admin', now(), 'BomUsageType.TRIAL'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_usage_type' and dict_value = 'TRIAL');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 4, '返工', 'REWORK', 'mes_bom_usage_type', 'danger', '0', 'admin', now(), 'BomUsageType.REWORK'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_usage_type' and dict_value = 'REWORK');

insert into sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
select 'BOM版本状态', 'mes_bom_version_status', '0', 'admin', now(), 'MES BOM版本状态'
where not exists (select 1 from sys_dict_type where dict_type = 'mes_bom_version_status');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 1, '草稿', 'DRAFT', 'mes_bom_version_status', 'default', '0', 'admin', now(), 'BomVersionStatus.DRAFT'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_version_status' and dict_value = 'DRAFT');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 2, '生效', 'EFFECTIVE', 'mes_bom_version_status', 'success', '0', 'admin', now(), 'BomVersionStatus.EFFECTIVE'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_version_status' and dict_value = 'EFFECTIVE');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 3, '冻结', 'FROZEN', 'mes_bom_version_status', 'warning', '0', 'admin', now(), 'BomVersionStatus.FROZEN'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_version_status' and dict_value = 'FROZEN');

insert into sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
select 'BOM审核状态', 'mes_bom_approve_status', '0', 'admin', now(), 'MES BOM审核状态'
where not exists (select 1 from sys_dict_type where dict_type = 'mes_bom_approve_status');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 1, '待审核', 'PENDING', 'mes_bom_approve_status', 'processing', '0', 'admin', now(), 'BomApproveStatus.PENDING'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_approve_status' and dict_value = 'PENDING');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 2, '已审核', 'APPROVED', 'mes_bom_approve_status', 'success', '0', 'admin', now(), 'BomApproveStatus.APPROVED'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_approve_status' and dict_value = 'APPROVED');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 3, '已驳回', 'REJECTED', 'mes_bom_approve_status', 'danger', '0', 'admin', now(), 'BomApproveStatus.REJECTED'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_bom_approve_status' and dict_value = 'REJECTED');

insert into sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
select '数据来源系统', 'mes_source_system', '0', 'admin', now(), 'MES数据来源系统'
where not exists (select 1 from sys_dict_type where dict_type = 'mes_source_system');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 1, '手工', 'MANUAL', 'mes_source_system', 'default', '0', 'admin', now(), 'SourceSystem.MANUAL'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_source_system' and dict_value = 'MANUAL');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 2, 'Excel', 'EXCEL', 'mes_source_system', 'success', '0', 'admin', now(), 'SourceSystem.EXCEL'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_source_system' and dict_value = 'EXCEL');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 3, 'U8', 'U8', 'mes_source_system', 'processing', '0', 'admin', now(), 'SourceSystem.U8'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_source_system' and dict_value = 'U8');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 4, 'U9C', 'U9C', 'mes_source_system', 'processing', '0', 'admin', now(), 'SourceSystem.U9C'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_source_system' and dict_value = 'U9C');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
select 5, 'AI导入', 'AI_IMPORT', 'mes_source_system', 'warning', '0', 'admin', now(), 'SourceSystem.AI_IMPORT'
where not exists (select 1 from sys_dict_data where dict_type = 'mes_source_system' and dict_value = 'AI_IMPORT');
