# Documentation API - Feedback Service



**URL de base** : `http://localhost:8083`  
**Port** : `8083`  
**Documentation Swagger** : `http://localhost:8083/swagger-ui.html`

---

##  Authentification

### JWT (JSON Web Token)

Les endpoints de **création** et **suppression** nécessitent un token JWT valide dans le header `Authorization`.

**Format** :
```
Authorization: Bearer <votre-token-jwt>
```

Le token JWT doit contenir un claim `userId` qui identifie l'utilisateur.

**Exemple** :
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

##  Endpoints disponibles

### 1. Page d'accueil

**GET** `/`

Retourne les informations générales du service.

**Authentification** : Non requise

**Réponse** :
```json
{
  "service": "Feedback Service",
  "version": "1.0.0",
  "status": "running",
  "documentation": "/swagger-ui.html",
  "endpoints": {
    "GET /api/ratings/product/{productId}": "Liste des notes d'un produit (public)",
    "GET /api/ratings/product/{productId}/summary": "Résumé des notes (public)",
    "POST /api/ratings": "Créer/mettre à jour une note (JWT requis)",
    "DELETE /api/ratings/{ratingId}": "Supprimer une note (JWT requis)"
  }
}
```

---

### 2. Créer ou mettre à jour une note

**POST** `/api/ratings`

Crée une nouvelle note ou met à jour la note existante d'un utilisateur pour un produit.

**Authentification** : ✅ **Requis (JWT)**

**Paramètres** :
- **Body** (JSON) :
  ```json
  {
    "productId": 123,
    "score": 5
  }
  ```

**Validation** :
- `productId` : Obligatoire, doit être un entier positif
- `score` : Obligatoire, doit être entre 1 et 5 (inclus)

**Comportement** :
- Si l'utilisateur n'a jamais noté ce produit → **Création** d'une nouvelle note
- Si l'utilisateur a déjà noté ce produit → **Mise à jour** de la note existante
- Le service vérifie automatiquement que le produit existe dans le service Catalog

**Réponse** :
- **201 Created** :
  ```json
  {
    "id": 1,
    "productId": 123,
    "userId": 456,
    "score": 5,
    "createdAt": "2025-11-30T20:00:00",
    "updatedAt": "2025-11-30T20:00:00"
  }
  ```

**Codes d'erreur** :
- **400 Bad Request** : Données invalides (score hors limites, productId manquant)
- **401 Unauthorized** : Token JWT manquant ou invalide
- **404 Not Found** : Produit non trouvé dans le service Catalog
- **500 Internal Server Error** : Erreur serveur

**Exemple de requête** :
```bash
curl -X POST http://localhost:8083/api/ratings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "productId": 123,
    "score": 5
  }'
```

---

### 3. Récupérer toutes les notes d'un produit

**GET** `/api/ratings/product/{productId}`

Retourne la liste complète de toutes les notes pour un produit donné, triées par date de création (plus récentes en premier).

**Authentification** : ❌ **Non requise (Public)**

**Paramètres** :
- **Path** :
  - `productId` (Long) : L'identifiant du produit

**Réponse** :
- **200 OK** :
  ```json
  [
    {
      "id": 3,
      "productId": 123,
      "userId": 789,
      "score": 4,
      "createdAt": "2025-11-30T21:00:00",
      "updatedAt": "2025-11-30T21:00:00"
    },
    {
      "id": 1,
      "productId": 123,
      "userId": 456,
      "score": 5,
      "createdAt": "2025-11-30T20:00:00",
      "updatedAt": "2025-11-30T20:00:00"
    }
  ]
  ```

**Codes d'erreur** :
- **200 OK** : Retourne une liste vide `[]` si aucune note n'existe pour ce produit

**Exemple de requête** :
```bash
curl http://localhost:8083/api/ratings/product/123
```

---

### 4. Récupérer le résumé des notes d'un produit

**GET** `/api/ratings/product/{productId}/summary`

Retourne la moyenne et le nombre total de notes pour un produit. **Cet endpoint est optimisé pour être appelé fréquemment** (par exemple, lors de l'affichage d'une liste de produits).

**Authentification** : ❌ **Non requise (Public)**

**Paramètres** :
- **Path** :
  - `productId` (Long) : L'identifiant du produit

**Réponse** :
- **200 OK** :
  ```json
  {
    "productId": 123,
    "average": 4.5,
    "ratingsCount": 10
  }
  ```

**Détails** :
- `average` : Moyenne des notes (arrondie à 2 décimales)
- `ratingsCount` : Nombre total de notes pour ce produit
- Si aucune note n'existe : `average = 0.0` et `ratingsCount = 0`

**Exemple de requête** :
```bash
curl http://localhost:8083/api/ratings/product/123/summary
```

---

### 5. Supprimer une note

**DELETE** `/api/ratings/{ratingId}`

Supprime une note. **Seul le propriétaire de la note peut la supprimer**.

**Authentification** : ✅ **Requis (JWT)**

**Paramètres** :
- **Path** :
  - `ratingId` (Long) : L'identifiant de la note à supprimer

