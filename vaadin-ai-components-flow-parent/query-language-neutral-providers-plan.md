# Drop the hard-coded SQL ties in the AI controllers

## Context

`DatabaseProvider` is the bridge between the LLM and the application's data.
Its contract is "the LLM writes SQL": the interface Javadoc, the
`get_database_schema` tool, the grid and chart instruction and tool texts,
and the grid's lazy loading (which wraps the LLM's query in
`SELECT * FROM (...) LIMIT/OFFSET`, `ORDER BY "col"` and `COUNT(*)`) all
assume it. The sample data endpoints added on this branch (REST, GraphQL,
MongoDB, JPA/JPQL, CSV, H2 variants) show that real backends speak other
query languages, and a provider for any of them cannot be written today
without the framework either rewriting its query as SQL (grid) or telling the
LLM the wrong language (both controllers).

This step makes the framework neutral about the query language, so that a
later step can add providers for those endpoints. It adds no provider, no
controller feature, and no controller redesign. Existing SQL providers keep
working unchanged and the benchmark's SQL scenarios must keep their pass
rates.

## Inventory of SQL ties (what the change has to cover)

Paths are relative to `vaadin-ai-components-flow-parent/`; `core` is
`vaadin-ai-core-flow/src/main/java/com/vaadin/flow/component/ai`, `ext` is
`vaadin-ai-extensions-flow/src/main/java/com/vaadin/flow/component/ai`.

| Layer | Where | SQL tie |
|---|---|---|
| Contract | `core/provider/DatabaseProvider.java` | `executeQuery(String sql)`, Javadoc and examples are JDBC/SQL; nothing tells the controllers which language the provider speaks |
| Schema tool | `core/provider/DatabaseProviderAITools.java` | `get_database_schema` description ends with "and the SQL dialect" |
| Grid query rewriting | `ext/grid/GridAIController.java:182` (validation `SELECT * FROM (q) AS _v LIMIT 1`), `ext/grid/GridRenderer.java:70,164-234` (sample `LIMIT 1`, `COUNT(*)`, `ORDER BY "col"`, `LIMIT/OFFSET`) | the only place the framework rewrites the LLM's query; breaks any non-SQL language |
| Grid prompt text | `ext/grid/GridAITools.java:189-229` (`update_grid_data` "SQL RULES", shape examples, parameter description), `:135` (`get_grid_state`), `ext/grid/GridAIController.java:118-141` (instructions) | tells the LLM to write SQL SELECT with AS aliases |
| Chart prompt text | `ext/chart/ChartAITools.java:565-676` (`update_chart_data_source`: ~15 `SELECT ... AS _x` examples, "in your SELECT statements", parameter description), `:246-258` (`get_chart_state`), `ext/chart/ChartAIController.java:149-172` (instructions) | same, chart side; queries themselves run unwrapped (`ChartRenderer.java:78-81`, `ChartAIController.java:237-251`) |
| Javadoc only | `GridState`, `GridEntry`, `ChartState`, `ChartEntry`, `ColumnNames`, `ChartRenderer`, `ChartAIController`, `GridAIController`, `LLMProvider.ToolSpec` example | "SQL query", "SQL aliases" |
| Tests | `ext/.../grid/GridAIControllerTest$LazyDataProviderTests` (five tests assert the exact wrapper strings) | wrappers become core defaults |
| Benchmark | `vaadin-ai-components-flow-benchmark` (`GridAIControllerBenchmark` runs the produced query on H2; one scenario rejects `SELECT *` and `LIMIT/OFFSET`) | stays SQL; used to prove no regression |
| Docs (separate repo) | `/repos/docs/articles/flow/ai-support/controllers.adoc`, `ai-powered-grid.adoc`, `ai-powered-chart.adoc`, `components/charts/ai-powered.adoc` | describe an SQL-writing LLM; updated in a docs PR after this step |

Already neutral and untouched: `DataConverter`, `DefaultDataConverter` (rows
are column-name-to-value maps), `AIDataRow`, `GridFormatting`,
`ChartConfigurationParser`, the orchestrator and `AIController`.

## Approach

Three ideas, chosen with the user:

