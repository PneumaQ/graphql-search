# Spring Boot GraphQL Person API

This project is a simple implementation of a GraphQL API using Spring Boot, Spring Data JPA, and H2 database. It is designed as a learning playground for GraphQL.

## Project Structure

The core logic is located in `src/main/java/com/example/graphql`:

*   **`Person.java`**: The JPA Entity representing a person. It maps to a database table and contains fields like `id`, `name`, and `age`.
*   **`PersonRepository.java`**: Extends `JpaRepository` to provide standard CRUD operations for the `Person` entity.
*   **`PersonService.java`**: A service layer that encapsulates business logic and calls the repository.
*   **`PersonController.java`**: The GraphQL controller. It uses `@QueryMapping` and `@MutationMapping` to bind GraphQL operations to Java methods.
*   **`GraphqlApplication.java`**: The main entry point. It also includes a `CommandLineRunner` that seeds the database with initial data ("John Doe" and "Jane Smith") when the application starts.

The GraphQL schema is defined in `src/main/resources/graphql/schema.graphqls`:

*   **`schema.graphqls`**: Defines the `Person` type, as well as the available `Query` (read) and `Mutation` (write) operations.

## Prerequisites

*   Java 21
*   Gradle (wrapper provided)

## How to Run

1.  Open a terminal in the project root.
2.  Run the application using the Gradle wrapper:
    *   **Windows**: `.\gradlew.bat bootRun`
    *   **Linux/Mac**: `./gradlew bootRun`

The application will start on `http://localhost:8080`.

## Using GraphiQL

This project has the GraphiQL explorer enabled. Once the application is running, verify it by visiting:

**[http://localhost:8080/graphiql](http://localhost:8080/graphiql)**

## Sample Operations

You can copy and paste these into the GraphiQL interface.

### 1. Query All Persons
Retrieves a list of all persons.
```graphql
query {
  persons {
    id
    name
    age
  }
}
```

### 2. Query Person by ID
Finds a specific person.
```graphql
query {
  personById(id: "1") {
    name
    age
  }
}
```

### 3. Create a Person (Mutation)
Adds a new person to the database.
```graphql
mutation {
  createPerson(name: "Alice", age: 28) {
    id
    name
  }
}
```
