-- entity types
insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('5410f1a9-2378-4005-a2f0-72770dc3498a', 'FOV', 'Folder Version', 'FOLDER_VERSION', 'FOV', 1);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('d06daad0-3111-41fb-8040-d075c023e877', 'FIV', 'File Version', 'FILE_VERSION', 'FIV', 0);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('05815bb1-22a6-4256-83cf-ce76ca98af68', 'FO', 'Folder', 'FOLDER', 'FO', 1);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('19e5ac15-04d1-4c6e-958b-c26dba604200', 'FI', 'File', 'FILE_NODE', 'FI', 0);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('abe501d8-fd6e-441a-8754-00ca29edf283', 'GR', 'Group', 'APP_GROUP', 'GR', 0);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('856b9d38-a4b3-44a1-8ca6-8746c08da470', 'AU', 'User', 'APP_MEMBER', 'AU', 0);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('f2acffff-85e1-4562-a1ad-f14d11087088', 'AUV', 'User Version', 'APP_USER_VERSION', 'AUV', 0);

insert into auth_member (id, obj_type, status)
values ('54a88f36-d9dd-43f7-a751-68d79f5f7093', 'G', 0);
insert into auth_group (id, gname, system_group)
values ('54a88f36-d9dd-43f7-a751-68d79f5f7093', 'Admin Group', 3);

insert into auth_member (id, obj_type, status)
values ('c97f213b-a030-4c1c-92d1-fbebe2a807c3', 'G', 0);
insert into auth_group (id, gname, system_group)
values ('c97f213b-a030-4c1c-92d1-fbebe2a807c3', 'Super User Group', 2);

insert into auth_member (id, obj_type, status)
values ('84f80398-efcc-4ea7-8de1-bbc774f371dc', 'U', 0);
insert into auth_user (id, name, email)
values ('84f80398-efcc-4ea7-8de1-bbc774f371dc', 'Seppi', 'ss@aimit.at');

insert into auth_member (id, obj_type, status)
values ('a8c0926d-1859-4048-bc0f-1509713ad7cf', 'U', 0);
insert into auth_user (id, name, email)
values ('a8c0926d-1859-4048-bc0f-1509713ad7cf', 'Guest', 'guest@aimit.at');

insert into auth_member (id, obj_type, status)
values ('e920a34d-fde0-450c-8e7a-7eed4dbae7ad', 'U', 0);
insert into auth_user (id, name, email)
values ('e920a34d-fde0-450c-8e7a-7eed4dbae7ad', 'Karl', 'kk@aimit.at');

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('d841d50f-ab2d-4ea9-ac5d-204e98ca4457', 'I', 'file type', 'file_node', 'FI', 0);

insert into entity_type(id, type, description, table_name, entity_abbrev, container)
values ('5c752e45-4593-46dd-b740-628e3e79f884', 'O', 'folder type', 'folder', 'FO', 1);

insert into space(id, name, security_concept)
values ('01026b95-149b-47fa-91a2-984b83b3ffb7', 'test space', 1);

insert into space(id, name, security_concept)
values ('6f0a1ecb-8ce4-4b2c-94b4-0798f038b095', 'space security space', 2);
