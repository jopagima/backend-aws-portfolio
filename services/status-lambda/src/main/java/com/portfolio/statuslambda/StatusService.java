package com.portfolio.statuslambda;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StatusService {

    private static final Logger logger = LoggerFactory.getLogger(StatusService.class);

    private final SqsClient sqsClient ;
    private final String QUEUE_URL = System.getenv("QUEUE_URL");


    // Constructor para inyección (Senior practice)
    public StatusService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;

    }

    public String getStatusMessage() {

        return "Service is running and access sended";
    }

    public void sendMessageToQueue(String userId, String timestamp) {
         // Construimos el mensaje JSON con los datos del acceso

        String message = String.format("{\"userId\": \"%s\", \"timestamp\": \"%s\"}",
                             userId, timestamp);

        SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(QUEUE_URL)
                .messageBody(message)
                .delaySeconds(0) // El mensaje es visible inmediatamente [4, 5]
                .build();

        logger.info("Sending access message to SQS queue for userId={}", userId);
        SendMessageResponse response = sqsClient.sendMessage(sendMsgRequest);
        logger.info("Message queued successfully. userId={}, messageId={}", userId, response.messageId());

    }
    /**
    * Extrae el identificador único (sub) de los claims de Cognito [4, 5]
    * @param authorizerContext El mapa 'authorizer' proveniente del requestContext
    * @return El UUID del usuario o 'anonymous' si no hay claims
    */
    public String extractUserIdFromEvent(Map<String, Object> authorizerContext) {

        if(authorizerContext == null || !authorizerContext.containsKey("claims")) {
            logger.warn("No authorizer context/claims present, defaulting to anonymous-user");
            return "anonymous-user";
        }

        //El sdk de API Gateway + Cognito envía un mapa con la estructura de claims,
        // de donde se puede extraer el sub (UUID del usuario)
        @SuppressWarnings("unchecked")
        Map<String, Object> claims = (Map<String, Object>) authorizerContext.get("claims");

        // El claim 'sub' es el identificador único del usuario en Cognito, si no existe se retorna un valor por defecto
        String userId = claims.getOrDefault("sub", "anonymous-user").toString();
        logger.debug("Extracted userId={} from Cognito claims", userId);
        return userId;
    }

}
