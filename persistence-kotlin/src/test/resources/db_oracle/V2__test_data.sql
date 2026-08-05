-- entity types
insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('5410F1A923784005A2F072770DC3498A', 'FOV', 'Folder Version', 'FOLDER_VERSION', 'FOV', 1);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('D06DAAD0311141FB8040D075C023E877', 'FIV', 'File Version', 'FILE_VERSION', 'FIV', 0);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('05815BB122A6425683CFCE76CA98AF68', 'FO', 'Folder', 'FOLDER', 'FO', 1);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('19E5AC1504D14C6E958BC26DBA604200', 'FI', 'File', 'FILE_NODE', 'FI', 0);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('ABE501D8FD6E441A875400CA29EDF283', 'GR', 'Group', 'APP_GROUP', 'GR', 0);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('856B9D38A4B344A18CA68746C08DA470', 'AU', 'User', 'APP_MEMBER', 'AU', 0);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('F2ACFFFF85E14562A1ADF14D11087088', 'AUV', 'User Version', 'APP_USER_VERSION', 'AUV', 0);

insert into auth_member (id, obj_type, status)
values ('54A88F36D9DD43F7A75168D79F5F7093', 'G', 0);
insert into auth_group (id, gname, system_group)
values ('54A88F36D9DD43F7A75168D79F5F7093', 'Admin Group', 3);

insert into auth_member (id, obj_type, status)
values ('C97F213BA0304C1C92D1FBEBE2A807C3', 'G', 0);
insert into auth_group (id, gname, system_group)
values ('C97F213BA0304C1C92D1FBEBE2A807C3', 'Super User Group', 2);

insert into auth_member (id, obj_type, status)
values ('84F80398EFCC4EA78DE1BBC774F371DC', 'U', 0);
insert into auth_user (id, name, email, role)
values ('84F80398EFCC4EA78DE1BBC774F371DC', 'Seppi', 'ss@aimit.at', 'donkey');

insert into auth_member (id, obj_type, status)
values ('A8C0926D18594048BC0F1509713AD7CF', 'U', 0);
insert into auth_user (id, name, email, role)
values ('A8C0926D18594048BC0F1509713AD7CF', 'Guest', 'guest@aimit.at', null);

insert into auth_member (id, obj_type, status)
values ('E920A34DFDE0450C8E7A7EED4DBAE7AD', 'U', 0);
insert into auth_user (id, name, email, role)
values ('E920A34DFDE0450C8E7A7EED4DBAE7AD', 'Karl', 'kk@aimit.at', 'codemonkey');

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('D841D50FAB2D4EA9AC5D204E98CA4457', 'I', 'file type', 'file_node', 'FI', 0);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('5C752E45459346DDB740628E3E79F884', 'O', 'folder type', 'folder', 'FO', 1);

insert into space(id, name, security_concept)
values ('01026B95149B47FA91A2984B83B3FFB7', 'test space', 1);

insert into space(id, name, security_concept)
values ('6F0A1ECB8CE44B2C94B40798F038B095', 'space security space', 2);
