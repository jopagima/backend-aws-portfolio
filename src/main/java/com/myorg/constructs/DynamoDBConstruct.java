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
