package com.example.photo_blog;

public class BlogPost {
    public String userId,description,imageUrl;

    public BlogPost(){}
    public BlogPost(String userId, String description, String imageUrl) {
        this.userId = userId;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
