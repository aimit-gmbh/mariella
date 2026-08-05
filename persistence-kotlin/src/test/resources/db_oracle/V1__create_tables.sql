CREATE TABLE AUTH_MEMBER
(
    ID       RAW (16)     NOT NULL,
    OBJ_TYPE CHAR(1)  NOT NULL,
    STATUS   SMALLINT NOT NULL,
    CONSTRAINT AUTH_MEMBER_PK PRIMARY KEY (ID)
) LOGGING;

CREATE TABLE AUTH_GROUP
(
    ID           RAW (16)    NOT NULL,
    GNAME        VARCHAR(200) NOT NULL,
    SYSTEM_GROUP INTEGER      NOT NULL,
    CONSTRAINT AUTH_GROUP_PK PRIMARY KEY (ID),
    CONSTRAINT AUTH_GROUP_ID_FK FOREIGN KEY (ID) REFERENCES AUTH_MEMBER (ID)
) LOGGING;

CREATE UNIQUE INDEX GROUP_NAME_UNIQUE_IDX ON AUTH_GROUP (GNAME);

CREATE TABLE AUTH_USER
(
    ID    RAW (16)    NOT NULL,
    NAME  VARCHAR(100) NOT NULL,
    EMAIL VARCHAR(100) NOT NULL,
    ROLE  VARCHAR(100) NULL,
    CONSTRAINT AUTH_USER_PK PRIMARY KEY (ID),
    CONSTRAINT AUTH_USER_ID_FK FOREIGN KEY (ID) REFERENCES AUTH_MEMBER (ID)
) LOGGING;

CREATE UNIQUE INDEX AUTH_USER_EMAIL_IDX ON AUTH_USER (EMAIL);

CREATE TABLE SPACE
(
    ID               RAW (16)         NOT NULL,
    NAME             VARCHAR(100) NOT NULL,
    SECURITY_CONCEPT INTEGER      NOT NULL,
    CONSTRAINT SPACE_PK PRIMARY KEY (ID),
    constraint SPACE_SECURITY_CONCEPT_VALUES_CK check ( SECURITY_CONCEPT >= 1 AND SECURITY_CONCEPT <= 3)
) LOGGING;
CREATE UNIQUE INDEX SPACE_NAME_IDX ON SPACE (NAME);
CREATE TABLE REVISION
(
    ID         RAW (16)                     NOT NULL,
    SPACE_ID   RAW (16)                     NOT NULL,
    CREATED_AT TIMESTAMP WITH TIME ZONE NOT NULL,
    CREATED_BY RAW (16)                     NOT NULL,
    CONSTRAINT REV_user_FK FOREIGN KEY (CREATED_BY) REFERENCES auth_user (id),
    CONSTRAINT REVISION_PK PRIMARY KEY (ID)
) LOGGING;
CREATE TABLE entity_type
(
    id            RAW (16)         not null,
    type          varchar(5)   NOT NULL,
    description   varchar(500) NOT NULL,
    table_name    varchar(50)  NOT NULL,
    entity_abbrev varchar(10)  NOT NULL,
    container     numeric(1)   NOT NULL,
    CONSTRAINT entity_type_pk PRIMARY KEY (id),
    CONSTRAINT unique_type_constraint UNIQUE (type)
) LOGGING;

CREATE TABLE resource_character
(
    id    RAW (16)         NOT NULL,
    name  varchar(100) NOT NULL,
    icon  BLOB NULL,
    scope varchar(50)  NOT NULL,
    CONSTRAINT resource_character_pk PRIMARY KEY (id)
) LOGGING;

CREATE TABLE resource_node
(
    id                 RAW (16)                     NOT NULL,
    node_type          varchar(5)               NOT NULL,
    node_comment       varchar(4000) NULL,
    description        varchar(4000) NULL,
    revision_id        RAW (16)                     NOT NULL,
    revision_time      TIMESTAMP WITH TIME ZONE NOT NULL,
    locked_at          TIMESTAMP WITH TIME ZONE NULL,
    entity_id          varchar(50)              NOT NULL,
    space_id           RAW (16)                     NOT NULL,
    resource_character RAW (16)                     NULL,
    owned_by           RAW (16)                     NULL,
    CONSTRAINT repository_node_pk PRIMARY KEY (id),
    CONSTRAINT owned_by FOREIGN KEY (owned_by) REFERENCES auth_member (id),
    CONSTRAINT resource_node_character FOREIGN KEY (resource_character) REFERENCES resource_character (id),
    CONSTRAINT resource_revision FOREIGN KEY (revision_id) REFERENCES revision (id),
    CONSTRAINT resource_type FOREIGN KEY (node_type) REFERENCES entity_type (type),
    CONSTRAINT resource_space FOREIGN KEY (space_id) REFERENCES SPACE (ID)
) LOGGING;

CREATE UNIQUE INDEX resource_node_idx_entity_id ON resource_node (entity_id, space_id);

CREATE TABLE file_node
(
    id RAW (16) NOT NULL,
    CONSTRAINT file_pk PRIMARY KEY (id),
    CONSTRAINT file_inheritance FOREIGN KEY (id) REFERENCES resource_node (id) ON DELETE CASCADE
) LOGGING;

