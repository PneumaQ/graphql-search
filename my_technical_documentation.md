# Technical Documentation: Universal Search Request Lifecycle

This document provides a deep-dive architectural analysis of how search requests are processed in the `graphql-search` platform. It follows the "Universal Search" pattern, which allows for dynamic, schemaless extensibility while maintaining a consistent GraphQL interface.

## 1. High-Level Overview
The system is built on a **Universal Search Platform** philosophy. Instead of creating specific GraphQL fields for every possible search attribute, we expose a single entry point (`searchProducts`) that accepts a structured list of conditions and returns a polymorphic result set. 

The architecture bridges three distinct worlds: the **GraphQL Schema** (typed and descriptive), the **Metadata Registry** (which defines dynamic extensions), and **Elasticsearch** (the high-performance search engine). The lifecycle is designed to be "Registry-First," where the system consults a Caffeine-cached metadata layer to determine how to build queries and interpret results without ever needing to perform expensive table scans on the primary data store.

## 2. Detailed Step-by-Step Flow

### Step 1: Request Entry (Spring GraphQL)
- **Class:** `ProductController`
- **Method:** `searchProducts(...)`
- **Reasoning:** The request enters via an HTTP POST to `/graphql`. Spring GraphQL's `AnnotatedControllerConfigurer` routes the request here based on the `@QueryMapping` annotation. This layer is responsible for translating GraphQL scalars and input types into standard Java types (e.g., `List<SearchCondition>`).

### Step 2: Metadata Discovery (The Registry)
- **Class:** `CustomFieldService`
- **Method:** `getFieldDefinitions("PRODUCT")`
- **Reasoning:** Before building the search query, the system must know which "Custom Attributes" exist and what their data types are. It queries the **Caffeine-backed** service. If the definitions are not in memory, it performs a single SQL lookup on the `custom_field_definition` table. This avoids the "Table Scan" performance trap by providing a dedicated source of truth for the extended schema.

### Step 3: Coordinate Search (Repository Orchestration)
- **Class:** `ProductSearchRepository`
- **Method:** `search(...)`
- **Reasoning:** This is the conductor. It holds the `SearchSession` (the Hibernate Search entry point). It fetches the cached metadata and prepares three distinct parts of the search: the **Predicate** (filter), the **Sort**, and the **Aggregations** (facets/stats).

### Step 4: Predicate Translation (Universal Query Building)
- **Class:** `UniversalQueryBuilder`
- **Method:** `build(...)`
- **Reasoning:** This abstraction decouples GraphQL input logic from Hibernate Search internals. It iterates through the `SearchCondition` list. For each condition, it checks the Metadata Registry:
    - If the field is a **STRING**, it maps to a `*_keyword` index path for exact matches or a base path for full-text wildcards.
    - If the field is **NUMERIC**, it constructs a `range()` predicate.
    - This allows the same GraphQL input field (`eq: "5"`) to be treated as an Integer or a String depending on the Registry definition.

### Step 5: DSL Construction (Hibernate Search)
- **Module:** `hibernate-search-mapper-orm`
- **Reasoning:** Hibernate Search takes the Java-based Predicate, Sort, and Aggregation objects and converts them into a massive, complex **Elasticsearch JSON Query**. This includes the "Full-Text" logic (`simpleQueryString`) which is applied across all searchable paths found in the Registry.

### Step 6: Elasticsearch Execution
- **External:** `Elasticsearch Server`
- **Reasoning:** Elasticsearch executes the query against its inverted index. It returns two things:
    1. A list of document **IDs** (the "Hits").
    2. A collection of **Aggregation Buckets** (raw counts and term distributions).

### Step 7: Aggregation Mapping (Facets & Stats)
- **Class:** `ProductSearchRepository`
- **Methods:** `getFacetPath`, `calculateNumericStats`
- **Reasoning:** Hibernate Search returns raw term buckets. The repository maps these into "Clean JSON Maps." 
    - **Facets:** Mapped directly to `{ "Value": Count }`.
    - **Stats:** Since certain search backends lack a native "Stats" aggregation for all types, the system performs a manual "term-to-stats" calculation (Min, Max, Avg, Sum) in Java using the term distribution returned by the engine.

### Step 8: Entity Loading (Hibernate ORM)
- **Class:** `Product`
- **Mechanism:** `Loading Options / @BatchSize`
- **Reasoning:** Elasticsearch only knows IDs. Hibernate Search now triggers Hibernate ORM to fetch the actual `Product` entities from the database (`select * from product where id in (...)`). 
    - **N+1 Mitigation:** To prevent a separate query for each product's `reviews` collection, the `Product` entity uses `@BatchSize(size = 20)`, allowing the ORM to fetch reviews for multiple products in a single database round-trip.

### Step 9: Result Wrapping & Serialization
- **Class:** `ProductController.ProductSearchResult` (Record)
- **Reasoning:** The hits, facets, and stats are wrapped into a Java Record. The `ExtendedScalars.Json` logic from `GraphQlConfig` ensures that the flexible maps are serialized as raw JSON objects in the GraphQL response, avoiding the need for a rigid, verbose GraphQL schema for every possible attribute.

---

## 3. Data Transformation Summary

| Phase | State | Representation |
| :--- | :--- | :--- |
| **Client** | GraphQL Query | `filter: [{ field: "color", eq: "black" }]` |
| **Controller** | Java POJO | `List<SearchCondition>` |
| **Repository** | Metadata Context | `field: "color" -> dataType: STRING` |
| **QueryBuilder** | HS Predicate | `f.match().field("custom_attributes.color_keyword").matching("black")` |
| **Engine** | Elasticsearch JSON | `{"term": {"custom_attributes.color_keyword": "black"}}` |
| **Database** | JPA Entities | `Product` object with `Map<String, String>` |
| **Response** | GraphQL JSON | `"facets": { "color": { "black": 12 } }` |

---

## 4. Extensibility and Performance

### Extensibility: The "No-Code" Extension
Because the system uses a **Registry + JSONB** approach, adding a new searchable property to the entire stack requires zero code changes:
1. Insert a row into `custom_field_definition`.
2. Add the key to a `Product`'s JSON map.
3. The property is immediately searchable, sortable, and aggregable.

### Performance: Caching and Sparse Data
- **Caching:** The use of **Caffeine** in `CustomFieldService` ensures that the cost of "Discovering" dynamic fields is paid only once every 10 minutes (or until eviction), making the search repository extremely lean.
- **Sparse Data:** We do not populate `null` keys for missing attributes. Hibernate Search naturally handles "missing" fields, ensuring the index size stays proportional to actual data, not the theoretical schema size.
- **Batch Loading:** By using Hibernate's `@BatchSize`, we optimize the "Loading" phase of search, transforming what would be a series of sequential database queries into efficient, batched operations.

### Limitations
- **Type Safety:** The GraphQL input uses `String` for most filter values. While the `UniversalQueryBuilder` performs type conversion, errors (e.g., passing "ABC" to an INT field) are caught at runtime rather than at the GraphQL validation layer.
- **Stat Calculations:** Manual calculation of statistics in the repository is a trade-off for backend-agnostic behavior (supporting both Lucene and Elasticsearch). For massive datasets, this should be offloaded to native Elasticsearch `stats` aggregations.
