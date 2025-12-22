# Search Architecture: Universal Search vs. Legacy Platform

This document captures the architectural pivot from a **Database-First** security model to a **Search-First** high-performance model.

## 1. Core Philosophy
| Feature | Legacy Platform (`platform-api`) | New Universal Search Design |
| :--- | :--- | :--- |
| **Primary Engine** | **Relational Database:** SQL is the source of truth for both search and security. | **Search Engine:** Elasticsearch is the primary intelligence engine for all queries. |
| **Postgres Role** | **Executioner:** Performs complex joins and applies DAC rules at runtime. | **Hydrator:** A "dumb" store that provides entity data by ID after filtering is complete. |
| **Security (DAC)** | **SQL Injection:** Dynamic WHERE clauses applied to database queries. | **Predicate Injection:** Dynamic DAC rules translated into Elasticsearch predicates. |

## 2. Data Access Control (DAC) Implementation
### The Legacy Approach (SQL-Side DAC)
*   **Mechanism:** Two-Phase Search. ES finds broad hits; SQL applies fine-grained security joins.
*   **Result:** Broken hit counts and "Phantom Paging" (where requested results are filtered out post-search).
*   **Cost:** High CPU/IO pressure on Postgres for every search request.

### The New Approach (Search-First DAC)
*   **Mechanism:** Denormalized Read Model. All attributes required for security (Store, Region, Category) are indexed within the target document.
*   **Injection:** Runtime DAC configurations (`DacCfg`) are translated into recursive `SearchCondition` trees and merged with user criteria.
*   **Result:** 100% accurate facets, perfect pagination, and sub-millisecond response times regardless of security complexity.

## 3. The "Big Assumption": Single-Index Read Models
The new design assumes that an Aggregate Root (AR) index contains all data necessary to satisfy both **User Search** and **Data Access Control**. 
*   **Joins are moved to Index-Time:** Using Hibernate Search `@IndexedEmbedded`, the cost of relational traversal is paid once during an update, rather than millions of times during reads.
*   **Dumb Hydration:** By the time Postgres is touched, the "Who" and "What" have already been decided by Elasticsearch. Postgres only handles the "How it looks."

## 4. Conclusion for Design Document
The transition to **Search-First Authorization** eliminates the largest performance bottleneck in the system. By treating security as a top-level search filter, we achieve a stable, scalable API that decouples business logic from database schema complexity.