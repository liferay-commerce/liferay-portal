create index IX_B3DA0163 on CVirtualOrderItemFileEntry (commerceVirtualOrderItemId);
create index IX_2FCB0E91 on CVirtualOrderItemFileEntry (uuid_[$COLUMN_LENGTH:75$]);

create unique index IX_44EADF9A on CommerceVirtualOrderItem (commerceOrderItemId);
create index IX_6345DC7D on CommerceVirtualOrderItem (uuid_[$COLUMN_LENGTH:75$]);