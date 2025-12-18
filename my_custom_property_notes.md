Three Essential Pillars for Custom Property Implementation
The implementation relies on three distinct technical pillars to ensure flexibility is maintained across the database, API, and search layers:
1.	Persistence Bridge (hypersistence-utils / JsonType):
o	Function: Maps the flexible Java Map to the rigid PostgreSQL JSONB column.
o	Essential Role: Guarantees that mutating the map's contents is correctly recognized as a "dirty" change by Hibernate, ensuring data is actually saved and search re-indexing is triggered.
2.	API Bridge (Jackson's @JsonAnyGetter / @JsonAnySetter):
o	Function: Flattens the map's key/value pairs into the root of the entity's JSON object during serialization.
o	Essential Role: Achieves API Transparency, making custom fields appear identical to static fields for consuming REST clients.
3.	Search Bridge/Binder (Hibernate Search):
o	Function: Uses a dynamic field template (TypeBinder) and runtime translation (TypeBridge) to index the map's contents.
o	Essential Role: Enables searchability, dynamically creating new fields in the Elasticsearch index schema (e.g., cp_priority) to allow users to search and filter by custom properties.

