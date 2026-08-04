package com.portfolio.workerlambda.repositories;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;

public class AccessRepository {
    private DynamoDbClient ddb;
    private final String tableName = System.getenv("TABLE_NAME");

    public AccessRepository(DynamoDbClient ddb) {
        this.ddb = ddb;
    }

    public void saveAccess(String userId, String timestamp) {
        // Implementation for saving access
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(Map.of(
                        "UserId", AttributeValue.builder().s(userId).build(),
                        "Timestamp", AttributeValue.builder().s(timestamp).build()
                ))
                .build();
        ddb.putItem(request);
    }
}
