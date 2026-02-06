INSERT IGNORE INTO franchises (name) VALUES ('McDonald''s');
INSERT IGNORE INTO franchises (name) VALUES ('Burger King');
INSERT IGNORE INTO franchises (name) VALUES ('Subway');

INSERT IGNORE INTO branches (franchise_id, name)
SELECT id, 'McDonald''s Centro' FROM franchises WHERE name = 'McDonald''s';

INSERT IGNORE INTO branches (franchise_id, name)
SELECT id, 'McDonald''s Norte' FROM franchises WHERE name = 'McDonald''s';

INSERT IGNORE INTO branches (franchise_id, name)
SELECT id, 'Burger King Mall' FROM franchises WHERE name = 'Burger King';

INSERT IGNORE INTO branches (franchise_id, name)
SELECT id, 'Subway Estación' FROM franchises WHERE name = 'Subway';

INSERT IGNORE INTO products (branch_id, name, stock)
SELECT b.id, 'Big Mac', 50
FROM branches b
JOIN franchises f ON f.id = b.franchise_id
WHERE f.name = 'McDonald''s' AND b.name = 'McDonald''s Centro';

INSERT IGNORE INTO products (branch_id, name, stock)
SELECT b.id, 'Papas Fritas', 120
FROM branches b
JOIN franchises f ON f.id = b.franchise_id
WHERE f.name = 'McDonald''s' AND b.name = 'McDonald''s Centro';

INSERT IGNORE INTO products (branch_id, name, stock)
SELECT b.id, 'McPollo', 40
FROM branches b
JOIN franchises f ON f.id = b.franchise_id
WHERE f.name = 'McDonald''s' AND b.name = 'McDonald''s Norte';

INSERT IGNORE INTO products (branch_id, name, stock)
SELECT b.id, 'Whopper', 35
FROM branches b
JOIN franchises f ON f.id = b.franchise_id
WHERE f.name = 'Burger King' AND b.name = 'Burger King Mall';

INSERT IGNORE INTO products (branch_id, name, stock)
SELECT b.id, 'Onion Rings', 80
FROM branches b
JOIN franchises f ON f.id = b.franchise_id
WHERE f.name = 'Burger King' AND b.name = 'Burger King Mall';

INSERT IGNORE INTO products (branch_id, name, stock)
SELECT b.id, 'Sub de Pollo', 25
FROM branches b
JOIN franchises f ON f.id = b.franchise_id
WHERE f.name = 'Subway' AND b.name = 'Subway Estación';

INSERT IGNORE INTO products (branch_id, name, stock)
SELECT b.id, 'Sub de Atún', 20
FROM branches b
JOIN franchises f ON f.id = b.franchise_id
WHERE f.name = 'Subway' AND b.name = 'Subway Estación';

