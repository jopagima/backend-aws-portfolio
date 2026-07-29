package com.myorg.constructs;

import org.jetbrains.annotations.NotNull;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

public class PortfolioAccessQueue extends Construct {

    private final Queue portfolioAccessQueue ;

    public Queue getPortfolioAccessQueue() {
        return portfolioAccessQueue;
   }       

    public PortfolioAccessQueue(@NotNull Construct scope, @NotNull String id) {
        super(scope, id);
        //TODO Auto-generated constructor stub

        Queue portfolioAccessDLQ = Queue.Builder.create(this, "PortfolioAccessDLQ")
                .retentionPeriod(Duration.days(14))
                .build();

        this.portfolioAccessQueue = Queue.Builder.create(this, id)
                .visibilityTimeout(Duration.seconds(30))
                .deadLetterQueue(DeadLetterQueue.builder()
                        .queue(portfolioAccessDLQ)
                        .maxReceiveCount(3)
                        .build())
                .build();
    }

}
