-- Add embedding column to notes table for similarity-based clustering
ALTER TABLE notes ADD COLUMN embedding TEXT;

-- Add subCategory column for hierarchical clustering
ALTER TABLE notes ADD COLUMN subCategory VARCHAR(255);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_notes_subject ON notes(subject);
CREATE INDEX IF NOT EXISTS idx_notes_user_subject ON notes(userId, subject);
CREATE INDEX IF NOT EXISTS idx_notes_subcategory ON notes(subCategory);
CREATE INDEX IF NOT EXISTS idx_notes_date ON notes(date);
