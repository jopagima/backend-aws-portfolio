package com.myorg;

import com.myorg.constructs.AuthConstruct;
import com.myorg.constructs.DynamoDBConstruct;
import com.myorg.constructs.PortfolioAccessQueue;
import com.myorg.constructs.PortfolioApiConstruct;
import com.myorg.constructs.StatusLamdaConstruct;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.AuthorizationType;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.MethodOptions;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

import software.amazon.awscdk.services.apigateway.CorsOptions;
import software.amazon.awscdk.services.apigateway.Cors;
import java.util.List;



public class BackendAwsPortfolioStack extends Stack {

    public BackendAwsPortfolioStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public BackendAwsPortfolioStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // The code that defines your stack goes here

        // example resource
        // final Queue queue = Queue.Builder.create(this, "BackendAwsPortfolioQueue")
        // .visibilityTimeout(Duration.seconds(300))
        // .build();

        // Define the Lambda function
        DynamoDBConstruct database = new DynamoDBConstruct(this, "PortfolioDatabase");

        PortfolioAccessQueue portfolioAccessQueue = new PortfolioAccessQueue(this, "PortfolioAccessQueue");
        Queue accessQueue = portfolioAccessQueue.getPortfolioAccessQueue();

        StatusLamdaConstruct statusService = new StatusLamdaConstruct(this, "StatusService",
                database.getTable().getTableName(), accessQueue.getQueueUrl());
                // Otorgar permiso de productor a la Lambda de API [13, 14]
        accessQueue.grantSendMessages(statusService.getLambdaFunction());


        // Add permissions to read/write to Lambda
        database.getTable().grantReadWriteData(statusService.getLambdaFunction());

        PortfolioApiConstruct api = new PortfolioApiConstruct(this, "PortFolioApi", statusService.getLambdaFunction());

        AuthConstruct authConstruct = new AuthConstruct(this, "PortfolioAuth");
        api.getRoot().addMethod("GET", new LambdaIntegration(statusService.getLambdaFunction()),
               MethodOptions.builder()
                        .authorizationType(AuthorizationType.COGNITO) //Define que se requiere cognito
                        .authorizer(authConstruct.getAuthorizer()) //asocia el autorizador de cognito al método
                        .build());

        



    }
}
