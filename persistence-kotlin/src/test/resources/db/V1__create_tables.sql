CREATE TABLE IF NOT EXISTS AUTH_MEMBER
(
    ID       UUID     NOT NULL,
    OBJ_TYPE CHAR(1)  NOT NULL,
    STATUS   SMALLINT NOT NULL,
    CONSTRAINT AUTH_MEMBER_PK PRIMARY KEY (ID)
);
CREATE TABLE IF NOT EXISTS AUTH_GROUP
(
    ID           UUID    NOT NULL,
    GNAME        VARCHAR NOT NULL,
    SYSTEM_GROUP INTEGER NOT NULL,
    CONSTRAINT AUTH_GROUP_PK PRIMARY KEY (ID),
    CONSTRAINT AUTH_GROUP_ID_FK FOREIGN KEY (ID) REFERENCES AUTH_MEMBER (ID),
    constraint SYSTEM_GROUP_VALUES_CK check ( SYSTEM_GROUP in (1, 2, 3))
);
CREATE UNIQUE INDEX IF NOT EXISTS GROUP_NAME_UNIQUE_IDX ON AUTH_GROUP (GNAME);

CREATE TABLE IF NOT EXISTS AUTH_USER
(
    ID    UUID    NOT NULL,
    NAME  VARCHAR NOT NULL,
    EMAIL VARCHAR NOT NULL,
    CONSTRAINT AUTH_USER_PK PRIMARY KEY (ID),
    CONSTRAINT AUTH_USER_ID_FK FOREIGN KEY (ID) REFERENCES AUTH_MEMBER (ID)
);
CREATE UNIQUE INDEX IF NOT EXISTS AUTH_USER_EMAIL_IDX ON AUTH_USER (EMAIL);
CREATE TABLE IF NOT EXISTS SPACE
(
    ID               UUID         NOT NULL,
    NAME             VARCHAR(100) NOT NULL,
    SECURITY_CONCEPT INTEGER      NOT NULL,
    CONSTRAINT SPACE_PK PRIMARY KEY (ID),
    constraint SPACE_SECURITY_CONCEPT_VALUES_CK check ( SECURITY_CONCEPT in (1, 2, 3))
);
CREATE UNIQUE INDEX IF NOT EXISTS SPACE_NAME_IDX ON SPACE (NAME);
CREATE TABLE IF NOT EXISTS REVISION
(
    ID         UUID                     NOT NULL,
    SPACE_ID   UUID                     NOT NULL,
    CREATED_AT TIMESTAMP WITH TIME ZONE NOT NULL,
    CREATED_BY UUID                     NOT NULL,
    CONSTRAINT REV_user_FK FOREIGN KEY (CREATED_BY) REFERENCES auth_user (id),
    CONSTRAINT REVISION_PK PRIMARY KEY (ID)
);
CREATE TABLE public.entity_type
(
    id            uuid         not null,
    type          varchar(5)   NOT NULL,
    description   varchar(500) NOT NULL,
    table_name    varchar(50)  NOT NULL,
    entity_abbrev varchar(10)  NOT NULL,
    container     numeric(1)   NOT NULL,
    CONSTRAINT entity_type_pk PRIMARY KEY (id),
    CONSTRAINT unique_type_constraint UNIQUE (type)
);
CREATE TABLE public.resource_character
(
    id    uuid         NOT NULL,
    name  varchar(100) NOT NULL,
    icon  bytea        NULL,
    scope varchar(50)  NOT NULL,
    CONSTRAINT resource_character_pk PRIMARY KEY (id)
);
CREATE TABLE public.resource_node
(
    id                 uuid                     NOT NULL,
    node_type          varchar(5)               NOT NULL,
    node_comment       varchar(4000)            NULL,
    description        varchar(4000)            NULL,
    revision_id        uuid                     NOT NULL,
    revision_time      TIMESTAMP WITH TIME ZONE NOT NULL,
    locked_at TIMESTAMP WITH TIME ZONE NULL,
    entity_id          varchar(50)              NOT NULL,
    space_id           uuid                     NOT NULL,
    resource_character uuid                     NULL,
    owned_by           uuid                     NULL,
    CONSTRAINT repository_node_pk PRIMARY KEY (id),
    CONSTRAINT owned_by FOREIGN KEY (owned_by) REFERENCES auth_member (id),
    CONSTRAINT resource_node_character FOREIGN KEY (resource_character) REFERENCES public.resource_character (id),
    CONSTRAINT resource_revision FOREIGN KEY (revision_id) REFERENCES public.revision (id),
    CONSTRAINT resource_type FOREIGN KEY (node_type) REFERENCES public.entity_type (type),
    CONSTRAINT resource_space FOREIGN KEY (space_id) REFERENCES public.SPACE (ID)
);
CREATE UNIQUE INDEX resource_node_idx_entity_id ON public.resource_node (entity_id, space_id);
CREATE TABLE public.file_node
(
    id uuid NOT NULL,
    CONSTRAINT file_pk PRIMARY KEY (id),
    CONSTRAINT file_inheritance FOREIGN KEY (id) REFERENCES public.resource_node (id) ON DELETE CASCADE
);
CREATE TABLE public.resource_node_version
(
    id                 uuid                     NOT NULL,
    node_type          varchar(5)               NOT NULL,
    parent             uuid                     NULL,
    name               varchar(256)             NOT NULL,
    deleted            int8                     NOT NULL,
    resource_node      uuid                     NOT NULL,
    space_id           uuid                     NOT NULL,
    revision_from_id   uuid                     NOT NULL,
    revision_from_time TIMESTAMP WITH TIME ZONE NOT NULL,
    revision_to_time   TIMESTAMP WITH TIME ZONE NOT NULL,
    entity_version_id  varchar(50)              NOT NULL,
    CONSTRAINT repository_node_v_pk PRIMARY KEY (id),
    CONSTRAINT RESOURCE FOREIGN KEY (resource_node) REFERENCES public.resource_node (id) ON DELETE CASCADE,
    CONSTRAINT parent FOREIGN KEY (parent) REFERENCES public.resource_node (id) ON DELETE CASCADE,
    CONSTRAINT resource_version_type FOREIGN KEY (node_type) REFERENCES public.entity_type (type),
    CONSTRAINT resource_version_space FOREIGN KEY (space_id) REFERENCES public.SPACE (id),
    CONSTRAINT revision_from FOREIGN KEY (revision_from_id) REFERENCES public.revision (id)
);

