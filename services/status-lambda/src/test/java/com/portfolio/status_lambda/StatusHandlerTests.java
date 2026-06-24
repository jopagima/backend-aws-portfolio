package com.portfolio.status_lambda;

import org.junit.jupiter.api.Test;



class StatusHandlerTests {
	
	@Test
	@DisplayName("La lógica de negocio debe devolver el mensaje de estado correcto")
	public void testProcessBusinessLogic() {
		//Given Dado un objeto StatusHandler
		StatusHandler statusHandler = new StatusHandler();

		//When Cuando se llama al método processBusinessLogic
		String result = statusHandler.processBusinessLogic();

		//Then Entonces se espera que el resultado sea el mensaje de estado correcto
		assertNotNull(result, "El resultado no debe ser nulo");
		assertEquals("Service is running", result);

	}	
	
 /**/


}
