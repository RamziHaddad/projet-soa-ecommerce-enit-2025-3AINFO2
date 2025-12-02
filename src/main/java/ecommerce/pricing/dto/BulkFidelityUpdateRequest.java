package ecommerce.pricing.dto;

import java.util.List;

public class BulkFidelityUpdateRequest {
    private List<FidelityUpdate> updates;

    public static class FidelityUpdate {
        private Long userId;
        private Integer pointsToAdd;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Integer getPointsToAdd() { return pointsToAdd; }
        public void setPointsToAdd(Integer pointsToAdd) { this.pointsToAdd = pointsToAdd; }
    }

    public List<FidelityUpdate> getUpdates() { return updates; }
    public void setUpdates(List<FidelityUpdate> updates) { this.updates = updates; }
}