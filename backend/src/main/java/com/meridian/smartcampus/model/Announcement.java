package com.meridian.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "announcements")
public class Announcement {
    @Id
    private String mongoId;

    @JsonProperty("id")
    private Long id;

    private String title;
    private String content;
    private String audience = "All campus";
    private String priority = "normal"; // "normal", "important", "urgent"
    private String author;
    private String publishedAt;

    public Announcement() {}

    public Announcement(Long id, String title, String content, String audience, String priority, String author, String publishedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.audience = audience;
        this.priority = priority;
        this.author = author;
        this.publishedAt = publishedAt;
    }

    public String getMongoId() {
        return mongoId;
    }

    public void setMongoId(String mongoId) {
        this.mongoId = mongoId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }
}
