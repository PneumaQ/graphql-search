# Gemini Context & Project Status

## Project: graphql-search

### Architecture
- **Framework:** Spring Boot 3.5.4
- **Language:** Java 21
- **API:** Spring GraphQL (Universal Search Platform)
- **Data:** Spring Data JPA + H2 (In-Memory)
- **Search:** Hibernate Search 7.1 (Elasticsearch backend)
- **Caching:** Caffeine (Used for Metadata Registry and Configuration Lookups)

### Current Status: Advanced Architectural Proof Complete
We have moved beyond basic search into a production-mirroring architecture that emphasizes performance, stability, and clean decoupling.

### Key Implementation Details
- **Metadata Registry:** Replaced expensive table scans with a `CustomFieldDefinition` registry. Search now consults a Caffeine-cached map to determine dynamic field paths and data types.
- **Recursive Boolean Logic:** `SearchCondition` now supports nested `and`, `or`, and `not` structures, allowing the UI to build complex multi-clause queries.
- **The GraphQL "Firewall":** Proved API stability by renaming domain field `sku` to `internalStockCode` while maintaining the public `sku` field via `@SchemaMapping`.
- **Immutable Lookup Pattern:** Implemented `LookupCfg` (entity) and `LookupCfgRecord` (immutable record). Used a JPA `AttributeConverter` + Caffeine cache to "hydrate" rich brand objects without DB joins.
- **Beautified Codebase:**
    - `ProductController`: Pure delegator.
    - `ProductService`: Orchestrator for pagination and DTO mapping.
    - `ProductSearchRepository`: Decomposed into readable private methods (`applyFullTextSearch`, `mapFacetResults`, etc.).
- **Tabular Transformation:** Implemented `searchProductReviewTable` to demonstrate flattening a 3-layer graph (Product + Review + Brand) into a flat "JDBC-like" row list for the UI.
- **Query Tracer:** Added instrumentation to label SQL queries (e.g., `/* Action: Entity Loading */`) and log GraphQL request boundaries.

### Outstanding Mysteries / Debugging Phase
- **Ghost Logging:** In "Run" mode (DevTools active), GraphQL banners and SQL logs repeat 16 times for a single request. In "Debug" mode, they appear only once. 
- **Batch Loading:** Hibernate Search is currently loading entities one-by-one (`where id in (?)`) instead of in batches, despite `batch_size` settings.
- **Baseline Test:** Seed data has been simplified to **1 Product** to isolate the logging duplication from the data volume.

### Todo List (Next Steps)
- [ ] Solve the Log Duplication mystery (Theory: DevTools RestartClassLoader or multiple bean registration).
- [ ] Force true SQL Batching for Phase 2 loading (Verify `IN (?, ?, ...)` behavior).
- [ ] **Searchable Security:** Research indexing permissions directly into the ES document to avoid DB-side DAC filtering.
- [ ] **Write-Side Validation:** Use the Registry to prevent "dirty" keys during `createProduct`.

### Future Roadmap
- [ ] Migrate to PostgreSQL for persistent JSONB storage.
- [ ] Test with real Elasticsearch container to verify globbing and native stats aggregations.
- [ ] Explore "Custom Directives" to reduce Java boilerplate for flattening logic.
