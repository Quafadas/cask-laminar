CREATE TABLE IF NOT EXISTS todos (
	todo_id serial primary KEY,
    description text not null, 
    completed boolean not null
);