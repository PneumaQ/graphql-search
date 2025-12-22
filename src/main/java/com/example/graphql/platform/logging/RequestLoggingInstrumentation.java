package com.example.graphql.platform.logging;

import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicLong;

public class RequestLoggingInstrumentation extends SimpleInstrumentation {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInstrumentation.class);
    private static final AtomicLong requestCounter = new AtomicLong(0);

    @Override
    public InstrumentationContext<graphql.ExecutionResult> beginExecution(InstrumentationExecutionParameters parameters, InstrumentationState state) {
        long id = requestCounter.incrementAndGet();
        String instanceId = Integer.toHexString(System.identityHashCode(this));
        String threadInfo = Thread.currentThread().getName() + " [" + Thread.currentThread().getId() + "]";
        String query = parameters.getQuery().replaceAll("\\s+", " ").trim();
        if (query.length() > 100) query = query.substring(0, 97) + "...";

        log.info("================================================================================");
        log.info(">>> START REQUEST #{} (Thread: {}) [Logger:{}]: {}", id, threadInfo, instanceId, query);
        log.info("================================================================================");

        return new InstrumentationContext<>() {
            @Override
            public void onDispatched() {}

            @Override
            public void onCompleted(graphql.ExecutionResult result, Throwable t) {
                log.info("--------------------------------------------------------------------------------");
                log.info("<<< END GRAPHQL REQUEST #{} (Thread: {})", id, threadInfo);
                log.info("--------------------------------------------------------------------------------");
                QueryContext.clear();
            }
        };
    }
}
