## 🧠 Logique Métier (Payment Workflow)

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

