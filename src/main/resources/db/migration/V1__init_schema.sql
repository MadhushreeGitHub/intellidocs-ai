-- Enable extensions
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tenants
CREATE TABLE tenants (
                         id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         name        VARCHAR(255) NOT NULL,
                         slug        VARCHAR(100) UNIQUE NOT NULL,
                         plan        VARCHAR(50)  NOT NULL DEFAULT 'FREE',
                         active      BOOLEAN      NOT NULL DEFAULT true,
                         created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                         updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Users
CREATE TABLE users (
                       id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       tenant_id   UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                       email       VARCHAR(255) NOT NULL,
                       password    VARCHAR(255) NOT NULL,
                       full_name   VARCHAR(255),
                       role        VARCHAR(50)  NOT NULL DEFAULT 'ROLE_USER',
                       active      BOOLEAN      NOT NULL DEFAULT true,
                       created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
                       UNIQUE (tenant_id, email)
);

-- Refresh tokens
CREATE TABLE refresh_tokens (
                                id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                token_hash  VARCHAR(512) NOT NULL UNIQUE,
                                expires_at  TIMESTAMP    NOT NULL,
                                created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Documents
CREATE TABLE documents (
                           id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           tenant_id        UUID         NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                           uploaded_by      UUID         NOT NULL REFERENCES users(id),
                           title            VARCHAR(512) NOT NULL,
                           file_name        VARCHAR(512) NOT NULL,
                           file_type        VARCHAR(50)  NOT NULL,
                           file_size_bytes  BIGINT,
                           storage_key      VARCHAR(1024),
                           status           VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
                           page_count       INTEGER,
                           content_hash     VARCHAR(64)  UNIQUE,
                           deleted_at       TIMESTAMP,
                           created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
                           updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Document chunks (RAG)
CREATE TABLE document_chunks (
                                 id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 tenant_id    UUID    NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                                 document_id  UUID    NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
                                 chunk_index  INTEGER NOT NULL,
                                 content      TEXT    NOT NULL,
                                 embedding    vector(1536),
                                 page_number  INTEGER,
                                 token_count  INTEGER,
                                 created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Conversations
CREATE TABLE conversations (
                               id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               tenant_id   UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                               user_id     UUID NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
                               title       VARCHAR(512),
                               created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                               updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Messages
CREATE TABLE messages (
                          id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          conversation_id  UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
                          tenant_id        UUID NOT NULL REFERENCES tenants(id)       ON DELETE CASCADE,
                          role             VARCHAR(20) NOT NULL,
                          content          TEXT        NOT NULL,
                          token_count      INTEGER,
                          created_at       TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- Message sources
CREATE TABLE message_sources (
                                 id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 message_id  UUID  NOT NULL REFERENCES messages(id)        ON DELETE CASCADE,
                                 chunk_id    UUID  NOT NULL REFERENCES document_chunks(id),
                                 score       FLOAT
);

-- Indexes
CREATE INDEX idx_users_tenant       ON users(tenant_id);
CREATE INDEX idx_docs_tenant        ON documents(tenant_id);
CREATE INDEX idx_docs_status        ON documents(status);
CREATE INDEX idx_chunks_document    ON document_chunks(document_id);
CREATE INDEX idx_chunks_tenant      ON document_chunks(tenant_id);
CREATE INDEX idx_convs_user         ON conversations(user_id);
CREATE INDEX idx_msgs_conversation  ON messages(conversation_id);

-- HNSW vector index
--CREATE INDEX idx_chunks_embedding
  --  ON document_chunks
    --USING hnsw (embedding vector_cosine_ops)
    --WITH (m = 16, ef_construction = 64);

-- Tell Flyway this migration was already applied manually
--INSERT INTO flyway_schema_history
--(installed_rank, version, description, type, script,
 --checksum, installed_by, installed_on, execution_time, success)
--VALUES
  --  (1, '1', 'init schema', 'SQL', 'V1__init_schema.sql',
    -- 0, 'postgres', NOW(), 100, true);