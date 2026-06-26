package com.myorg;

import com.myorg.constructs.PortfolioApiConstruct;
import com.myorg.constructs.StatusLamdaConstruct;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.servicecatalog.Portfolio;
import software.constructs.Construct;

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
        StatusLamdaConstruct statusService = new StatusLamdaConstruct(this, "StatusService");

        new PortfolioApiConstruct(this, "PortFolioApi", statusService.getLambdaFunction());


    }
}
