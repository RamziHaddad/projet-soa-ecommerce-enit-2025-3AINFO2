# Feedback Service

Microservice de gestion des ratings et commentaires pour l'e-commerce.

## 📚 Documentation

- **[Documentation API complète](./API_DOCUMENTATION.md)** - Guide détaillé pour les équipes Products et Frontend
- **Swagger UI** : `http://localhost:8083/swagger-ui.html` (quand l'application est démarrée)

## Technologies

- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- PostgreSQL
- JWT (jjwt)
- SpringDoc OpenAPI (Swagger)
- RestTemplate (communication inter-microservices)

## Prérequis

- Java 17+
- Maven 3.6+
- PostgreSQL 12+

## Configuration

1. **Installer les prérequis** :
   - Java 17 JDK
   - Maven 3.6+
   - PostgreSQL 12+

2. **Configurer les paramètres dans `application.yml`** :
   - URL de la base de données (déjà configuré : `localhost:5432/feedback_db`)
   - Username/Password PostgreSQL (déjà configuré)
   - JWT secret key
   - URL du microservice Catalog

## Démarrage

```bash
mvn spring-boot:run
```

L'application sera accessible sur `http://localhost:8083`

## Endpoints principaux

- `GET /` - Page d'accueil
- `GET /api/ratings/product/{productId}` - Liste des notes (public)
- `GET /api/ratings/product/{productId}/summary` - Résumé des notes (public)
- `POST /api/ratings` - Créer/mettre à jour une note (JWT requis)
- `DELETE /api/ratings/{ratingId}` - Supprimer une note (JWT requis)

Pour plus de détails, consultez [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)