1. A new language-neutral core interface `QueryableProvider` is what the
   controllers and renderers accept. `DatabaseProvider` extends it as the SQL
   specialisation: its new default methods carry today's SQL wrapping, so every
   existing SQL provider compiles and sends the same statements as before.
   After this step the extensions module contains no SQL text at all; SQL is
   composed in exactly one place, `DatabaseProvider`.
2. The provider describes its language with a `QueryLanguage` value: a name,
   LLM-facing rules, and `exampleQuery(source, columns, aliases)` that renders
   the tool texts' examples. The SQL language renders today's grid and chart
   examples byte-identical, so the SQL prompts barely change; a later GraphQL
   or Mongo language renders its own examples.
3. Paging, sorting and counting are provider concerns: a `QueryPage` record
   carries offset, limit and sort orders, the neutral default pages in memory,
   `count` is optional (unknown size makes the grid use Flow's undefined-size
   lazy loading), and the SQL defaults do what `GridRenderer` does today.

Branch: Implement it on `prototype/query-language-neutral-providers` in multiple meaningful commits. Every commit is auto-accepted on this branch.

### Step 1: core value types (`vaadin-ai-core-flow`, package `com.vaadin.flow.component.ai.common`, Apache header, all `@since 25.4`)

**`QueryPage`** (new record, Serializable)

```java
public record QueryPage(int offset, int limit, List<QuerySortOrder> sortOrders) implements Serializable {
    // compact constructor: offset >= 0, limit > 0, sortOrders null -> List.of(), else List.copyOf
    public static QueryPage first(int limit)            // offset 0, no sort
    public List<Map<String, Object>> apply(List<Map<String, Object>> rows)  // stable sort by result column name, then slice
}
```

`QuerySortOrder` is `com.vaadin.flow.data.provider.QuerySortOrder` from
`flow-data`, already on core's compile classpath through
`vaadin-flow-components-base` (declare `com.vaadin:flow-data` with
`<scope>provided</scope>` and no version in `vaadin-ai-core-flow/pom.xml` for
clarity, as `vaadin-grid-flow/pom.xml` does). Javadoc: `getSorted()` of a sort
order names a result column as `executeQuery` returns it. `apply` comparator:
nulls last, same-class `Comparable` natural order, mixed numbers via
`BigDecimal`, anything else by `String.valueOf`; input never mutated.

**`QueryLanguage`** (new interface, Serializable) and package-private
`SqlQueryLanguage` (final, stateless, `readResolve` returns the constant)

```java
public interface QueryLanguage extends Serializable {
    QueryLanguage SQL = new SqlQueryLanguage();
    String getName();                                  // "SQL": the word the tool texts use
    String getRules();                                 // LLM-facing rules block, constant per language
    String exampleQuery(String source, List<String> columns, List<String> aliases);
}
```

`SqlQueryLanguage.exampleQuery("data", ["temperature","sales"], ["_x","_y"])`
returns exactly `SELECT temperature AS _x, sales AS _y FROM data`; an alias
that is not a plain identifier is double-quoted (`AS "Readable A"`,
`AS "GroupName.Readable A"`). Size mismatch or empty columns throw
`IllegalArgumentException`. `getRules()` holds the SQL-only lines of today's
grid "SQL RULES" plus the aggregation hint the chart's basic example loses:

```
- Write one SELECT statement; never modify data
- ALWAYS list specific columns — NEVER use SELECT *
- Alias every column with AS; double-quote an alias with spaces or dots
- Aggregate with GROUP BY, e.g. SELECT month AS category, SUM(revenue) AS value FROM sales GROUP BY month
```

Javadoc on both types: values go into tool descriptions, which LLM providers
cache, so a provider must return the same language on every call and the
text must never carry per-request content (guideline: tool definitions
identical turn to turn).

### Step 2: core `QueryableProvider` (new, `com.vaadin.flow.component.ai.provider`, `@since 25.4`)

```java
public interface QueryableProvider extends Serializable {
    QueryLanguage getQueryLanguage();                      // constant per instance
    String getSchema();                                    // what the queries can refer to, plus notes
    List<Map<String, Object>> executeQuery(String query);  // the LLM's query, never rewritten by the framework
    default List<Map<String, Object>> executeQuery(String query, QueryPage page) { return page.apply(executeQuery(query)); }
    default OptionalInt count(String query) { return OptionalInt.empty(); }   // empty = unknown
}
```

