package com.portfolio.statuslambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;


public class StatusHandler implements RequestHandler <APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>  {

	private final StatusService service = new StatusService();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		String message = processBusinessLogic();

		return new APIGatewayProxyResponseEvent()
		.withStatusCode(200)
		.withBody("{\"status\": \"" + message + "\"}");
	}


	 


	public String processBusinessLogic() {
		// Implement your business logic here
		return service.getStatusMessage();
	}



}
