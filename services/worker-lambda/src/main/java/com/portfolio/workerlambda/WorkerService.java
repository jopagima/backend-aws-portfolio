package com.portfolio.workerlambda;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.workerlambda.repositories.AccessRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerService {

    
    private static final Logger logger = LoggerFactory.getLogger(WorkerService.class);

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
        logger.info("Processing SQS message body");

        String userId = extractField(USER_ID_PATTERN, messageBody);
        String timestamp = extractField(TIMESTAMP_PATTERN, messageBody);

        JsonNode jsonNode = mapper.readTree(messageBody);
        if (jsonNode.has("userId")) {
            userId = jsonNode.get("userId").asText();
        }
        if (jsonNode.has("timestamp")) {
            timestamp = jsonNode.get("timestamp").asText();
        }

        // Agregar anotaciones a AWS X-Ray para rastreo
        Segment segment = AWSXRay.getCurrentSegment();
        if(segment == null) {
            segment = AWSXRay.beginSegment("WorkerServiceSegment");
        }
         // Añadimos una anotación indexada para búsquedas rápidas en la consola [1, 3]
        segment.putAnnotation("PortfolioUserId", userId);
        // Podríamos añadir metadatos (no indexados) para información adicional
        segment.putMetadata("FullMessageBody", messageBody); 



        logger.info("Message parsed. userId={}, timestamp={}", userId, timestamp);

        recordAccess(userId, timestamp);
    }

    public void recordAccess(String userId, String timestamp) {
        logger.info("Saving access record for userId={}", userId);
        repository.saveAccess(userId, timestamp);
        logger.info("Access record saved successfully for userId={}", userId);
    }

    private String extractField(Pattern pattern, String messageBody) {
        Matcher matcher = pattern.matcher(messageBody);
        if (!matcher.find()) {
            logger.warn("Field not found for pattern={} in message body, defaulting to 'unknown'", pattern.pattern());
            return "unknown";
        }
        return matcher.group(1);
    }
}
