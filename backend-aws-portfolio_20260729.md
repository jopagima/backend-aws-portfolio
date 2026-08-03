# Fuente del proyecto: backend-aws-portfolio

> Generado automáticamente el 2026-07-29 17:49:32
> Incluye archivos: `.java`, `.xml`, `.properties`

---

## `src/test/java/com/myorg/BackendAwsPortfolioTest.java`

```java
package com.myorg;

 import software.amazon.awscdk.App;
import software.amazon.awscdk.assertions.Match;
import software.amazon.awscdk.assertions.Template;


import java.io.IOException;

 
import java.util.Map;

import org.junit.jupiter.api.Test;


 /*example test. To run these tests, uncomment this file, along with the
 example resource in java/src/main/java/com/myorg/BackendAwsPortfolioStack.java
 */
 public class BackendAwsPortfolioTest {

     @Test
     public void testStack() throws IOException {
         App app = new App();
         BackendAwsPortfolioStack stack = new BackendAwsPortfolioStack(app, "test");

         Template template = Template.fromStack(stack);

        
        /* 
        template.hasResourceProperties("AWS::SQS::Queue", new HashMap<String, Number>() {{
           put("VisibilityTimeout", 300);
         }});
         */
        template.hasResourceProperties("AWS::ApiGateway::RestApi", Match.anyValue());
        template.hasResourceProperties("AWS::Lambda::Function", Map.of(
            "Handler", "com.portfolio.statuslambda.StatusHandler",
            "Runtime", "java21"
        ));
     }
 }
```

## `src/main/java/com/myorg/BackendAwsPortfolioStack.java`

```java
package com.myorg;

import com.myorg.constructs.AuthConstruct;
import com.myorg.constructs.DynamoDBConstruct;
import com.myorg.constructs.PortfolioApiConstruct;
import com.myorg.constructs.StatusLamdaConstruct;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.AuthorizationType;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.MethodOptions;
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
        StatusLamdaConstruct statusService = new StatusLamdaConstruct(this, "StatusService",
                database.getTable().getTableName());

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
```

## `src/main/java/com/myorg/constructs/StatusLamdaConstruct.java`

```java
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
```

## `src/main/java/com/myorg/constructs/DynamoDBConstruct.java`

```java
package com.myorg.constructs;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.constructs.Construct;

public class DynamoDBConstruct extends Construct {

    private final Table table;

    public Table getTable() {
        return table;
    }

    public DynamoDBConstruct(final Construct scope, final String id) {
        super(scope, id);

        this.table = Table.Builder.create(this, "PortfolioTable")
                .partitionKey(Attribute.builder().name("UserId").type(AttributeType.STRING).build())
                .sortKey(Attribute.builder().name("Timestamp").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

    }

}
```

## `src/main/java/com/myorg/constructs/AuthConstruct.java`

```java
package com.myorg.constructs;

import software.constructs.Construct;
import software.amazon.awscdk.services.cognito.*;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.apigateway.CognitoUserPoolsAuthorizer;

import java.util.List;

public class AuthConstruct extends Construct {
    
 
   
    private final UserPool userPool;
    private final CognitoUserPoolsAuthorizer authorizer;
 


    public AuthConstruct(final Construct scope, final String id) {
        super(scope, id);

        // Create a Cognito User Pool
        this.userPool = UserPool.Builder.create(this, "PortfolioUserPool")
                .userPoolName("PortfolioUserPool")
                .selfSignUpEnabled(true)
                .signInAliases(SignInAliases.builder().email(true).build())
                .autoVerify(AutoVerifiedAttrs.builder().email(true).build())
                .passwordPolicy(PasswordPolicy.builder()
                        .minLength(8)
                        .requireLowercase(true)
                        .requireUppercase(true)
                        .requireDigits(true)
                        .build()
                )
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        // Create a app client para que la cli/app se conecten
        this.userPool.addClient("PortfolioAppClient", UserPoolClientOptions.builder()
                .generateSecret(false)
                .authFlows(AuthFlow.builder()
                        .userSrp(true) // ESTO HABILITA USER_SRP_AUTH
                        .userPassword(true)
                        .custom(true)
                        .build())
                .build());

        // Create a Cognito User Pool Authorizer for API Gateway
        this.authorizer = CognitoUserPoolsAuthorizer.Builder.create(this, "PortfolioAuthorizer")
                .cognitoUserPools(List.of(this.userPool))
                .authorizerName("PortfolioAuthorizer")
                .build();


    }

    public UserPool getUserPool() {
        return userPool;
    }

    public CognitoUserPoolsAuthorizer getAuthorizer() {
        return authorizer;
    }

}
```

