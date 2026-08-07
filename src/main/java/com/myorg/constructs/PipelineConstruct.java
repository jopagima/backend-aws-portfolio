package com.myorg.constructs;
import software.amazon.awscdk.SecretValue;
import software.amazon.awscdk.pipelines.CodeBuildOptions;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.CodePipelineSource;
import software.amazon.awscdk.pipelines.GitHubSourceOptions;
import software.amazon.awscdk.pipelines.ShellStep;
import software.amazon.awscdk.services.codebuild.BuildEnvironment;
import software.amazon.awscdk.services.codebuild.LinuxBuildImage;
import software.constructs.Construct;
import java.util.List;

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
            // --- ENFOQUE SENIOR: ESPECIFICAR ENTORNO JAVA 17 (única versión usada en todo el proyecto) ---
            .codeBuildDefaults(CodeBuildOptions.builder()
                .buildEnvironment(BuildEnvironment.builder()
                    .buildImage(LinuxBuildImage.AMAZON_LINUX_2023_4) // Imagen AL2023; javac por defecto es Java 17
                    .build())
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
                    "npm install -g aws-cdk",
                    "cdk synth"
                ))
                .build())
            .build();
        
    }

}
