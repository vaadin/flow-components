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
controllers, install those two modules first, or build them in the same run.
A plain `-am` would also run the unit tests of every module the benchmark
depends on, so limit the tests to the benchmark:

```sh
AI_BENCHMARK_MODEL=gpt-4.1-mini AI_BENCHMARK_API_KEY=sk-... \
  mvn test -pl vaadin-ai-components-flow-parent/vaadin-ai-components-flow-benchmark -am \
  -Dtest='*Benchmark' -Dsurefire.failIfNoSpecifiedTests=false
```

Against a local OpenAI-compatible server such as Ollama:

```sh
AI_BENCHMARK_MODEL=qwen3-vl:8b AI_BENCHMARK_BASE_URL=http://localhost:11434/v1 \
  mvn test -pl vaadin-ai-components-flow-parent/vaadin-ai-components-flow-benchmark
```

| Variable                    | Meaning                                                        | Default |
|-----------------------------|----------------------------------------------------------------|---------|
| `AI_BENCHMARK_MODEL`        | Model name. Required, enables the suite.                       |         |
| `AI_BENCHMARK_API_KEY`      | API key. Falls back to `OPENAI_API_KEY`.                        |         |
| `AI_BENCHMARK_BASE_URL`     | OpenAI-compatible endpoint including `/v1`. Makes the API key optional. |         |
| `AI_BENCHMARK_MAX_TOKENS`   | Optional completion cap sent as `max_tokens`. Some hosts reject a request without one; OpenAI reasoning models reject the field. | unset |
| `AI_BENCHMARK_RUNS`         | Attempts per scenario.                                         | `3`     |
| `AI_BENCHMARK_MIN_PASS_RATE`| Fraction of attempts that must pass for a scenario to pass.    | `0.6`   |

The model is driven through `SpringAILLMProvider` and Spring AI's OpenAI
module, the same path the documentation teaches, so the numbers reflect what
a Spring application sees. Any OpenAI-compatible endpoint works through
`AI_BENCHMARK_BASE_URL`.

The form scenarios attach a receipt image and a PDF invoice, so the model has
to read images and accept PDFs. Only the image scenario checks where the model
says it read a value: some hosts, OpenAI among them, pass a PDF to the model
as its extracted text, which has no positions.

The controllers are commercial, but they only check the license inside a
running Vaadin application, so the run needs no Vaadin license.

## Output

Each scenario appends one line to `target/ai-benchmark.jsonl`:

```json
{"scenario":"GridAIController.findsCustomersWithoutRecentOrders","model":"gpt-4.1-mini","runs":3,"passed":3,"inputTokens":41210,"outputTokens":812,"failures":[]}
```

The token counts are summed over the scenario's attempts. Priced at the
model's list rates, they give the cost of the run. Under TeamCity the per-controller
totals are reported as `<Controller>.inputTokens.<model>` and
`<Controller>.outputTokens.<model>`, so a prompt change that makes every turn
more expensive shows up even when the pass rate does not move.

The file is cleared when a run starts, so it holds the latest run only. Keep a
copy from a run before a prompt change and compare it with the file of a run
after it to see the effect. A scenario fails the build only when its pass rate
is below
`AI_BENCHMARK_MIN_PASS_RATE`.

### TeamCity

When `TEAMCITY_VERSION` is set (TeamCity sets it in every build), the run also
prints a build statistic per scenario, per controller and for the run as a
whole, keyed by name and model, with the pass rate as the value:

```
##teamcity[buildStatisticValue key='GridAIController.findsCustomersWithoutRecentOrders.vendor_model-name' value='1.000']
##teamcity[buildStatisticValue key='GridAIController.vendor_model-name' value='0.667']
##teamcity[buildStatisticValue key='AIControllers.vendor_model-name' value='0.917']
```

Anything outside `A-Za-z0-9._-` in the model name becomes an underscore, since
TeamCity addresses a statistic by its key. `AIControllers` covers the whole
run: the series to graph and the one to hang a failure condition on.

TeamCity graphs these over builds, which is the intended way to track the
effect of prompt changes. For a scheduled build, set
`AI_BENCHMARK_MIN_PASS_RATE=0` so the build stays green and the graphs carry
the signal, and raise `AI_BENCHMARK_RUNS` to five or more so the rate moves in
smaller steps.

## Writing a scenario

A scenario is a plain `@Test` whose body is one attempt: `AIBenchmark`
invokes the method once per attempt and scores the pass rate. `@BeforeEach`
and `@AfterEach` methods run around every attempt, so each attempt starts from
fresh components, controller and conversation set up in a `@BeforeEach`
method. The attempt sends one or more user messages and asserts on the
resulting server-side state.
Scoring is deterministic: field values, the rows the produced SQL returns, the
chart type and series data. `BenchmarkDatabase` provides one small H2
database with every table for the grid and chart scenarios, so the model also
has to pick the right tables for each request.

```java
private BenchmarkDatabase db;
private GridAIController controller;
private AIBenchmark.Conversation conversation;

@BeforeEach
void startConversation() {
    db = BenchmarkDatabase.create();
    var grid = new Grid<AIDataRow>();
    controller = new GridAIController(grid, db);
    conversation = bench.conversation(grid, controller);
}

@Test
void findsCustomersWithoutRecentOrders() {
    conversation.say(
            "List the customers that have not placed any order in 2026");
    var rows = db.executeQuery(controller.getState().query());
    Assertions.assertEquals(Set.of("Iberia Textiles", "Sakura Robotics"),
            new HashSet<>(db.customerNames(rows)));
}
```

Keep one scenario per capability: every extra scenario costs tokens on every
run, so a scenario whose checks are a subset of another one should be dropped.
Keep the system prompt generic (it is fixed in `AIBenchmark`): the point is to
measure the controllers' own instructions, not to compensate for them. Only a
scenario about how a controller treats the application's system prompt adds
instructions to it, through `conversation(root, controller, instructions)`.

### Attachments

The files the form scenarios attach live in
`src/test/resources/com/vaadin/flow/component/ai/benchmark`. `receipt.png` and
`invoice.pdf` are rendered from the HTML next to them. `receipt.html` places
every line at a fixed position, so the image scenario can check where the
model says it read a value. After changing the HTML, render the files again
with headless Chromium, and after changing `receipt.html`, update the
positions the image scenario expects:

```sh
cd src/test/resources/com/vaadin/flow/component/ai/benchmark
chromium --headless --hide-scrollbars --window-size=600,900 \
  --screenshot=receipt.png "file://$PWD/receipt.html"
chromium --headless --no-pdf-header-footer \
  --print-to-pdf=invoice.pdf "file://$PWD/invoice.html"
```
