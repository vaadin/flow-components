# Run the LLM's SQL over endpoints that are not databases

## Context

`DatabaseProvider` is the bridge between the LLM and the application's data,
and its contract is "the LLM writes SQL". The shared base
`custom-data-providers-base` adds sample endpoints that are not SQL databases
to the integration tests module: a REST API with equality filters and
sorting, a GraphQL API, MongoDB, JPA and CSV files, beside the H2 variants.
The REST, GraphQL, MongoDB, JPA and H2 endpoints log each call at `INFO`. The
chart and grid AI chat pages run over `InMemoryDatabaseProvider` (H2).

On this branch the LLM keeps writing SQL with the prompts of today, and the
endpoints look like the tables of one schema.
Apache Calcite runs the SQL in the JVM, pushes the equality filters a source
supports down to it, and computes joins, `GROUP BY`, sorting and paging in
memory.

The controllers stay as they are by construction: the result is a plain
`DatabaseProvider`, so the tools, their texts and parameter schemas, the
grid's `LIMIT`/`OFFSET` and `COUNT(*)` wrappers and the number of tool rounds
do not change. `vaadin-ai-extensions-flow` is not touched, and since the base
has no `QueryableProvider`, no existing class of core changes either. No new
controller constructor is needed, `get_database_schema` is unchanged, rows
never reach the LLM, and only a `ToolException` carries a message the LLM may
see.

What building it taught (Calcite's behaviour, the MongoDB adapter that was
tried and replaced, the cost of a provider written without the helper, the
dependency footprint) is under Findings at the end.

Paths are relative to `vaadin-ai-components-flow-parent/`; `core` is
`vaadin-ai-core-flow/src/main/java/com/vaadin/flow/component/ai`, `it` is
`vaadin-ai-components-flow-integration-tests/src/main/java/com/vaadin/flow/component/ai/tests`.

## Endpoints and their tables

| Endpoint (base) | Source on this branch | One table per | Pushed down | Column types from |
|---|---|---|---|---|
| `it/endpoints/SampleRestApi` | `it/providers/RestTableSource` | collection of the API index | equality filters as `field=value` query parameters | the first page |
| `it/endpoints/SampleGraphQLApi` | `it/providers/GraphQLTableSource` | root field returning a list of objects | root field arguments that match a column by name and type | introspection, ISO dates from a first fetch |
| `it/endpoints/SampleMongoServer` | `it/providers/MongoTableSource` | collection | equality filters as a `find` filter, dates, decimals and ids included | a sample of 100 documents |
| `it/endpoints/SampleCsvFiles` | `it/providers/CsvTableSource` | `.csv` file | nothing | every value of the file |
| `it/endpoints/jpa/SampleJpaDatabase`, JPQL | `it/providers/JpqlTableSource` | entity of `ENTITY_CLASSES` | equality filters in the JPQL `WHERE`, association ids included | the entity's attributes |
| `it/endpoints/jpa/SampleJpaDatabase`, Flow data providers | `core/provider/DataProviderTableSource` over a callback `DataProvider` | entity of `ENTITY_CLASSES` | nothing | the entity's properties |
| `it/endpoints/SampleH2Database`, `TenantScopedH2Database`, the JPA tables | none: `it/InMemoryDatabaseProvider` and `it/providers/JdbcDatabaseProvider` | | | |

The SQL endpoints need no Calcite: the H2 variants, the tenant-scoped database
and the tables Hibernate generated are queried directly through JDBC, so the
chat pages offer every endpoint of the base.

## Approach

The design rests on these choices:

1. The federated provider implements `DatabaseProvider`. Nothing on the SQL
   path knows it exists.
2. A source implements `TableSource`, a small contract without Calcite types:
   a name, typed columns, what it can push down, and `fetch(Pushdown)`
   returning rows as column maps. Calcite stays inside core, in
   `FederatedDatabaseProvider` and the package-private `SourceTable`.
   `AdapterTable` is the one public type that names Calcite, for one of
   Calcite's own adapters.
3. Push-down is equality filters only, and the engine checks them again on
   the rows, so a source may push down less than it was asked for.
   Projection, sort, limit and count stay in the engine. A short row cache
   per source and pushdown keeps a grid render to one fetch per backend.
4. Calcite is an optional dependency of core, like LangChain4j and Spring AI.

Branch: `prototype/federated-sql-calcite`, on top of
`custom-data-providers-base`.

### Step 1: optional Calcite dependency

- `pom.xml` (the AI parent): property `calcite.version` = `1.42.0`, shared by
  core and the integration tests module. The root pom stays untouched.
