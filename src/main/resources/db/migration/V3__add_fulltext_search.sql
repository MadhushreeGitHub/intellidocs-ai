-- Add tsvector column for full-text search (BM25-equivalent)
ALTER TABLE document_chunks
    ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- GIN index for fast full-text search
-- GIN = Generalized Inverted Index — standard for tsvector columns
CREATE INDEX IF NOT EXISTS idx_chunks_search_vector
    ON document_chunks
    USING GIN (search_vector);

--Populate tsvector for any existing chunks (if any)
UPDATE document_chunks
SET search_vector = to_tsvector('english', content)
WHERE search_vector IS NULL;