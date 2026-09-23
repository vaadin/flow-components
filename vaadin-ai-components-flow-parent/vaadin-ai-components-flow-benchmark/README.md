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
| `AI_BENCHMARK_BASE_URL`     | OpenAI-compatible endpoint including `/v1`. Makes the API key optional. |         |
| `AI_BENCHMARK_MAX_TOKENS`   | Optional completion cap sent as `max_tokens`. Some hosts reject a request without one; OpenAI reasoning models reject the field. | unset |
| `AI_BENCHMARK_RUNS`         | Attempts per scenario.                                         | `3`     |
| `AI_BENCHMARK_MIN_PASS_RATE`| Fraction of attempts that must pass for a scenario to pass.    | `0.6`   |

The model is driven through `SpringAILLMProvider` and Spring AI's OpenAI
module, the same path the documentation teaches, so the numbers reflect what
a Spring application sees. Any OpenAI-compatible endpoint works through
`AI_BENCHMARK_BASE_URL`.

The controllers are commercial, so the run also needs a valid Vaadin license
in dev mode, the same as running the extension module's unit tests.

## Output

Each scenario appends one line to `target/ai-benchmark.jsonl`:

```json
{"scenario":"GridAIController.aggregatesRegionsAboveThreshold","model":"gpt-4.1-mini","runs":3,"passed":3,"inputTokens":41210,"outputTokens":812,"failures":[]}
```

The token counts are summed over the scenario's attempts and priced at the
model's list rates give the cost of the run. Under TeamCity the per-controller
totals are reported as `<Controller>.inputTokens.<model>` and
`<Controller>.outputTokens.<model>`, so a prompt change that makes every turn
more expensive shows up even when the pass rate does not move.

Compare the file from a run before and after a prompt change to see the effect.
A scenario fails the build only when its pass rate is below
`AI_BENCHMARK_MIN_PASS_RATE`.

### TeamCity

When `TEAMCITY_VERSION` is set (TeamCity sets it in every build), the run also
prints a build statistic per scenario, per controller and for the run as a
whole, keyed by name and model, with the pass rate as the value:

```
##teamcity[buildStatisticValue key='GridAIController.aggregatesRegionsAboveThreshold.qwen_qwen3-235b-a22b-2507' value='1.000']
##teamcity[buildStatisticValue key='GridAIController.qwen_qwen3-235b-a22b-2507' value='0.667']
##teamcity[buildStatisticValue key='AIControllers.qwen_qwen3-235b-a22b-2507' value='0.917']
```

Anything outside `A-Za-z0-9._-` in the model name becomes an underscore, since
TeamCity addresses a statistic by its key. `AIControllers` covers the whole
run: the series to graph and the one to hang a failure condition on.

TeamCity graphs these over builds, which is the intended way to track the
effect of prompt changes. For a scheduled build, set
`AI_BENCHMARK_MIN_PASS_RATE=0` so the build stays green and the graphs carry
the signal, and raise `AI_BENCHMARK_RUNS` to five or more so the rate moves in
smaller steps.

#### Weekly build

The build lives in the Benchmark tests project and runs one model, so adding a
model means another build configuration with the model parameters changed. Make
the first one a template if several models are coming.

| Parameter                        | Value                          |
|----------------------------------|--------------------------------|
| `env.AI_BENCHMARK_MODEL`         | `qwen/qwen3-235b-a22b-2507`    |
| `env.AI_BENCHMARK_BASE_URL`      | `https://openrouter.ai/api/v1` |
| `env.AI_BENCHMARK_API_KEY`       | OpenRouter key, password type  |
| `env.AI_BENCHMARK_MAX_TOKENS`    | `4096`                         |
| `env.AI_BENCHMARK_RUNS`          | `5`                            |
| `env.AI_BENCHMARK_MIN_PASS_RATE` | `0`                            |
| `env.VAADIN_PRO_KEY`             | Vaadin pro key, password type  |

The completion cap is there because OpenRouter routes Qwen to hosts that reject
a request without one. A model that has no such host does not need it, and
OpenAI reasoning models reject the parameter outright.

Two Maven steps on JDK 21:

```sh
install -DskipTests -pl vaadin-ai-components-flow-parent/vaadin-ai-components-flow-benchmark -am
test -pl vaadin-ai-components-flow-parent/vaadin-ai-components-flow-benchmark
```

The first step builds the controllers from the checkout, so the run measures
the branch rather than the published snapshot. Expect about 25 minutes for the
two steps with five runs per scenario.

The schedule trigger must build even when there are no pending changes: the
point is to track the model and the provider, which change without the
repository changing. Publish
`vaadin-ai-components-flow-benchmark/target/ai-benchmark.jsonl` as an artifact
so a build can be compared against an older one scenario by scenario.

With the minimum pass rate at zero the suite never fails the build, so the
regression signal is a failure condition on the `AIControllers.<model>` metric,
which trips when the value falls far enough below the last successful build.
Set it once the first builds have established a baseline.

The controllers check the Vaadin license in dev mode, so the agent needs
`VAADIN_PRO_KEY` and access to `tools.vaadin.com` as well as to the model
endpoint.

## Writing a scenario

A scenario is a plain `@Test` whose body is one attempt: `AIBenchmark`
invokes the method once per attempt and scores the pass rate. The attempt
builds fresh components and a controller, opens a conversation, sends one or
more user messages, and asserts on the resulting server-side state. The
attempts share the test instance, so keep the components and the controller
in local variables.
Scoring is deterministic: field values, the rows the produced SQL returns, the
chart type and series data. `BenchmarkDatabase` provides small H2 data sets
for grid and chart scenarios.

```java
@Test
void aggregatesRegionsAboveThreshold() {
    var db = BenchmarkDatabase.customers();
    var grid = new Grid<AIDataRow>();
    var controller = new GridAIController(grid, db);
    bench.conversation(grid, controller).say("""
            Which regions have a combined customer revenue \
            above one million? Show the region and its total, \
            largest total first.""");
    var rows = db.executeQuery(controller.getState().query());
    Assertions.assertEquals(List.of("North America", "Europe"),
            BenchmarkDatabase.column(rows, "region"));
}
```

Keep one scenario per capability: every extra scenario costs tokens on every
run, so a scenario whose checks are a subset of another one should be dropped.
Keep the system prompt generic (it is fixed in `AIBenchmark`): the point is to
measure the controllers' own instructions, not to compensate for them.
