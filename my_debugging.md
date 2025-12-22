10:35:09.749 [http-nio-8080-exec-7] INFO  c.e.g.p.l.RequestLoggingInstrumentation - ================================================================================
10:35:09.749 [http-nio-8080-exec-7] INFO  c.e.g.p.l.RequestLoggingInstrumentation - >>> START REQUEST #6 (Thread: http-nio-8080-exec-7 [97]) [Logger:611c1f92]: query { searchProducts(filter: [ { field: "price", gt: 50.0 } ]) { results { name price } } }
10:35:09.749 [http-nio-8080-exec-7] INFO  c.e.g.p.l.RequestLoggingInstrumentation - ================================================================================
10:35:09.760 [http-nio-8080-exec-7] DEBUG org.hibernate.SQL - 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-7 [97] */ /* <criteria> */ select
        p1_0.id,
        p1_0.brand_id,
        p1_0.category,
        p1_0.custom_attributes,
        p1_0.internal_stock_code,
        p1_0.name,
        p1_0.price 
    from
        product p1_0 
    where
        p1_0.id in (?, ?, ?)
10:35:09.766 [http-nio-8080-exec-7] INFO  c.e.g.p.l.RequestLoggingInstrumentation - --------------------------------------------------------------------------------
10:35:09.766 [http-nio-8080-exec-7] INFO  c.e.g.p.l.RequestLoggingInstrumentation - <<< END GRAPHQL REQUEST #6 (Thread: http-nio-8080-exec-7 [97])
10:35:09.766 [http-nio-8080-exec-7] INFO  c.e.g.p.l.RequestLoggingInstrumentation - --------------------------------------------------------------------------------
10:35:09.749 [http-nio-8080-exec-7] INFO  c.e.g.p.l.RequestLoggingInstrumentation - ================================================================================
10:35:09.749 [http-nio-8080-exec-7] INFO  c.e.g.p.l.RequestLoggingInstrumentation - >>> START REQUEST #6 (Thread: http-nio-8080-exec-7 [97]) [Logger:611c1f92]: query { searchProducts(filter: [ { field: "price", gt: 50.0 } ]) { results { name price } } }
10:35:09.749 [http-nio-8080-exec-7] INFO  c.e.g.p.l.RequestLoggingInstrumentation - ================================================================================
10:35:09.760 [http-nio-8080-exec-7] DEBUG org.hibernate.SQL - 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-7 [97] */ /* <criteria> */ select
        p1_0.id,
        p1_0.brand_id,
        p1_0.category,
        p1_0.custom_attributes,
        p1_0.internal_stock_code,
        p1_0.name,
        p1_0.price 
    from
        product p1_0 
    where
        p1_0.id in (?, ?, ?)
10:35:09.766 [http-nio-8080-exec-7] INFO  c.e.g.p.l.RequestLoggingInstrumentation - --------------------------------------------------------------------------------
10:35:09.766 [http-nio-8080-exec-7] INFO  c.e.g.p.l.RequestLoggingInstrumentation - <<< END GRAPHQL REQUEST #6 (Thread: http-nio-8080-exec-7 [97])
10:35:09.766 [http-nio-8080-exec-7] INFO  c.e.g.p.l.RequestLoggingInstrumentation - --------------------------------------------------------------------------------