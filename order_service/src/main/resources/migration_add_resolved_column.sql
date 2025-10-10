-- Migration script to safely add the 'resolved' column to disputes table
-- Run this manually in your database if you have existing data

-- Step 1: Add the column as nullable with default value
ALTER TABLE disputes ADD COLUMN IF NOT EXISTS resolved BOOLEAN DEFAULT FALSE;

-- Step 2: Update all existing rows to have resolved = false
UPDATE disputes SET resolved = FALSE WHERE resolved IS NULL;

-- Step 3: Make the column NOT NULL (optional, but recommended for data integrity)
ALTER TABLE disputes ALTER COLUMN resolved SET NOT NULL;