## `src/main/java/com/myorg/constructs/PortfolioApiConstruct.java`

```java
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
```

## `src/main/java/com/myorg/BackendAwsPortfolioApp.java`

```java
package com.myorg;

import software.amazon.awscdk.App;

import software.amazon.awscdk.StackProps;



public class BackendAwsPortfolioApp {
    public static void main(final String[] args) {
        App app = new App();

        new BackendAwsPortfolioStack(app, "BackendAwsPortfolioStack", StackProps.builder()
                // If you don't specify 'env', this stack will be environment-agnostic.
                // Account/Region-dependent features and context lookups will not work,
                // but a single synthesized template can be deployed anywhere.

                // Uncomment the next block to specialize this stack for the AWS Account
                // and Region that are implied by the current CLI configuration.
                /*
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                */

                // Uncomment the next block if you know exactly what Account and Region you
                // want to deploy the stack to.
                /*
                .env(Environment.builder()
                        .account("123456789012")
                        .region("us-east-1")
                        .build())
                */

                // For more information, see https://docs.aws.amazon.com/cdk/latest/guide/environments.html
                .build());

        app.synth();
    }
}

```

## `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
         xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.myorg</groupId>
    <artifactId>backend-aws-portfolio</artifactId>
    <version>0.1</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <cdk.version>[2.260.0,3.0.0)</cdk.version>
        <constructs.version>[10.5.0,11.0.0)</constructs.version>
        <junit.version>5.7.1</junit.version>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <release>17</release>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <mainClass>com.myorg.BackendAwsPortfolioApp</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <!-- AWS Cloud Development Kit -->
        <dependency>
            <groupId>software.amazon.awscdk</groupId>
            <artifactId>aws-cdk-lib</artifactId>
            <version>${cdk.version}</version>
        </dependency>

        <dependency>
            <groupId>software.constructs</groupId>
            <artifactId>constructs</artifactId>
            <version>${constructs.version}</version>
        </dependency>

        <dependency>
          <groupId>org.junit.jupiter</groupId>
          <artifactId>junit-jupiter</artifactId>
          <version>${junit.version}</version>
          <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

## `services/status-lambda/src/test/java/com/portfolio/statuslambda/StatusHandlerTests.java`

```java
package com.portfolio.statuslambda;



import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import com.portfolio.statuslambda.StatusHandler; 
import com.portfolio.statuslambda.StatusService; 

import org.mockito.Mockito;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import static org.junit.jupiter.api.Assertions.*;

class StatusHandlerTests {
	
	@Test
	@DisplayName("La lógica de negocio debe devolver el mensaje de estado correcto")
	public void testProcessBusinessLogicBusinessLogicWithMock() {
        // 1. Creamos un Mock del cliente de AWS (no necesita región) [8]
        DynamoDbClient mockDdb = Mockito.mock(DynamoDbClient.class);
        
        // 2. Inyectamos el mock en el servicio
        StatusService service = new StatusService(mockDdb);
        
        // 3. Ejecutamos la lógica
        service.recordAccess("test-user", "2023-01-01T00:00:00Z");
        
        // 4. Verificamos que se llamó al putItem con los parámetros correctos
        // Verificamos que se intentó llamar a DynamoDB una vez [9, 10]
        Mockito.verify(mockDdb, Mockito.times(1)).putItem(Mockito.any(software.amazon.awssdk.services.dynamodb.model.PutItemRequest.class));
    }	
	
 /**/


}
```

