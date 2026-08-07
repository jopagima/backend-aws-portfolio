package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;
import com.myorg.constructs.PipelineConstruct;

public class PipeLineStack extends Stack {
    public PipeLineStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public PipeLineStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // The code that defines your stack goes here
        new PipelineConstruct(this, "PortfolioPipelineLogic");
    }

}
