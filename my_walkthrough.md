# End-to-End Walkthrough: The "Kitchen Sink" Search Request

This document details the precise execution flow of the **"Kitchen Sink" Master Query** from the Search Showcase UI to Elasticsearch and back.

## 1. The Trigger: Browser to Server
When you click **"Run Master Query"** in `index.html`, the following happens:
1.  **JavaScript Execution:** The `runQuery('q8', 'r8')` function extracts the GraphQL query string from the `<pre>` tag.
2.  **HTTP POST:** A `fetch` request is sent to `/graphql` with a JSON body:
    ```json
    { "query": "query { searchProducts(...) { ... } }" }
    ```
3.  **Spring Integration:** The request is intercepted by Spring's `GraphQlHttpHandler`. It extracts the query and hands it to the `ExecutionGraphQlService`.

## 2. The GraphQL Engine: Parsing & Validation
Spring GraphQL uses **GraphQL-Java** to process the request:
1.  **Parsing:** The query is parsed into an Abstract Syntax Tree (AST).
2.  **Validation:** The engine checks the query against `schema.graphqls`. It verifies that `searchProducts` exists, that the `filter` matches the `[SearchCondition]` input type, and that `JSON` scalars are valid.
3.  **Execution Plan:** The `AsyncExecutionStrategy` determines the sequence of "Data Fetchers" needed. Since `searchProducts` is a top-level field in `Query`, it looks for a mapping.

## 3. The Entry Point: `ProductController`
1.  **Discovery:** Spring's `AnnotatedControllerConfigurer` has already scanned `@Controller` beans. It finds the `@QueryMapping` for `searchProducts`.
2.  **Argument Binding:** The engine converts the GraphQL input (like `price: { gt: 10.0 }`) into Java objects (`List<SearchCondition>`) using standard Spring conversion logic.
3.  **Invocation:** The method `searchProducts(...)` is called. 
    - **Step:** It calculates `pageNum` and `pageSize`.
    - **Step:** It delegates to `productService.searchProducts`.

## 4. The Search Logic: `ProductSearchRepository`
This is where the heavy lifting occurs. The `search()` method executes the following:

### A. Dynamic Field Discovery (The Table Scan)
```java
Set<String> dynamicTextFields = fetchDynamicTextFields(entityManager);
```
- **Why:** To support "Google-like" search across custom attributes, the code needs to know every unique key currently in the `custom_attributes` JSON column (e.g., "color", "material").
- **How:** It executes a native SQL `SELECT custom_attributes FROM PRODUCT`.
- **Performance Impact:** This is a **full table scan**. If you have 1,000,000 products, it reads all 1,000,000 JSON blobs into memory just to find the keys.

### B. Query Building
- **UniversalQueryBuilder:** Translates your `[SearchCondition]` list into Hibernate Search Predicates. 
    - `gt: 10.0` becomes a `range().greaterThan(10.0)`.
- **Full-Text Search:** The `text: "Cotton"` argument is applied to `name`, `sku`, `category`, and every discovered dynamic field path (e.g., `custom_attributes.material_text`).

### C. Aggregations (Facets & Stats)
- **Facets:** For each key in `facetKeys` (category, color), it adds a `terms()` aggregation.
- **Stats:** Since Lucene/Local environments don't support the full Elasticsearch "stats" aggregation natively in all versions, the code uses a `terms()` aggregation on numeric fields and manually calculates min/max/avg in Java via `calculateNumericStats()`.

## 5. The Fetch: Hibernate Search $\rightarrow$ Elasticsearch $\rightarrow$ Database
1.  **Elasticsearch Request:** Hibernate Search generates a massive JSON query containing the boolean logic and all aggregations, then sends it to Elasticsearch.
2.  **The Result Set:** Elasticsearch returns a list of **IDs** (e.g., `[1, 2, 3, 4]`) and the raw aggregation buckets.
3.  **Loading Entities:** Hibernate Search then performs the "Loading" phase. This is the `select ... from product where id in (?)` query you see in the logs. It fetches the full JPA entities from the database using the IDs found in the index.

## 6. The "7 Calls" Mystery Revealed
You observed the table scan and ID fetch repeating multiple times. Here is why:
1.  **GraphQL Property Resolution:** The `searchProducts` method returns a `ProductSearchResult` object.
2.  **The Record Accessors:** `ProductSearchResult` is a Java **Record**. Records have auto-generated methods like `results()`, `facets()`, `stats()`, etc.
3.  **The "Hidden" Re-execution:** In your current code, `searchProducts` is a simple method. However, if the GraphQL engine or a proxy layer (like Spring AOP or DevTools) triggers a "re-fetch" or if the field is resolved via a separate path, it might re-invoke the controller.
4.  **The real culprit:** If the return type `ProductSearchResult` was being resolved as a "component" rather than a static value, GraphQL-Java might attempt to call the data fetcher for each field in the selection set (`totalElements`, `results`, `facets`, `stats`, etc.) if it thinks the parent source is missing.
5.  **The N+1 Connection:** The table scan (`SELECT custom_attributes FROM PRODUCT`) is hard-coded into the `search()` method. If anything causes `search()` to be called more than once, the table scan repeats.

## 7. The Return: Data Transformation
1.  **Mapping:** The repository wraps the `hits`, `facets`, and `stats` into a `ProductSearchResponse`.
2.  **Controller Wrap:** The controller converts this into the final `ProductSearchResult`.
3.  **JSON Scalar:** The `facets` and `stats` maps are serialized into the response by the `ExtendedScalars.Json` logic we configured in `GraphQlConfig`.
4.  **Final Response:** The browser receives the JSON, and `index.html` renders it in the result box.

## Summary of Design Decisions
- **Trade-off (Dynamic Schema):** Using a table scan to find keys allows for a truly dynamic schema where you never have to "register" a new attribute. The trade-off is extreme latency as the dataset grows.
- **Trade-off (Universal Search):** Using a generic `SearchCondition` input makes the API very flexible for UIs, but makes it harder to leverage type-safe GraphQL features like Enums for specific fields.
- **Decision (JSON Scalar):** Using a raw JSON map for facets/stats avoids the "Schema Explosion" problem where every new attribute would need a new GraphQL type definition.