Javadoc carries the contract the later providers rely on: the framework
passes the query string through unmodified; `ToolException` semantics (move
the text from `DatabaseProvider`); the paged default materialises the whole
result, override to push paging down; a present count means exact size, an
empty one makes the grid scroll open-ended. The class-level example is a
non-SQL sketch (GraphQL-shaped, hand-written) so the neutral contract's own
documentation is not SQL.

### Step 3: core `DatabaseProvider` becomes the SQL specialisation

File: `vaadin-ai-core-flow/.../provider/DatabaseProvider.java`

- `extends QueryableProvider`; keep `getSchema()` and `executeQuery(String sql)`
  with their JDBC-flavoured Javadoc (rename the parameter to `query` in the
  Javadoc text only where it says "SQL query to execute" is wrong; keeping
  `sql` as the name is fine for the SQL specialisation).
- New defaults, `@since 25.4`:
  - `getQueryLanguage()` → `QueryLanguage.SQL`
  - `executeQuery(String sql, QueryPage page)` → today's strings exactly:
    `SELECT * FROM (SELECT * FROM (sql) AS _t ORDER BY "a" ASC, "b" DESC) AS _limited LIMIT n OFFSET m`
    with sort orders, `SELECT * FROM (sql) AS _limited LIMIT n OFFSET m` without.
    Move `enrichQuery`/`wrapWithLimit` from `GridRenderer` here as private
    static interface methods.
  - `count(String sql)` → `SELECT COUNT(*) FROM (sql) AS _counted`, first value
    of the first row as a `Number`; empty result, no column or non-numeric
    value give `OptionalInt.of(0)` (today's behaviour, kept on purpose).
- Class Javadoc: one paragraph saying this is the SQL implementation of
  `QueryableProvider`, that the defaults page with `LIMIT`/`OFFSET` and count
  with a `COUNT(*)` subquery, and that a dialect without that syntax overrides
  them. `ToolException` Javadoc mentions both interfaces.

### Step 4: core `DatabaseProviderAITools`

File: `vaadin-ai-core-flow/.../provider/DatabaseProviderAITools.java`

- Widen `getDatabaseSchema(QueryableProvider)` and `createAll(QueryableProvider)`.
- Tool name `get_database_schema` stays (both controllers' instruction texts and
  application system prompts cite it; renaming is a prompt-compatibility
  break for no gain in this step). Description is built once from
  `provider.getQueryLanguage().getName()`:
  `"Gets the schema the <NAME> queries run against: the tables or types, their columns or fields with types, and notes on writing <NAME> queries."`

### Step 5: extensions, grid (`vaadin-ai-extensions-flow/.../grid`, commercial header)

**`GridAITools`**
- `Callbacks` gains `default QueryLanguage getQueryLanguage() { return QueryLanguage.SQL; }`
  (`@since 25.4`, Javadoc: named and quoted in tool descriptions, so constant).
  No new `createAll` overload and no deprecation needed; `createAll(Callbacks)`
  keeps its `AIExtensionsLicense.check()`.
- `updateGridData(callbacks)`: read `callbacks.getQueryLanguage()` once, build
  the description with `String.replace` (never `formatted`: rules may contain
  `%` or braces). Sketch, SQL rendering in brackets:

```
Updates the grid data using a {NAME} query. The grid creates its columns from the names of the result columns.

{NAME} RULES:
{RULES}

GRID RULES:
- ALWAYS give every column a human-readable name; it becomes the column header
- Do NOT limit or page the rows in the query — the grid fetches them in pages
- Use ONLY the names listed by get_database_schema()
Shape (placeholders, not real names):
{exampleQuery("TableName", [ColumnA, ColumnB], ["Readable A", "Readable B"])}
   [SELECT ColumnA AS "Readable A", ColumnB AS "Readable B" FROM TableName]

COLUMN GROUPING (only when the user asks for grouping):
... today's prose unchanged ...
Correct query: {exampleQuery("TableName", [ColumnA, ColumnB], ["GroupName.Readable A", "GroupName.Readable B"])}
```
- Parameter JSON schema stays language-free (nothing interpolated into JSON):
  `"query": "The query, listing specific columns with human-readable names. Do not limit or page the result."`
- `get_grid_state` description: "Returns the current grid state including its query."
- `Callbacks.updateData` Javadoc: "a query in the language of getQueryLanguage()".

**`GridAIController`**
- Field and constructor type `QueryableProvider` (source-compatible widening;
  `AIExtensionsLicense.check()` stays).
- Callbacks implementation adds `getQueryLanguage()` delegating to the provider.
- Validation: `provider.executeQuery(query, QueryPage.first(1))` (for SQL this
  is `... AS _limited LIMIT 1 OFFSET 0` instead of `... AS _v LIMIT 1`; nothing
  asserts the old text and the database treats them the same).
- `INSTRUCTIONS_TEXT` becomes a template with `{NAME}`, resolved once in the
  constructor; `getDescription()` and `execute()` of the instructions tool
  return the same resolved text. Lines: "Retrieves the schema of the data
  (tables or types, columns, types)", "Updates grid data with a {NAME} query",
  "Call update_grid_data() with a {NAME} query using only names from the
  schema", "so every column keeps the qualifier the schema gives it".
- Class Javadoc bullet "Lazy loading with SQL LIMIT/OFFSET" → "Lazy loading
  through the provider's paged query execution".

**`GridRenderer`**
- `renderGrid(Grid<AIDataRow>, QueryableProvider, String)`; sample row via
  `provider.executeQuery(query, QueryPage.first(1))`.
- Replace `createDataProvider` with

```java
var count = provider.count(query);   // once per render; sorting cannot change it
var view = grid.setItems(fetchQuery -> provider
        .executeQuery(query, new QueryPage(fetchQuery.getOffset(), fetchQuery.getLimit(), fetchQuery.getSortOrders()))
        .stream().map(AIDataRow::new));
count.ifPresent(n -> view.setItemCountCallback(q -> n));
```
  (`Grid.setItems(FetchCallback)` returns `GridLazyDataView`, which has
  `setItemCountCallback`; without it the view is in undefined-size mode.)
- Delete the `SQL helpers` section and the `SortDirection`/`Collectors`
  imports; Javadoc "from SQL queries" → "from the query results of a
  QueryableProvider".

**`GridState`, `GridEntry`**: Javadoc "SQL query" → "query" (no code change).

### Step 6: extensions, chart (`vaadin-ai-extensions-flow/.../chart`)

**`ChartAITools`**
- `Callbacks` gains the same `default QueryLanguage getQueryLanguage()`;
  `getState`/`updateData` Javadoc drop "SQL".
- `updateChartDataSource(callbacks)`: read the language once; resolve the
  `ColumnNames` placeholders first, then insert `{NAME}` and `{RULES}` with
  `replace` (rules may contain braces, e.g. a Mongo pipeline). Text changes:
  "Updates the chart data using {NAME} queries (one per series)"; "Give the
  result columns the exact names below (prefixed with '{PREFIX}')"; every
  `- Example:` line becomes `language.exampleQuery(...)` with today's
  table/column/alias data (pass the `ColumnNames` constants as aliases), e.g.
  `exampleQuery("data", ["temperature","sales"], [X, Y])`; the basic example
  becomes `exampleQuery("sales", ["month","revenue"], ["category","value"])`
  (its `SUM ... GROUP BY` moved to the SQL rules); the OHLC volume note keeps
  prose: "a second query with {X}, {Y} and a {SERIES} column holding the
  constant series name (for example 'Volume')" plus
  `exampleQuery("stock_prices", ["date","volume"], [X, Y])`; `{PARENT} ... or
  null for a root` (lower-case null); add `{NAME} RULES:\n{RULES}` before
  `Parameters:`; parameter line "{NAME} queries, one per series, replacing the
  current ones". Parameter JSON schema description language-free:
  "Queries to execute, one per chart series".
- `get_chart_state` description: "the queries that populate its series".

**`ChartAIController`**: field/constructor `QueryableProvider`; Callbacks
implementation adds `getQueryLanguage()`; `INSTRUCTIONS_TEXT` template
("populates chart series from {NAME} queries", "ensure the result column
names of the queries match the new type") resolved once; validation keeps
`provider.executeQuery(queries.get(i))` (chart queries run as-is); Javadoc
at the four SQL mentions reworded.

**`ChartRenderer`**: widen the provider type; Javadoc only.
**`ChartState`, `ChartEntry`, `ColumnNames`**: Javadoc only ("the result
column names the LLM must produce", "real data column names").
**`LLMProvider.ToolSpec`** Javadoc example "The SQL query" → "The query"
(cosmetic, optional).

### Step 7: tests

Core (`vaadin-ai-core-flow/src/test/java/com/vaadin/flow/component/ai/`):
- `common/QueryPageTest`: validation, `first`, slicing past the end, asc/desc,
  multiple keys, nulls last, mixed numbers, non-comparable values, input not
  mutated. Serializability is covered by `AIComponentsSerializableTest`
  (`common` is not excluded).
- `common/QueryLanguageTest`: `SQL` name; rules mention `SELECT *`; the three
  grid example strings and two chart example strings pinned verbatim; alias
  quoting; size mismatch throws; `SQL` deserializes to the same instance.
- `provider/QueryableProviderTest`: paged default calls `executeQuery(String)`
  exactly once with the identical string and returns `page.apply`; `count`
  default empty.
- `provider/DatabaseProviderTest`: the wrapper assertions moved from
  `GridAIControllerTest$LazyDataProviderTests` against a recording stub
  (sample `LIMIT 1 OFFSET 0`, offset/limit, one and several sort orders,
  count string and value 42, the three `count_*` edge cases, default
  language `SQL`, `ToolException` relayed).
- `provider/DatabaseProviderAIToolsTest`: description names "SQL" with the
  existing stub; with a fake `QueryableProvider` whose language is named
  "GraphQL" it names GraphQL and not SQL.

Extensions (`vaadin-ai-extensions-flow/src/test/java/com/vaadin/flow/component/ai/`):
- `grid/GridAIControllerTest`: keep `StubDatabaseProvider` and the existing
  exact-SQL `LazyDataProviderTests` as the end-to-end proof that an untouched
  SQL provider sees the same strings (only `render_executesSampleQueryWithLimit1`
  changes its expected text to `... AS _limited LIMIT 1 OFFSET 0`). Add
  `PagedProviderTests` with a recording `QueryableProvider` stub: sample fetch
  is `QueryPage.first(1)` with the unmodified query; fetch passes offset 20,
  limit 10 and the sort orders through; count present → defined size with
  that number; count empty → `grid.getDataCommunicator().isDefinedSize()`
  false and fetching still works; validation uses the paged method with the
  unmodified query. Add `NonSqlProviderTests` with a fake GraphQL-shaped
  language: `update_grid_data`, `get_database_schema` and the instructions
  texts contain "GraphQL" and the rendered example and contain neither
  "SELECT" nor "SQL"; the query reaches the provider byte-identical;
  `executeQuery(String)` is never called with a rewritten string.
- `grid/GridAIToolsTest`: default callbacks → SQL text with the pinned shape
  example; callbacks overriding `getQueryLanguage()` → that language's text;
  parameter schema parses as JSON.
- `chart/ChartAIToolsTest$UpdateChartDataSource`: keep
  `description_containsColumnNameConstants`; add SQL keeps today's example
  strings (`SELECT temperature AS _x, sales AS _y FROM data`, the OHLC one);
  another language renders its own examples and rules; rules containing
  braces survive placeholder resolution; parameter schema parses as JSON;
  `get_chart_state` description has no "SQL".
- `chart/ChartAIControllerTest`: constructor accepts a `QueryableProvider`;
  instructions text names the provider's language; queries reach
  `executeQuery(String)` unmodified. `ChartRenderingTest` unchanged.
- Serializable tests: nothing new to exclude (`provider` package excluded,
  `GridAITools.*`/`ChartAITools.*` patterns cover the anonymous tools).

### Step 8: guideline and docs notes

- `guidelines/ai-components.md`, "Architecture": the boundary sentence names
  `QueryableProvider`/`DatabaseProvider`/`DatabaseProviderAITools`; add one
  bullet: the extensions never read or rewrite the query string; paging,
  sorting, counting and all language-specific prompt text come from the
  provider (`QueryableProvider`, `QueryLanguage`, `QueryPage`);
  `DatabaseProvider` is the SQL specialisation and the only place SQL is
  composed.
- Docs repo (separate PR, not from this container):
  `flow/ai-support/controllers.adoc` (Database Provider section: introduce
  `QueryableProvider`, reposition `DatabaseProvider` as the SQL
  implementation, `executeQuery(String sql)` wording, "lazy loading via SQL
  LIMIT/OFFSET"), `ai-powered-grid.adoc` L13/18/54/61, `ai-powered-chart.adoc`
  L13/18/20/69/115, `components/charts/ai-powered.adoc` L13.
- The integration-tests module (`InMemoryDatabaseProvider`,
  `DashboardChartControllerPage`), the benchmark's `BenchmarkDatabase` and the
  endpoint tests compile and behave unchanged; `SampleH2DatabaseTest`'s
  comment "the wrapping the grid controller applies" may say "the SQL
  wrapping DatabaseProvider's paged default applies".

### Alternatives considered (asked for; not recommended for this step)

- **Keep SQL as the only medium and adapt endpoints** (SQL mirrors of
  REST/Mongo data, SQL-to-X translation): no framework change and the LLM's
  strongest language, but a mirror is a second copy of the data (staleness,
  memory, tenant scoping and read-only guarantees redone per copy) and
  translation beyond projections is a research project. Nothing in this step
  prevents an application from doing it: a `DatabaseProvider` over an embedded
  SQL engine fed from an API is just another SQL provider.
- **Structured JSON query DSL** emitted by the LLM and compiled by the
  provider: language-neutral and validatable, but no model is trained on it,
  it cannot express joins or aggregation without reinventing SQL, and every
  provider needs a compiler. The chosen design admits it later as one more
  `QueryLanguage`, since the query stays an opaque string.
- **Provider unwraps the framework's SQL wrapper** by parsing it: fragile,
  ties every provider to the wrapper format forever, and leaks SQL into
  non-SQL providers' errors. The chosen design is the inverse: the SQL
  provider composes the wrapper, the extensions never touch query text.
- **Defaults on `DatabaseProvider` only** (no new type): smallest diff, but
  the neutral contract would still generate SQL unless a non-SQL provider
  overrides all three defaults, and a missed override sends SQL to a non-SQL
  backend.

### Follow-ups (separate steps)

1. First non-SQL provider over the sample endpoints (GraphQL first), with its
   `QueryLanguage`, push-down decisions, a grouping separator that works
   without dots in aliases, and the question whether `get_database_schema`
   and `DatabaseProviderAITools` get neutral names.
2. Chart: a per-query series name in `update_chart_data_source` so the
   `'Volume' AS _series` idiom is no longer needed.
3. `DatabaseProvider.count`: return unknown instead of 0 when the count row
   cannot be read.
4. `GridState`/`ChartState` could record the language name so a restore into
   a provider of another language fails clearly.
5. Docs repo update.

## Verification

1. Build and unit tests, offline where possible:
   `mvn -o spotless:apply` for the changed files, then
   `mvn -o test -pl vaadin-ai-components-flow-parent/vaadin-ai-core-flow,vaadin-ai-components-flow-parent/vaadin-ai-extensions-flow`
   (includes both `ClassesSerializableTest` subclasses).
2. Compile the dependants with the local modules:
   `mvn -o -am test-compile -pl vaadin-ai-components-flow-parent/vaadin-ai-components-flow-integration-tests -DskipFrontend -DskipJetty`
   and the benchmark module's test-compile; `InMemoryDatabaseProvider`,
   `DashboardChartControllerPage`'s inline provider and `BenchmarkDatabase`
   must compile untouched.
3. Benchmark, base commit vs change, same model, several runs
   (`AI_BENCHMARK_RUNS`), OpenAI key only (per the user's standing rule):
   `GridAIControllerBenchmark` and `ChartAIControllerBenchmark`; compare pass
   rates and input tokens per scenario in `target/ai-benchmark.jsonl`. Most
   sensitive scenarios: grid `showsAllRowsWithReadableColumns` (no `SELECT *`,
   no `LIMIT/OFFSET`), `groupsContactColumnsUnderHeading` (dotted aliases),
   chart pie "order value per region" (aggregation hint moved into the SQL
   rules). A regression is fixed in `SqlQueryLanguage.getRules()`, not by
   putting SQL back into the tools.
4. Manual smoke, optional: start the IT server and open
   `vaadin-ai/chart-ai-chat` with `OPENAI_API_KEY` set; the chart still answers
   from `InMemoryDatabaseProvider` (SQL) and `get_database_schema` names SQL.
5. `git status` clean of artefacts; commits shown to the user before they are
   made.