**Réponse** :
- **204 No Content** : Note supprimée avec succès (pas de body)

**Codes d'erreur** :
- **401 Unauthorized** : Token JWT manquant ou invalide
- **403 Forbidden** : L'utilisateur n'est pas le propriétaire de la note
- **404 Not Found** : Note non trouvée
- **500 Internal Server Error** : Erreur serveur

**Exemple de requête** :
```bash
curl -X DELETE http://localhost:8083/api/ratings/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## Exemples d'utilisation par équipe

### Pour l'équipe **Products Service**

#### Scénario : Afficher la note moyenne d'un produit

Quand votre service Products affiche un produit, vous devez récupérer la note moyenne pour l'afficher à côté du produit.

**Endpoint à appeler** :
```
GET /api/ratings/product/{productId}/summary
```

**Exemple d'intégration** :

```java
// Dans votre ProductService ou ProductController
@Autowired
private RestTemplate restTemplate;

public ProductWithRatingDTO getProductWithRating(Long productId) {
    // 1. Récupérer le produit depuis votre base de données
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException(productId));
    
    // 2. Appeler le Feedback Service pour récupérer le résumé des notes
    String feedbackServiceUrl = "http://localhost:8083";
    String url = feedbackServiceUrl + "/api/ratings/product/" + productId + "/summary";
    
    try {
        ResponseEntity<ProductRatingSummaryResponse> response = restTemplate.getForEntity(
            url, 
            ProductRatingSummaryResponse.class
        );
        
        ProductRatingSummaryResponse ratingSummary = response.getBody();
        
        // 3. Construire la réponse avec les informations du produit et la note
        return ProductWithRatingDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .averageRating(ratingSummary.getAverage())
            .ratingsCount(ratingSummary.getRatingsCount())
            .build();
            
    } catch (Exception e) {
        // En cas d'erreur, retourner le produit sans note
        log.warn("Impossible de récupérer les notes pour le produit {}: {}", productId, e.getMessage());
        return ProductWithRatingDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .averageRating(0.0)
            .ratingsCount(0L)
            .build();
    }
}
```

**DTO à créer dans votre service** :
```java
public class ProductRatingSummaryResponse {
    private Long productId;
    private Double average;
    private Long ratingsCount;
    // getters/setters
}
```

**Exemple de réponse complète** :
```json
{
  "id": 123,
  "name": "iPhone 15 Pro",
  "price": 1299.99,
  "averageRating": 4.5,
  "ratingsCount": 10
}
```

---

### Pour l'équipe **Frontend**

#### Scénario 1 : Afficher la note moyenne sur la page produit

**Endpoint** : `GET /api/ratings/product/{productId}/summary`

**Exemple avec JavaScript/React** :

```javascript
// Fonction pour récupérer le résumé des notes
async function getProductRatingSummary(productId) {
  try {
    const response = await fetch(
      `http://localhost:8083/api/ratings/product/${productId}/summary`
    );
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Erreur lors de la récupération des notes:', error);
    return { average: 0.0, ratingsCount: 0 };
  }
}

// Utilisation dans un composant React
function ProductCard({ productId }) {
  const [rating, setRating] = useState({ average: 0, ratingsCount: 0 });
  
  useEffect(() => {
    getProductRatingSummary(productId).then(setRating);
  }, [productId]);
  
  return (
    <div>
      <h2>Produit {productId}</h2>
      <div>
        ⭐ {rating.average.toFixed(1)} ({rating.ratingsCount} avis)
      </div>
    </div>
  );
}
```

---

#### Scénario 2 : Permettre à un utilisateur de noter un produit

**Endpoint** : `POST /api/ratings`

**Exemple avec JavaScript/React** :

```javascript
// Fonction pour créer/mettre à jour une note
async function submitRating(productId, score, jwtToken) {
  try {
    const response = await fetch('http://localhost:8083/api/ratings', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${jwtToken}`
      },
      body: JSON.stringify({
        productId: productId,
        score: score
      })
    });
    
    if (response.ok) {
      const data = await response.json();
      console.log('Note enregistrée:', data);
      return data;
    } else {
      const error = await response.json();
      throw new Error(error.message || 'Erreur lors de l\'enregistrement de la note');
    }
  } catch (error) {
    console.error('Erreur:', error);
    throw error;
  }
}

// Composant React pour noter un produit
function RatingForm({ productId, jwtToken, onRatingSubmitted }) {
  const [selectedScore, setSelectedScore] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedScore) {
      setError('Veuillez sélectionner une note');
      return;
    }
    
    setLoading(true);
    setError(null);
    
    try {
      await submitRating(productId, selectedScore, jwtToken);
      onRatingSubmitted();
      alert('Votre note a été enregistrée avec succès !');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label>Notez ce produit :</label>
        {[1, 2, 3, 4, 5].map(score => (
          <button
            key={score}
            type="button"
            onClick={() => setSelectedScore(score)}
            className={selectedScore === score ? 'selected' : ''}
          >
            ⭐ {score}
          </button>
        ))}
      </div>
      {error && <div className="error">{error}</div>}
      <button type="submit" disabled={loading || !selectedScore}>
        {loading ? 'Envoi...' : 'Envoyer la note'}
      </button>
    </form>
  );
}
```

---

#### Scénario 3 : Afficher toutes les notes d'un produit

**Endpoint** : `GET /api/ratings/product/{productId}`

```javascript
// Fonction pour récupérer toutes les notes
async function getAllRatings(productId) {
  try {
    const response = await fetch(
      `http://localhost:8083/api/ratings/product/${productId}`
    );
    const ratings = await response.json();
    return ratings;
  } catch (error) {
    console.error('Erreur:', error);
    return [];
  }
}

