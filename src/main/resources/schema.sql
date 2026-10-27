CREATE TABLE IF NOT EXISTS todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at DATETIME NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) CHARACTER SET utf8mb4;

-- Existing databases are updated safely when the app starts.
SET @deleted_at_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'todos'
      AND column_name = 'deleted_at'
);
SET @deleted_at_ddl = IF(
    @deleted_at_exists = 0,
    'ALTER TABLE todos ADD COLUMN deleted_at DATETIME NULL',
    'SELECT 1'
);
PREPARE deleted_at_stmt FROM @deleted_at_ddl;
EXECUTE deleted_at_stmt;
DEALLOCATE PREPARE deleted_at_stmt;

INSERT INTO todos (title, category, priority, completed, completed_at)
SELECT 'todo-06', 'プログラミング', 2, TRUE, '2026-04-05 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE title = 'todo-06');
INSERT INTO todos (title, category, priority, completed)
SELECT 'todo-07', 'プログラミング', 2, FALSE
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE title = 'todo-07');
INSERT INTO todos (title, category, priority, completed)
SELECT 'todo-08', 'プログラミング', 2, FALSE
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE title = 'todo-08');
INSERT INTO todos (title, category, priority, completed)
SELECT 'todo-09', 'プログラミング', 2, FALSE
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE title = 'todo-09');
INSERT INTO todos (title, category, priority, completed)
SELECT 'todo-10', 'プログラミング', 2, FALSE
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE title = 'todo-10');
INSERT INTO todos (title, category, priority, completed)
SELECT 'todo-11', 'プログラミング', 2, FALSE
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE title = 'todo-11');
INSERT INTO todos (title, category, priority, completed)
SELECT 'todo-12', 'プログラミング', 2, FALSE
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE title = 'todo-12');
INSERT INTO todos (title, category, priority, completed)
SELECT 'todo-13', 'プログラミング', 2, FALSE
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE title = 'todo-13');
INSERT INTO todos (title, category, priority, completed)
SELECT 'todo-14', 'プログラミング', 2, FALSE
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE title = 'todo-14');
INSERT INTO todos (title, category, priority, completed)
SELECT 'todo-15', 'プログラミング', 2, FALSE
WHERE NOT EXISTS (SELECT 1 FROM todos WHERE title = 'todo-15');
