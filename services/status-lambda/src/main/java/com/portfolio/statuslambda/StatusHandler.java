package com.portfolio.statuslambda;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;


public class StatusHandler implements RequestHandler <APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>  {


	 // Inicializamos el cliente real aquí para el entorno de AWS [6, 7]
    private final StatusService statusService = new StatusService(DynamoDbClient.create(), SqsClient.create());


	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
	

		//Extraer el userId del contexto de autorización (si está disponible)
		Map<String, Object> authorizerContext = (Map<String, Object>) input.getRequestContext().getAuthorizer();
		String userId = statusService.extractUserIdFromEvent(authorizerContext);
		context.getLogger().log("UserId: " + userId);

		String message = processBusinessLogic(userId);

		return new APIGatewayProxyResponseEvent()
		.withStatusCode(200)
				.withHeaders(Map.of(
				"Access-Control-Allow-Origin", "http://localhost:3000",
				"Content-Type", "application/json"
		))
		.withBody("{\"status\": \"" + message + "\"}")
		.withIsBase64Encoded(false);
	}


	 


	public String processBusinessLogic(String userId) {
		// Implement your business logic here
		String timestamp = java.time.Instant.now().toString();
       
		//1. ejectuar la escritura
		/*
        statusService.recordAccess(userId, timestamp);
		*/
    
		statusService.sendMessageToQueue(userId, timestamp);
	
		return statusService.getStatusMessage();
	}



}
