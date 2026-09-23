/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.benchmark;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.support.ReflectionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ai.AIComponentsFeatureFlagProvider;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.orchestrator.RequestListener;
import com.vaadin.flow.component.ai.orchestrator.ResponseListener;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.ResponseMetadata;
import com.vaadin.flow.component.ai.provider.SpringAILLMProvider;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

/**
 * JUnit extension that drives an {@link AIController} through a real LLM and
 * scores repeated attempts of a scenario.
 * <p>
 * Each test method is a scenario and its body is one attempt: the extension
 * invokes the method once per attempt and fails the test when the pass rate
 * falls below the configured minimum. The attempts share the test instance, so
 * keep the components and controller of an attempt in local variables.
 * <p>
 * Register it with {@code @RegisterExtension} and gate the test class with
 * {@code @EnabledIfEnvironmentVariable(named = AIBenchmark.MODEL_VARIABLE,
 * matches = ".+")} so the suite is skipped unless a model is configured.
 * Configuration is read from environment variables:
 * <ul>
 * <li>{@code AI_BENCHMARK_MODEL}: model name, e.g. {@code gpt-4.1-mini}</li>
 * <li>{@code AI_BENCHMARK_API_KEY} (or {@code OPENAI_API_KEY}): API key</li>
 * <li>{@code AI_BENCHMARK_BASE_URL}: optional OpenAI-compatible endpoint, for
 * example a local Ollama server</li>
 * <li>{@code AI_BENCHMARK_MAX_TOKENS}: optional completion cap sent as
 * {@code max_tokens}, for hosts that reject a request without one</li>
 * <li>{@code AI_BENCHMARK_RUNS}: attempts per scenario, default 3</li>
 * <li>{@code AI_BENCHMARK_MIN_PASS_RATE}: fraction of attempts that must pass
 * for the scenario to pass, default 0.6</li>
 * </ul>
 * Every scenario appends one JSON line to {@code target/ai-benchmark.jsonl} so
 * runs can be compared against a baseline. Under TeamCity (detected from
 * {@code TEAMCITY_VERSION}) each scenario, each test class and the run as a
 * whole also report their pass rate as a build statistic, keyed by name and
 * model, so the values show up as graphs over time.
 * <p>
 * Register the extension as a {@code static} field: the per-class statistic is
 * reported from the class-level callback, which JUnit only invokes for
 * statically registered extensions.
 */
