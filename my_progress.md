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
-   **Full Project Refactor to "Gold Standard":** Successfully refactored all vertical domains (People, Product, Publications) to strict Hexagonal/Search-First standards.
    -   Centralized all models in `*.domain.model`.
    -   Established technology-agnostic contracts in `*.domain.repository`.
    -   Implemented robust infrastructure adapters in `*.infrastructure.persistence` (JPA) and `*.infrastructure.search` (Hibernate Search).
    -   Ensured all GraphQL inputs and types are correctly package-aligned (`*.graphql.input/type`).
-   **Metadata Registry Maturity:** Completed registration for all entity properties across all domains, enabling dynamic, registry-driven search, facets, and filters project-wide.
-   **Architectural Integrity & Stability:**
    -   Resolved all compilation and type mismatch issues arising from the deep refactor.
    -   Fixed Hibernate Search field-level binding and property-level binder annotations.
    -   Standardized test mocking and verified all 16 tests passing across all domains.
-   **Domain Alignment:** Achieved full parity for search, facets, and statistics functionality across People, Product, and Publications.

## Current State
-   **Logic Entry:** `ProductService.searchProducts` / `PersonService.searchPeople`
-   **Key Files:** `UniversalQueryBuilder.java`, `DacService.java`, `PropertyCfgRepository.java`, `PersonMergeService.java`, `index.html`.
-   **Stability:** 100% test pass rate across all unit, integration, and mutation tests.
-   **Development Mode:** All domains (Products, People) are fully functional with "Google-like" full-text search and advanced intelligence features.

## Next Steps
1.  **Universal Search Refactoring:** Unify the Search Repositories into a single `UniversalSearchRepository` to reduce boilerplate as domain count grows.
2.  **Extended Registry Logic:** Implement automated "Registry-to-Schema" generation so new entities in the Metadata Registry automatically appear in the GraphQL UI.
3.  **UI Polish:** Add a notification toast system to replace the `alert()` calls for "Copy to Clipboard" actions.
