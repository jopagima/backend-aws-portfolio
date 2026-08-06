package com.myorg.constructs;

import software.constructs.Construct;

import java.util.Map;



import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.dynamodb.ITable;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSourceProps;
import software.amazon.awscdk.services.sqs.IQueue;

public class WorkerLambdaConstruct extends Construct {
    private final Function lambdaFunction;



    public WorkerLambdaConstruct (Construct scope, String id, IQueue queue, ITable table) {
        super(scope, id);

        this.lambdaFunction = Function.Builder.create(this, id)
                .runtime(Runtime.JAVA_21)
                .handler("com.portfolio.workerlambda.WorkerHandler")
                .code(Code.fromAsset("services/worker-lambda/target/worker-lambda-1.0-SNAPSHOT.jar"))
                .memorySize(512)
                .timeout(Duration.seconds(15))
                .tracing(Tracing.ACTIVE) // Habilita Observabilidad con X-Ray [6]
                   .environment(Map.of(
                         "TABLE_NAME", table.getTableName()
                 ))
                .build();
                this.lambdaFunction.addEventSource(
                    new SqsEventSource(queue, SqsEventSourceProps.builder()
                        .batchSize(10) //procesa hasta 10 mensajes a la vez
                        .enabled(true)
                        .build()));

                table.grantReadWriteData(this.lambdaFunction); // Otorgar permisos de lectura/escritura a la tabla DynamoDB
                queue.grantSendMessages(this.lambdaFunction); // Otorgar permisos de envío de mensajes a la cola SQS
    }

    public Function getLambdaFunction() {
        return lambdaFunction;
    }

}
