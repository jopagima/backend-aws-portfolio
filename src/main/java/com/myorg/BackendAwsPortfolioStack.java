package com.myorg;

import com.myorg.constructs.AuthConstruct;
import com.myorg.constructs.DynamoDBConstruct;
import com.myorg.constructs.PipelineConstruct;
import com.myorg.constructs.PortfolioAccessQueue;
import com.myorg.constructs.PortfolioApiConstruct;
import com.myorg.constructs.StatusLamdaConstruct;
import com.myorg.constructs.WorkerLambdaConstruct;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.AuthorizationType;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.MethodOptions;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;


public class BackendAwsPortfolioStack extends Stack {

    public BackendAwsPortfolioStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public BackendAwsPortfolioStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);


        // Define the Lambda function
        DynamoDBConstruct database = new DynamoDBConstruct(this, "PortfolioDatabase");

        PortfolioAccessQueue portfolioAccessQueue = new PortfolioAccessQueue(this, "PortfolioAccessQueue");
        Queue accessQueue = portfolioAccessQueue.getPortfolioAccessQueue();

        // Productor que envia los mensajes a la cola
        StatusLamdaConstruct statusService = new StatusLamdaConstruct(this, "StatusService",
                database.getTable().getTableName(), accessQueue.getQueueUrl());
        // Otorgar permiso de productor a la Lambda de API [13, 14]
        accessQueue.grantSendMessages(statusService.getLambdaFunction());

        WorkerLambdaConstruct workerService = new WorkerLambdaConstruct(this,
                "AccessWorker",
                accessQueue, // cola de la que lee
                database.getTable()); // tabla donde escribe

        // El consumidor (Worker) ya tiene sus permisos y su event source
        // de SQS registrados dentro de su propio Constructo (WorkerLambdaConstruct).

        PortfolioApiConstruct api = new PortfolioApiConstruct(this, "PortFolioApi", statusService.getLambdaFunction());

        AuthConstruct authConstruct = new AuthConstruct(this, "PortfolioAuth");
        api.getRoot().addMethod("GET", new LambdaIntegration(statusService.getLambdaFunction()),
                MethodOptions.builder()
                        .authorizationType(AuthorizationType.COGNITO) // Define que se requiere cognito
                        .authorizer(authConstruct.getAuthorizer()) // asocia el autorizador de cognito al método
                        .build());

        new PipelineConstruct(this, "PortfolioPipeline");

    }
}