CREATE TABLE public.file_version
(
    id              uuid         NOT NULL,
    filesize        int8         NOT NULL,
    file_store_path varchar(200) NOT NULL,
    file_hash bytea null,
    CONSTRAINT file_version_pk PRIMARY KEY (id),
    CONSTRAINT file_version_inheritance FOREIGN KEY (id) REFERENCES public.resource_node_version (id) ON DELETE CASCADE
);
CREATE TABLE public.folder
(
    id uuid NOT NULL,
    CONSTRAINT folder_pk PRIMARY KEY (id),
    CONSTRAINT folder_inheritance FOREIGN KEY (id) REFERENCES public.resource_node (id) ON DELETE CASCADE
);
CREATE TABLE public.folder_version
(
    id uuid NOT NULL,
    CONSTRAINT folder_version_pk PRIMARY KEY (id),
    CONSTRAINT folder_version_inheritance FOREIGN KEY (id) REFERENCES public.resource_node_version (id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS PARENTAL_RELATION
(
    ID UUID NOT NULL,
    CONSTRAINT PARENTAL_RELATION_PK PRIMARY KEY (ID)
);
CREATE TABLE IF NOT EXISTS PARENTAL_INPUT
(
    PARENTAL_RELATION_ID UUID NOT NULL,
    RESOURCE_VERSION_ID  UUID NOT NULL,
    CONSTRAINT PARENTAL_INPUT_PK PRIMARY KEY (PARENTAL_RELATION_ID, RESOURCE_VERSION_ID),
    CONSTRAINT PAREN_INPUT_PAREN_RELAT_ID_FK FOREIGN KEY (PARENTAL_RELATION_ID) REFERENCES PARENTAL_RELATION (ID),
    CONSTRAINT PAREN_INPUT_RESOU_VERSI_ID_FK FOREIGN KEY (RESOURCE_VERSION_ID) REFERENCES resource_node_version (ID)
);
CREATE TABLE IF NOT EXISTS PARENTAL_OUTPUT
(
    PARENTAL_RELATION_ID UUID NOT NULL,
    RESOURCE_VERSION_ID  UUID NOT NULL,
    CONSTRAINT PARENTAL_OUTPUT_PK PRIMARY KEY (PARENTAL_RELATION_ID, RESOURCE_VERSION_ID),
    CONSTRAINT PAREN_OUTPU_PAREN_RELAT_ID_FK FOREIGN KEY (PARENTAL_RELATION_ID) REFERENCES PARENTAL_RELATION (ID),
    CONSTRAINT PAREN_OUTPU_RESOU_VERSI_ID_FK FOREIGN KEY (RESOURCE_VERSION_ID) REFERENCES resource_node_version (ID)
);
CREATE TABLE IF NOT EXISTS AUTH_MEMBERSHIP
(
    PARENT_ID UUID NOT NULL,
    CHILD_ID  UUID NOT NULL,
    ID        UUID,
    STATUS    SMALLINT,
    CONSTRAINT AUTH_MEMBERSHIP_PK PRIMARY KEY (PARENT_ID, CHILD_ID),
    CONSTRAINT AUTH_MEMBE_PAREN_ID_FK FOREIGN KEY (PARENT_ID) REFERENCES AUTH_GROUP (ID),
    CONSTRAINT AUTH_MEMBE_CHILD_ID_FK FOREIGN KEY (CHILD_ID) REFERENCES AUTH_MEMBER (ID)
);
CREATE SEQUENCE public.entity_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START WITH 1
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE public.cached_entity_id_seq
    INCREMENT BY 1000
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START WITH 1
    CACHE 1
    NO CYCLE;

CREATE TABLE public.batch_job_instance
(
    job_instance_id int8         NOT NULL,
    version         int8         NULL,
    job_name        varchar(100) NOT NULL,
    job_key         varchar(32)  NOT NULL,
    CONSTRAINT batch_job_instance_pkey PRIMARY KEY (job_instance_id),
    CONSTRAINT job_inst_un UNIQUE (job_name, job_key)
);