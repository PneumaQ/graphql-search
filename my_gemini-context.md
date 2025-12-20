# Gemini Context & Project Status

## Project: graphql-search

### Architecture
- **Framework:** Spring Boot 3.5.4
- **Language:** Java 21
- **API:** Spring GraphQL (Universal Search Pattern)
- **Data:** Spring Data JPA + H2 (In-Memory)
- **Search:** Hibernate Search 7.1 (Elasticsearch backend / Lucene for tests)

### Current Status: Universal Search Overhaul Complete
We have moved away from type-specific GraphQL constructs to a **Universal Search Platform** approach. This allows any field (core or custom) to be searched, filtered, and sorted using a consistent, string-based API.

### Implementation Details
- **Entity:** `Product.java` enriched with `price`, `category`, and `custom_attributes` (JSON).
- **Universal Input:**
    - **Filtering:** Uses `[SearchCondition]` list with a generic `{ field, operator, value }` structure.
    - **Sorting:** Uses `[ProductSort]` with string-based `field` names (e.g., `"name"`, `"price"`, or `"color"`).
- **Universal Output:**
    - **Facets:** Returns a raw `JSON` Map of counts (e.g., `{ "category": { "Electronics": 5 } }`).
    - **Stats:** Returns a raw `JSON` Map of numeric statistics (e.g., `{ "price": { "avg": 45.0 } }`).
- **Search Engine:**
    - **Full-Text:** Supports "Google-like" search across core fields and dynamic attributes (`*_text` sub-fields).
    - **Query Builder:** `UniversalQueryBuilder` handles the mapping of field strings to index paths and handles type-conversion for numeric fields (rating, price).
- **UI:** `index.html` rebuilt as a dedicated Search Showcase for Products.

### Todo List
- [x] Standardize Sorting API (String-based fields).
- [x] Standardize Filtering API (`SearchCondition` list).
- [x] Standardize Output (JSON Map for Facets/Stats).
- [x] Implement Full-Text Search for Custom Attributes.
- [x] Add Price and Category core fields to Product.
- [x] Verify all features with `ProductGraphqlTest`.
- [x] Revamp `index.html` for Search Showcase.

### Future Roadmap / Backlog
- [ ] **Research:** Revisit "Live Schema Refresh" if strict GraphQL typing is required for dynamic attributes in the future.
- [ ] **Feature:** Support `OR` logic within the new `[SearchCondition]` list pattern.
- [ ] **Infra:** Migrate to PostgreSQL for persistent JSONB storage.
- [ ] **Infra:** Test with real Elasticsearch container to verify globbing patterns (`custom_attributes.*_text`).