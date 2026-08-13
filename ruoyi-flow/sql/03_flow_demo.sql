-- 演示数据：立项审批流程（节点审批人默认 admin，可在流程定义页修改）
insert into pm_flow_definition(flow_key, flow_name, status, remark, create_by, create_time)
values('PROJECT_INITIATION_FLOW', '项目立项审批流程', '1', '项目管理立项申请的默认审批流程：部门负责人 → 总经理', 'admin', sysdate());

set @flow_id = last_insert_id();

insert into pm_flow_definition_node(flow_id, node_name, assign_type, assign_value, sign_type, sort_order, create_by, create_time)
values(@flow_id, '部门负责人审批', 'user', 'admin', 'OR', 1, 'admin', sysdate()),
       (@flow_id, '总经理审批', 'user', 'admin', 'OR', 2, 'admin', sysdate());

-- 绑定：项目管理立项申请走该流程
insert into pm_flow_binding(biz_type, flow_id, create_by, create_time)
values('PROJECT_INITIATION', @flow_id, 'admin', sysdate());
