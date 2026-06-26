package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.StageOptions;

public class BackendAwsPortfolioStackOld extends Stack {
    public BackendAwsPortfolioStackOld(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public BackendAwsPortfolioStackOld(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // The code that defines your stack goes here

        // example resource
        // final Queue queue = Queue.Builder.create(this, "BackendAwsPortfolioQueue")
        // .visibilityTimeout(Duration.seconds(300))
        // .build();

        // Define the Lambda function
        Function statusLambda = Function.Builder.create(this, "StatusLambda")
                .runtime(Runtime.JAVA_21)
                .handler("com.portfolio.statuslambda.StatusHandler")
                .code(Code.fromAsset("services/status-lambda/target/status-lambda-0.0.1-SNAPSHOT.jar"))
                .memorySize(512)
                .timeout(Duration.seconds(15))
                .build();

        LambdaRestApi api = LambdaRestApi.Builder.create(this, "PortFolioApi")
                .handler(statusLambda) // referencia a la función lambda
                .proxy(true) // habilita la integración proxy
                .deployOptions(StageOptions.builder()
                        .stageName("prod")  //define el entorno de producción
                        .build())
                .build();

    }
}
