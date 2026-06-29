-- Fix existing Agent menu route generated as //ai by removing the leading slash.
update sys_menu
set path = 'ai'
where menu_id = 2000
  and path = '/ai';
