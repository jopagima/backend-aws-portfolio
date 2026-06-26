package com.myorg.constructs;

import org.jetbrains.annotations.NotNull;

import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.lambda.IFunction;
import software.constructs.Construct;

public class PortfolioApiConstruct extends Construct {

    public PortfolioApiConstruct(@NotNull Construct scope, @NotNull String id, IFunction handler) {
        super(scope, id);
        //TODO Auto-generated constructor stub
        LambdaRestApi.Builder.create(this, "PortFolioApi")
                .handler(handler) // referencia a la función lambda
                .proxy(true) // habilita la integración proxy
                .deployOptions(StageOptions.builder()
                        .stageName("prod")  //define el entorno de producción
                        .build())
                .build();
    }

}
