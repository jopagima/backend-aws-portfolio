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
                        .userPassword(true)
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
