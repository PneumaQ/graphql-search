# Search-First Architecture POC - Transition Context

## Overview
This POC demonstrates a "Search-First" paradigm where Elasticsearch (via Hibernate Search) handles all business intelligence, filtering, and authorization (DAC), relegating Postgres to a simple hydration store.

## Current State
- **Metadata Registry:** A production-style JPA-backed registry (`EntityCfg`, `PropertyCfg`) maps logical names (e.g., "rating") to technical paths (e.g., "reviews.rating") and data types.
- **Dynamic DAC Engine:** `DacService` translates relational security rules (`DacCfg`) into Elasticsearch predicates at runtime.
- **Universal Query Builder:** A unified component that resolves dot-paths and handles type-safe value conversion for both Products and People.
- **Synchronized Filtering:** Root search criteria (like `minRating`) are passed via `GraphQLContext` to `@BatchMapping` loaders to keep child collections in sync with the search results.
- **Interactive Dashboard:** A Vue.js 3 dashboard (`index.html`) provides a point-and-click interface for query building, faceting, and security insights.

## Recent Fixes (Done)
- **Brand Faceting:** Registered the `brand` property to map to `brand.name_keyword`, resolving ES aggregation errors.
- **Type-Safe Sync:** Refactored `ProductService` to use metadata `dataType` before parsing numbers, eliminating `NumberFormatExceptions`.
- **Seeding scoping:** Fixed Java scoping issues in `GraphqlApplication` to ensure a "Success-First" dataset for demos.

## Next Steps
1. **Verification:** Confirm the "Search People" domain works with the USA-only DAC.
2. **Expansion:** Potentially add "Material" or other JSON attributes to the dashboard's default facets.
3. **Architecture Summary:** Finalize the technical document for stakeholders.

## Technical Map
- **Logic Entry:** `ProductService.searchProducts` / `PersonService.searchPeople`
- **Core Builder:** `UniversalQueryBuilder.java`
- **Security Logic:** `DacService.java`
- **UI Logic:** `index.html` (Vue.js + Tailwind)
- **Metadata Registry:** `PropertyCfgRepository`