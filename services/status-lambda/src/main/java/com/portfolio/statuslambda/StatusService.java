package com.portfolio.statuslambda;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*; 
import java.time.Instant;
import java.util.List;
import java.util.Map;


public class StatusService {

    private  final DynamoDbClient ddb;
    private static final String TABLE_NAME = System.getenv("TABLE_NAME");


    // Constructor para inyección (Senior practice)
    public StatusService(DynamoDbClient ddb) {
        this.ddb = ddb;
    }

    public String getStatusMessage() {
        String timestamp = java.time.Instant.now().toString();

        //1. ejectuar la escritura
        recordAccess("anonymous-user", timestamp);
        return "Service is running and access recorded";
    }

    public void recordAccess(String userId, String timestamp) {
        PutItemRequest request = PutItemRequest.builder()
        .tableName(TABLE_NAME)
        .item(Map.of(
            "UserId", AttributeValue.builder().s(userId).build(),
            "Timestamp", AttributeValue.builder().s(timestamp).build()
        )).build();

        // Operación de escritura en el plano de datos [20]
        ddb.putItem(request);
    }

    public List<Map<String, AttributeValue>> getAccessLogs(String userId) {
        // Construir la consulta para obtener los registros de acceso del usuario
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression("UserId = :userId")
                .expressionAttributeValues(Map.of(
                    ":userId", AttributeValue.builder().s(userId).build()))
                .build();

        // Ejecutar la consulta y devolver los resultados
        return ddb.query(queryRequest).items();
    }

}
