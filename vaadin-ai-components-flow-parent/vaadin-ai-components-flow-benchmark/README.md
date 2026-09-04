# AI components benchmark

JUnit suite that runs the AI controllers (`FormAIController`, `GridAIController`,
`ChartAIController`) against a real LLM and scores the result. It measures the
parts the unit and integration tests cannot: the built-in instructions, tool
descriptions and schemas. The module is not published.

The suite is skipped unless `AI_BENCHMARK_MODEL` is set, so it never runs as
part of normal PR validation.

## Running

```sh
AI_BENCHMARK_MODEL=gpt-4.1-mini AI_BENCHMARK_API_KEY=sk-... \
  mvn test -pl vaadin-ai-components-flow-parent/vaadin-ai-components-flow-benchmark
```

The module tests the `vaadin-ai-core-flow` and `vaadin-ai-extensions-flow`
snapshots from the local repository. To benchmark uncommitted changes to the
controllers, add `-am` or install those two modules first.

Against a local OpenAI-compatible server such as Ollama:

```sh
AI_BENCHMARK_MODEL=qwen3:8b AI_BENCHMARK_BASE_URL=http://localhost:11434/v1 \
  mvn test -pl vaadin-ai-components-flow-parent/vaadin-ai-components-flow-benchmark
```

| Variable                    | Meaning                                                        | Default |
|-----------------------------|----------------------------------------------------------------|---------|
| `AI_BENCHMARK_MODEL`        | Model name. Required, enables the suite.                       |         |
| `AI_BENCHMARK_API_KEY`      | API key. Falls back to `OPENAI_API_KEY`.                        |         |
| `AI_BENCHMARK_BASE_URL`     | OpenAI-compatible endpoint. Makes the API key optional.        |         |
| `AI_BENCHMARK_RUNS`         | Attempts per scenario.                                         | `3`     |
| `AI_BENCHMARK_MIN_PASS_RATE`| Fraction of attempts that must pass for a scenario to pass.    | `0.67`  |

The controllers are commercial, so the run also needs a valid Vaadin license
in dev mode, the same as running the extension module's unit tests.

## Output

Each scenario appends one line to `target/ai-benchmark.jsonl`:

```json
{"scenario":"GridAIControllerBenchmark.filtersAndSortsInOneRequest","model":"gpt-4.1-mini","runs":3,"passed":3,"failures":[]}
```

Compare the file from a run before and after a prompt change to see the effect.
A scenario fails the build only when its pass rate is below
`AI_BENCHMARK_MIN_PASS_RATE`.

## Writing a scenario

A scenario is a plain `@Test` that hands one attempt to `AIBenchmark.score`.
The attempt builds fresh components and a controller, opens a conversation,
sends one or more user messages, and asserts on the resulting server-side
state. Scoring is deterministic: field values, the rows the produced SQL
returns, the chart type and series data. `BenchmarkDatabase` provides small
H2 data sets for grid and chart scenarios.

```java
@Test
void filtersAndSortsInOneRequest() {
    bench.score(() -> {
        try (var db = BenchmarkDatabase.customers()) {
            var grid = new Grid<AIDataRow>();
            var controller = new GridAIController(grid, db);
            try (var conversation = bench.conversation(grid, controller)) {
                conversation.say("Show only the European customers, highest revenue first");
            }
            var rows = db.executeQuery(controller.getState().query());
            Assertions.assertEquals(List.of("Nordic Traders", "Alpine Foods", "Iberia Textiles"),
                    BenchmarkDatabase.column(rows, "name"));
        }
    });
}
```

Keep the system prompt generic (it is fixed in `AIBenchmark`): the point is to
measure the controllers' own instructions, not to compensate for them.
