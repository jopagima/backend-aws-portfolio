package com.portfolio.workerlambda;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import com.portfolio.workerlambda.repositories.AccessRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

@ExtendWith(MockitoExtension.class)
class WorkerServiceTest {

    AccessRepository repository;
    WorkerService service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(AccessRepository.class);
        service = new WorkerService(repository);
    }

    @Test
    void shouldPersistUserIdAndTimestampFromSqsMessageBody() throws JsonProcessingException {

        //Given: El mensaje json que se envia 
        String messageBody = "{\"userId\": \"user-123\", \"timestamp\": \"2024-01-01T00:00:00Z\"}";

        //When: El servicio procesa el mensaje
        service.processMessage(messageBody);

        //Then: verificaciones

        Mockito.verify(repository, Mockito.times(1)).saveAccess(eq("user-123"), anyString());
    }

    @Test
    void shouldRecordAccessDirectlyWithGivenValues() {
        service.recordAccess("test-user", "2023-01-01T00:00:00Z");

        Mockito.verify(repository, Mockito.times(1)).saveAccess(eq("test-user"), anyString());
    }
}