- `vaadin-ai-core-flow/pom.xml`: `org.apache.calcite:calcite-core` at
  `${calcite.version}`, `<optional>true</optional>`, with these exclusions,
  each commented with its reason:
  - `com.fasterxml.jackson.core:jackson-annotations`: Calcite's Jackson 2
    annotations shadow the ones Flow's Jackson 3 needs, since the two share a
    package, and Flow's mapper then fails to initialise. The IT module
    already excludes the same artifact from `spring-ai-openai`.
  - `org.slf4j:slf4j-api`: Calcite's `slf4j-api` 1.7 wins the resolution in
    a module that declares `calcite-core` directly. A 2.x SLF4J
    implementation cannot bind to it, so every log line of the module,
    the controllers' own included, silently goes to the no-operation logger.
    Flow brings the 2.x API.

### Step 2: core `TableSource` (new, package `com.vaadin.flow.component.ai.provider`, Apache header, `@since 25.4`)

```java
public interface TableSource extends Serializable {
    record Column(String name, Class<?> type) implements Serializable { }  // both non-null
    enum Capability { EQUALITY_FILTER }
    record Pushdown(Map<String, Object> equalityFilters) implements Serializable {
        public static final Pushdown NONE = new Pushdown(Map.of());       // compact constructor: Map.copyOf
    }
    String getName();                                    // table name in SQL, matched case-insensitively
    List<Column> getColumns();                           // at least one, in the order the LLM sees them
    default Set<Capability> getCapabilities() { return Set.of(); }
    default String getDescription() { return null; }    // shown next to the table name
    List<Map<String, Object>> fetch(Pushdown pushdown);  // every column of each row; a missing one is null
}
```

Javadoc: column types are `String`, `Integer`, `Long`, `Double`,
`BigDecimal`, `Boolean`, `LocalDate`, `LocalDateTime` or `LocalTime`; a
numeric column may return any `Number`; every column is nullable; a pushed
value has the column's type. A `ToolException` from `fetch` carries a
message safe for the LLM, any other exception becomes a generic error. The
nested types carry `@since 25.4` too.

### Step 3: core `FederatedDatabaseProvider` and `SourceTable`

**`FederatedDatabaseProvider`** (new, `public class`, `implements DatabaseProvider`, `@since 25.4`)

```java
public FederatedDatabaseProvider(List<TableSource> sources)
public FederatedDatabaseProvider(List<TableSource> sources, String notes)
public Duration getCacheDuration()
public void setCacheDuration(Duration cacheDuration)  // default 10 s, ZERO fetches every time, negative rejected
public String getSchema()
public List<Map<String, Object>> executeQuery(String sql)
```

- The constructors throw `IllegalArgumentException` for no tables, two names
  equal ignoring case, a source without columns, or a column type outside
  Step 2. `notes` is appended to the schema text.
- One Calcite connection (`jdbc:calcite:`, `lex=MYSQL_ANSI`) is opened on
  first use, with a schema `federated` holding one `SourceTable` per source.
  Connection, schema and cache are `transient` and rebuilt after
  deserialization. The public methods are `synchronized`.
- `getSchema()`: `DATABASE SCHEMA:`, then `TABLE <name>  (<description>)`
  with one `  <column> <SQL type>` line per column as Calcite reports it,
  then `NOTES:` with the dialect notes (standard SQL in Apache Calcite with
  `JOIN`, `GROUP BY`, subqueries, `LIMIT`, `OFFSET`; case-insensitive
  identifiers; a quoted column is a reserved word and must be written quoted;
  `DATE` literals for `DATE` columns; only `SELECT`) and the application's
  notes. The parser's keyword table decides which names to quote (`month`,
  `value`, `date`, ...), so the list never goes stale.
- `executeQuery(sql)` plans through the Frameworks API with parser config
  `Lex.MYSQL_ANSI`, `Programs.standard()` and
  `SqlToRelConverter.config().withTrimUnusedFields(true).withRemoveSortInSubQuery(false)`.
  Calcite drops an `ORDER BY` in a subquery without a limit by default, and
  the grid's sort wrapper
  `SELECT * FROM (SELECT * FROM (q) AS _t ORDER BY "c" DESC) AS _limited LIMIT n OFFSET m`
  relies on it. A statement that is not a query throws
  `ToolException("Only SELECT queries are allowed.")` before any source is
  read. `BINDABLE_SCANS` turns the logical scans of sources into
  `Bindables.BindableTableScan`, as Calcite's `RelRunners` does, and the plan
  runs through `RelRunner.prepareStatement`. Columns keep the labels the
  query wrote; `java.sql` dates, timestamps and times come back as
  `java.time`.
- Errors: a parse error becomes a `ToolException` with its position, without
  the list of expected tokens; a validation error becomes one with the
  validator's message, which names only what the query and schema contain; a
  `ToolException` from a source is found in the cause chain and relayed;
  anything else is logged at `WARN` and thrown as
  `IllegalStateException("Query failed")`.
