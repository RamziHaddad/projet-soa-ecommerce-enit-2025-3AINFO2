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
## Patrons de Microservices Implémentés
### 1. Pattern de Retry (Resilience4j)

Répète automatiquement les appels échoués au service Order jusqu’à 3 fois avec un backoff exponentiel (2s, 4s, 8s). Configuré dans application.properties et utilisé via l’annotation @Retry dans PaymentService.

### 2. Pattern de Circuit Breaker (Resilience4j)

Prévient les défaillances en cascade en ouvrant le circuit après un taux d’échec de 50% sur une fenêtre de 10 requêtes. Protège l’intégration avec le service Order grâce à une gestion automatique de fallback.

### 3. Tracing Distribué (Micrometer Tracing + Zipkin)

Suit les requêtes à travers les services avec des IDs de trace et de span uniques. Tous les logs incluent le contexte de trace pour le débogage des transactions distribuées. Envoie les données de trace à Zipkin pour visualisation.

### 4. Intégration API Gateway (OpenFeign)

Utilise un client REST déclaratif (OrderClient) pour la communication entre services avec sérialisation/désérialisation automatique et support de load balancing.

### 5. Pattern Saga (Transactions Compensatoires)

Implémente la gestion des transactions distribuées : si la confirmation de commande échoue après le paiement, le paiement est automatiquement remboursé et marqué comme REFUNDED dans la base de données.

### 6. Pattern d’Idempotence

Empêche le traitement en double des paiements en utilisant un requestId unique. Chaque requête de paiement est vérifiée par rapport aux transactions existantes avant traitement.
