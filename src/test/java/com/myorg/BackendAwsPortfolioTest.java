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
