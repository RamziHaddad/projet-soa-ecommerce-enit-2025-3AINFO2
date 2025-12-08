Workflow du Traitement des Paiements (processPayment)
Le service implémente un flux séquentiel en 4 étapes principales :

1. Validation et Intégrité des Données Avant tout traitement, le service vérifie la conformité de la requête (PaymentRequest) :

Contrôle de surface : Vérification de la présence des champs obligatoires (via les annotations DTO).

Contrôle métier :

Validité de la date d'expiration de la carte (comparaison avec la date système).

Vérification des plafonds de paiement (Rejet si montant > 50 000).

Gestion d'erreur : Une InvalidPaymentException est levée immédiatement si une règle est violée, retournant un code HTTP 400.

2. Persistance Initiale (Audit) Une entité Transaction est créée et sauvegardée en base de données avec le statut PENDING. Cela garantit la traçabilité de la tentative de paiement, même en cas de crash ultérieur.

3. Exécution de la Logique de Paiement Le service simule le traitement bancaire via la méthode processPaymentLogic :

Simulation de latence : Une pause (Thread.sleep) imite le délai de réponse d'un serveur bancaire externe.

Règles d'autorisation :

Refus automatique si le montant dépasse le seuil de sécurité (10 000).

Simulation de défaillance réseau ou bancaire (5% de rejet aléatoire).

Le résultat est un booléen (true pour succès, false pour échec).

4. Orchestration et Communication Inter-services Selon le résultat du traitement :

Mise à jour locale : Le statut de la transaction passe à SUCCESS ou FAILED en base de données.

Appel Distant (OpenFeign) :

En cas de succès, le client OrderClient envoie une requête PUT au microservice Order-Service pour confirmer la commande.

Cet appel est isolé dans un bloc try-catch pour garantir que le paiement reste valide même si la notification au service commande échoue (principe de résilience).