## `services/status-lambda/src/test/java/com/portfolio/statuslambda/StatusServiceTest.java`

```java
package com.portfolio.statuslambda;


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*; 




// Imports para JUnit 5 (Corrigen class BeforeEach y method assertFalse)
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

// Imports para Mockito (Corrigen class ArgumentCaptor)
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

// Imports de utilidades de Java (Corrige variable Collections)
import java.util.Collections;
import java.util.List;
import java.util.Map;


import com.portfolio.statuslambda.StatusService; 

public class StatusServiceTest {

    DynamoDbClient mockDdb;
    StatusService service;

    @BeforeEach
    void setup() {
            // 1. Creamos un Mock del cliente de AWS (no necesita región) [8]
        mockDdb = Mockito.mock(DynamoDbClient.class);
        service = new StatusService(mockDdb);
    }

    @Test
    public void testGetStatusMessage() {
 
        String result = service.getStatusMessage();
        assertEquals("Service is running and access recorded", result);
    }

    @Test
    public void testRecordedAndRetrieveFlow() {
        String userId = "user-tdd";
        String timestamp = java.time.Instant.now().toString();

        //1. ejectuar la escritura
        service.recordAccess(userId, timestamp);

        //2. verificar que se llamó al putItem  con los parámetros correctos
        ArgumentCaptor<PutItemRequest> argumentCaptor = ArgumentCaptor.forClass(PutItemRequest.class);
        Mockito.verify(mockDdb, Mockito.times(1)).putItem(argumentCaptor.capture());
        assertEquals(userId, argumentCaptor.getValue().item().get("UserId").s());

        //3. Simular la resupuesta de DynamoDB para la consulta
        QueryResponse mockResponse = QueryResponse.builder()
                .items(Collections.singletonList(
                        Map.of("UserId", AttributeValue.builder().s(userId).build(),
                               "Timestamp", AttributeValue.builder().s(timestamp).build())))
                .build();
        Mockito.when(mockDdb.query(Mockito.any(QueryRequest.class))).thenReturn(mockResponse);


        //4. Ejecutar consulta y validar

        List<Map<String, AttributeValue>> results = service.getAccessLogs(userId);
        assertFalse(results.isEmpty());
        assertEquals(userId, results.get(0).get("UserId").s());
        

    }

    @Test
    public void shouldExtractUserIdFromCognitoClaims() {
        //1. simular la estructura que envía API Gateway + Cognito
        Map<String, Object> claims = Map.of(
            "sub", "user-12345-uuid",
            "email", "test@porfolio.com"
        );

        Map<String, Object> authorizer = Map.of("claims", claims);

        //2. Ejecutar la lógica de extacción 
        String extractedId = service.extractUserIdFromEvent(authorizer);
        assertEquals("user-12345-uuid", extractedId);
    }


}
```

## `services/status-lambda/src/main/java/com/portfolio/statuslambda/StatusService.java`

