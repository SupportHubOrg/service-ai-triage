--liquibase formatted sql

--changeset vllad:1.3-create-knowledge-article-rag-projection
create extension if not exists vector;
create extension if not exists hstore;
create extension if not exists "uuid-ossp";

create table if not exists vector_store (
    id uuid default uuid_generate_v4() primary key,
    content text,
    metadata json,
    embedding vector(1536)
);

create index if not exists ix_vector_store_embedding
    on vector_store using hnsw (embedding vector_cosine_ops);

create table knowledge_article_projection (
    article_id uuid primary key,
    organization_id uuid not null,
    draft_id uuid,
    source_ticket_id uuid,
    published_by_id uuid not null,
    title varchar(255) not null,
    article_content text not null,
    content_hash varchar(64) not null,
    chunk_count integer not null,
    status varchar(32) not null,
    approved_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_knowledge_article_projection_organization_status
    on knowledge_article_projection (organization_id, status);

create index idx_knowledge_article_projection_source_ticket
    on knowledge_article_projection (source_ticket_id);

create table knowledge_article_chunk_projection (
    chunk_id uuid primary key,
    article_id uuid not null references knowledge_article_projection (article_id) on delete cascade,
    organization_id uuid not null,
    chunk_index integer not null,
    chunk_count integer not null,
    content text not null,
    content_hash varchar(64) not null,
    vector_document_id uuid not null,
    embedding_model varchar(128) not null,
    embedding_dimensions integer not null,
    index_status varchar(32) not null,
    index_attempts integer not null default 0,
    last_index_error varchar(1000),
    indexed_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

alter table knowledge_article_chunk_projection
    add constraint uq_knowledge_article_chunk_projection_article_index
        unique (article_id, chunk_index);

create index idx_knowledge_article_chunk_projection_article
    on knowledge_article_chunk_projection (article_id, chunk_index);

create index idx_knowledge_article_chunk_projection_index_status
    on knowledge_article_chunk_projection (index_status, updated_at);

create index idx_knowledge_article_chunk_projection_organization
    on knowledge_article_chunk_projection (organization_id, article_id);

create table knowledge_article_tag_projection (
    article_id uuid not null references knowledge_article_projection (article_id) on delete cascade,
    tag_id uuid not null,
    organization_id uuid not null,
    created_at timestamptz not null,
    primary key (article_id, tag_id)
);

create index idx_knowledge_article_tag_projection_organization_tag
    on knowledge_article_tag_projection (organization_id, tag_id);
