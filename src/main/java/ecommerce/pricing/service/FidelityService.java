package ecommerce.pricing.service;


import ecommerce.pricing.entity.Fidelity;
import ecommerce.pricing.repository.FidelityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class FidelityService {

    @Autowired
    private FidelityRepository fidelityRepository;

    // ========== MÉTHODES CRUD ==========

    public Fidelity createOrUpdateFidelity(Long userId, Integer pointsToAdd) {
        Optional<Fidelity> existingFidelity = fidelityRepository.findByUserId(userId);
        
        Fidelity fidelity;
        if (existingFidelity.isPresent()) {
            fidelity = existingFidelity.get();
            fidelity.setPoints(fidelity.getPoints() + pointsToAdd);
            fidelity.setLoyaltyTier(calculateLoyaltyTier(fidelity.getPoints()));
            fidelity.setDiscountPercentage(calculateDiscountPercentage(fidelity.getLoyaltyTier()));
        } else {
            fidelity = new Fidelity(userId);
            fidelity.setPoints(pointsToAdd);
            fidelity.setLoyaltyTier(calculateLoyaltyTier(pointsToAdd));
            fidelity.setDiscountPercentage(calculateDiscountPercentage(fidelity.getLoyaltyTier()));
        }
        
        return fidelityRepository.save(fidelity);
    }

    public Fidelity getFidelityByUserId(Long userId) {
        return fidelityRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Aucun programme de fidélité trouvé pour l'utilisateur: " + userId));
    }

    public List<Fidelity> getAllFidelities() {
        return fidelityRepository.findAll();
    }

    public Fidelity updateFidelity(Long id, Fidelity fidelityDetails) {
        Fidelity fidelity = fidelityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fidélité non trouvée avec l'id: " + id));
        
        fidelity.setPoints(fidelityDetails.getPoints());
        fidelity.setLoyaltyTier(fidelityDetails.getLoyaltyTier());
        fidelity.setDiscountPercentage(fidelityDetails.getDiscountPercentage());
        
        return fidelityRepository.save(fidelity);
    }

    public void deleteFidelity(Long id) {
        fidelityRepository.deleteById(id);
    }

    // ========== MÉTHODES PRIVÉES DE CALCUL ==========

    private String calculateLoyaltyTier(Integer points) {
        if (points >= 1000) return "PLATINUM";
        else if (points >= 500) return "GOLD";
        else if (points >= 100) return "SILVER";
        else return "BRONZE";
    }

    private Double calculateDiscountPercentage(String loyaltyTier) {
        switch (loyaltyTier) {
            case "PLATINUM": return 15.0;
            case "GOLD": return 10.0;
            case "SILVER": return 5.0;
            case "BRONZE": return 2.0;
            default: return 0.0;
        }
    }

    // ========== MÉTHODE D'APPLICATION DE FIDÉLITÉ (VERSION BIGDECIMAL) ==========

    public BigDecimal applyFidelityDiscount(BigDecimal price, Long userId) {
       
        if (userId == null) {
            return price;
        }

        try {
            Fidelity fidelity = getFidelityByUserId(userId);
            
            BigDecimal discountPercentage = BigDecimal.valueOf(fidelity.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100));
            
            BigDecimal discountAmount = price.multiply(discountPercentage);
            BigDecimal finalPrice = price.subtract(discountAmount);
            
            return finalPrice;
            
        } catch (RuntimeException e) {
            return price;
        }
    }

    // ========== MÉTHODE POUR L'ENDPOINT /apply-discount (VERSION DOUBLE) ==========

    
    public Double applyFidelityDiscount(Double price, Long userId) {
      
        if (userId == null) {
            return price;
        }

        try {
            Fidelity fidelity = getFidelityByUserId(userId);
            Double discount = price * (fidelity.getDiscountPercentage() / 100);
            return price - discount;
        } catch (RuntimeException e) {
            return price;
        }
    }}