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
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ai.AIComponentsFeatureFlagProvider;
import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.orchestrator.ResponseListener;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.LangChain4JLLMProvider;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.tests.EnableFeatureFlagExtension;
import com.vaadin.tests.MockUIExtension;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * JUnit extension that drives an {@link AIController} through a real LLM and
 * scores repeated attempts of a scenario.
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
 * <li>{@code AI_BENCHMARK_RUNS}: attempts per scenario, default 3</li>
 * <li>{@code AI_BENCHMARK_MIN_PASS_RATE}: fraction of attempts that must pass
 * for the scenario to pass, default 0.67</li>
 * </ul>
 * Every scenario appends one JSON line to {@code target/ai-benchmark.jsonl} so
 * runs can be compared against a baseline.
 */
public final class AIBenchmark
        implements BeforeEachCallback, AfterEachCallback {

    /** Environment variable naming the model to benchmark. */
    public static final String MODEL_VARIABLE = "AI_BENCHMARK_MODEL";

    private static final String API_KEY_VARIABLE = "AI_BENCHMARK_API_KEY";
    private static final String BASE_URL_VARIABLE = "AI_BENCHMARK_BASE_URL";
    private static final String RUNS_VARIABLE = "AI_BENCHMARK_RUNS";
    private static final String MIN_PASS_RATE_VARIABLE = "AI_BENCHMARK_MIN_PASS_RATE";
    private static final Path REPORT = Path.of("target", "ai-benchmark.jsonl");
    private static final Duration TURN_TIMEOUT = Duration.ofMinutes(3);

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

    private final MockUIExtension ui = new MockUIExtension();
    private final EnableFeatureFlagExtension featureFlag = new EnableFeatureFlagExtension(
            AIComponentsFeatureFlagProvider.AI_COMPONENTS);

    private String scenario;

    /**
     * One attempt of a scenario: builds fresh components, runs the
     * conversation, and asserts on the resulting state.
     */
    @FunctionalInterface
    public interface Attempt {
        void run() throws Exception;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        featureFlag.beforeEach(context);
        ui.beforeEach(context);
        scenario = context.getRequiredTestClass().getSimpleName() + "."
                + context.getRequiredTestMethod().getName();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ui.afterEach(context);
        featureFlag.afterEach(context);
    }

    /**
     * Runs the attempt the configured number of times and fails the test when
     * the pass rate falls below the configured minimum. Each attempt must build
     * its own components and controller so attempts do not share state.
     *
     * @param attempt
     *            the scenario to score
     */
    public void score(Attempt attempt) {
        var runs = intVariable(RUNS_VARIABLE, 3);
        var minPassRate = doubleVariable(MIN_PASS_RATE_VARIABLE, 0.67);
        var failures = new ArrayList<String>();
        for (var run = 1; run <= runs; run++) {
            try {
                attempt.run();
            } catch (AssertionError | Exception e) {
                failures.add("run " + run + ": " + e.getMessage());
                LOGGER.info("{} run {} failed: {}", scenario, run,
                        e.getMessage());
            }
        }
        var passed = runs - failures.size();
        var passRate = passed / (double) runs;
        LOGGER.info("{}: {}/{} runs passed", scenario, passed, runs);
        appendReport(runs, passed, failures);
        if (passRate < minPassRate) {
            Assertions.fail(String.format(
                    "%s passed %d/%d runs, below the minimum pass rate %.2f%n%s",
                    scenario, passed, runs, minPassRate,
                    String.join("\n", failures)));
        }
    }

    /**
     * Starts a conversation with an orchestrator that has the given controller
     * registered. The root component is attached to the mock UI for the
     * duration of the conversation and detached on {@link Conversation#close}.
     *
     * @param root
     *            the component tree the controller works on
     * @param controller
     *            the controller under test
     * @return the conversation, to be closed after the last turn
     */
    public Conversation conversation(Component root, AIController controller) {
        ui.add(root);
        return new Conversation(root, controller);
    }

    /**
     * A multi-turn conversation with one orchestrator. Each {@link #say} blocks
     * until the turn has ended and fails if the turn ended with an error.
     */
    public final class Conversation implements AutoCloseable {
        private final Component root;
        private final AIOrchestrator orchestrator;
        private final AtomicReference<CountDownLatch> turnEnded = new AtomicReference<>();
        private final AtomicReference<ResponseListener.ResponseEvent> lastEvent = new AtomicReference<>();

        private Conversation(Component root, AIController controller) {
            this.root = root;
            // The orchestrator notifies the response listener before it
            // lets the controller apply its staged state, so the turn is
            // only over once the controller's onResponse has returned.
            orchestrator = AIOrchestrator
                    .builder(new LangChain4JLLMProvider(chatModel()),
                            SYSTEM_PROMPT)
                    .withController(new TurnTracker(controller,
                            () -> turnEnded.get().countDown()))
                    .withResponseListener(lastEvent::set).build();
        }

        /**
         * Sends one user message and waits for the turn to end.
         *
         * @param message
         *            the user message
         * @return the assistant's response text
         * @throws InterruptedException
         *             if interrupted while waiting for the turn
         */
        public String say(String message) throws InterruptedException {
            var latch = new CountDownLatch(1);
            turnEnded.set(latch);
            orchestrator.prompt(message);
            if (!latch.await(TURN_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
                throw new AssertionError(
                        "Turn did not end within " + TURN_TIMEOUT);
            }
            var event = lastEvent.get();
            event.getError().ifPresent(error -> {
                throw new AssertionError("Turn failed: " + error, error);
            });
            return event.getResponse();
        }

        @Override
        public void close() {
            root.removeFromParent();
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
        public void onRequest() {
            delegate.onRequest();
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
            var builder = OpenAiChatModel.builder().modelName(model)
                    .apiKey(apiKey).timeout(TURN_TIMEOUT);
            baseUrl.ifPresent(builder::baseUrl);
            chatModel = builder.build();
            LOGGER.info("Benchmarking model {} at {}", model,
                    baseUrl.orElse("api.openai.com"));
        }
        return chatModel;
    }

    private void appendReport(int runs, int passed, List<String> failures) {
        var line = JacksonUtils.createObjectNode();
        line.put("scenario", scenario);
        line.put("model", variable(MODEL_VARIABLE).orElse(null));
        line.put("runs", runs);
        line.put("passed", passed);
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