- Cache: a source's rows are kept per source name and `Pushdown` for
  `getCacheDuration()`. A grid render runs the same query for validation, the
  sample row, the count and each page, so it reaches each backend once. A hit
  is logged at `INFO` with the table, filters and row count.

**`SourceTable`** (new, package-private, `final`, `extends AbstractTable implements FilterableTable`)

- `sqlType(Class<?>)`: `VARCHAR`, `INTEGER`, `BIGINT`, `DOUBLE`,
  `DECIMAL(19, 4)`, `BOOLEAN`, `DATE`, `TIMESTAMP`, `TIME`, all nullable;
  `null` for other types (the constructor check uses it).
- `scan(DataContext, List<RexNode> filters)`: for a source with
  `EQUALITY_FILTER`, expand `SEARCH`, split conjunctions, and take
  `column = literal` (either side, casts unwrapped) and the boolean forms
  (bare column, `NOT`, `IS TRUE`, `IS FALSE`). A literal that does not convert
  to the column type is not pushed. A filter node leaves the list only when
  all its conjuncts were taken, because Calcite checks that the remaining
  nodes are the original ones.
- Rows come through the provider's cache, are checked against the pushed
  filters (numbers as `BigDecimal`) and converted to Calcite's internal
  values (epoch days, epoch milliseconds in UTC, milliseconds of the day). A
  value of the wrong type throws `IllegalArgumentException` naming the column.
- `FilterableTable`, not `ProjectableFilterableTable`: with assertions on
  (surefire's default) the planner asserts on a filtered and sorted
  projectable scan.

### Step 4: core `AdapterTable` (new, `@since 25.4`)

```java
public interface AdapterTable extends Serializable {
    String getName();
    default String getDescription() { return null; }
    Table createTable(SchemaPlus rootSchema);  // once on connect; may register the adapter's schema
}
```

A table one of Calcite's own adapters reads and pushes down into. The
constructor
`FederatedDatabaseProvider(List<TableSource> sources, List<AdapterTable> adapterTables, String notes)`
adds them to the schema under the same name check; they are not cached, and
`BINDABLE_SCANS` leaves them to their adapters. With them come:

- `MERGE_SORTS`: a limit directly over a sorted subquery (through an identity
  projection, as the grid writes it) becomes one `LogicalSort` with order,
  offset and limit before planning. Otherwise an adapter's sort rule may drop
  the inner order, as Calcite's MongoDB rule does. Correct for any plan, so
  it always runs.
- `public String explain(String sql)`: the physical plan, one operator per
  line, showing what reaches each backend. Throws `ToolException` for a query
  that does not parse or validate.
- An adapter's "Unable to implement" failure becomes a `ToolException` that
  names the expressions to use on the columns.

The class Javadoc says the application adds `calcite-core` and each
adapter's module. No sample uses an adapter table; MongoDB is read through
the driver (Step 6), and Findings say why.

### Step 5: core `DataProviderTableSource<T>` (new, `@since 25.4`)

```java
public class DataProviderTableSource<T> implements TableSource {
    public DataProviderTableSource(String name, Class<T> itemType, DataProvider<T, ?> dataProvider)
}
```

Data an application already serves to its grids joins the schema. The
columns are the top-level `BeanPropertySet` properties of the item type
(beans and records), primitives boxed, in name order, of the Step 2 types
only; an item type with none throws `IllegalArgumentException`. The
description is `"Flow data provider of <simple name>"`. `fetch` asks for
every item with `new Query<>()` and pushes nothing down, since a data
provider's filter type is its own. `flow-data` comes through
`vaadin-flow-components-base`.

### Step 6: table sources over the sample endpoints (`it/providers`)

The IT pom declares `calcite-core` with the exclusions of Step 1, because
core's optional dependency does not reach it.

- **`RestTableSource`**: `discover(URI api)` returns one source per
  collection of the index; `of(URI api, String collection)` infers the
  columns from the first page of 100 (`Integer`, `Long`, `BigDecimal`,
  `Boolean`, ISO `LocalDate` and `LocalDateTime`, else `String`). Filters go
  out as `field=value`; `fetch` reads every page. A `400` becomes a
  `ToolException` with the API's `error` text, which names only fields.
- **`GraphQLTableSource`**: `discover(URI endpoint)` posts graphql-java's
  `IntrospectionQuery.INTROSPECTION_QUERY` (a hand-written query that asks
  for `fields` in more than one place is refused) and finds the query type
  by name. Scalar and enum fields become columns, an object field with an
  `id` becomes `<field>Id`, list fields are left out, and a string column
  whose first-fetch values all parse as ISO dates becomes a date column.
  Root field arguments that match a column by name and type are the
  push-down (`getArguments()`); the capability is declared only when there
  is one.
- **`MongoTableSource`**: `MongoTableSource(String connectionString, String database, String collection)`
  and `discover(String connectionString, String database)` over the base's
  5.x driver, with a lazily created `transient` `MongoClient`. Types come
  from 100 sampled documents (numbers widen, a `Date` at midnight UTC is a
  `LocalDate`, mixed types become `String`), `_id` is `id`, nested documents
  and arrays are left out. Filters go into `find` as `Filters.eq`, dates as
  `Date` in UTC and decimals as `Decimal128`.
- **`CsvTableSource`**: `CsvTableSource(Path file)` and
  `inDirectory(Path directory)`. The table is the file name without `.csv`,
  the first row is the header, quoted fields are handled, types come from
  every value (`Integer`, `Long`, `BigDecimal`, `LocalDate`,
  `LocalDateTime`, `Boolean`, else `String`), and an empty field is `null`.
  Read again on each fetch, nothing pushed down.
- **`JpqlTableSource`**:
  `JpqlTableSource(SerializableSupplier<EntityManagerFactory>, Class<?> entityClass)`.
  The table is the entity name, and the columns are the basic attributes and
  `<attribute>Id` for each to-one association, in name order. One JPQL query
  selects the columns, left-joins the associations and takes the equality
  filters as `WHERE <path> = :p<n>`.

The sources do not log their calls: the base logs them where they are served
(`SampleApiServer`, `LoggingMongoBackend`, `SampleJpaDatabase`).

### Step 7: sample providers and the chat pages

- **`it/providers/FederatedSampleProvider`**, static factories returning
  `FederatedDatabaseProvider`:
  - `rest()`, `graphql()`, `mongo()`, `csv()`: the whole data set from one
    endpoint type.
  - `jpql()`: a `JpqlTableSource` per entity of `ENTITY_CLASSES`.
  - `dataProviders()`: a `DataProviderTableSource` per entity of
    `ENTITY_CLASSES`, over
    `DataProvider.fromCallbacks` with a paged
    `SELECT e FROM <Entity> e ORDER BY e.id` and a `COUNT(e)` query, the data
    provider an application already has for its grids.
  - `federated()`: `sales`, `products`, `websiteTraffic` from REST;
    `employees`, `projectTasks`, `stockPrices` from GraphQL; `energyFlow`,
    `kpiMetrics`, `budgetItems` from MongoDB; `org_chart`, `sales_pipeline`,
    `expense_categories` from CSV. The LLM writes one SQL across them.
  - Notes in each endpoint's naming (the public `CAMEL_CASE_NOTES`,
    snake_case for CSV, entity names for JPA) say which column orders the
    sales and what the website traffic indices mean; MongoDB adds that nested
    documents are not exposed.
