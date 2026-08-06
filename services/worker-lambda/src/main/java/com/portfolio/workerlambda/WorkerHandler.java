package com.portfolio.workerlambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.portfolio.workerlambda.repositories.AccessRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class WorkerHandler implements RequestHandler <SQSEvent, Void>  {
    
    private final WorkerService workerService = new WorkerService(new AccessRepository(DynamoDbClient.create()));



    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        // SQS siempre envía una lista, aunque sea de un solo mensaje [3, 7]
        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            String messageBody = msg.getBody();
            context.getLogger().log("Received message: " + messageBody);

            if (messageBody == null || messageBody.isEmpty()) {
                context.getLogger().log("Ignoring empty message");
                continue;
            }

            try {
                workerService.processMessage(messageBody);
                // Si llegamos aquí, Lambda considera el mensaje procesado con éxito
            } catch (Exception e) {
                context.getLogger().log("Error processing message: " + e.getMessage());
                // IMPORTANTE: Debes lanzar la excepción. 
                // Si la capturas y no la relanzas, SQS creerá que todo fue bien y BORRARÁ el mensaje [8, 9].
                throw new RuntimeException("Fallo en el procesamiento de SQS para reintento", e);
            }
        }
        return null;
    }

}
