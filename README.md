# Feedback Service

Microservice de gestion des ratings et commentaires pour l'e-commerce.

## Technologies

- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- PostgreSQL
- JWT (jjwt)
- SpringDoc OpenAPI (Swagger)

## Prérequis

- Java 17+
- Maven 3.6+
- PostgreSQL 12+

## Configuration

1. **Installer les prérequis** (voir `SETUP_GUIDE.md` pour instructions détaillées) :
   - Java 17 JDK
   - Maven 3.6+
   - PostgreSQL 12+


3. **Configurer les paramètres dans `application.yml`** :
   - URL de la base de données (déjà configuré : `localhost:5432/feedback_db`)
   - Username/Password PostgreSQL (déjà configuré)
   - JWT secret key
   - URL du microservice Catalog