- **`it/providers/RestCalciteDatabaseProvider(URI api, String notes)`**: the
  REST API as a `DatabaseProvider` written against Calcite directly, doing
  what `rest()` does (inferred types, filters as query parameters, every
  page, a ten-second cache with logged hits, `SELECT` only, safe errors, the
  grid wrappers, serializable). It measures what an application writes
  without the helper; the comparison is under Findings.
- **`it/providers/JdbcDatabaseProvider(Supplier<DataSource> dataSource, String dialect, String schemaName, String notes)`**:
  a plain `DatabaseProvider` for the endpoints that are SQL databases
  already. The schema text names the dialect and comes from
  `JdbcSchemaDescriber`, which marks a column the database rejects bare as a
  reserved word by running it in a query that returns no rows. Only `SELECT`
  and `WITH` run; syntax, unknown-name and rights errors are relayed.
- **`it/SampleDatabaseProviders`** (package-private): names in select order
  with a description and a factory: `h2` (`DEFAULT`,
  `InMemoryDatabaseProvider`); `postgresql`, `mysql`, `tenant-north`,
  `tenant-south` and `jpa-native` (`JdbcDatabaseProvider` over the H2
  compatibility modes, the tenant schemas and the JPA tables, with the
  dialect and H2 reserved-word rules in the notes); `jpql`, `data-provider`,
  `rest`, `rest-direct`, `graphql`, `mongo`, `csv` and `federated` through
  Calcite. `names()`, `describe(name)`, `get(name)`; each
  provider is created on first use and shared by every page, so concurrent
  page loads cannot fill a database twice. An unknown name throws
  `IllegalArgumentException`.
- **`it/GridAIChatPage` and `it/ChartAIChatPage`**: a `Select<String>`
  (`provider-select`, label "Data provider", items `name: description`,
  starting at `h2`). A change from the client starts a new chat with a new
  grid or chart, controller and orchestrator. Both pages read
  `?provider=<name>` in `beforeEnter` (`BeforeEnterObserver`), because the
  view is created in the client-side navigation request, not the page
  request. The chart page replaces its `DATABASE` constant and keeps save
  and restore; the grid page moves its set-up into a `startChat()` and keeps
  its query view. The IT pom adds `com.vaadin:vaadin-select-flow`, which the
  base lacks.

