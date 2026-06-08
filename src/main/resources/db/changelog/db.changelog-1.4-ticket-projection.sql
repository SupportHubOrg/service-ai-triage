--liquibase formatted sql

--changeset vllad:1.4-create-ticket-analysis-projection
create table ticket_projection (
    ticket_id uuid primary key,
    organization_id uuid not null,
    creator_id uuid,
    subject varchar(255),
    description text,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table ticket_message_projection (
    message_id uuid primary key,
    ticket_id uuid not null,
    organization_id uuid not null,
    sender_id uuid not null,
    sender_type varchar(32) not null,
    message text not null,
    created_at timestamptz not null
);

create index idx_ticket_message_projection_ticket
    on ticket_message_projection (ticket_id, created_at, message_id);

create table ticket_tag_projection (
    ticket_id uuid not null,
    tag_id uuid not null,
    organization_id uuid not null,
    created_at timestamptz not null,
    primary key (ticket_id, tag_id)
);

create index idx_ticket_tag_projection_ticket
    on ticket_tag_projection (ticket_id);

create index idx_ticket_projection_organization
    on ticket_projection (organization_id, ticket_id);