```java
package com.portfolio.statuslambda;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*; 
import java.time.Instant;
import java.util.List;
import java.util.Map;


public class StatusService {

    private  final DynamoDbClient ddb;
    private static final String TABLE_NAME = System.getenv("TABLE_NAME");


    // Constructor para inyección (Senior practice)
    public StatusService(DynamoDbClient ddb) {
        this.ddb = ddb;
    }

    public String getStatusMessage() {

        return "Service is running and access recorded";
    }

    public void recordAccess(String userId, String timestamp) {
        PutItemRequest request = PutItemRequest.builder()
        .tableName(TABLE_NAME)
        .item(Map.of(
            "UserId", AttributeValue.builder().s(userId).build(),
            "Timestamp", AttributeValue.builder().s(timestamp).build()
        )).build();

        // Operación de escritura en el plano de datos [20]
        ddb.putItem(request);
    }

    public List<Map<String, AttributeValue>> getAccessLogs(String userId) {
        // Construir la consulta para obtener los registros de acceso del usuario
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression("UserId = :userId")
                .expressionAttributeValues(Map.of(
                    ":userId", AttributeValue.builder().s(userId).build()))
                .build();

        // Ejecutar la consulta y devolver los resultados
        return ddb.query(queryRequest).items();
    }

    /**
    * Extrae el identificador único (sub) de los claims de Cognito [4, 5]
    * @param authorizerContext El mapa 'authorizer' proveniente del requestContext
    * @return El UUID del usuario o 'anonymous' si no hay claims
    */
    public String extractUserIdFromEvent(Map<String, Object> authorizerContext) {

        if(authorizerContext == null || !authorizerContext.containsKey("claims")) {
            return "anonymous-user";
        }

        //El sdk de API Gateway + Cognito envía un mapa con la estructura de claims, 
        // de donde se puede extraer el sub (UUID del usuario)
        @SuppressWarnings("unchecked")
        Map<String, Object> claims = (Map<String, Object>) authorizerContext.get("claims");

        // El claim 'sub' es el identificador único del usuario en Cognito, si no existe se retorna un valor por defecto
        return claims.getOrDefault("sub", "anonymous-user").toString();
    }

}
```

## `services/status-lambda/src/main/java/com/portfolio/statuslambda/StatusHandler.java`

```java
package com.portfolio.statuslambda;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;


public class StatusHandler implements RequestHandler <APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>  {


	 // Inicializamos el cliente real aquí para el entorno de AWS [6, 7]
    private final StatusService statusService = new StatusService(DynamoDbClient.create());


	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
	

		//Extraer el userId del contexto de autorización (si está disponible)
		Map<String, Object> authorizerContext = (Map<String, Object>) input.getRequestContext().getAuthorizer();
		String userId = statusService.extractUserIdFromEvent(authorizerContext);
		context.getLogger().log("UserId: " + userId);

		String message = processBusinessLogic(userId);

		return new APIGatewayProxyResponseEvent()
		.withStatusCode(200)
				.withHeaders(Map.of(
				"Access-Control-Allow-Origin", "http://localhost:3000",
				"Content-Type", "application/json"
		))
		.withBody("{\"status\": \"" + message + "\"}")
		.withIsBase64Encoded(false);
	}


	 


	public String processBusinessLogic(String userId) {
		// Implement your business logic here
		String timestamp = java.time.Instant.now().toString();
        //1. ejectuar la escritura
        statusService.recordAccess(userId, timestamp);
		return statusService.getStatusMessage();
	}



}
```

## `services/status-lambda/src/main/resources/log4j2.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration packages="com.amazonaws.services.lambda.runtime.log4j2">
  <Appenders>
    <Lambda name="Lambda">
      <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} %X{LambdaRequestId} %-5p %c{1}:%L - %m%n" />
    </Lambda>
  </Appenders>
  <Loggers>
    <Root level="INFO">
      <AppenderRef ref="Lambda" />
    </Root>
  </Loggers>
