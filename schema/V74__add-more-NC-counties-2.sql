INSERT INTO county (name, state) VALUES
('Alamance', 'NC'),
('Davidson', 'NC'),
('Forsyth', 'NC'),
('Guilford', 'NC'),
('Montgomery', 'TN'),
('Caswell', 'NC'),
('Moore', 'NC'),
('Rockingham', 'NC'),
('Orange', 'NC'),
('Chatham', 'NC'),
('Durham', 'NC'),
('Lincoln', 'GA')
ON CONFLICT DO NOTHING;