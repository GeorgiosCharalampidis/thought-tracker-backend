-- Add embedding column to notes table for similarity-based clustering
ALTER TABLE notes ADD COLUMN embedding TEXT;

-- Add subCategory column for hierarchical clustering
ALTER TABLE notes ADD COLUMN subcategory VARCHAR(255);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_notes_category ON notes(category);
CREATE INDEX IF NOT EXISTS idx_notes_user_category ON notes(user_id, category);
CREATE INDEX IF NOT EXISTS idx_notes_subcategory ON notes(subcategory);
CREATE INDEX IF NOT EXISTS idx_notes_date ON notes(date);
