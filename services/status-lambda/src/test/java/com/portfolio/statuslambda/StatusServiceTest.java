package com.portfolio.statuslambda;


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*; 
import software.amazon.awssdk.services.sqs.SqsClient;




// Imports para JUnit 5 (Corrigen class BeforeEach y method assertFalse)
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

// Imports para Mockito (Corrigen class ArgumentCaptor)
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

// Imports de utilidades de Java (Corrige variable Collections)
import java.util.Collections;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.sqs.SqsClient;


import com.portfolio.statuslambda.StatusService; 
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.mockito.InjectMocks;

@ExtendWith(MockitoExtension.class)
public class StatusServiceTest {

    DynamoDbClient mockDdb;
  
    private SqsClient sqsClient; // No intentará buscar región porque es un Mock

    private StatusService service;

    @BeforeEach
    void setup() {
            // 1. Creamos un Mock del cliente de AWS (no necesita región) [8]
        mockDdb = Mockito.mock(DynamoDbClient.class);
        sqsClient = Mockito.mock(SqsClient.class);
        service = new StatusService(mockDdb, sqsClient);
    }

    @Test
    public void testGetStatusMessage() {
 
        String result = service.getStatusMessage();
        assertEquals("Service is running and access sended", result);
    }

    @Test
    public void testRecordedAndRetrieveFlow() {
        String userId = "user-tdd";
        String timestamp = java.time.Instant.now().toString();

        //1. ejectuar la escritura
        service.recordAccess(userId, timestamp);

        //2. verificar que se llamó al putItem  con los parámetros correctos
        ArgumentCaptor<PutItemRequest> argumentCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
        Mockito.verify(mockDdb, Mockito.times(1)).putItem(argumentCaptor.capture());
        assertEquals(userId, argumentCaptor.getValue().item().get("UserId").s());

        //3. Simular la resupuesta de DynamoDB para la consulta
        QueryResponse mockResponse = QueryResponse.builder()
                .items(Collections.singletonList(
                        Map.of("UserId", AttributeValue.builder().s(userId).build(),
                               "Timestamp", AttributeValue.builder().s(timestamp).build())))
                .build();
        Mockito.when(mockDdb.query(Mockito.any(QueryRequest.class))).thenReturn(mockResponse);


        //4. Ejecutar consulta y validar

        List<Map<String, AttributeValue>> results = service.getAccessLogs(userId);
        assertFalse(results.isEmpty());
        assertEquals(userId, results.get(0).get("UserId").s());
        

    }

    @Test
    public void shouldExtractUserIdFromCognitoClaims() {
        //1. simular la estructura que envía API Gateway + Cognito
        Map<String, Object> claims = Map.of(
            "sub", "user-12345-uuid",
            "email", "test@porfolio.com"
        );

        Map<String, Object> authorizer = Map.of("claims", claims);

        //2. Ejecutar la lógica de extacción 
        String extractedId = service.extractUserIdFromEvent(authorizer);
        assertEquals("user-12345-uuid", extractedId);
    }


}