// Composant pour afficher les avis
function ProductReviews({ productId }) {
  const [ratings, setRatings] = useState([]);
  
  useEffect(() => {
    getAllRatings(productId).then(setRatings);
  }, [productId]);
  
  return (
    <div>
      <h3>Avis clients ({ratings.length})</h3>
      {ratings.map(rating => (
        <div key={rating.id} className="review">
          <div>
            {Array(rating.score).fill('⭐').join('')}
            {Array(5 - rating.score).fill('☆').join('')}
          </div>
          <div>Par utilisateur #{rating.userId}</div>
          <div>{new Date(rating.createdAt).toLocaleDateString()}</div>
        </div>
      ))}
      {ratings.length === 0 && <p>Aucun avis pour le moment.</p>}
    </div>
  );
}
```

---

## 📊 Modèles de données

### CreateRatingRequest
```json
{
  "productId": 123,
  "score": 5
}
```

### RatingResponse
```json
{
  "id": 1,
  "productId": 123,
  "userId": 456,
  "score": 5,
  "createdAt": "2025-11-30T20:00:00",
  "updatedAt": "2025-11-30T20:00:00"
}
```

### ProductRatingSummaryResponse
```json
{
  "productId": 123,
  "average": 4.5,
  "ratingsCount": 10
}
```

### ErrorResponse
```json
{
  "timestamp": "2025-11-30T20:00:00",
  "status": 404,
  "error": "Product Not Found",
  "message": "Produit avec l'id 123 n'a pas été trouvé dans le service Catalog"
}
```

---

##  Gestion des erreurs

### Codes HTTP

| Code | Signification | Exemple |
|------|---------------|---------|
| 200 | Succès | Liste des notes récupérée |
| 201 | Créé | Note créée avec succès |
| 204 | Pas de contenu | Note supprimée |
| 400 | Requête invalide | Score hors limites (1-5) |
| 401 | Non autorisé | Token JWT manquant |
| 403 | Interdit | Tentative de supprimer la note d'un autre utilisateur |
| 404 | Non trouvé | Produit ou note inexistant |
| 500 | Erreur serveur | Erreur interne |

### Format des erreurs

Toutes les erreurs suivent le format `ErrorResponse` :

```json
{
  "timestamp": "2025-11-30T20:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Erreurs de validation",
  "validationErrors": {
    "score": "Le score doit être entre 1 et 5"
  }
}
```

---

## 🔗 Intégration avec d'autres services

### Service Catalog

Le Feedback Service vérifie automatiquement l'existence des produits dans le service Catalog avant de créer une note.

**Configuration** (dans `application.yml`) :
```yaml
catalog:
  service:
    url: http://localhost:8080
    endpoint:
      products: /products
```

**Comportement** :
- Si le produit n'existe pas → Erreur 404 `ProductNotFoundException`
- Si le service Catalog est indisponible → Le rating est accepté (pour ne pas bloquer le service)

---

##  Tests avec cURL

### Créer une note
```bash
curl -X POST http://localhost:8083/api/ratings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT" \
  -d '{"productId": 123, "score": 5}'
```

### Récupérer le résumé
```bash
curl http://localhost:8083/api/ratings/product/123/summary
```

### Récupérer toutes les notes
```bash
curl http://localhost:8083/api/ratings/product/123
```

### Supprimer une note
```bash
curl -X DELETE http://localhost:8083/api/ratings/1 \
  -H "Authorization: Bearer VOTRE_TOKEN_JWT"
```

---

##  Notes importantes

1. **Unicité** : Un utilisateur ne peut avoir qu'**une seule note** par produit. Si une note existe déjà, elle sera **mise à jour** au lieu d'être créée.

2. **Validation du produit** : Le service vérifie automatiquement que le produit existe dans le service Catalog avant de créer une note.

3. **Authentification** : Les endpoints de création et suppression nécessitent un JWT valide avec un `userId` dans les claims.

4. **Performance** : L'endpoint `/summary` est optimisé pour être appelé fréquemment (par exemple, dans une liste de produits).

5. **Résilience** : Si le service Catalog est indisponible, le service continue de fonctionner (les ratings sont acceptés).

---

##  Support

Pour toute question ou problème :
- Consultez la documentation Swagger : `http://localhost:8083/swagger-ui.html`
- Vérifiez les logs du service pour plus de détails
- Contactez l'équipe Feedback Service

---

**Version de l'API** : 1.0.0

