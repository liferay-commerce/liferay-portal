create index IX_C606354 on CPDVirtualSettingFileEntry (CPDefinitionVirtualSettingId);
create index IX_5AA274D4 on CPDVirtualSettingFileEntry (uuid_[$COLUMN_LENGTH:75$], companyId);
create unique index IX_762A2056 on CPDVirtualSettingFileEntry (uuid_[$COLUMN_LENGTH:75$], groupId);

create unique index IX_19B2FD20 on CPDefinitionVirtualSetting (classNameId, classPK);
create index IX_F1182A3F on CPDefinitionVirtualSetting (uuid_[$COLUMN_LENGTH:75$], companyId);
create unique index IX_8ED43481 on CPDefinitionVirtualSetting (uuid_[$COLUMN_LENGTH:75$], groupId);