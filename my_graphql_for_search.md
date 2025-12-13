# GraphQL for Enterprise Search: Full-Text, Facets, and Dynamic Filters

## Context
This document captures the architectural strategy for implementing "Google-like" full-text search and complex faceted search using GraphQL. It addresses the common concern that GraphQL's strict typing is incompatible with unstructured or dynamic search requirements.

---

## 1. The "Google-like" Search (Full-Text & Polymorphism)

**The Challenge:** A global search bar where a user types a string (e.g., "Apple") and expects mixed results (Companies, Fruits, Computers). REST endpoints struggle with mixed return types.

**The GraphQL Solution:** Use **Union Types** to handle polymorphism natively.

### The Schema
```graphql
type Query {
    # The entry point accepting unstructured text and structured filters
    search(text: String, filter: GlobalFilter): [SearchResult]
}

# The Magic: A result can be any of these types
union SearchResult = Person | Order | Document

type Person {
    id: ID
    name: String
    email: String
}

type Order {
    id: ID
    orderNumber: String
    amount: Float
}

type Document {
    id: ID
    title: String
    snippet: String # Specific to search results
}
```

### The Client Query
The client specifies exactly what fields to show for each result type.
```graphql
query {
  search(text: "John", filter: { status: ACTIVE }) {
    ... on Person {
      name
      email
    }
    ... on Order {
      orderNumber
      amount
    }
    ... on Document {
      title
      snippet
    }
  }
}
```

---

## 2. Faceted Search (Results + Counts)

**The Challenge:** Search often requires returning the data **AND** the metadata (counts for sidebar filters) in a single response. REST typically requires two calls or a cluttered custom JSON structure.

**The GraphQL Solution:** The **Connection Pattern**. Return a wrapper object containing both the list and the facets.

### The Schema
```graphql
type Query {
    searchPeople(text: String, filter: PersonFilter): PersonConnection
}

type PersonConnection {
    # 1. The actual search results
    items: [Person]
    
    # 2. The Pagination Metadata
    totalCount: Int
    
    # 3. The Facets (The "Counts")
    facets: PersonFacets
}

type PersonFacets {
    byStatus: [FacetBucket]
    byDepartment: [FacetBucket]
    byRole: [FacetBucket]
}

type FacetBucket {
    value: String  # e.g. "Active", "Pending"
    count: Int     # e.g. 42, 12
}
```

### The Client Query (Single Round-Trip)
The frontend requests everything it needs to render the Search Page + Sidebar in one go.

```graphql
query {
  searchPeople(text: "Manager", filter: { region: "US" }) {
    items {
      name
      title
    }
    facets {
      byStatus {
        value
        count
      }
      byDepartment {
        value
        count
      }
    }
  }
}
```

### Efficiency & Optimization
A major advantage of GraphQL here is **Conditional Execution**.
*   **Scenario A (Initial Load):** Client asks for `items` AND `facets`. The backend instructs the search engine (Elasticsearch/Solr) to compute aggregations.
*   **Scenario B (Next Page):** Client clicks "Page 2". The sidebar doesn't change. The client query **only asks for `items`**.
    *   **Result:** The GraphQL resolver sees `facets` was not requested. It tells the search engine **SKIP aggregations**. This saves significant CPU and latency. REST cannot easily do this.

---

## 3. Dynamic Filters (AND/OR Logic)

To support complex filtering (e.g., "Status is Active AND (Date > 2022 OR Region = US)"), use **Recursive Input Types**.

```graphql
input PersonFilter {
    status: StringFilter
    date: DateFilter
    
    # Recursive Logic
    and: [PersonFilter]
    or: [PersonFilter]
}

input StringFilter {
    eq: String
    contains: String
    in: [String]
}
```

---

## 4. Why GraphQL Wins over REST for Search

| Feature | REST | GraphQL |
| :--- | :--- | :--- |
| **Mixed Results** | Difficult. Requires generic untyped JSON blobs or multiple endpoints. | **Native.** handled via Unions (`... on Type`). |
| **Facets** | Clunky. often side-loaded in headers or a complex custom envelope. | **Clean.** Explicitly modeled in the schema alongside data. |
| **Over-fetching** | High. Search results often dump full objects when only a "Card View" is needed. | **Zero.** Client fetches only the "Card" fields (e.g. `title`, `snippet`). |
| **Performance** | Rigid. Backend calculates facets every time or never. | **Adaptive.** Backend only calculates facets if the client asks for them. |

## 5. Implementation Strategy (Spring Boot)
1.  **Search Service:** Use existing logic (Hibernate Search, Elasticsearch Client) to get raw hits.
2.  **Resolvers:** Map the raw hits to the GraphQL `Connection` object.
3.  **Data Fetcher:** Inspect `DataFetchingEnvironment`. If `facets` is present in the selection set, enable aggregations in the underlying search query.
