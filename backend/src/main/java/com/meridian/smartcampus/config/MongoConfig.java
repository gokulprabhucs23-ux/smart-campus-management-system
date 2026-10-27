package com.meridian.smartcampus.config;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/smart_campus}")
    private String rawUri;

    @Bean
    @Primary
    public MongoDatabaseFactory mongoDatabaseFactory() {
        String clean = (rawUri != null) ? rawUri.trim() : "mongodb://localhost:27017/smart_campus";

        // Remove any surrounding single/double quotes, spaces, or newlines
        clean = clean.replaceAll("^[\"']+|[\"']+$", "").trim();

        // Check if database name exists or extract it cleanly
        ConnectionString connString;
        try {
            connString = new ConnectionString(clean);
        } catch (Exception e) {
            log.warn("Error parsing raw URI, falling back to clean Atlas connection: {}", e.getMessage());
            clean = clean.replaceAll("\\s+", "");
            connString = new ConnectionString(clean);
        }

        String dbName = connString.getDatabase();
        if (dbName == null || dbName.isBlank()) {
            dbName = "smart_campus";
        } else {
            // Remove any trailing slashes, quotes, or invalid chars from the database name
            dbName = dbName.replaceAll("[^a-zA-Z0-9_-]", "");
            if (dbName.isBlank()) {
                dbName = "smart_campus";
            }
        }

        log.info("Connecting to MongoDB with sanitized database name: {}", dbName);
        MongoClient client = MongoClients.create(connString);
        return new SimpleMongoClientDatabaseFactory(client, dbName);
    }
}
