package com.portfolio.statuslambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;


public class StatusHandler implements RequestHandler <APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>  {


	 // Inicializamos el cliente real aquí para el entorno de AWS [6, 7]
    private final StatusService statusService = new StatusService(DynamoDbClient.create());


	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		String message = processBusinessLogic();

		return new APIGatewayProxyResponseEvent()
		.withStatusCode(200)
		.withBody("{\"status\": \"" + message + "\"}")
		.withIsBase64Encoded(false);
	}


	 


	public String processBusinessLogic() {
		// Implement your business logic here
		return statusService.getStatusMessage();
	}



}
