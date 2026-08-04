package com.portfolio.workerlambda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class WorkerServiceTest {

    DynamoDbClient mockDdb;
    WorkerService service;

    @BeforeEach
    void setup() {
        mockDdb = Mockito.mock(DynamoDbClient.class);
        service = new WorkerService(mockDdb);
    }

    @Test
    void shouldPersistUserIdAndTimestampFromSqsMessageBody() {

        //Given: El mensaje json que se envia 
        String messageBody = "{\"userId\": \"user-123\", \"timestamp\": \"2024-01-01T00:00:00Z\"}";

        //When: El servicio procesa el mensaje
        service.processMessage(messageBody);

        //Then: verificaciones
        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        Mockito.verify(mockDdb, Mockito.times(1)).putItem(captor.capture());
        assertEquals("user-123", captor.getValue().item().get("UserId").s());
        assertEquals("2024-01-01T00:00:00Z", captor.getValue().item().get("Timestamp").s());
        Mockito.verify(mockDdb, Mockito.times(1)).saveAccess(eq("user-123"), anyString());
    }

    @Test
    void shouldRecordAccessDirectlyWithGivenValues() {
        service.recordAccess("test-user", "2023-01-01T00:00:00Z");

        Mockito.verify(mockDdb, Mockito.times(1)).putItem(Mockito.any(PutItemRequest.class));
    }
}
