# catalogue

📦 Catalogue Service – SOA E-commerce
📖 Description

Le Catalogue Service est un microservice REST responsable de la gestion des produits du catalogue dans une application e-commerce basée sur une architecture SOA / Microservices.

Il permet de gérer les produits
🚀 Démarrage Rapide
Prérequis

Java 17+
Maven 3.8+
PostgreSQL

Installation

Créer la base de données

sqlCREATE DATABASE Catalog;

Lancer l'application

bash./mvnw quarkus:dev
L'application démarre sur http://localhost:8083
📋 API Endpoints
Produits
MéthodeEndpointDescriptionGET/api/productsListe tous les produitsGET
/api/products/{id}Récupère un produit
POST/api/productsCrée un produit
PUT/api/products/{id}Met à jour un produitP
UT/api/products/{id}/priceMet à jour le prix
DELETE/api/products/{id}Supprime un produit

#🐛 Dépannage
Les événements ne se traitent pas ?

Vérifier que le service d'indexation tourne sur le port 8082
Consulter les logs : ./mvnw quarkus:dev
Vérifier les événements en attente : GET /api/outbox/pending

Erreur de connexion à la DB ?

Vérifier que PostgreSQL est démarré
Vérifier les credentials dans application.properties

📦 Technologies

Quarkus - Framework Java
PostgreSQL - Base de données
Hibernate - ORM
JAX-RS - API REST