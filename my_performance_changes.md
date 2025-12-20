# Performance Optimizations for Universal Search

The following changes were implemented to address redundant database calls and schema errors identified during the execution of complex GraphQL search queries.

## 1. Redundant Table Scan Optimization
**File:** `ProductSearchRepository.java`
- **Issue:** The method `fetchDynamicTextFields` was executing `SELECT custom_attributes FROM PRODUCT` on every search request to discover dynamic field keys.
- **Fix:**
    - Introduced a `static Set<String> cachedDynamicFields` to store discovered keys.
    - Implemented `getDynamicTextFields()` as a synchronized wrapper to ensure the table scan only runs once per application lifecycle.
    - Added logging to confirm when a scan occurs versus a cache hit.

## 2. N+1 Problem Mitigation
**File:** `Product.java`
- **Issue:** Loading search results with nested `reviews` caused Hibernate to execute individual queries for each product's review collection.
- **Fix:**
    - Added `@BatchSize(size = 20)` to the `reviews` collection.
    - This allows Hibernate to fetch reviews for multiple products in a single "IN" query (e.g., `WHERE product_id IN (?, ?, ...)`).

## 3. Aggregation and Sorting Schema Fixes
**File:** `Product.java`
- **Issue:** Hibernate Search 7.1 threw errors (HSEARCH000604) stating `category_keyword` was not aggregable, despite the annotation.
- **Fix:**
    - Explicitly added `projectable = Projectable.YES` and `aggregable = Aggregable.YES` to the `category` and `price` fields.
    - This ensures the underlying index segments are created with the necessary metadata for facets and stats.

## 4. Diagnostics and Call Tracking
**File:** `ProductSearchRepository.java`
- **Fix:**
    - Added an `AtomicInteger callCount` to track the number of times `search()` is invoked.
    - Implemented stack trace logging to `search_calls.log` using `java.nio.file.Files.writeString` to identify why the search method was being triggered multiple times per request.
    - Added hits/result count logging to standard output.
