# Gemini Context & Project Status

## Project: graphql-search

### Architecture
- **Framework:** Spring Boot 3.5.4
- **Language:** Java 21
- **API:** Spring GraphQL
- **Data:** Spring Data JPA + H2 (In-Memory)
- **Search:** Hibernate Search 7.1 (Elasticsearch backend / Lucene for tests)

### Current Task: Product Dynamic Attributes
We are implementing a flexible `Product` entity where users can define arbitrary attributes (e.g., "color": "red", "size": "M") that are:
1.  Stored as JSON in the database.
2.  Indexed by Hibernate Search for full-text search and filtering.

### Implementation Status
- **Entity:** `Product.java` has a `Map<String, String> attributes` field.
    - Annotation: `@JdbcTypeCode(SqlTypes.JSON)` for H2/DB JSON storage.
    - Annotation: `@PropertyBinding(binder = ...)` for custom search indexing.
- **Binder:** `ProductAttributeBinder` (assumed existing, need to verify) maps the map keys to dynamic index fields.
- **GraphQL Schema:**
    - `Product` type has `attributes: [ProductAttribute]`.
    - `ProductFilterInput` supports filtering by `attributes` (key + string filter).
    - `createProduct` mutation accepts `attributes`.

### Todo List
- [x] Verify `ProductAttributeBinder` implementation details.
- [x] Verify `ProductAttributeBridge` implementation details.
- [x] Verify `ProductSearchRepository` implementation.
    - [x] **Refactor:** `ProductSearchRepository` improved to support robust String filtering on dynamic attributes.
- [x] Test `createProduct` mutation with attributes.
- [x] Test `searchProducts` with attribute filtering.
    - [x] Created `ProductGraphqlTest` integration test.
    - [x] Fixed "lowercase" normalizer issue in test config.
    - [x] Updated UI `index.html`.

### Implementation Notes
- **Dynamic Attributes:** Stored as JSON in H2 (`@JdbcTypeCode(SqlTypes.JSON)`).
- **Indexing:** 
    - `ProductAttributeBinder` defines an `attributes` object field.
    - `ProductAttributeBridge` maps Map entries to `attributes.{key}_keyword`.
    - Template `*` -> `attributes.*_keyword` uses `lowercase` normalizer.
- **Testing:** `ProductGraphqlTest` confirms that attributes are indexed and searchable.
- **Search:** `ProductSearchRepository` constructs queries dynamically targeting `attributes.{key}_keyword`.
