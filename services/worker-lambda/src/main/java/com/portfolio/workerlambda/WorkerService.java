package com.portfolio.workerlambda;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.workerlambda.repositories.AccessRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

public class WorkerService {

    private final AccessRepository repository;
    private final ObjectMapper mapper;
    private static final String TABLE_NAME = System.getenv("TABLE_NAME");

    private static final Pattern USER_ID_PATTERN = Pattern.compile("\"userId\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("\"timestamp\"\\s*:\\s*\"([^\"]*)\"");

    // Constructor para inyección (Senior practice)
    public WorkerService(AccessRepository repository) {
        this.repository = repository;
        this.mapper = new ObjectMapper();
    }

    public void processMessage(String messageBody) throws JsonProcessingException {
        String userId = extractField(USER_ID_PATTERN, messageBody);
        String timestamp = extractField(TIMESTAMP_PATTERN, messageBody);

        JsonNode jsonNode = mapper.readTree(messageBody);
        if (jsonNode.has("userId")) {
            userId = jsonNode.get("userId").asText();
        }
        if (jsonNode.has("timestamp")) {    
            timestamp = jsonNode.get("timestamp").asText();
        }



        recordAccess(userId, timestamp);
    }

    public void recordAccess(String userId, String timestamp) {
        repository.saveAccess(userId, timestamp);
    }

    private String extractField(Pattern pattern, String messageBody) {
        Matcher matcher = pattern.matcher(messageBody);
        return matcher.find() ? matcher.group(1) : "unknown";
    }
}
