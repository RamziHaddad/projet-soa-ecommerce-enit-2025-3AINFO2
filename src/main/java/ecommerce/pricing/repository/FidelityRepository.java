package ecommerce.pricing.repository;

import ecommerce.pricing.entity.Fidelity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FidelityRepository extends JpaRepository<Fidelity, Long> {
    
    // Trouver la fidélité d'un utilisateur
    Optional<Fidelity> findByUserId(Long userId);
    
    // Vérifier si un utilisateur a déjà un programme de fidélité
    boolean existsByUserId(Long userId);
}