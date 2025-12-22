package com.example.graphql.platform.logging;

import org.hibernate.resource.jdbc.spi.StatementInspector;

public class QueryContextInspector implements StatementInspector {
    @Override
    public String inspect(String sql) {
        if (sql == null) return null;
        String threadInfo = Thread.currentThread().getName() + " [" + Thread.currentThread().getId() + "]";
        // Inject the current action and thread info into the SQL as a comment
        return "/* Action: " + QueryContext.get() + " | Thread: " + threadInfo + " */ " + sql;
    }
}
