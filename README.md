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

2. **Créer la base de données PostgreSQL** :

   **Option A : Via le script PowerShell**
   ```powershell
   .\create_database.ps1
   ```

   **Option B : Via pgAdmin**
   - Ouvrir pgAdmin
   - Clic droit sur "Databases" → "Create" → "Database"
   - Nom : `feedback_db`

   **Option C : Via psql**
   ```powershell
   psql -U postgres -c "CREATE DATABASE feedback_db;"
   ```

3. **Configurer les paramètres dans `application.yml`** :
   - URL de la base de données (déjà configuré : `localhost:5432/feedback_db`)
   - Username/Password PostgreSQL (déjà configuré)
   - JWT secret key
   - URL du microservice Catalog

## Démarrage

**Vérifier d'abord les installations :**
```powershell
java -version    # Doit afficher Java 17+
mvn -version     # Doit afficher Apache Maven (pas Python!)
```

**Lancer le projet :**
```powershell
mvn spring-boot:run
```

**Si Maven n'est pas installé, utiliser un IDE :**
- IntelliJ IDEA : Ouvrir le projet → Maven détecté automatiquement → Run
- Eclipse : Import Maven Project → Run

Le service sera accessible sur `http://localhost:8083`

## Documentation API

Une fois le service démarré, la documentation Swagger est disponible sur :
`http://localhost:8083/swagger-ui.html`

## Structure du projet

```
src/main/java/com/ecommerce/feedback/
├── model/          # Entités JPA (Rating, Comment)
├── repository/     # Repositories Spring Data
├── service/        # Services métier
├── controller/     # Controllers REST
├── security/       # Configuration sécurité JWT
├── dto/           # Data Transfer Objects
├── config/        # Configurations Spring
└── exception/     # Gestion des exceptions
```

