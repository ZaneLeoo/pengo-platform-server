-- 清理历史脚本因采购订单 list 权限重复而生成的重复审核/弃审功能权限。
delete rm
from sys_role_menu rm
  inner join sys_menu m on m.menu_id = rm.menu_id
  cross join (
    select menu_id as target_parent_id
    from (select menu_id from sys_menu where menu_type = 'C' and perms = 'mes:purchaseOrder:list' limit 1) parent_menu
  ) target_parent
where m.perms in ('mes:purchaseOrder:approve', 'mes:purchaseOrder:unapprove')
  and m.parent_id <> target_parent.target_parent_id;

delete m
from sys_menu m
  cross join (
    select menu_id as target_parent_id
    from (select menu_id from sys_menu where menu_type = 'C' and perms = 'mes:purchaseOrder:list' limit 1) parent_menu
  ) target_parent
where m.perms in ('mes:purchaseOrder:approve', 'mes:purchaseOrder:unapprove')
  and m.parent_id <> target_parent.target_parent_id;
