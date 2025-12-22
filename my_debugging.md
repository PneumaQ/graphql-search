2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : >>> START REQUEST #5 (Thread: http-nio-8080-exec-3 [93]) [Logger:bf3aa52]: query { searchProducts(text: "Navy") { results { name category reviews { author comment rating } ...
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.475-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ /* <criteria> */ select
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
        p1_0.id in (?)
2025-12-22T13:41:58.478-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ select
        r1_0.product_id,
        r1_0.id,
        r1_0.author,
        r1_0.comment,
        r1_0.rating 
    from
        review r1_0 
    where
        r1_0.product_id=?
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : --------------------------------------------------------------------------------
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : <<< END GRAPHQL REQUEST #5 (Thread: http-nio-8080-exec-3 [93])
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : --------------------------------------------------------------------------------
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : >>> START REQUEST #5 (Thread: http-nio-8080-exec-3 [93]) [Logger:bf3aa52]: query { searchProducts(text: "Navy") { results { name category reviews { author comment rating } ...
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.475-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ /* <criteria> */ select
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
        p1_0.id in (?)
2025-12-22T13:41:58.478-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ select
        r1_0.product_id,
        r1_0.id,
        r1_0.author,
        r1_0.comment,
        r1_0.rating 
    from
        review r1_0 
    where
        r1_0.product_id=?
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : --------------------------------------------------------------------------------
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : <<< END GRAPHQL REQUEST #5 (Thread: http-nio-8080-exec-3 [93])
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : --------------------------------------------------------------------------------
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : >>> START REQUEST #5 (Thread: http-nio-8080-exec-3 [93]) [Logger:bf3aa52]: query { searchProducts(text: "Navy") { results { name category reviews { author comment rating } ...
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.475-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ /* <criteria> */ select
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
        p1_0.id in (?)
2025-12-22T13:41:58.478-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ select
        r1_0.product_id,
        r1_0.id,
        r1_0.author,
        r1_0.comment,
        r1_0.rating 
    from
        review r1_0 
    where
        r1_0.product_id=?
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : --------------------------------------------------------------------------------
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : <<< END GRAPHQL REQUEST #5 (Thread: http-nio-8080-exec-3 [93])
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : --------------------------------------------------------------------------------
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : >>> START REQUEST #5 (Thread: http-nio-8080-exec-3 [93]) [Logger:bf3aa52]: query { searchProducts(text: "Navy") { results { name category reviews { author comment rating } ...
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.475-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ /* <criteria> */ select
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
        p1_0.id in (?)
2025-12-22T13:41:58.478-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ select
        r1_0.product_id,
        r1_0.id,
        r1_0.author,
        r1_0.comment,
        r1_0.rating 
    from
        review r1_0 
    where
        r1_0.product_id=?
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : --------------------------------------------------------------------------------
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : <<< END GRAPHQL REQUEST #5 (Thread: http-nio-8080-exec-3 [93])
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : --------------------------------------------------------------------------------
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : >>> START REQUEST #5 (Thread: http-nio-8080-exec-3 [93]) [Logger:bf3aa52]: query { searchProducts(text: "Navy") { results { name category reviews { author comment rating } ...
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.475-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ /* <criteria> */ select
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
        p1_0.id in (?)
2025-12-22T13:41:58.478-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ select
        r1_0.product_id,
        r1_0.id,
        r1_0.author,
        r1_0.comment,
        r1_0.rating 
    from
        review r1_0 
    where
        r1_0.product_id=?
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : --------------------------------------------------------------------------------
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : <<< END GRAPHQL REQUEST #5 (Thread: http-nio-8080-exec-3 [93])
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : --------------------------------------------------------------------------------
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : >>> START REQUEST #5 (Thread: http-nio-8080-exec-3 [93]) [Logger:bf3aa52]: query { searchProducts(text: "Navy") { results { name category reviews { author comment rating } ...
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.475-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ /* <criteria> */ select
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
        p1_0.id in (?)
2025-12-22T13:41:58.478-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ select
        r1_0.product_id,
        r1_0.id,
        r1_0.author,
        r1_0.comment,
        r1_0.rating 
    from
        review r1_0 
    where
        r1_0.product_id=?
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : --------------------------------------------------------------------------------
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : <<< END GRAPHQL REQUEST #5 (Thread: http-nio-8080-exec-3 [93])
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : --------------------------------------------------------------------------------
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : >>> START REQUEST #5 (Thread: http-nio-8080-exec-3 [93]) [Logger:bf3aa52]: query { searchProducts(text: "Navy") { results { name category reviews { author comment rating } ...
2025-12-22T13:41:58.460-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : ================================================================================
2025-12-22T13:41:58.475-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ /* <criteria> */ select
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
        p1_0.id in (?)
2025-12-22T13:41:58.478-05:00 DEBUG 17576 --- [graphql] [nio-8080-exec-3] org.hibernate.SQL                        : 
    /* Action: Hibernate Search - Phase 2 (
        Entity Loading
    ) | Thread: http-nio-8080-exec-3 [93] */ select
        r1_0.product_id,
        r1_0.id,
        r1_0.author,
        r1_0.comment,
        r1_0.rating 
    from
        review r1_0 
    where
        r1_0.product_id=?
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : --------------------------------------------------------------------------------
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : <<< END GRAPHQL REQUEST #5 (Thread: http-nio-8080-exec-3 [93])
2025-12-22T13:41:58.479-05:00  INFO 17576 --- [graphql] [nio-8080-exec-3] c.e.g.p.l.RequestLoggingInstrumentation  : -------------------------------------------------------------------------------