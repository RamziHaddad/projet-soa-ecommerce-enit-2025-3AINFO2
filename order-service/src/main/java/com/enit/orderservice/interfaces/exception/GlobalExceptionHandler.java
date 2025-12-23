package com.enit.orderservice.interfaces.exception;

import com.enit.orderservice.application.exception.SagaCompensationFailedException;
import com.enit.orderservice.application.exception.SagaExecutionException;
import com.enit.orderservice.application.exception.SagaTimeoutException;
import com.enit.orderservice.domaine.exception.InvalidOrderStateException;
import com.enit.orderservice.domaine.exception.OrderNotFoundException;
import com.enit.orderservice.domaine.exception.OrderValidationException;
import com.enit.orderservice.infrastructure.exception.ExternalServiceException;
import com.enit.orderservice.infrastructure.exception.MessageConsumptionException;
import com.enit.orderservice.infrastructure.exception.MessagePublishException;
import com.enit.orderservice.infrastructure.exception.PersistenceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API
 * Converts exceptions to standardized error responses
 */
public class GlobalExceptionHandler {

    /**
     * Handle OrderNotFoundException
     * Returns 404 Not Found
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                404,
                "Not Found",
                ex.getMessage(),
                "/orders/" + ex.getOrderId()
        );
        return RestResponse.status(Response.Status.NOT_FOUND, error);
    }

    /**
     * Handle InvalidOrderStateException
     * Returns 409 Conflict
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleInvalidOrderState(InvalidOrderStateException ex) {
        ErrorResponse error = new ErrorResponse(
                409,
                "Conflict",
                ex.getMessage(),
                "/orders/" + ex.getOrderId()
        );
        return RestResponse.status(Response.Status.CONFLICT, error);
    }

    /**
     * Handle OrderValidationException
     * Returns 400 Bad Request
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleOrderValidation(OrderValidationException ex) {
        ErrorResponse error = new ErrorResponse(
                400,
                "Bad Request",
                ex.getMessage(),
                "/orders"
        );
        return RestResponse.status(Response.Status.BAD_REQUEST, error);
    }

    /**
     * Handle SagaExecutionException
     * Returns 500 Internal Server Error
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleSagaExecution(SagaExecutionException ex) {
        ErrorResponse error = new ErrorResponse(
                500,
                "Saga Execution Failed",
                ex.getMessage(),
                "/orders/" + ex.getOrderId()
        );
        System.err.println("Saga execution failed: " + ex.getMessage());
        ex.printStackTrace();
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, error);
    }

    /**
     * Handle SagaCompensationFailedException
     * Returns 500 Internal Server Error (Critical!)
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleSagaCompensation(SagaCompensationFailedException ex) {
        ErrorResponse error = new ErrorResponse(
                500,
                "Saga Compensation Failed",
                "Critical: " + ex.getMessage() + " - Manual intervention required",
                "/orders/" + ex.getOrderId()
        );
        System.err.println("CRITICAL - Saga compensation failed: " + ex.getMessage());
        ex.printStackTrace();
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, error);
    }

    /**
     * Handle SagaTimeoutException
     * Returns 504 Gateway Timeout
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleSagaTimeout(SagaTimeoutException ex) {
        ErrorResponse error = new ErrorResponse(
                504,
                "Gateway Timeout",
                ex.getMessage(),
                "/orders/" + ex.getOrderId()
        );
        System.err.println("Saga timeout: " + ex.getMessage());
        return RestResponse.status(Response.Status.GATEWAY_TIMEOUT, error);
    }

    /**
     * Handle MessagePublishException (Kafka publish failure)
     * Returns 503 Service Unavailable
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleMessagePublish(MessagePublishException ex) {
        ErrorResponse error = new ErrorResponse(
                503,
                "Service Unavailable",
                "Message broker is unavailable: " + ex.getMessage(),
                "/orders"
        );
        System.err.println("Kafka publish failed: " + ex.getMessage());
        ex.printStackTrace();
        return RestResponse.status(Response.Status.SERVICE_UNAVAILABLE, error);
    }

    /**
     * Handle MessageConsumptionException (Kafka consume failure)
     * Returns 500 Internal Server Error
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleMessageConsumption(MessageConsumptionException ex) {
        ErrorResponse error = new ErrorResponse(
                500,
                "Message Processing Failed",
                "Failed to process message: " + ex.getMessage(),
                "/orders"
        );
        System.err.println("Kafka consumption failed: " + ex.getMessage());
        ex.printStackTrace();
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, error);
    }

    /**
     * Handle ExternalServiceException (External API failure)
     * Returns 502 Bad Gateway
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleExternalService(ExternalServiceException ex) {
        ErrorResponse error = new ErrorResponse(
                502,
                "Bad Gateway",
                "External service failure: " + ex.getMessage(),
                "/orders"
        );
        System.err.println("External service failed: " + ex.getMessage());
        ex.printStackTrace();
        return RestResponse.status(Response.Status.BAD_GATEWAY, error);
    }

    /**
     * Handle PersistenceException (Database failure)
     * Returns 500 Internal Server Error
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handlePersistence(PersistenceException ex) {
        ErrorResponse error = new ErrorResponse(
                500,
                "Database Error",
                "Database operation failed: " + ex.getMessage(),
                "/orders"
        );
        System.err.println("Persistence failed: " + ex.getMessage());
        ex.printStackTrace();
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, error);
    }

    /**
     * Handle Bean Validation failures (ConstraintViolationException)
     * Returns 400 Bad Request with field-level validation errors
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<ErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> new ErrorResponse.ValidationError(
                        getFieldName(violation),
                        violation.getMessage()
                ))
                .collect(Collectors.toList());

        ErrorResponse error = new ErrorResponse(
                400,
                "Bad Request",
                "Validation failed",
                "/orders",
                validationErrors
        );
        return RestResponse.status(Response.Status.BAD_REQUEST, error);
    }

    /**
     * Handle generic exceptions
     * Returns 500 Internal Server Error
     */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> handleGenericException(Exception ex) {
        // Don't expose internal error details in production
        ErrorResponse error = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                "/orders"
        );
        // Log the full exception for debugging
        System.err.println("Unexpected error: " + ex.getMessage());
        ex.printStackTrace();
        
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, error);
    }

    /**
     * Extract field name from constraint violation path
     */
    private String getFieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        String[] parts = path.split("\\.");
        return parts[parts.length - 1];
    }
}