### Step 8: tests

Core (`vaadin-ai-core-flow/src/test/java/com/vaadin/flow/component/ai/provider/`):
- `FederatedDatabaseProviderTest` with a recording `TableSource` and a fixed
  adapter table: schema text and quoting; labels and identifiers; equality
  push-down with and without the capability, per column type; other filters
  in the engine; joins and aggregation across sources; the grid's three
  wrappers; `java.time` values and nulls; safe `ToolException`s for unknown
  names and syntax errors; writes rejected before any fetch; a source's
  `ToolException` relayed, other failures not; the cache; serialization; an
  adapter table in a join; `explain`; constructor rejections.
- `DataProviderTableSourceTest`: records and beans, supported properties in
  name order, every item fetched, a query through the provider, an unusable
  item type rejected.
- `AIComponentsSerializableTest` excludes the `provider` package, so the
  serialization test above covers it.

Integration tests module (`src/test/java/com/vaadin/flow/component/ai/tests/providers/`),
JUnit against the sample endpoints:
- `RestTableSourceTest`, `GraphQLTableSourceTest`, `MongoTableSourceTest`,
  `CsvTableSourceTest`: discovery, inferred columns, typed rows, push-down
  reaching the endpoint (dates and ids included), the REST API's rejection
  of an unknown field relayed, CSV quoting; for MongoDB also functions such
  as `UPPER` and aggregation in the engine.
- `FederatedSampleProviderTest`: every endpoint alone serves all of
  `SampleData.TABLES`; `federated()` lists each table once; aggregation over
  REST; joins of REST with MongoDB and of GraphQL with CSV; the grid
  wrappers; date literals; a quoted reserved column.
- `JpqlTableSourceTest`: columns in name order with the association id,
  equality filters, dates and association ids pushed into the JPQL, a null
  association, and aggregation and a self-join through the provider.
- `JdbcDatabaseProviderTest`: the dialect in the schema text, PostgreSQL's
  plain `month`, MySQL's backticks, a tenant that sees its region only with
  the reserved `MONTH` marked, native SQL over the JPA tables, and writes
  rejected.
- `FederatedJpaProviderTest` and `RestCalciteDatabaseProviderTest`: schema,
  aggregation, dates and the grid wrappers; for the direct provider also safe
  errors and serialization.

### Step 9: findings

The Findings section at the end of this plan is kept up to date as the steps
land: what the console shows, what Calcite taught, the replaced MongoDB
adapter, the comparison with `RestCalciteDatabaseProvider`, the GraphQL
adapter that does not exist, and a verdict.

### Alternatives considered

- **H2 alone** (function tables or `org.h2.api.TableEngine`): no new
  dependency, and the cheapest place to start. But H2 pushes nothing down
  beyond function arguments, so the filters REST, GraphQL and MongoDB can
  take would be lost. This branch goes to Calcite to measure push-down and
  the dependency cost in one prototype.
- **DuckDB**: a native library per platform, and Java-side virtual tables
  are less direct.
- **Calcite's own adapters as the main route**: the MongoDB adapter brought
  `$match`, `$sort`, `$skip`, `$limit` and `$group` push-down, and also a
  driver version pin, date filters that silently matched nothing, failures
  on most functions and no nested documents. `MongoTableSource` replaced it;
  `AdapterTable` stays as the hook.
- **JPA through Calcite's JDBC adapter**: it would push everything down to
  the database, but an application reaches its entities through the data
  providers of its grids, so `DataProviderTableSource` goes that way.
- **`ProjectableFilterableTable`**: the planner assertion above, and the
  sample APIs have no field selection to push to.
- **No helper**: `RestCalciteDatabaseProvider` shows that each provider
  written against Calcite has to learn every workaround again, so a framework
  that takes this route ships `FederatedDatabaseProvider` and `TableSource`.
- **Mirror providers** (`MirrorDatabaseProvider` over `SqlMirror` on the
  research branch): simpler when a snapshot is acceptable, but a second copy
  to refresh and secure. This branch pulls per query, only what the engine
  asks for.
- **Typed operations**, **a semantic model** and **SQL parsed into a tree**:
  each a new provider kind with its own controller constructor. The first
  two replace SQL with JSON the LLM fills in; the third keeps SQL and also
  the limits of the collection-shaped endpoints.

### Follow-ups

1. Sort, limit and count push-down: more `Capability` values, which need
   planner rules on a custom convention. Until then a source returns every
   row of a scan, and a count is a full fetch.
2. A row cap per source, so the scan of a large collection stops.
3. Projection push-down for APIs with field selection, once the planner
   assertion is avoided.
