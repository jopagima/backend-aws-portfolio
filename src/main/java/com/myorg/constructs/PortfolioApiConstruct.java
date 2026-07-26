package com.myorg.constructs;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import software.amazon.awscdk.services.apigateway.CorsOptions;
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
                .proxy(false) // deshabilita la integración proxy
                .deployOptions(StageOptions.builder()
                        .stageName("prod")  //define el entorno de producción
                        .build())
                .build();
            // CONFIGURACIÓN DE CORS
        this.api.getRoot().addCorsPreflight(CorsOptions.builder()
            .allowOrigins(List.of("http://localhost:3000")) // Origen de tu app React
            .allowMethods(List.of("GET", "POST", "OPTIONS")) // Métodos permitidos [2]
            .allowHeaders(List.of(
                    "Content-Type", 
                    "X-Amz-Date", 
                    "Authorization", // Vital para enviar el token de Cognito [6]
                    "X-Api-Key"
            ))
            .build());                
    }
    /**
     * Expone el recurso raíz de la API para permitir la configuración de 
     * métodos y autorizadores desde el Stack principal.
     */
    public IResource getRoot() {
        return this.api.getRoot();
    }    

}
