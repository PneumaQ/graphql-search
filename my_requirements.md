Based on my analysis of the codebase, I can confirm the feature sets for both search mechanisms.

  Important Note on Naming:
  There appears to be a swap in the naming conventions described in your prompt versus what is in the code.
   * `SearchQueryRequest.java` is the class that targets Elasticsearch (via Hibernate Search).
   * `QueryRequest.java` is the class that targets Postgres (via the Universal Query Service / SQL generation).

  Here is the breakdown of the features supported by each:

  1. Elasticsearch Mechanism (SearchQueryRequest.java)
  Primary Use Case: Full-text search, faceting, and broad entity discovery.
  Key Service: SearchServiceImpl.java

   * Full Text Search:
       * Fuzzy Search: Supported (terms without prefixes are treated as fuzzy).
       * Exact Match: Supported (terms prefixed with + or -).
       * Logic: Handles boolean logic within the search text parsing.
   * Filtering:
       * Field Filters: Supports exact matching on specific fields via searchFilters.
       * Facets: Designed to return counts for filter values (e.g., for sidebar navigation).
       * Ranges: Supports from/to range queries (dates, numbers) via searchRanges.
   * Paging & Sorting:
       * Supported, but not fields on the request object itself. They are passed as a separate Spring Pageable argument to the service methods.
   * Configuration:
       * Can target specific configuration versions via usedCfgId.

  2. Postgres/SQL Mechanism (QueryRequest.java)
  Primary Use Case: Precise relational data retrieval, complex filtering, and reporting.
  Key Service: UniversalQueryServiceImpl.java

   * Advanced Filtering:
       * Operators: Extensive support including EQ (=), GT/GTE (>), LT/LTE (<), IN, NOTIN, NOT_EQ, STARTS_WITH, ENDS_WITH, CONTAINS, EMPTY (IS NULL),
         NOT_EMPTY.
       * Boolean Logic: Supports deeply nested AND/OR conditions via the Condition class.
       * Relational: Supports filtering on related entities (sub-conditions).
   * Relational Features:
       * Joins: Supports explicit Join definitions.
       * Distinct: Explicit boolean flag for DISTINCT results.
   * Sorting:
       * Multi-field Sort: Supports sorting on multiple fields via a list of Sort objects.
       * Aliasing: Supports sorting on aliased columns (useful for aggregations).
   * Paging:
       * Supported directly as a field (Pageable) within the request object.
   * Aggregations (Group By):
       * Grouping: Supports grouping by multiple fields.
       * Aggregates: Supports aggregate functions (COUNT, SUM, etc.).
       * Having Clause: Supports filtering on aggregated results (conditions within the GroupBy object).
       * SQL Generation: Supports different QueryModes (e.g., generating raw SQL).
  Summary for GraphQL Migration
  To fully replace both systems, your GraphQL implementation would need to support:
   1. Arguments: A robust filter input type that handles both "fuzzy text" (for the Elastic use case) and "structured operators" (EQ, GT, etc. for the SQL use
      case).
   2. Pagination: Standard limit/offset or cursor-based pagination.
   3. Sorting: A list of sort fields + direction.
   4. Aggregations: A way to request "facets" (counts of field values) alongside the main results.
   5. Relations: GraphQL handles fetching related data natively, but efficient filtering on those relations (e.g., "Find Users where Department Name starts
      with 'A'") will require careful resolver or join implementation to match the QueryRequest capabilities.
