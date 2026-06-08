--liquibase formatted sql

--changeset vllad:1.2-create-organization-tag-projection
create table organization_tag_projection (
    tag_id uuid primary key,
    organization_id uuid not null,
    name varchar(255),
    excluded_from_ai boolean not null default false,
    deleted boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_organization_tag_projection_available
    on organization_tag_projection (organization_id, deleted, excluded_from_ai, name);
