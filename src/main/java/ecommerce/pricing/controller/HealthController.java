package ecommerce.pricing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @Autowired
    private HealthEndpoint healthEndpoint;
    
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getHealthSummary() {
        HealthComponent healthComponent = healthEndpoint.health();
        Status status = healthComponent.getStatus();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.getCode());
        response.put("service", "Pricing Microservice");
        response.put("timestamp", System.currentTimeMillis());
        
        // Extraire les détails
        Map<String, Object> details = new HashMap<>();
        details.put("overall", status.getCode());
        
        // Gérer les détails selon le type de HealthComponent
        if (healthComponent instanceof SystemHealth) {
            SystemHealth systemHealth = (SystemHealth) healthComponent;
            systemHealth.getComponents().forEach((key, value) -> {
                if (value instanceof HealthComponent) {
                    HealthComponent comp = (HealthComponent) value;
                    details.put(key, comp.getStatus().getCode());
                }
            });
        }
        
        response.put("details", details);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/readiness")
    public ResponseEntity<Map<String, Object>> readinessProbe() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "READY");
        response.put("service", "Pricing Service");
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Service is ready to accept traffic");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/liveness")
    public ResponseEntity<Map<String, Object>> livenessProbe() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "LIVE");
        response.put("service", "Pricing Service");
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Service is alive and running");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> simpleHealth() {
        HealthComponent health = healthEndpoint.health();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", health.getStatus().getCode());
        response.put("service", "Pricing Service");
        response.put("timestamp", System.currentTimeMillis());
        
        // Version alternative plus simple
        if (health instanceof SystemHealth) {
            Map<String, String> components = new HashMap<>();
            ((SystemHealth) health).getComponents().forEach((key, component) -> {
                if (component instanceof HealthComponent) {
                    components.put(key, ((HealthComponent) component).getStatus().getCode());
                }
            });
            response.put("components", components);
        }
        
        return ResponseEntity.ok(response);
    }
}