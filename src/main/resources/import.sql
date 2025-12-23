-- Produits mockés
INSERT INTO products (id, name, description, category, created_at) VALUES 
(1, 'Laptop Gaming', 'Ordinateur portable gaming haute performance', 'Electronique', CURRENT_TIMESTAMP),
(2, 'Smartphone', 'Smartphone Android dernière génération', 'Electronique', CURRENT_TIMESTAMP),
(3, 'Casque Audio', 'Casque sans fil avec réduction de bruit', 'Audio', CURRENT_TIMESTAMP),
(4, 'Livre Programmation', 'Livre sur Java et Quarkus', 'Livres', CURRENT_TIMESTAMP),
(5, 'Souris Gaming', 'Souris ergonomique pour gaming', 'Informatique', CURRENT_TIMESTAMP),
(6, 'Clavier Mécanique', 'Clavier mécanique RGB', 'Informatique', CURRENT_TIMESTAMP),
(7, 'Monitor 4K', 'Ecran 4K 27 pouces', 'Electronique', CURRENT_TIMESTAMP),
(8, 'Webcam HD', 'Webcam 1080p pour visioconférence', 'Informatique', CURRENT_TIMESTAMP),
(9, 'Livre IA', 'Introduction à lintelligence artificielle', 'Livres', CURRENT_TIMESTAMP),
(10, 'Enceinte Bluetooth', 'Enceinte portable étanche', 'Audio', CURRENT_TIMESTAMP),
(11, 'Tablette Graphique', 'Tablette pour dessin numérique', 'Informatique', CURRENT_TIMESTAMP),
(12, 'Montre Connectée', 'Montre intelligente avec capteurs', 'Electronique', CURRENT_TIMESTAMP);

-- Commandes mockées
INSERT INTO orders (id, user_id, order_date) VALUES 
(1, 1, '2024-01-15 10:30:00'),
(2, 2, '2024-01-16 14:20:00'),
(3, 1, '2024-01-20 09:15:00'),
(4, 3, '2024-01-22 16:45:00');

-- Items des commandes
INSERT INTO order_items (order_id, product_id, quantity) VALUES 
(1, 1, 1), (1, 3, 2),
(2, 2, 1), (2, 5, 1), (2, 6, 1),
(3, 7, 1), (3, 8, 1),
(4, 1, 1), (4, 4, 1);

-- Notes/ratings mockés
INSERT INTO ratings (user_id, product_id, rating, created_at) VALUES 
(1, 1, 5, CURRENT_TIMESTAMP),
(1, 3, 4, CURRENT_TIMESTAMP),
(2, 2, 5, CURRENT_TIMESTAMP),
(2, 5, 3, CURRENT_TIMESTAMP),
(2, 6, 4, CURRENT_TIMESTAMP),
(3, 1, 5, CURRENT_TIMESTAMP),
(3, 4, 4, CURRENT_TIMESTAMP),
(1, 7, 5, CURRENT_TIMESTAMP),
(2, 1, 4, CURRENT_TIMESTAMP),
(3, 3, 5, CURRENT_TIMESTAMP),
(1, 5, 4, CURRENT_TIMESTAMP);
(2, 4, 5, CURRENT_TIMESTAMP);