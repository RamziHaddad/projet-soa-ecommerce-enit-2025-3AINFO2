package com.ecommerce.recommendation.api;

import java.util.List;

import com.ecommerce.recommendation.dto.RecommendationRequest;
import com.ecommerce.recommendation.dto.RecommendationResponse;
import com.ecommerce.recommendation.service.RecommendationService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/recommendations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecommendationResource {

    @Inject
    RecommendationService recommendationService;

    @GET
    @Path("/algorithms")
    public List<String> getAvailableAlgorithms() {
        return recommendationService.getAvailableAlgorithms();
    }

    @POST
    @Path("/generate")
    public Response generateRecommendations(RecommendationRequest request) {
        try {
            RecommendationResponse response = recommendationService.generateRecommendations(request);
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/user/{userId}")
    public Response getRecommendationsForUser(
            @PathParam("userId") Long userId,
            @QueryParam("algorithm") String algorithm,
            @QueryParam("maxResults") @DefaultValue("10") Integer maxResults) {

        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(userId);
        request.setAlgorithm(algorithm);
        request.setMaxResults(maxResults);

        return generateRecommendations(request);
    }

}