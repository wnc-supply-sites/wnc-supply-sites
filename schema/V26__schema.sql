-- 'is_need' is set to true when we 'need' an item. Otherwise it is available or oversupply
alter table item_status add column is_need boolean;
update item_status set is_need = true where name = 'Urgently Needed';
update item_status set is_need = true where name = 'Needed';
update item_status set is_need = false where name = 'Available';
update item_status set is_need = false where name = 'Oversupply';
alter table item_status alter column is_need set not null;
