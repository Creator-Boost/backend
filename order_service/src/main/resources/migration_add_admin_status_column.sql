-- Migration script to safely add the 'admin_status' column to orders table
-- Run this manually in your database if you have existing data

-- Step 1: Add the column as nullable with default value
ALTER TABLE orders ADD COLUMN IF NOT EXISTS admin_status VARCHAR(255) DEFAULT 'PENDING';

-- Step 2: Update all existing rows to have admin_status = 'PENDING'
UPDATE orders SET admin_status = 'PENDING' WHERE admin_status IS NULL;

-- Step 3: Make the column NOT NULL (optional, but recommended for data integrity)
ALTER TABLE orders ALTER COLUMN admin_status SET NOT NULL;
