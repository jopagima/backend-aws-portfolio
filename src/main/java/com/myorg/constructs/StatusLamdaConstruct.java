package com.myorg.constructs;

import software.constructs.Construct;

import java.util.Map;

import software.amazon.awscdk.Duration;

import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;

public class StatusLamdaConstruct extends Construct {
    private final Function lambdaFunction;



    public StatusLamdaConstruct (Construct scope, String id, String tableName) {
        super(scope, id);

        this.lambdaFunction = Function.Builder.create(this, "StatusLambda")
                .runtime(Runtime.JAVA_21)
                .handler("com.portfolio.statuslambda.StatusHandler")
                .code(Code.fromAsset("services/status-lambda/target/status-lambda-0.0.1-SNAPSHOT.jar"))
                .memorySize(512)
                .timeout(Duration.seconds(15))
                .tracing(Tracing.ACTIVE) // Habilita Observabilidad con X-Ray [6]
                   .environment(Map.of(
                         "TABLE_NAME", tableName
                 ))
                .build();
    }

    public Function getLambdaFunction() {
        return lambdaFunction;
    }

}
