package ecommerce.pricing.repository;

import ecommerce.pricing.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    
    // Trouver toutes les promotions d'un produit
    List<Promotion> findByProductId(Long productId);
    
    // Trouver la promotion active d'un produit
    @Query("SELECT p FROM Promotion p WHERE p.productId = :productId AND :currentDate BETWEEN p.startDate AND p.endDate")
    Optional<Promotion> findActivePromotionByProductId(@Param("productId") Long productId, 
                                                      @Param("currentDate") LocalDate currentDate);
    
    // Trouver toutes les promotions actives
    @Query("SELECT p FROM Promotion p WHERE :currentDate BETWEEN p.startDate AND p.endDate")
    List<Promotion> findAllActivePromotions(@Param("currentDate") LocalDate currentDate);
    
    // Vérifier si une promotion existe pour un produit
    boolean existsByProductId(Long productId);
}