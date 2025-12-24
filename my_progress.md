# Search-First Architecture POC - Progress & Checkpoint

## Core Concept
This POC demonstrates a "Search-First" paradigm where **Elasticsearch** (via Hibernate Search) handles business intelligence, filtering, and authorization (DAC), while **PostgreSQL** serves as a simple hydration store.

## Architectural Pillars
1.  **Persistence Bridge:** Maps flexible Java Maps to PostgreSQL JSONB using `JsonType`, ensuring "dirty" changes trigger re-indexing.
2.  **API Bridge:** Uses Jackson (`@JsonAnyGetter/@JsonAnySetter`) to flatten custom attributes into the root JSON object for API transparency.
3.  **Search Bridge/Binder:** Utilizes Hibernate Search `TypeBinder`/`TypeBridge` to dynamically index JSON map contents into Elasticsearch.
4.  **Keyword-to-Package Architecture:** Enforces a strict 1:1 mapping between GraphQL keywords and Java packages across all verticals:
    - `*.graphql.input`: Everything defined as an `input` in the schema (Commands, Sorts, Filters, e.g., `PersonSortInput`).
    - `*.graphql.type`: Everything defined as a `type` or `enum` in the schema (Result envelopes, e.g., `PersonSearchResult`).
    - `model`: Pure JPA Domain Entities.
    - `service`: Business logic and Merge/Hydration accelerators.

## Key Features & Components
-   **Strict Schema-Java Alignment:** Java class names (e.g., `SearchConditionInput`) and locations now perfectly mirror the GraphQL schema, eliminating cognitive load for developers.
-   **Metadata Registry:** JPA-backed registry (`EntityCfg`, `PropertyCfg`) mapping logical names to technical paths and data types.
-   **Universal Query Builder:** Decouples GraphQL input from Hibernate Search internals, handling type-safe path resolution and predicate building. Moved to `platform.filter` for global reuse.
-   **Dynamic DAC Engine:** Translates relational security rules (`DacCfg`) into Elasticsearch predicates at runtime for automatic authorization.
-   **Synchronized Filtering:** Passes root search criteria via `GraphQLContext` to child collection loaders (`@BatchMapping`) to keep results consistent.
-   **Intelligence Layer:** Supports dynamic **Facets** (counts) and **Statistics** (min, max, avg) for both Product (price, rating) and Person (salary, age) domains.

## Recent Progress & Improvements
-   **Project-Wide Refactor:** Successfully reorganized the entire codebase into the "Clean Vertical Architecture" described above.
-   **Domain Alignment:** Fully implemented `searchPeople` in the backend, supporting facets and statistics parity with `searchProducts`.
-   **Schema Consistency:** Updated GraphQL schema and repository layers to support `facetKeys` and `statsKeys` across all domains.
-   **Performance Optimizations:**
    -   Implemented caching for dynamic field discovery to avoid redundant table scans.
    -   Added `@BatchSize` to nested collections to mitigate the N+1 loading problem.
-   **UI Enhancements:**
    -   Interactive Dashboard with point-and-click filter/facet/stat building.
    -   Added "Copy to Clipboard" buttons for raw GraphQL requests and JSON responses.
    -   Enabled "Enter" key to execute searches.
    -   Fixed stats rendering to show Average, Min, and Max in a sidebar discovery panel.
-   **DDD-Style Mutations:**
    -   Implemented `createPerson` and `updatePerson` mutations with explicit input types to enforce POST vs PATCH semantics (ID requirement).
    -   Introduced `PersonMergeService` as a "Hydration Accelerator" to handle canonical JPA entity construction before business logic.
    -   Added comprehensive unit tests (`PersonMergeServiceTest`), GraphQL mutation tests (`PersonMutationTest`), and fixed all integration tests.
-   **Security Configuration:** Disabled the default "USA Residents Only" DAC for People to ensure full dataset visibility during development.

## Current State
-   **Logic Entry:** `ProductService.searchProducts` / `PersonService.searchPeople`
-   **Key Files:** `UniversalQueryBuilder.java`, `DacService.java`, `PropertyCfgRepository.java`, `PersonMergeService.java`, `index.html`.
-   **Stability:** 100% test pass rate across all unit, integration, and mutation tests.
-   **Development Mode:** All domains (Products, People) are fully functional with "Google-like" full-text search and advanced intelligence features.

## Next Steps
1.  **Universal Search Refactoring:** Unify the Search Repositories into a single `UniversalSearchRepository` to reduce boilerplate as domain count grows.
2.  **Extended Registry Logic:** Implement automated "Registry-to-Schema" generation so new entities in the Metadata Registry automatically appear in the GraphQL UI.
3.  **UI Polish:** Add a notification toast system to replace the `alert()` calls for "Copy to Clipboard" actions.