CREATE TABLE resource_node_version
(
    id                 RAW (16)                     NOT NULL,
    node_type          varchar(5)               NOT NULL,
    parent             RAW (16)                     NULL,
    name               varchar(256)             NOT NULL,
    deleted            number(1)                     NOT NULL,
    node_comment       varchar(4000) NULL,
    resource_node      RAW (16)                     NOT NULL,
    space_id           RAW (16)                     NOT NULL,
    revision_from_id   RAW (16)                     NOT NULL,
    revision_from_time TIMESTAMP WITH TIME ZONE NOT NULL,
    revision_to_time   TIMESTAMP WITH TIME ZONE NOT NULL,
    entity_version_id  varchar(50)              NOT NULL,
    CONSTRAINT repository_node_v_pk PRIMARY KEY (id),
    CONSTRAINT RESOURCE_NODE_FK FOREIGN KEY (resource_node) REFERENCES resource_node (id) ON DELETE CASCADE,
    CONSTRAINT parent FOREIGN KEY (parent) REFERENCES resource_node (id) ON DELETE CASCADE,
    CONSTRAINT resource_version_type FOREIGN KEY (node_type) REFERENCES entity_type (type),
    CONSTRAINT resource_version_space FOREIGN KEY (space_id) REFERENCES SPACE (id),
    CONSTRAINT revision_from FOREIGN KEY (revision_from_id) REFERENCES revision (id)
) LOGGING;

CREATE TABLE file_version
(
    id              RAW (16)         NOT NULL,
    filesize        integer      NOT NULL,
    file_store_path varchar(200) NOT NULL,
    file_hash       RAW(32) null,
    CONSTRAINT file_version_pk PRIMARY KEY (id),
    CONSTRAINT file_version_inheritance FOREIGN KEY (id) REFERENCES resource_node_version (id) ON DELETE CASCADE
) LOGGING;
CREATE TABLE folder
(
    id RAW (16) NOT NULL,
    CONSTRAINT folder_pk PRIMARY KEY (id),
    CONSTRAINT folder_inheritance FOREIGN KEY (id) REFERENCES resource_node (id) ON DELETE CASCADE
) LOGGING;
CREATE TABLE folder_version
(
    id RAW (16) NOT NULL,
    CONSTRAINT folder_version_pk PRIMARY KEY (id),
    CONSTRAINT folder_version_inheritance FOREIGN KEY (id) REFERENCES resource_node_version (id) ON DELETE CASCADE
) LOGGING;
CREATE TABLE PARENTAL_RELATION
(
    ID RAW (16) NOT NULL,
    CONSTRAINT PARENTAL_RELATION_PK PRIMARY KEY (ID)
) LOGGING;
CREATE TABLE PARENTAL_INPUT
(
    PARENTAL_RELATION_ID RAW (16) NOT NULL,
    RESOURCE_VERSION_ID  RAW (16) NOT NULL,
    CONSTRAINT PARENTAL_INPUT_PK PRIMARY KEY (PARENTAL_RELATION_ID, RESOURCE_VERSION_ID),
    CONSTRAINT PAREN_INPUT_PAREN_RELAT_ID_FK FOREIGN KEY (PARENTAL_RELATION_ID) REFERENCES PARENTAL_RELATION (ID),
    CONSTRAINT PAREN_INPUT_RESOU_VERSI_ID_FK FOREIGN KEY (RESOURCE_VERSION_ID) REFERENCES resource_node_version (ID)
) LOGGING;
CREATE TABLE PARENTAL_OUTPUT
(
    PARENTAL_RELATION_ID RAW (16) NOT NULL,
    RESOURCE_VERSION_ID  RAW (16) NOT NULL,
    CONSTRAINT PARENTAL_OUTPUT_PK PRIMARY KEY (PARENTAL_RELATION_ID, RESOURCE_VERSION_ID),
    CONSTRAINT PAREN_OUTPU_PAREN_RELAT_ID_FK FOREIGN KEY (PARENTAL_RELATION_ID) REFERENCES PARENTAL_RELATION (ID),
    CONSTRAINT PAREN_OUTPU_RESOU_VERSI_ID_FK FOREIGN KEY (RESOURCE_VERSION_ID) REFERENCES resource_node_version (ID)
) LOGGING;
CREATE TABLE AUTH_MEMBERSHIP
(
    PARENT_ID RAW (16) NOT NULL,
    CHILD_ID  RAW (16) NOT NULL,
    ID        RAW (16),
    STATUS    SMALLINT,
    CONSTRAINT AUTH_MEMBERSHIP_PK PRIMARY KEY (PARENT_ID, CHILD_ID),
    CONSTRAINT AUTH_MEMBE_PAREN_ID_FK FOREIGN KEY (PARENT_ID) REFERENCES AUTH_GROUP (ID),
    CONSTRAINT AUTH_MEMBE_CHILD_ID_FK FOREIGN KEY (CHILD_ID) REFERENCES AUTH_MEMBER (ID)
) LOGGING;

CREATE SEQUENCE entity_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START WITH 1
    NOCYCLE;

CREATE SEQUENCE cached_entity_id_seq
    INCREMENT BY 1000
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START WITH 1
    NOCYCLE;

CREATE TABLE batch_job_instance
(
    job_instance_id number(19)         NOT NULL,
    version         number(19)         NULL,
    job_name        varchar(100) NOT NULL,
    job_key         varchar(32)  NOT NULL,
    CONSTRAINT batch_job_instance_pkey PRIMARY KEY (job_instance_id),
    CONSTRAINT job_inst_un UNIQUE (job_name, job_key)
) LOGGING;

create table ntf_record
(
    id                integer generated by default as identity primary key,
    notification_type varchar(20) not null
) LOGGING;

create table tstest
(
    revision_time TIMESTAMP WITH TIME ZONE NOT NULL
) LOGGING;