create table partner
(
    id    uuid primary key,
    type  char(1) not null,
    alias varchar(10)
);

create table phone
(
    id            	uuid primary key,
    partner_id		uuid not null,
    phone_number 	text not null,
    foreign key (partner_id) references partner (id)
);

create table email
(
    id            	uuid primary key,
    partner_id		uuid not null,
    mail		 	text not null,
    foreign key (partner_id) references partner (id)
);

create table person
(
    id         uuid primary key,
    first_name varchar(32),
    last_name  varchar(32),
    foreign key (id) references partner (id)
);

create table company
(
    id   uuid primary key,
    boss_id uuid,
    name varchar(32),
    foreign key (id) references partner (id),
    foreign key (boss_id) references person (id)
);

create table employment
(
	id   			uuid primary key,
	employer_id 	uuid not null,
	employee_id		uuid not null,
	employment_year	int	not null,
	foreign key (employer_id) references person(id),
	foreign key (employee_id) references person(id)
);

alter table company
    add foreign key (id) references partner (id);

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

