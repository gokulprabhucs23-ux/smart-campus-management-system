package com.meridian.smartcampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "events")
public class CampusEvent {
    @Id
    private String mongoId;

    @JsonProperty("id")
    private Long id;

    private String title;
    private String description;
    private String date;
    private String time;
    private String venue;
    private String organizer;
    private Integer capacity = 500;
    private Integer registered = 0;
    private Boolean registeredByMe = false;
    private List<Long> registeredUserIds = new ArrayList<>();

    public CampusEvent() {}

    public CampusEvent(Long id, String title, String description, String date, String time, String venue, String organizer, Integer capacity, Integer registered, Boolean registeredByMe) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.venue = venue;
        this.organizer = organizer;
        this.capacity = capacity;
        this.registered = registered;
        this.registeredByMe = registeredByMe;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getRegistered() {
        return registered;
    }

    public void setRegistered(Integer registered) {
        this.registered = registered;
    }

    public Boolean getRegisteredByMe() {
        return registeredByMe;
    }

    public void setRegisteredByMe(Boolean registeredByMe) {
        this.registeredByMe = registeredByMe;
    }

    public List<Long> getRegisteredUserIds() {
        return registeredUserIds;
    }

    public void setRegisteredUserIds(List<Long> registeredUserIds) {
        this.registeredUserIds = registeredUserIds;
    }
}
