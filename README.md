## Instructions de Configuration

### 1. Configuration de la Base de Données
#### Créez une base de données MySQL 

CREATE DATABASE payment_db;


### 2. Configurer les Identifiants de la Base de Données

#### Utiliser des variables d’environnement
Définissez les variables d’environnement avant de lancer l’application :

Windows (PowerShell) :

$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
.\mvnw.cmd spring-boot:run


## Logique Métier (Payment Workflow)

Le service implémente un flux de traitement séquentiel en 4 étapes via la méthode principale `processPayment` :

### 1. Validation et Intégrité des Données
Avant tout traitement, le service vérifie la conformité de la requête (`PaymentRequest`) :
* **Contrôle de surface :** Vérification des champs obligatoires (via les annotations de validation).
* **Contrôle métier :**
  * Validité de la date d'expiration de la carte (comparaison avec la date système).
  * Vérification des plafonds (Rejet immédiat si montant > 50 000).
* **Gestion d'erreur :** Une `InvalidPaymentException` est levée si une règle est violée (Code HTTP 400).

### 2. Persistance Initiale (Audit)
Une entité `Transaction` est créée et sauvegardée en base de données avec le statut **PENDING**. Cela garantit la traçabilité de la tentative, même en cas de défaillance ultérieure du système.

### 3. Exécution de la Logique Bancaire (Simulation)
Le service simule le traitement via la méthode `processPaymentLogic` :
* **Latence réseau :** Une pause (`Thread.sleep`) imite le délai d'un serveur bancaire.
* **Règles d'autorisation :**
  * **Plafond de sécurité :** Refus automatique si montant > 10 000.
  * **Simulation d'incidents :** 5% de rejet aléatoire pour simuler des pannes réseaux ou refus bancaires.

### 4. Orchestration et Communication (OpenFeign)
Selon le résultat du traitement :
* **Mise à jour locale :** Le statut de la transaction passe à `SUCCESS` ou `FAILED` dans MySQL.
* **Appel Distant :**
  * En cas de succès, le client **Feign** (`OrderClient`) envoie une requête `PUT` au microservice **Order-Service** pour confirmer la commande.
  * Cet appel est protégé par un bloc `try-catch` (Resilience Pattern) pour ne pas invalider un paiement réussi en cas d'indisponibilité du service commande.
## Microservices Patterns Implemented
### 1. Retry Pattern (Resilience4j)
Automatically retries failed calls to the Order Service up to 3 times with exponential backoff (2s, 4s, 8s). Configured in application.properties and used via @Retry annotation in PaymentService.
### 2. Circuit Breaker Pattern (Resilience4j)
Prevents cascading failures by opening the circuit after 50% failure rate in a 10-request window. Protects the Order Service integration with automatic fallback handling.
### 3. Distributed Tracing (Micrometer Tracing + Zipkin)
Tracks requests across services with unique trace IDs and span IDs. All logs include trace context for debugging distributed transactions. Sends trace data to Zipkin for visualization.
### 4. API Gateway Integration (OpenFeign)
Uses declarative REST client (OrderClient) for inter-service communication with automatic serialization/deserialization and load balancing support.
### 5. Saga Pattern (Compensating Transactions)
Implements distributed transaction management - if order confirmation fails after payment, the payment is automatically refunded and marked as REFUNDED in the database.
### 6. Idempotency Pattern
Prevents duplicate payment processing using unique requestId. Each payment request is checked against existing transactions before processing.

