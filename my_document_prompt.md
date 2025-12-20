I want you to generate end-to-end technical documentation for this codebase.

Your goal is to explain, in precise detail, how a single GraphQL search request flows through the system from the client all the way to Elasticsearch and back.

This documentation is intended for an experienced backend engineer who is new to the codebase.

Requirements:
- Treat this as an execution flow, not a code listing or API reference.
- Walk the request chronologically from entry to exit.
- Use numbered steps.
- Name the key classes, methods, and modules involved at each step.
- Explain why each abstraction exists, not just what it does.
- Explicitly describe how GraphQL concepts are translated into Hibernate Search concepts.
- Explicitly describe how Hibernate Search constructs Elasticsearch queries.
- Call out how faceting, sorting, and paging are implemented and where they are handled.
- Highlight any important design decisions or trade-offs you observe.

Structure the document like this:
1. High-level overview of the request lifecycle (1–2 paragraphs)
2. Detailed step-by-step flow
3. Data transformation summary (GraphQL input → internal model → Hibernate Search → Elasticsearch)
4. Notes on extensibility, limitations, and performance considerations

Do not oversimplify. Assume the reader wants to truly understand the system, not just use it.

Start the flow from the GraphQL resolver exposed to the client.
End the flow when the Elasticsearch response is mapped back into the GraphQL response.

If there are multiple code paths, choose the primary happy-path search flow and note alternatives briefly.

Save the results to a file called "my_technical_documentation.md" in markdown format.
