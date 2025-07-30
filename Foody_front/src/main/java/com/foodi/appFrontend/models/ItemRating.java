package com.foodi.appFrontend.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ItemRating {
    @JsonProperty("avg_rating")
    private Double avgRating;
    
    @JsonProperty("comments")
    private List<RatingComment> comments;
    
    public ItemRating() {}
    
    public ItemRating(Double avgRating, List<RatingComment> comments) {
        this.avgRating = avgRating;
        this.comments = comments;
    }
    
    public Double getAvgRating() {
        return avgRating;
    }
    
    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }
    
    public List<RatingComment> getComments() {
        return comments;
    }
    
    public void setComments(List<RatingComment> comments) {
        this.comments = comments;
    }
    
    public static class RatingComment {
        @JsonProperty("id")
        private Integer id;
        
        @JsonProperty("order_id")
        private Integer orderId;
        
        @JsonProperty("rating")
        private Integer rating;
        
        @JsonProperty("comment")
        private String comment;
        
        @JsonProperty("image_base64")
        private List<String> imageBase64;
        
        @JsonProperty("user_id")
        private Integer userId;
        
        @JsonProperty("created_at")
        private List<Integer> createdAt;
        
        public RatingComment() {}
        
        public Integer getId() {
            return id;
        }
        
        public void setId(Integer id) {
            this.id = id;
        }
        
        public Integer getOrderId() {
            return orderId;
        }
        
        public void setOrderId(Integer orderId) {
            this.orderId = orderId;
        }
        
        public Integer getRating() {
            return rating;
        }
        
        public void setRating(Integer rating) {
            this.rating = rating;
        }
        
        public String getComment() {
            return comment;
        }
        
        public void setComment(String comment) {
            this.comment = comment;
        }
        
        public List<String> getImageBase64() {
            return imageBase64;
        }
        
        public void setImageBase64(List<String> imageBase64) {
            this.imageBase64 = imageBase64;
        }
        
        public Integer getUserId() {
            return userId;
        }
        
        public void setUserId(Integer userId) {
            this.userId = userId;
        }
        
        public List<Integer> getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(List<Integer> createdAt) {
            this.createdAt = createdAt;
        }
    }
} 