4. Type inference beyond the first page or sample, and nested JSON and
   documents (`stockPrices.prices`, `orgChart.manager`).
5. `DataProviderTableSource`: page through the data provider, and push
   filters where the application maps them to its filter type.
6. Concurrency: one connection runs one query at a time, and the sample
   providers are shared by every page.
7. Packaging: about 23 MB in 30 jars that Calcite does not split. Decide
   between an optional dependency of core and a module of its own, and
   document the pom exclusions for applications.
8. Evaluation: a benchmark over the federated providers, and a comparison
   with the research branch's mirror providers on the same chat pages.

## Verification

1. `mvn -o spotless:apply` for the changed files, then
   `mvn -o test -pl vaadin-ai-components-flow-parent/vaadin-ai-core-flow`
   (includes `AIComponentsSerializableTest`).
2. `mvn -o install -DskipTests -pl vaadin-ai-components-flow-parent/vaadin-ai-core-flow,vaadin-ai-components-flow-parent/vaadin-ai-extensions-flow`,
   then
   `mvn -o test -pl vaadin-ai-components-flow-parent/vaadin-ai-components-flow-integration-tests -DskipFrontend -DskipJetty -Dtest='*TableSourceTest,Federated*Test,RestCalciteDatabaseProviderTest,JdbcDatabaseProviderTest,Sample*Test'`.
   The IDE rebuilds `target/classes` of both modules from a stale project
   model, and Maven's incremental compile then keeps its error stubs: delete
   the module's `target/classes` and `target/test-classes` before a Maven run
   when that happens.
3. `mvn -o dependency:tree` on both modules with
   `-Dincludes=com.fasterxml.jackson.core:jackson-annotations,org.slf4j:slf4j-api`
   shows `jackson-annotations` only at the version Flow's Jackson 3 needs and
   `slf4j-api` only at 2.x; the endpoints' `INFO` lines appear in the IT
   server console.
4. The SQL path is unchanged:
   `git diff --stat custom-data-providers-base -- vaadin-ai-components-flow-parent/vaadin-ai-extensions-flow vaadin-ai-components-flow-parent/vaadin-ai-components-flow-benchmark`
   is empty, and core's `DatabaseProvider` and `DatabaseProviderAITools` are
   untouched, so the SQL benchmark is not run again.
5. Manual, with `OPENAI_API_KEY` set: start the IT server
   (`mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -B -DskipTests -pl vaadin-ai-components-flow-parent/vaadin-ai-components-flow-integration-tests`),
   open `vaadin-ai/grid-ai-chat?provider=federated` and
   `vaadin-ai/chart-ai-chat?provider=federated`, and ask for rows of one
   region, a total per group, and something that needs two endpoints. The
   console shows each backend call as its endpoint logged it (query
   parameters, field arguments, the `find` filter) and the cache hits while
   the grid pages. Changing the select starts a new chat. Stop the server
   with `mvn jetty:stop` on the same module.
6. `git status` is clean of build and test artefacts.

## Findings

What building the steps above taught.

### What the console shows

The sample endpoints of the base log every call they receive at `INFO`, with
what was asked and what came back, so the sequence from the LLM's SQL to the
endpoints can be read off the console:

```
SampleApiServer - GET http://127.0.0.1:PORT/api/sales?size=100&page=0&region=North -> 6 items of 6
SampleApiServer - POST http://127.0.0.1:PORT/graphql { employees(department: "Engineering") { id name department salary age } } -> employees: 2 items
LoggingMongoBackend - find sample.stockPrices {"filter" : {"$and" : [ { "tradeDate" : "2025-01-06T00:00:00Z" } ]}} -> 1 documents
SampleJpaDatabase - SELECT e.age AS c0, e.department AS c1, e.id AS c2, e.name AS c3, e.salary AS c4 FROM Employee e WHERE e.department = :p0 -> 2 rows
```

The query parameters, the field arguments, the `find` filter and the JPQL
`WHERE` in those lines are the push-down at work; a source without the
capability, or a query with no equality filter, shows the bare call. The CSV
source reads its files directly, so nothing logs its reads.

### What Calcite taught

Each of these cost an iteration and is handled in the code or recorded as a
limit:

- **It drops `ORDER BY` in a subquery** that has no limit, which SQL allows
  and the grid's sort wrapper relies on. There is no connection property for
  it, so planning goes through the Frameworks API with
  `withRemoveSortInSubQuery(false)`, and the plan is executed through
  `RelRunner` after replacing the logical scans with bindable ones, the way
  Calcite's own `RelRunners` does.
- **An adapter's sort rule can drop it a second time**: Calcite's MongoDB
  rule converts the input of a limit to an empty collation, so the inner sort
  of the grid's wrapper disappeared from the pipeline and the page came back
  unsorted. The provider now merges a limit over a sorted subquery into one
  sort before planning, which any adapter keeps.
