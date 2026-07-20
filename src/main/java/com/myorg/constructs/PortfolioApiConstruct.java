package com.myorg.constructs;

import org.jetbrains.annotations.NotNull;

import software.amazon.awscdk.services.apigateway.IResource;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.lambda.IFunction;
import software.constructs.Construct;

public class PortfolioApiConstruct extends Construct {

     private final LambdaRestApi api; // Este es el objeto real de AWS

    public PortfolioApiConstruct(@NotNull Construct scope, @NotNull String id, IFunction handler) {
        super(scope, id);
          // Se construye el LambdaRestApi y se asigna a la variable de clase 'api'
        this.api = LambdaRestApi.Builder.create(this, "PortFolioApi")
                .handler(handler) // referencia a la función lambda
                .proxy(true) // habilita la integración proxy
                .deployOptions(StageOptions.builder()
                        .stageName("prod")  //define el entorno de producción
                        .build())
                .build();
    }
    /**
     * Expone el recurso raíz de la API para permitir la configuración de 
     * métodos y autorizadores desde el Stack principal.
     */
    public IResource getRoot() {
        return this.api.getRoot();
    }    

}
