package com.enit.orderservice.infrastructure.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import java.io.IOException;
import java.net.Socket;

/**
 * Health check for Kafka broker connectivity
 * Verifies that the Kafka broker is reachable
 */
@Readiness
@ApplicationScoped
public class KafkaHealthCheck implements HealthCheck {

    @ConfigProperty(name = "kafka.bootstrap.servers", defaultValue = "localhost:9092")
    String kafkaBootstrapServers;

    @Override
    public HealthCheckResponse call() {
        try {
            // Extract host and port from bootstrap.servers
            String[] hostPort = kafkaBootstrapServers.split(":")[0].split(",")[0].split(":");
            String host = hostPort.length > 1 ? kafkaBootstrapServers.split(":")[0] : "localhost";
            int port = hostPort.length > 1 ? 
                Integer.parseInt(kafkaBootstrapServers.split(":")[1].split(",")[0]) : 9092;
            
            // Try to establish a TCP connection to Kafka broker
            try (Socket socket = new Socket(host, port)) {
                return HealthCheckResponse
                        .named("Kafka broker connection")
                        .up()
                        .withData("broker", kafkaBootstrapServers)
                        .build();
            }
        } catch (IOException e) {
            return HealthCheckResponse
                    .named("Kafka broker connection")
                    .down()
                    .withData("broker", kafkaBootstrapServers)
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
