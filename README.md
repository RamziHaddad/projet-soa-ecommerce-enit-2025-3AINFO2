# MICROSERVICE INVENTAIRE 
Le **Service Stock** est une composante critique de l'architecture microservices e-commerce. Il assure l’**intégrité des niveaux de stock** et garantit la **disponibilité des produits** tout au long du cycle de commande, de la consultation du catalogue à la validation du paiement.

---

## 1. SCÉNARIO GLOBAL DE GESTION DU STOCK

La logique métier repose exclusivement sur des appels **synchrones** entre services. Aucune communication asynchrone (événements, messages) n’est utilisée.

### 1.1. Consultation et Panier
* Le **Catalogue** et le **Service Panier** interroge le Stock en lecture seule via une API pour obtenir la quantité **disponible en temps réel**.
* Aucune réservation n’est effectuée à ce stade : le stock affiché reflète uniquement la disponibilité immédiate.

### 1.2. Sécurisation de la Commande
1.  Le **Service Commande** appelle le Stock pour vérifier la disponibilité au moment de la création de la commande.
2.  Si le stock est suffisant, il effectue un appel **synchrone** pour **réserver fermement** la quantité requise. Cette opération bloque immédiatement les unités concernées jusqu’à la finalisation ou l’annulation de la commande.

### 1.3. Déstockage et Finalisation
* **Paiement Réussi** : Le Service Commande appelle le Stock pour **confirmer la vente**. Le stock est alors décrémenté définitivement et la réservation est levée.
* **Paiement Échoué ou Annulation** : Le Service Commande appelle le Stock pour **annuler la réservation**. Les unités sont immédiatement rendues disponibles.

| Étape | Action Moteur | Impact Stock |
| :--- | :--- | :--- |
| Consultation | Lecture de disponibilité | Aucune modification. |
| Validation Commande | Réservation Ferme | Stock réservé (état interne modifié). |
| Paiement OK | Confirmation Vente | Quantité décrémentée définitivement. |
| Paiement KO | Annulation Réservation | Réservation annulée, stock rendu disponible. |

---

## 2. RESPONSABILITÉS MÉTIER PRINCIPALES

Le Service Stock détient l’autorité exclusive sur :

1.  **État du Stock** : Maintenir la quantité disponible et réservée par produit (et éventuellement par entrepôt).
2.  **Réservations Fermes** : Allouer ou libérer des quantités réservées via des appels synchrones.
3.  **Mouvements d’Inventaire** : Gérer les réassortiments (entrées) et les ventes (sorties).
4.  **Disponibilité en Temps Réel** : Fournir une vue cohérente et à jour du stock disponible.
5.  **Audit et Traçabilité** : Enregistrer un historique immuable de toutes les opérations (réservations, annulations, ventes, réassort).
6.  **Alertes Stock Bas** : Renvoyer des indicateurs de seuil critique via l’API d’administration.


## 2. ARCHITECTURE DE MS

Voici une justification **stricte et simple** de l’architecture hexagonale pour le Service Stock :

- Le domaine métier est **isolé de toute technologie externe** (HTTP, base de données, etc.).  
- La logique métier est **testable sans infrastructure** (ni base de données, ni serveur).  
- Les interactions (API, stockage) sont **modélisées comme des interfaces** (ports), implémentées par des adaptateurs.  
- Ajouter ou remplacer une technologie (cache, DB, protocole) **ne modifie pas le cœur métier**.  
- Le code exprime **clairement les règles métier** sans pollution technique.  
- Architecture **stable, évolutive et conforme aux principes de microservice** : faible couplage, haute cohésion.
