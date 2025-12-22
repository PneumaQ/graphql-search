# Comparison of Hibernate Search Implementations

This document compares the "Universal Search" POC implementation with the production-grade implementation found in the `platform-api` project.

## 1. Entity Loading Strategy
| Feature | Universal Search POC | Platform API (Production) |
| :--- | :--- | :--- |
| **Loading Phase** | **Automatic:** Hibernate Search automatically fetches managed entities from the DB using IDs returned by Elasticsearch. | **Manual (Two-Phase):** Hibernate Search returns only `Long` IDs. The service then manually fetches entities from the DB. |
| **Reasoning** | Simpler code, leverages built-in Hibernate Search optimizations for basic use cases. | **DAC Support:** Fetching IDs first allows the DB phase to apply complex Data Access Controls (permissions) before paging results. |
| **Paging** | Performed at the Elasticsearch layer (`fetch(offset, limit)`). | Performed at the Database layer after filtering all hits by permissions. |

## 2. Metadata & Registry
| Feature | Universal Search POC | Platform API (Production) |
| :--- | :--- | :--- |
| **Source of Truth** | `CustomFieldDefinition` entity (Simplified Registry). | Comprehensive `EntityCfg`, `PropertyCfg`, `FieldCfg` hierarchy. |
| **Caching** | **Caffeine Cache:** Actively caches field definitions to avoid SQL lookups. | Likely uses a similar service-level or Hibernate second-level cache. |
| **Field Resolution** | Uses `dotPath` logic to map GraphQL keys to index paths. | Deeply nested path resolution (`getDotPath(targetEntity)`) supporting embedded entities. |

## 3. Query Construction
| Feature | Universal Search POC | Platform API (Production) |
| :--- | :--- | :--- |
| **Text Search** | Basic `simpleQueryString` across discovered text fields. | **Sophisticated Fuzzing:** Custom logic to apply `~1` slop values, handle quotes, and avoid fuzzing excluded terms. |
| **Aggregations** | Manual Java calculation for statistics (Min/Max/Avg) to support Lucene. | Heavy use of `AggregationKey` for term counts (facets) across diverse data types. |
| **Flexibility** | Uses a generic `[SearchCondition]` list. | Uses structured `SearchQueryRequest` with specific `SearchFilter` and `SearchRange` objects. |

## 4. Key Takeaways for POC Maturity
1.  **Transition to Two-Phase Search:** If the POC requires row-level security or multi-tenant permissions that Elasticsearch isn't aware of, we should adopt the `select(f -> f.id(Long.class))` pattern.
2.  **Robust Fuzzing:** The `SearchTextManager` in the production project provides a great blueprint for how to make the global search bar feel "smarter" (e.g., handles `~1` and quoted phrases more gracefully).
3.  **Advanced Sorting:** The production project handles nested embedded sorting significantly more robustly via `findEmbeddedSortableField`.
