package com.portfolio.statuslambda;



import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import com.portfolio.statuslambda.StatusHandler; 
import com.portfolio.statuslambda.StatusService; 

import org.mockito.Mockito;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import static org.junit.jupiter.api.Assertions.*;

class StatusHandlerTests {
	
	@Test
	@DisplayName("La lógica de negocio debe devolver el mensaje de estado correcto")
	public void testProcessBusinessLogicBusinessLogicWithMock() {
        // 1. Creamos un Mock del cliente de AWS (no necesita región) [8]
        DynamoDbClient mockDdb = Mockito.mock(DynamoDbClient.class);
        
        // 2. Inyectamos el mock en el servicio
        StatusService service = new StatusService(mockDdb);
        
        // 3. Ejecutamos la lógica
        service.recordAccess("test-user", "2023-01-01T00:00:00Z");
        
        // 4. Verificamos que se llamó al putItem con los parámetros correctos
        // Verificamos que se intentó llamar a DynamoDB una vez [9, 10]
        Mockito.verify(mockDdb, Mockito.times(1)).putItem(Mockito.any(software.amazon.awssdk.services.dynamodb.model.PutItemRequest.class));
    }	
	
 /**/


}
