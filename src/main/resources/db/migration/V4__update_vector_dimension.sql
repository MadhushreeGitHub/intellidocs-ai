-- Switch from mock 1536-dim to nomic-embed-text 768-dim
-- Wipe existing mock vectors first
TRUNCATE document_chunks CASCADE;

-- Change vector column dimension
ALTER TABLE document_chunks
ALTER COLUMN embedding TYPE vector(768);

-- Recreate HNSW index for new dimension
DROP INDEX IF EXISTS idx_chunks_embedding;
CREATE INDEX idx_chunks_embedding
    ON document_chunks
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);