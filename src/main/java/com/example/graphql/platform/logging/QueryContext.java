package com.example.graphql.platform.logging;

public class QueryContext {
    private static final ThreadLocal<String> currentAction = new ThreadLocal<>();

    public static void set(String action) {
        currentAction.set(action);
    }

    public static String get() {
        return currentAction.get() != null ? currentAction.get() : "Generic Action";
    }

    public static void clear() {
        currentAction.remove();
    }
}
