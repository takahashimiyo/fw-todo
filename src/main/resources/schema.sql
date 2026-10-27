CREATE TABLE IF NOT EXISTS todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) CHARACTER SET utf8mb4;

INSERT INTO todos (title, category, priority, completed)
SELECT 'todo-06', 'プログラミング', 2, FALSE
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
