create index IX_B3DA0163 on CVirtualOrderItemFileEntry (commerceVirtualOrderItemId);
create index IX_8378E0F7 on CVirtualOrderItemFileEntry (uuid_[$COLUMN_LENGTH:75$], companyId);
create unique index IX_68E33939 on CVirtualOrderItemFileEntry (uuid_[$COLUMN_LENGTH:75$], groupId);

create unique index IX_44EADF9A on CommerceVirtualOrderItem (commerceOrderItemId);
create index IX_98F0678B on CommerceVirtualOrderItem (uuid_[$COLUMN_LENGTH:75$], companyId);
create unique index IX_81F354CD on CommerceVirtualOrderItem (uuid_[$COLUMN_LENGTH:75$], groupId);