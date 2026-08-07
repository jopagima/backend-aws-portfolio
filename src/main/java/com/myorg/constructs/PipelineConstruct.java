package com.myorg.constructs;
import software.amazon.awscdk.SecretValue;
import software.amazon.awscdk.pipelines.CodeBuildOptions;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.CodePipelineSource;
import software.amazon.awscdk.pipelines.GitHubSourceOptions;
import software.amazon.awscdk.pipelines.ShellStep;
import software.amazon.awscdk.services.codebuild.BuildEnvironment;
import software.amazon.awscdk.services.codebuild.BuildSpec;
import software.amazon.awscdk.services.codebuild.LinuxBuildImage;
import software.constructs.Construct;
import java.util.List;
import java.util.Map;

public class PipelineConstruct extends Construct{

    public PipelineConstruct(Construct scope, String id) {
        super(scope, id);
  // Definimos la fuente (Source Stage) conectada a GitHub
        // El CDK buscará automáticamente el secreto 'github-token' que creaste

        CodePipelineSource source = CodePipelineSource.gitHub("jopagima/backend-aws-portfolio", "master", 
            GitHubSourceOptions.builder()
                .authentication(SecretValue.secretsManager("github-token"))
                .build());

        //Configuramoes la etapa de Sintesis (Synth stage)
        //Aqui conguramoes los comandos de compilación de maven para los microservicios de java
        CodePipeline pipeline = CodePipeline.Builder.create(this, id)
            .pipelineName("BackendAwsPortfolioPipeline")
            // --- ENFOQUE SENIOR: FIJAR EXPLÍCITAMENTE LAS VERSIONES DE RUNTIME ---
            // No confiar en los defaults de la imagen: se fija Java 21 (única versión
            // usada en todo el proyecto) y un Node.js reciente (requerido por la CLI
            // de aws-cdk, cuyo bundle WASM necesita soporte de WebAssembly reftypes).
            // NOTA: la imagen standard:4.0 (AMAZON_LINUX_2023_4) no incluye Corretto 21
            // como runtime instalable; Corretto 21 solo está disponible desde standard:5.0.
            .codeBuildDefaults(CodeBuildOptions.builder()
                .buildEnvironment(BuildEnvironment.builder()
                    .buildImage(LinuxBuildImage.AMAZON_LINUX_2023_5)
                    .build())
                .partialBuildSpec(BuildSpec.fromObject(Map.of(
                    "phases", Map.of(
                        "install", Map.of(
                            "runtime-versions", Map.of(
                                "java", "corretto21",
                                "nodejs", "20"
                            )
                        )
                    )
                )))
                .build())
            // --------------------------------------------------
            .synth(ShellStep.Builder.create("Synth")
                .input(source)
                .commands(List.of(
                    // 1. Compilar microservicio Status (Productor)
                    "cd services/status-lambda && mvn clean package",
                    // 2. Compilar microservicio Worker (Consumidor)
                    "cd ../worker-lambda && mvn clean package",
                    // 3. Regresar a la raíz y sintetizar infraestructura CDK
                    "cd ../..",
                    "npm install -g aws-cdk@2.1135.1",
                    "cdk synth"
                ))
                .build())
            .build();
        
    }

}
