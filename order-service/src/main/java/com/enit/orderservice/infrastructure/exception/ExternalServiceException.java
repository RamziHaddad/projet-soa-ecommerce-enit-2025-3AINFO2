package com.enit.orderservice.infrastructure.exception;

/**
 * Thrown when external service (REST API) call fails
 */
public class ExternalServiceException extends RuntimeException {
    
    private final String serviceName;
    private final String endpoint;
    private final int statusCode;
    
    public ExternalServiceException(String serviceName, String endpoint, int statusCode, String message) {
        super(String.format("External service '%s' failed at %s (status: %d): %s", 
            serviceName, endpoint, statusCode, message));
        this.serviceName = serviceName;
        this.endpoint = endpoint;
        this.statusCode = statusCode;
    }
    
    public ExternalServiceException(String serviceName, String endpoint, Throwable cause) {
        super(String.format("External service '%s' failed at %s", serviceName, endpoint), cause);
        this.serviceName = serviceName;
        this.endpoint = endpoint;
        this.statusCode = -1;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}