public final class AIBenchmark implements BeforeAllCallback, BeforeEachCallback,
        AfterEachCallback, AfterAllCallback, InvocationInterceptor {

    /** Environment variable naming the model to benchmark. */
    public static final String MODEL_VARIABLE = "AI_BENCHMARK_MODEL";

    private static final String API_KEY_VARIABLE = "AI_BENCHMARK_API_KEY";
    private static final String BASE_URL_VARIABLE = "AI_BENCHMARK_BASE_URL";
    private static final String RUNS_VARIABLE = "AI_BENCHMARK_RUNS";
    private static final String MIN_PASS_RATE_VARIABLE = "AI_BENCHMARK_MIN_PASS_RATE";
    private static final String MAX_TOKENS_VARIABLE = "AI_BENCHMARK_MAX_TOKENS";
    private static final String TEAMCITY_VARIABLE = "TEAMCITY_VERSION";
    private static final Path REPORT = Path.of("target", "ai-benchmark.jsonl");
    private static final String TOTALS_KEY = "ai-benchmark-run-totals";
    private static final Duration TURN_TIMEOUT = Duration.ofMinutes(3);
    private static final Duration PROMPT_ACCEPT_TIMEOUT = Duration
            .ofSeconds(10);

    /**
     * Deliberately generic: the benchmark measures the built-in controller
     * instructions, so the system prompt must not restate them.
     */
    private static final String SYSTEM_PROMPT = """
            You are an assistant embedded in a business application.
            Use the available tools to carry out the user's request.
            """;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AIBenchmark.class);

    private static ChatModel chatModel;

    private static long totalRuns;
    private static long totalPassed;
    private static long totalInputTokens;
    private static long totalOutputTokens;

    private final MockUIExtension ui = new MockUIExtension();
    private final EnableFeatureFlagExtension featureFlag = new EnableFeatureFlagExtension(
            AIComponentsFeatureFlagProvider.AI_COMPONENTS);

    private String subject;
    private String scenario;
    private int classRuns;
    private int classPassed;
    private long scenarioInputTokens;
    private long scenarioOutputTokens;
    private long classInputTokens;
    private long classOutputTokens;

    @Override
    public void beforeAll(ExtensionContext context) {
        // Closed once the whole test plan is done, which is the only point at
        // which the totals over all controllers are complete
        context.getStore(ExtensionContext.StoreScope.EXECUTION_REQUEST,
                ExtensionContext.Namespace.GLOBAL)
                .getOrComputeIfAbsent(TOTALS_KEY,
                        key -> (AutoCloseable) AIBenchmark::reportRunTotals);
        // "FormAIControllerBenchmark" reports as "FormAIController"
        subject = context.getRequiredTestClass().getSimpleName()
                .replaceAll("Benchmark$", "");
        classRuns = 0;
        classPassed = 0;
        classInputTokens = 0;
        classOutputTokens = 0;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        featureFlag.beforeEach(context);
        ui.beforeEach(context);
        scenario = subject + "." + context.getRequiredTestMethod().getName();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ui.afterEach(context);
        featureFlag.afterEach(context);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (classRuns > 0) {
            reportStatistic(subject, classPassed / (double) classRuns);
            // Token totals catch a prompt change that quietly doubles the
            // cost of a run, which the pass rate alone would never show.
            reportStatistic(subject + ".inputTokens", classInputTokens);
            reportStatistic(subject + ".outputTokens", classOutputTokens);
            LOGGER.info("{}: {} input and {} output tokens over {} runs",
                    subject, classInputTokens, classOutputTokens, classRuns);
            totalRuns += classRuns;
            totalPassed += classPassed;
            totalInputTokens += classInputTokens;
            totalOutputTokens += classOutputTokens;
        }
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) {
        // Each attempt is one invocation of the method, so the extension
        // invokes it itself instead of letting JUnit invoke it once
        invocation.skip();
        score(() -> ReflectionSupport.invokeMethod(
                invocationContext.getExecutable(),
                invocationContext.getTarget().orElse(null),
                invocationContext.getArguments().toArray()));
    }

    /**
     * Runs the attempt the configured number of times and fails the test when
     * the pass rate falls below the configured minimum.
     */
    private void score(Runnable attempt) {
        var runs = intVariable(RUNS_VARIABLE, 3);
        var minPassRate = doubleVariable(MIN_PASS_RATE_VARIABLE, 0.6);
        var failures = new ArrayList<String>();
        scenarioInputTokens = 0;
        scenarioOutputTokens = 0;
        for (var run = 1; run <= runs; run++) {
            try {
                attempt.run();
            } catch (AssertionError | Exception e) {
                failures.add("run " + run + ": " + e.getMessage());
                LOGGER.info("{} run {} failed: {}", scenario, run,
                        e.getMessage());
            } finally {
                // Detaches what the attempt's conversations attached, so the
                // next attempt starts from an empty UI
                ui.removeAll();
            }
        }
        var passed = runs - failures.size();
        var passRate = passed / (double) runs;
        classRuns += runs;
        classPassed += passed;
        classInputTokens += scenarioInputTokens;
        classOutputTokens += scenarioOutputTokens;
        LOGGER.info("{}: {}/{} runs passed, {} input and {} output tokens",
                scenario, passed, runs, scenarioInputTokens,
                scenarioOutputTokens);
        appendReport(runs, passed, failures);
        reportStatistic(scenario, passRate);
        if (passRate < minPassRate) {
            Assertions.fail(String.format(
                    "%s passed %d/%d runs, below the minimum pass rate %.2f%n%s",
                    scenario, passed, runs, minPassRate,
                    String.join("\n", failures)));
        }
    }

    /**
     * Starts a conversation with an orchestrator that has the given controller
     * registered. The root component is attached to the mock UI until the
     * attempt ends.
     *
     * @param root
     *            the component tree the controller works on
     * @param controller
     *            the controller under test
     * @return the conversation
     */
    public Conversation conversation(Component root, AIController controller) {
        ui.add(root);
        return new Conversation(controller);
    }

    /**
     * A multi-turn conversation with one orchestrator. Each {@link #say} blocks
     * until the turn has ended and fails if the turn ended with an error.
     */
    public final class Conversation {
        private final AIOrchestrator orchestrator;
        private final AtomicReference<CountDownLatch> turnEnded = new AtomicReference<>();
        private final AtomicReference<CountDownLatch> requestSent = new AtomicReference<>(
                new CountDownLatch(0));
        private final AtomicReference<ResponseListener.ResponseEvent> lastEvent = new AtomicReference<>();

        private Conversation(AIController controller) {
            // The orchestrator notifies the response listener before it
            // lets the controller apply its staged state, so the turn is
            // only over once the controller's onResponse has returned.
            orchestrator = AIOrchestrator
                    .builder(new SpringAILLMProvider(chatModel()),
                            SYSTEM_PROMPT)
                    .withController(new TurnTracker(controller,
                            () -> turnEnded.get().countDown()))
                    .withRequestListener(event -> requestSent.get().countDown())
                    .withResponseListener(lastEvent::set).build();
        }

        /**
         * Sends one user message and waits for the turn to end.
         *
         * @param message
         *            the user message
         * @return the assistant's response text
         */
        public String say(String message) {
            try {
                return takeTurn(message);
            } catch (InterruptedException e) {
                // Failed like a turn that timed out, so the scenarios need
                // not declare the exception. Restoring the flag keeps the
                // interrupt visible to whoever sent it.
                Thread.currentThread().interrupt();
                throw new AssertionError(
                        "Interrupted while waiting for the turn", e);
            }
        }

        private String takeTurn(String message) throws InterruptedException {
            var latch = new CountDownLatch(1);
            turnEnded.set(latch);
            promptUntilAccepted(message);
            // MockUIExtension keeps the session locked on the test thread.
            // A provider that finishes the turn on a worker thread has to
            // take that lock inside ui.access(), so release it for as long
            // as this thread only waits, the way a real session is free
            // between requests, and take it back before the assertions.
            var sessionLock = ui.getSession().getLockInstance();
            sessionLock.unlock();
            boolean ended;
            try {
                ended = latch.await(TURN_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            } finally {
                sessionLock.lock();
            }
            if (!ended) {
                throw new AssertionError(
                        "Turn did not end within " + TURN_TIMEOUT);
            }
            var event = lastEvent.get();
            // The provider accumulates usage across the tool rounds of a
            // turn, so the final metadata is the whole turn's cost.
            event.getMetadata().map(ResponseMetadata::tokenUsage)
                    .ifPresent(usage -> {
                        scenarioInputTokens += zeroIfNull(usage.inputTokens());
                        scenarioOutputTokens += zeroIfNull(
                                usage.outputTokens());
                    });
            event.getError().ifPresent(error -> {
                throw new AssertionError("Turn failed: " + error, error);
            });
            return event.getResponse();
        }

        /**
         * Sends the prompt, retrying briefly while the orchestrator still
         * reports the previous turn as in progress. The orchestrator notifies
         * listeners that a turn has ended before it clears its own busy flag,
         * so a prompt sent right after the previous turn can be dropped as
         * overlapping. An accepted prompt fires the request listener
         * synchronously inside {@code prompt()}, which is how acceptance is
         * detected.
         */
        private void promptUntilAccepted(String message)
                throws InterruptedException {
            var deadline = System.nanoTime() + PROMPT_ACCEPT_TIMEOUT.toNanos();
            while (true) {
                var accepted = new CountDownLatch(1);
                requestSent.set(accepted);
                orchestrator.prompt(message);
                if (accepted.getCount() == 0) {
                    return;
                }
                if (System.nanoTime() > deadline) {
                    throw new AssertionError(
                            "Orchestrator still reported the previous turn as in progress after "
                                    + PROMPT_ACCEPT_TIMEOUT);
                }
                Thread.sleep(25);
            }
        }
    }

    /**
     * Delegates to the real controller and signals the end of the turn after
     * the controller has applied its state.
     */
    private static final class TurnTracker implements AIController {
        private final AIController delegate;
        private final Runnable turnEnded;

        private TurnTracker(AIController delegate, Runnable turnEnded) {
            this.delegate = delegate;
            this.turnEnded = turnEnded;
        }

        @Override
        public List<LLMProvider.ToolSpec> getTools() {
            return delegate.getTools();
        }

        @Override
        public void onRequest(RequestListener.RequestEvent event) {
            delegate.onRequest(event);
        }

        @Override
        public void onResponse(ResponseListener.ResponseEvent event) {
            try {
                delegate.onResponse(event);
            } finally {
                turnEnded.run();
            }
        }
    }

    private static synchronized ChatModel chatModel() {
        if (chatModel == null) {
            var model = variable(MODEL_VARIABLE)
                    .orElseThrow(() -> new IllegalStateException(
                            MODEL_VARIABLE + " not set"));
            var baseUrl = variable(BASE_URL_VARIABLE);
            var apiKey = variable(API_KEY_VARIABLE)
                    .or(() -> variable("OPENAI_API_KEY"))
                    .orElseGet(() -> baseUrl.map(url -> "unused")
                            .orElseThrow(() -> new IllegalStateException(
                                    API_KEY_VARIABLE + " not set and no "
                                            + BASE_URL_VARIABLE + " given")));
            // Spring AI, the framework the documentation teaches, driven the
            // way a non-Boot application would: connection settings on the
            // options, the HTTP timeout through Spring AI's own client.
            // streamUsage asks OpenAI-compatible endpoints to report token
            // usage on the stream; without it a streamed turn costs nothing
            // on paper.
            var options = OpenAiChatOptions.builder().apiKey(apiKey)
                    .model(model).streamUsage(true);
            baseUrl.ifPresent(options::baseUrl);
            // Some hosts behind OpenAI-compatible endpoints treat a missing
            // completion cap as the whole context window and reject the
            // request. Reasoning models at OpenAI reject max_tokens, so the
            // cap is opt-in per model.
            variable(MAX_TOKENS_VARIABLE).map(Integer::parseInt)
                    .ifPresent(options::maxTokens);
            chatModel = OpenAiChatModel.builder().options(options.build())
                    .httpClientBuilderCustomizer(
                            client -> client.timeout(TURN_TIMEOUT))
                    .build();
            LOGGER.info("Benchmarking model {} at {}", model,
                    baseUrl.orElse("api.openai.com"));
        }
        return chatModel;
    }

    /**
     * Reports the totals over every controller once the test plan is done. Each
     * controller is benchmarked in its own class, so these are the only values
     * that describe the run as a whole: one pass rate to graph and to hang a
     * build failure condition on, plus the token counts that price the run.
     */
    private static void reportRunTotals() {
        if (totalRuns == 0) {
            return;
        }
        reportStatistic("AIControllers", totalPassed / (double) totalRuns);
        reportStatistic("AIControllers.inputTokens", totalInputTokens);
        reportStatistic("AIControllers.outputTokens", totalOutputTokens);
    }

    /**
     * Prints a TeamCity build statistic so the value is graphed over builds.
     * The key carries the model so each benchmarked model gets its own series.
     * Silent outside TeamCity.
     */
    private static void reportStatistic(String key, double value) {
        if (variable(TEAMCITY_VARIABLE).isEmpty()) {
            return;
        }
        // A model name can carry a provider prefix, as in
        // "qwen/qwen3-235b-a22b-2507", and TeamCity addresses a statistic by
        // its key in REST paths, so anything outside the key charset goes.
        var model = variable(MODEL_VARIABLE).orElse("unknown")
                .replaceAll("[^A-Za-z0-9._-]", "_");
        var fullKey = key + "." + model;
        // Service messages are read from the build log by TeamCity, so they
        // must go to stdout as-is rather than through the logger.
        System.out.printf(
                "##teamcity[buildStatisticValue key='%s' value='%.3f']%n",
                escapeServiceMessage(fullKey), value);
    }

    private static String escapeServiceMessage(String value) {
        return value.replace("|", "||").replace("'", "|'").replace("[", "|[")
                .replace("]", "|]").replace("\n", "|n").replace("\r", "|r");
    }

    private void appendReport(int runs, int passed, List<String> failures) {
        var line = JacksonUtils.createObjectNode();
        line.put("scenario", scenario);
        line.put("model", variable(MODEL_VARIABLE).orElse(null));
        line.put("runs", runs);
        line.put("passed", passed);
        line.put("inputTokens", scenarioInputTokens);
        line.put("outputTokens", scenarioOutputTokens);
        var failureNode = line.putArray("failures");
        failures.forEach(failureNode::add);
        try {
            Files.createDirectories(REPORT.getParent());
            Files.writeString(REPORT, line + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.warn("Could not write benchmark report to {}", REPORT, e);
        }
    }

    private static long zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    private static Optional<String> variable(String name) {
        return Optional.ofNullable(System.getenv(name)).map(String::trim)
                .filter(value -> !value.isEmpty());
    }

    private static int intVariable(String name, int defaultValue) {
        return variable(name).map(Integer::parseInt).orElse(defaultValue);
    }

    private static double doubleVariable(String name, double defaultValue) {
        return variable(name).map(Double::parseDouble).orElse(defaultValue);
    }
}