- **The planner asserts on `ProjectableFilterableTable` plans** when Java
  assertions are on (surefire's default): "rel has lower cost than best cost
  of subset" on a filtered and sorted scan. A `FilterableTable` does not
  trigger it, so sources push filters only and leave projection to the
  engine. Projection push-down would matter for an API with field selection;
  the sample APIs have none.
- **Filters arrive rewritten**: `a = 1 AND b = 2` can be one node, `x = 1`
  becomes `SEARCH(x, Sarg[1])`, and `active = TRUE` becomes the bare column.
  The adapter expands `SEARCH`, splits conjunctions and reads the boolean
  forms. A node is removed from the filter list only when every conjunct was
  pushed, because Calcite checks that what remains are the original nodes.
- **`SUM` over an `INTEGER` column is an `INTEGER`**, where H2 gives a
  `BIGINT`. `COUNT(*)` is a `BIGINT` in both.
- **Reserved words**: `month`, `value`, `date`, `year`, `start`, `end` and
  many more must be quoted. The schema text renders such columns quoted, and
  the parser's keyword table is used to decide, so the list never goes stale.
- **String literals of different lengths are padded in a UNION or CASE**:
  `'Rent'` is a `CHAR(4)` and `'Marketing'` a `CHAR(9)`, and a UNION or CASE
  over both is a `CHAR(9)`, so `'Rent'` becomes `'Rent     '` and no longer
  equals `Rent` read from a table. A join on such labels silently dropped
  rows: a budget-against-actuals chart lost three of its four columns. The
  planner of the Frameworks API takes the type system from its own
  configuration and ignores the connection's conformance, so the provider
  passes a type system whose `shouldConvertRaggedUnionTypesToVarying()` is
  true.
- **Calcite's Jackson 2 `jackson-annotations` shadows Flow's Jackson 3 one**;
  the two artifacts share a package. With Calcite declared first in a pom,
  Flow's mapper fails to initialise, which here showed up as the sample HTTP
  server's handler dying and the client waiting forever. Both poms exclude
  the artifact, as the integration tests already do for Spring AI.
- **Calcite's `slf4j-api` 1.7.25 wins the dependency resolution** in a
  module that declares `calcite-core` directly, and a 2.x SLF4J
  implementation cannot bind to it, so every log line of the module went to
  the no-operation logger without a word, the controllers' own lines
  included. Both poms exclude the artifact, and Flow's 2.x API takes over.
- **graphql-java rejects an introspection query that asks for `fields` in
  more than one place** ("not asking for introspection in good faith"), so
  the GraphQL source uses graphql-java's own standard introspection query and
  looks the query type up by name in the returned types.
- **Dependency footprint**: about 23 MB in 30 jars under `calcite-core`
  (guava, janino, avatica, commons-math3, JTS, proj4j, json-path, sketches,
  dbcp2, a second Jackson, protobuf and httpclient5 through Avatica), plus
  the MongoDB adapter and driver. Most of it is optional for this use
  (spatial, JSON functions, lattices, remote Avatica) but Calcite does not
  split it into modules, so an application that opts in carries it all.

### MongoDB: Calcite's adapter, tried and replaced

The first version read MongoDB through Calcite's own adapter, as an
`AdapterTable`, which brought `$match`, `$sort`, `$skip`, `$limit` and
`$group` push-down for free. It also brought this, each verified by a test at
the time:

- **It is compiled against driver 4.x.** With the 5.x driver the project
  uses, `MongoDatabase.listCollectionNames()` has another return type, a
  change the driver documents as binary-breaking, and the adapter fails with
  `NoSuchMethodError` when it lists the tables. Pinning the driver to 4.10.2
  worked; a recompile of the adapter's package against 5.x would too.
- **It is greedy and partial.** Its project rule claims every projection, so
  a function it cannot translate (`UPPER`, `COALESCE`, `EXTRACT`, most
  others) fails the whole query at implementation time instead of running in
  the engine. Comparisons, arithmetic, `CASE`, `CONCAT` and `SUBSTRING` are
  what it translates. The provider still turns an adapter's "unable to
  implement" into a hint for the LLM, should an adapter table be used.
- **A `DATE` literal reaches MongoDB as a number** rather than a BSON date,
  so a date filter matched nothing, silently.
- **Nested documents cannot be reached**, so the sample's `stockPrices.prices`
  and `orgChart.manager` could not be exposed.
- **It presents a collection as one `_MAP` column**, so a typed view with a
  cast per field had to be generated, the way Calcite's documentation shows.

Calcite itself is active (1.42.0 in May 2026, seven months after 1.41.0,
with 39 contributors), but its adapters are maintained by whoever needs them.
For this prototype the adapter was replaced by `MongoTableSource`, a source
over the 5.x driver like the REST and CSV ones: equality filters, dates and
ids included, go into `find`, everything else runs in the engine, nested
documents are still left out, and the sort, limit and aggregate push-down is
gone. The `AdapterTable` hook stays for an adapter that is kept current, and
the sort merge it needed is kept because it is correct for any adapter.

### Without the helper: a provider written against Calcite directly

To see what an application has to write when the framework ships nothing,
`it/providers/RestCalciteDatabaseProvider` implements `DatabaseProvider` over the
same REST API with Calcite alone: no `FederatedDatabaseProvider`, no
`TableSource`. It does what the `rest` provider does (typed columns inferred
from the first page, equality filters pushed as query parameters, every page
fetched, a ten-second row cache, `SELECT` only, safe errors relayed, the
grid's wrappers honoured, serializable) and is selectable on the chat pages as
`rest-direct`. The same six scenarios pass against it.

Lines of code without the license header, blank lines and comments:

| | Code lines | Comment lines | Calcite imports |
|---|---|---|---|
| `RestCalciteDatabaseProvider`, everything in one class | 506 | 62 | 31 |
| `RestTableSource`, the same endpoint through the helper | 196 | 37 | 0 |
| The helper itself in core (`FederatedDatabaseProvider`, `SourceTable`, `TableSource`, `AdapterTable`) | 692 | 323 | |

Of the direct provider's code, roughly 300 lines are Calcite (connection and
schema, the `FilterableTable`, filter extraction, type mapping both ways,
planning with the Frameworks API, reading the result set, turning the three
kinds of failure into something the LLM may see) and roughly 170 are the REST
API (discovery, paging, type inference, JSON values, the HTTP client). The
helper route keeps only the second part, and its `TableSource` has no Calcite
type in its signature at all.

What a developer writing the direct version has to know, none of which is in
Calcite's getting-started material:

- Plan through the Frameworks API with `withRemoveSortInSubQuery(false)` and
  run the plan with `RelRunner`, or the grid's sort wrapper returns unsorted
  pages. A plain `statement.executeQuery(sql)` on the Calcite connection is
  the obvious way and is wrong for this use.
- Implement `FilterableTable`, not `ProjectableFilterableTable`, or the
  planner asserts under `-ea`.
- Expand `SEARCH` and split conjunctions before looking for `column =
  literal`, and leave a filter in the list unless every part of it was taken.
- Hand dates to Calcite as days since the epoch and timestamps as
  milliseconds, and read `java.sql.Date` and `Timestamp` back out.
- Use the parser's keyword table to quote reserved column names in the
  schema text, or the LLM writes `SELECT month FROM sales` and gets a parse
  error.
- Give the planner a type system that turns a UNION or CASE of string
  literals into `VARCHAR`, or a join on such a label drops rows without an
  error.
- Exclude Calcite's Jackson 2 annotations in the pom.
- Find a `ToolException` thrown inside a scan in the cause chain of the
  `SQLException` Calcite wraps it in, and strip the expected-token list from
  parse errors.

Two things the direct version did not need, because it has no adapter tables
and its schema lives on its own connection: the bindable-scan rewrite and the
sort merge of the federated provider.

The verbosity itself is the finding. Everything above was learned once while
building the helper and is paid again by every provider written without it,
so a framework that takes this route has to ship the helper, and the
`TableSource` contract is the right size for what applications then write.

### GraphQL and the adapter that does not exist

GavinRay97's GraphQLCalcite looked like a candidate, but it goes the other
way: it
generates a GraphQL API from a Calcite schema, converting incoming GraphQL
queries into relational expressions, and it is a Kotlin work-in-progress that
is not published. No open-source Calcite adapter over a GraphQL API exists
either. `GraphQLTableSource` is the "similar" thing built here: an
introspection-driven mapping of root list fields to tables with argument
push-down, which is how such an adapter would start. A full adapter would add
nested selections as joins and the `where`, `order_by` and `limit` arguments
of Hasura-style APIs as pushed sort and limit.

### Verdict

The approach does what it set out to do: the LLM writes the SQL it writes
today, the prompts and tools are untouched, and the collection-shaped
endpoints gain joins and aggregation without a copy of the data. The costs
are concrete now: a heavy optional dependency, a growing list of Calcite
behaviours that have to be known and worked around, and in-memory sorting and
paging for hand-written sources until push-down rules are written. Calcite's
own adapters cut both ways: the MongoDB one brings real push-down for free,
and also a driver version pin, a silent miss on date filters and a hard
failure on most functions, which is why the prototype ended up reading
MongoDB through the driver like every other endpoint. The mirror providers on
the research branch remain the simpler route when a snapshot is acceptable;
this one wins when the data must be fresh per query or when a query reaches
across endpoints.
