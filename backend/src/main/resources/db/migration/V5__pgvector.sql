-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Convert embedding column from TEXT to vector(1024)
-- mxbai-embed-large produces 1024-dimensional embeddings
-- The existing JSON format [0.1, 0.2, ...] is compatible with pgvector's text input
ALTER TABLE notes ALTER COLUMN embedding TYPE vector(1024) USING embedding::vector;

-- Create HNSW index for fast approximate cosine similarity search
-- vector_cosine_ops corresponds to the <=> (cosine distance) operator
CREATE INDEX IF NOT EXISTS idx_notes_embedding_hnsw ON notes USING hnsw (embedding vector_cosine_ops);
