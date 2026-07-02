-- Dify 多应用配置：聊天和 BOM OCR 分别维护应用密钥，避免所有 AI 能力共用一套 sys_config。

create table if not exists agent_dify_app_config
(
    id           bigint(20)   not null auto_increment comment '配置ID',
    app_code     varchar(64)  not null comment '应用编码',
    app_name     varchar(100) not null comment '应用名称',
    app_type     varchar(32)  not null comment '应用类型：chatflow、workflow',
    api_base_url varchar(255) not null default 'https://api.dify.ai/v1' comment 'Dify API地址',
    api_key      varchar(255)          default null comment 'Dify API Key',
    enabled      char(1)      not null default 'Y' comment '是否启用（Y是 N否）',
    remark       varchar(500)          default null comment '备注',
    create_by    varchar(64)           default '' comment '创建者',
    create_time  datetime              default null comment '创建时间',
    update_by    varchar(64)           default '' comment '更新者',
    update_time  datetime              default null comment '更新时间',
    primary key (id),
    unique key uk_agent_dify_app_config_code (app_code)
) engine=innodb auto_increment=1 comment='Dify应用配置表';

insert into agent_dify_app_config(app_code, app_name, app_type, api_base_url, enabled, remark, create_by, create_time)
select 'AGENT_CHAT', '企业智能体聊天', 'chatflow', 'https://api.dify.ai/v1', 'Y',
       '企业智能体主聊天应用；未配置 api_key 时后端仍兼容旧 sys_config 参数。', 'admin', sysdate()
where not exists (select 1 from agent_dify_app_config where app_code = 'AGENT_CHAT');

insert into agent_dify_app_config(app_code, app_name, app_type, api_base_url, enabled, remark, create_by, create_time)
select 'BOM_OCR', 'BOM图纸OCR识别', 'workflow', 'https://api.dify.ai/v1', 'N',
       'BOM 图纸 OCR/结构化识别应用，后续接入 PaddleOCR 与大模型结构化流程。', 'admin', sysdate()
where not exists (select 1 from agent_dify_app_config where app_code = 'BOM_OCR');

-- 如需在 RuoYi 主前端维护该配置，可执行以下菜单初始化；若只走独立 agent-ui，可暂不执行菜单部分。
insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'Dify应用配置',
       coalesce(
           (select menu_id from (select menu_id from sys_menu where menu_name = 'AI' and menu_type = 'M' limit 1) t),
           (select menu_id from (select menu_id from sys_menu where menu_name = '智能助手' and menu_type = 'M' limit 1) t),
           0
       ),
       10, 'dify-app', 'agent/difyApp/index', 1, 0, 'C', '0', '0', 'agent:difyApp:list', 'system', 'admin', sysdate(), 'Dify多应用配置菜单'
where not exists (select 1 from sys_menu where perms = 'agent:difyApp:list');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'Dify应用查询', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'agent:difyApp:query', '#', 'admin', sysdate(), ''
from sys_menu
where perms = 'agent:difyApp:list'
  and not exists (select 1 from sys_menu where perms = 'agent:difyApp:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'Dify应用新增', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'agent:difyApp:add', '#', 'admin', sysdate(), ''
from sys_menu
where perms = 'agent:difyApp:list'
  and not exists (select 1 from sys_menu where perms = 'agent:difyApp:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'Dify应用修改', menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'agent:difyApp:edit', '#', 'admin', sysdate(), ''
from sys_menu
where perms = 'agent:difyApp:list'
  and not exists (select 1 from sys_menu where perms = 'agent:difyApp:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'Dify应用删除', menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'agent:difyApp:remove', '#', 'admin', sysdate(), ''
from sys_menu
where perms = 'agent:difyApp:list'
  and not exists (select 1 from sys_menu where perms = 'agent:difyApp:remove');
