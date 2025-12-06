package ecommerce.pricing.repository;

import ecommerce.pricing.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    
    // Trouver le prix actif d'un produit
    @Query("SELECT p FROM Price p WHERE p.productId = :productId AND p.status = 'ACTIVE' " +
           "ORDER BY p.effectiveDate DESC LIMIT 1")
    Optional<Price> findActivePriceByProductId(@Param("productId") Long productId);
    
    // Trouver tous les prix d'un produit (pour l'historique)
    List<Price> findByProductIdOrderByEffectiveDateDesc(Long productId);
    
    // Trouver les prix pour plusieurs produits
    List<Price> findByProductIdIn(List<Long> productIds);
    
    // Vérifier si un produit a déjà un prix
    boolean existsByProductId(Long productId);
    
    // Trouver les prix actifs pour plusieurs produits
    @Query("SELECT p FROM Price p WHERE p.productId IN :productIds AND p.status = 'ACTIVE'")
    List<Price> findActivePricesByProductIds(@Param("productIds") List<Long> productIds);

    long countByStatus(Price.PriceStatus status);
    long countByStatus(String status);  // Avec une annotation @Query

}