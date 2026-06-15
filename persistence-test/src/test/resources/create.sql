drop table if exists collaborators;

drop table if exists person;

drop table if exists company;

drop table if exists ngo;

drop table if exists partner;

create table partner
(
    id    uuid primary key,
    type  char(1) not null,
    alias varchar(10)
);

create table person
(
    partner_id uuid primary key,
    first_name varchar(32),
    last_name  varchar(32),
    foreign key (partner_id) references partner (id)
);

create table company
(
    partner_id uuid primary key,
    boss_id uuid not null,
    name varchar(32),
    foreign key (partner_id) references partner (id),
    foreign key (boss_id) references partner (id)
);

create table ngo
(
    partner_id uuid primary key,
    head_id uuid,
    title varchar(32),
    foreign key (partner_id) references partner (id),
    foreign key (head_id) references partner (id)
);
create table collaborators
(
    partner_id      uuid not null,
    collaborator_id uuid not null,
    primary key (partner_id, collaborator_id)
);

drop table if exists resource;

create table resource
(
    id            uuid primary key,
    type          varchar(16)  not null,
    name          varchar(100) not null,
    last_modified timestamp    not null,
    parent_id     uuid,
    size          int,
    foreign key (parent_id) references resource (id)
);