</Configuration>```

## `services/status-lambda/src/main/resources/application.properties`

```properties
spring.application.name=status-lambda
```

## `services/status-lambda/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
		<groupId>com.portfolio</groupId>
	<artifactId>status-lambda</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name/>
	<description/>
	<url/>
	<licenses>
		<license/>
	</licenses>
	<developers>
		<developer/>
	</developers>
	<scm>
		<connection/>
		<developerConnection/>
		<tag/>
		<url/>
	</scm>
    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>bom</artifactId>
            <version>2.25.0</version> <!-- O la versión más reciente -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>    
	<dependencies>
     <!-- Core de AWS Lambda -->
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-lambda-java-core</artifactId>
            <version>1.2.3</version>
        </dependency>
        <!-- Eventos para interactuar con API Gateway -->
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-lambda-java-events</artifactId>
            <version>3.11.4</version>
        </dependency>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>dynamodb</artifactId>
        </dependency>        
        <!-- JUnit 5 para tus tests de Senior -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>5.11.0</version>
            <scope>test</scope>
        </dependency>  
        <!-- Implementación de Logging para Lambda -->
        <dependency>
            <groupId>com.amazonaws</groupId>
            <artifactId>aws-lambda-java-log4j2</artifactId>
            <version>1.6.0</version>
        </dependency>
        <!-- SLF4J a Log4j2 Bridge (para que el SDK use nuestra config) -->
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-slf4j2-impl</artifactId>
            <version>2.20.0</version>
        </dependency>   
                
	</dependencies>

	<build>
		<plugins>
            <!-- Plugin para ejecutar tests en Java 21 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>        
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.0</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
							<goal>shade</goal>
						</goals>
                        <configuration>
                            <transformers>
                                <!-- ESTA ES LA PARTE QUE SOLUCIONA EL ERROR [d] -->
                                <transformer implementation="org.apache.logging.log4j.maven.plugins.shade.transformer.Log4j2PluginCacheFileTransformer"/>
                            </transformers>
                        </configuration>                        
                    </execution>
                </executions>
                <dependencies>
                    <!-- Dependencia necesaria para que el transformer funcione -->
                    <dependency>
                        <groupId>org.apache.logging.log4j</groupId>
                        <artifactId>log4j-transform-maven-shade-plugin-extensions</artifactId>
                        <version>0.1.0</version>
                    </dependency>
                </dependencies>

            </plugin>
		</plugins>
	</build>

</project>
```

## `services/status-lambda/dependency-reduced-pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.portfolio</groupId>
  <artifactId>status-lambda</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <developers>
    <developer />
  </developers>
  <licenses>
    <license />
  </licenses>
  <scm />
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.5</version>
      </plugin>
      <plugin>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.5.0</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
            <configuration>
              <transformers>
                <transformer />
              </transformers>
            </configuration>
          </execution>
        </executions>
        <dependencies>
          <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-transform-maven-shade-plugin-extensions</artifactId>
            <version>0.1.0</version>
          </dependency>
        </dependencies>
      </plugin>
    </plugins>
  </build>
  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.10.0</version>
      <scope>test</scope>
      <exclusions>
        <exclusion>
          <artifactId>junit-jupiter-api</artifactId>
          <groupId>org.junit.jupiter</groupId>
        </exclusion>
        <exclusion>
          <artifactId>junit-jupiter-params</artifactId>
          <groupId>org.junit.jupiter</groupId>
        </exclusion>
        <exclusion>
          <artifactId>junit-jupiter-engine</artifactId>
          <groupId>org.junit.jupiter</groupId>
        </exclusion>
      </exclusions>
    </dependency>
    <dependency>
      <groupId>org.mockito</groupId>
      <artifactId>mockito-core</artifactId>
      <version>5.11.0</version>
      <scope>test</scope>
      <exclusions>
        <exclusion>
          <artifactId>byte-buddy</artifactId>
          <groupId>net.bytebuddy</groupId>
        </exclusion>
        <exclusion>
          <artifactId>byte-buddy-agent</artifactId>
          <groupId>net.bytebuddy</groupId>
        </exclusion>
        <exclusion>
          <artifactId>objenesis</artifactId>
          <groupId>org.objenesis</groupId>
        </exclusion>
      </exclusions>
    </dependency>
  </dependencies>
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>bom</artifactId>
        <version>2.25.0</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
  <properties>
    <maven.compiler.target>21</maven.compiler.target>
    <maven.compiler.source>21</maven.compiler.source>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>
</project>
```

