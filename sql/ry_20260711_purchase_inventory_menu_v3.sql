-- 将采购订单、到货单、入库单、库存余额调整为 MES 下的独立业务菜单
set @mes_menu_id := (select menu_id from sys_menu where menu_name='MES' and parent_id=0 limit 1);
update sys_menu set parent_id=@mes_menu_id, menu_name='采购订单', path='purchaseOrder', component='mes/purchase/order' where perms='mes:purchaseOrder:list';
update sys_menu set parent_id=@mes_menu_id, menu_name='库存余额', path='inventoryBalance', component='mes/purchase/inventory' where perms='mes:inventoryBalance:list';
update sys_menu set parent_id=@mes_menu_id, path='purchaseReceipt', component='mes/purchase/receipt' where perms='mes:purchaseReceipt:menu';
update sys_menu set parent_id=@mes_menu_id, path='purchaseInbound', component='mes/purchase/inbound' where perms='mes:purchaseInbound:menu';
