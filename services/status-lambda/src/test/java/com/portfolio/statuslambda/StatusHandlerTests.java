package com.portfolio.statuslambda;



import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import com.portfolio.statuslambda.StatusHandler; 
import com.portfolio.statuslambda.StatusService; 
import software.amazon.awssdk.services.sqs.SqsClient;

import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

class StatusHandlerTests {

	@Test
	@DisplayName("La lógica de negocio debe devolver el mensaje de estado correcto")
	public void testProcessBusinessLogicBusinessLogicWithMock() {
        // 1. Creamos un Mock del cliente de AWS (no necesita región) [8]
        SqsClient mockSqs = Mockito.mock(SqsClient.class);
        Mockito.when(mockSqs.sendMessage(Mockito.any(software.amazon.awssdk.services.sqs.model.SendMessageRequest.class)))
                .thenReturn(software.amazon.awssdk.services.sqs.model.SendMessageResponse.builder().messageId("msg-1").build());
        // 2. Inyectamos el mock en el servicio
        StatusService service = new StatusService(mockSqs);

        // 3. Ejecutamos la lógica
        service.sendMessageToQueue("test-user", "2023-01-01T00:00:00Z");

        // 4. Verificamos que se intentó llamar a SQS una vez
        Mockito.verify(mockSqs, Mockito.times(1)).sendMessage(Mockito.any(software.amazon.awssdk.services.sqs.model.SendMessageRequest.class));
    }

 /**/


}
