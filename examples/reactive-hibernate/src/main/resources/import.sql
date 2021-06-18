DROP TABLE IF EXISTS books
CREATE TABLE books(id SERIAL PRIMARY KEY, title TEXT NOT NULL)
INSERT INTO books(id,title) VALUES (1, 'Slovník')
INSERT INTO books(id,title) VALUES (2, 'Thinking fast and slow')
DROP TABLE IF EXISTS authors
CREATE TABLE authors(id SERIAL PRIMARY KEY, name TEXT NOT NULL)
INSERT INTO authors(id,name) VALUES (1, 'Homer')
INSERT INTO authors(id,name) VALUES (2, 'Vern')
