DROP TABLE IF EXISTS authors
CREATE TABLE authors(id SERIAL PRIMARY KEY, name TEXT NOT NULL)
INSERT INTO authors(id,name) VALUES (1, 'Homer')
INSERT INTO authors(id,name) VALUES (2, 'Vern')
INSERT INTO authors(id,name) VALUES (3, 'Dlugi')
INSERT INTO authors(id,name) VALUES (4, 'Kahneman')
DROP TABLE IF EXISTS books
CREATE TABLE books(id SERIAL PRIMARY KEY, author int references authors(id) , title TEXT NOT NULL)
INSERT INTO books(id,author, title) VALUES (1, 3, 'Slovník')
INSERT INTO books(id,author, title) VALUES (2, 4, 'Thinking fast and slow')
INSERT INTO books(id,author, title) VALUES (3, 4, 'Attention and Effort')
