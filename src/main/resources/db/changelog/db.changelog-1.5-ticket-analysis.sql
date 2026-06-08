--liquibase formatted sql

--changeset vllad:1.5-create-ticket-analysis-jobs
create table ticket_analysis (
    analysis_id uuid primary key,
    ticket_id uuid not null,
    organization_id uuid not null,
    status varchar(32) not null,
    attempts integer not null default 0,
    last_error varchar(1000),
    locked_at timestamptz,
    completed_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

alter table ticket_analysis
    add constraint uq_ticket_analysis_ticket unique (ticket_id);

create index idx_ticket_analysis_claim
    on ticket_analysis (status, updated_at, analysis_id);

create index idx_ticket_analysis_ticket
    on ticket_analysis (ticket_id);

create table ticket_analysis_suggested_tag (
    analysis_id uuid not null,
    tag_id uuid not null,
    tag_name varchar(255) not null,
    primary key (analysis_id, tag_id),
    constraint fk_ticket_analysis_suggested_tag_analysis
        foreign key (analysis_id) references ticket_analysis (analysis_id) on delete cascade
);

create index idx_ticket_analysis_suggested_tag_analysis
    on ticket_analysis_suggested_tag (analysis_id);
