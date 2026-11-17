/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.ai.provider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.AttachmentContentType;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.internal.JacksonUtils;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.VideoContent;
import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonRawSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.observability.api.event.AiServiceResponseReceivedEvent;
import dev.langchain4j.observability.api.listener.AiServiceListener;
import dev.langchain4j.observability.api.listener.OutputGuardrailExecutedListener;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.V;
import dev.langchain4j.service.memory.ChatMemoryAccess;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import tools.jackson.databind.JsonNode;

/**
 * LangChain4j implementation of {@link LLMProvider}, built on a LangChain4j AI
 * service.
 * <p>
 * Supports both streaming and non-streaming LangChain4j models. Tool calling is
 * supported through LangChain4j's {@link Tool} annotation.
 * </p>
 * <p>
 * <b>Customizing the AI service:</b> the provider runs each turn through an AI
 * service that LangChain4j's {@link AiServices} builds. The constructors that
 * take a customizer hand you that builder before the service is built, so you
 * can add what LangChain4j offers there with its own API: retrieval
 * augmentation (RAG), tool providers such as MCP, tool objects, input and
 * output guardrails, listeners, and so on. A moderation model set in the
 * customizer is not used, since LangChain4j applies it only to service methods
 * annotated with {@code @Moderate}; use an input guardrail to check the user
 * message instead.
 * </p>
 *
 * <pre>
 * var provider = new LangChain4JLLMProvider(streamingChatModel,
 *         service -&gt; service.contentRetriever(retriever)
 *                 .toolProvider(mcpToolProvider)
 *                 .inputGuardrails(new PromptInjectionGuardrail()));
 * </pre>
 * <p>
 * The provider keeps doing what the orchestrator needs from it, whatever the
 * customizer configured: it supplies the tools of each turn next to the tools
 * of the customizer, adds the session context and the attachments to the user
 * message, reports the response metadata and enforces the tool call limits.
 * Once a turn is cancelled, the provider calls the model no more and does not
 * pass a model response that arrives later on to the service, so no tool runs
 * for it, whether the provider or the customizer supplied it. Before the
 * customizer runs, the provider sets defaults that the customizer may replace:
 * its own chat memory, no limit on LangChain4j's tool calling round trips (the
 * provider's own tool call limits bound the loop), and a result that tells the
 * model when it called a tool that does not exist. The model is the one given
 * to the constructor, so a model set in the customizer is not used. Errors of
 * the tools added in the customizer are handled by LangChain4j, with the tool
 * error handlers set in the customizer or with LangChain4j's defaults; see the
 * LangChain4j documentation on how to handle them safely.
 * </p>
 * <p>
 * <b>System prompt:</b> you can set a system prompt in the customizer with
 * {@code systemMessage(...)}. LangChain4j renders it as a prompt template: a
 * {@code {{name}}} placeholder needs a value, and {@code {{current_date}}},
 * {@code {{current_time}}} and {@code {{current_date_time}}} are replaced, so
 * the prompt must not use double braces for any other purpose. When the
 * orchestrator also sends a system prompt, the orchestrator's prompt is used
 * instead of the one set in the customizer, and it is sent as it is, without
 * template rendering. The text of a user message is never rendered as a
 * template.
 * </p>
 * <p>
 * <b>Retrieval augmentation:</b> a content retriever or retrieval augmentor set
 * in the customizer works on the text of the user message, which ends with the
 * session context of the turn, as do input guardrails. The attachments of the
 * turn are added to the augmented message, and the provider's chat memory keeps
 * them with the user message also when
 * {@code storeRetrievedContentInChatMemory(false)} leaves the retrieved content
 * out of it. Retrieval and input guardrails run on the thread that asks for the
 * response, before the model is called, also with a {@link StreamingChatModel}.
 * When you use retrieval or input guardrails with a streaming model, call
 * {@link #setBackgroundExecution(boolean) setBackgroundExecution(true)} so that
 * they do not block the UI thread.
 * </p>
 * <p>
 * <b>Output guardrails:</b> the text of the response is the text the output
 * guardrails set in the customizer returned. LangChain4j runs them on the final
 * answer only, so with output guardrails the text a model sends next to its
 * tool calls is not passed on.
 * </p>
 * <p>
 * <b>Streaming vs. non-streaming:</b> The mode is determined by the constructor
 * used. Pass a {@link StreamingChatModel} to
 * {@link #LangChain4JLLMProvider(StreamingChatModel)} for streaming, or a
 * {@link ChatModel} to {@link #LangChain4JLLMProvider(ChatModel)} for
 * non-streaming. Streaming mode pushes partial responses to the UI as they
 * arrive, which requires automatic server push or polling to deliver them.
 * Annotate your UI class or application shell with {@code @Push}, or enable
 * polling with {@code UI.setPollInterval()}, before using a streaming model. A
 * warning is logged at runtime when neither is active. With output guardrails
 * configured in the customizer, the answer is not streamed: it arrives in one
 * piece once the guardrails have checked it. Without streaming, text a model
 * sends next to its tool calls arrives with the answer at the end of the turn.
 * </p>
 * <p>
 * <b>Blocking the request thread:</b> a {@link ChatModel} call blocks the
 * thread that subscribes to the response, which is the UI thread for a prompt
 * triggered from the browser. A turn with a {@link StreamingChatModel} blocks
 * that thread until the model call starts, which includes the retrieval and the
 * input guardrails configured in the customizer. Call
 * {@link #setBackgroundExecution(boolean) setBackgroundExecution(true)} to run
 * the turn on a background thread instead, so the request completes and the
 * user's message renders while the LLM works.
 * </p>
 * <p>
 * <b>Tool call limits:</b> a model that keeps requesting tool calls instead of
 * answering would never end its turn, and each round costs another model call.
 * The provider ends such a turn with a {@link ToolCallLimitExceededException}
 * once the model has requested more than {@code 40} calls to any one tool, or
 * more than {@code 150} tool calls in total, within the turn. The limits count
 * every tool call the model requests, including calls to the tools configured
 * in the customizer. Adjust or remove them with
 * {@link #setMaxCallsPerTool(int)} and {@link #setMaxTotalToolCalls(int)}.
 * </p>
 * <p>
 * Each provider instance maintains its own chat memory. To share conversation
 * history across components, reuse the same provider instance. The memory holds
 * the user messages and the assistant's final answer of each turn; tool calls
 * and their results are sent to the model only within the turn they belong to
 * and are not replayed on later turns. A final answer without text, which a
 * model may give once a tool call has done what was asked, is not stored
 * either, so that the turn leaves only its user message in the memory. A chat
 * memory you configure in the customizer replaces the provider's own, and
 * LangChain4j fills it as it does for any AI service: it also receives the tool
 * calls and their results, including those of a turn that failed or was
 * cancelled, and with {@code storeRetrievedContentInChatMemory(false)} a user
 * message without its attachments. History restore with
 * {@link #setHistory(List, Map)} writes to the chat memory the service uses,
 * whichever it is. A {@code chatMemoryProvider} set in the customizer creates
 * that memory on the first turn, so a history restored before it is not written
 * and a warning is logged.
 * </p>
 * <p>
 * <b>Note:</b> LangChain4JLLMProvider is not serializable. If your application
 * uses session persistence, you will need to create a new provider instance
 * after session restore.
 * </p>
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public class LangChain4JLLMProvider implements LLMProvider {

    /**
     * The AI service the provider drives. LangChain4j implements it through
     * {@link AiServices}; the provider calls {@link #stream} with a
     * {@link StreamingChatModel} and {@link #call} with a {@link ChatModel}.
     * <p>
     * The user message of a turn is passed in two parts, its text and its
     * attachments. LangChain4j applies retrieval augmentation to the text and
     * adds the attachments to the result, so retrieval also works on a turn
     * that carries an image or a file. The text is the value of the one
     * variable of a template that the provider passes with each call:
     * LangChain4j renders the text part of a user message as a prompt template,
     * which the text a user typed must not go through. The state of the turn
     * travels in the invocation parameters.
     */
    private interface ChatService extends ChatMemoryAccess {

        TokenStream stream(@dev.langchain4j.service.UserMessage String template,
                @V(USER_TEXT_VARIABLE) String text,
                @dev.langchain4j.service.UserMessage List<Content> attachments,
                InvocationParameters parameters);

        Result<String> call(
                @dev.langchain4j.service.UserMessage String template,
                @V(USER_TEXT_VARIABLE) String text,
                @dev.langchain4j.service.UserMessage List<Content> attachments,
                InvocationParameters parameters);
    }

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LangChain4JLLMProvider.class);

    private static final int MAX_MESSAGES = 30;
    private static final int DEFAULT_MAX_CALLS_PER_TOOL = 40;
    private static final int DEFAULT_MAX_TOTAL_TOOL_CALLS = 150;

    /**
     * The invocation parameter under which the turn travels to the hooks the
     * provider registers on the service.
     */
    private static final String TURN_PARAMETER = LangChain4JLLMProvider.class
            .getName() + ".turn";

    /**
     * The variable through which the text of a user message reaches the
     * service, and the template that consists of that variable alone. The value
     * of a variable is inserted as it is, so braces in the text stay braces.
     */
    private static final String USER_TEXT_VARIABLE = "vaadinUserText";
    private static final String USER_TEXT_TEMPLATE = "{{" + USER_TEXT_VARIABLE
            + "}}";

    /**
     * The result of a tool the provider did not run because the turn was over
     * when the model's request for it arrived. It is not sent to the model
     * within the turn, which ends before the next model call.
     */
    private static final String TOOL_NOT_EXECUTED = "Not executed: the turn has ended.";

    /**
     * The id of the conversation of a service whose methods take no memory id,
     * as the {@code AiServices} documentation gives it.
     */
    private static final String DEFAULT_MEMORY_ID = "default";

    private final transient ChatService service;
    private final transient boolean streaming;
    /**
     * The turn whose service calls run on the current thread. The model
     * decorator and the chat memory read it, since neither a model call nor a
     * memory access carries the invocation it belongs to. It is bound for the
     * service call that starts a turn, and for each callback of a streaming
     * model, which is where the follow-up model calls of a turn are made.
     */
    private final transient ThreadLocal<Turn> turnOnThread = new ThreadLocal<>();
    private final BackgroundExecution backgroundExecution = new BackgroundExecution(
            LangChain4JLLMProvider.class);
    private int maxCallsPerTool = DEFAULT_MAX_CALLS_PER_TOOL;
    private int maxTotalToolCalls = DEFAULT_MAX_TOTAL_TOOL_CALLS;

    /**
     * Constructor with a streaming chat model.
     *
     * @param chatModel
     *            the streaming chat model, not {@code null}
     * @throws NullPointerException
     *             if chatModel is {@code null}
     */
    public LangChain4JLLMProvider(StreamingChatModel chatModel) {
        this(null, Objects.requireNonNull(chatModel,
                "StreamingChatModel must not be null"), null);
    }

    /**
     * Constructor with a non-streaming chat model.
     *
     * @param chatModel
     *            the non-streaming chat model, not {@code null}
     * @throws NullPointerException
     *             if chatModel is {@code null}
     */
    public LangChain4JLLMProvider(ChatModel chatModel) {
        this(Objects.requireNonNull(chatModel, "ChatModel must not be null"),
                null, null);
    }

    /**
     * Constructor with a streaming chat model and a customizer of the AI
     * service the provider is built on. The customizer receives LangChain4j's
     * {@link AiServices} builder before the service is built and can configure
     * retrieval augmentation, tool providers such as MCP, guardrails and the
     * other features of the builder. See the class documentation for what the
     * provider adds to the service and which of its defaults the customizer can
     * replace.
     *
     * <pre>
     * new LangChain4JLLMProvider(streamingChatModel, service -&gt; service
     *         .contentRetriever(retriever).toolProvider(mcpToolProvider));
     * </pre>
     *
     * @param chatModel
     *            the streaming chat model, not {@code null}
     * @param customizer
     *            the customizer of the AI service builder, not {@code null}
     * @throws NullPointerException
     *             if chatModel or customizer is {@code null}
     * @since 25.4
     */
    public LangChain4JLLMProvider(StreamingChatModel chatModel,
            Consumer<AiServices<?>> customizer) {
        this(null,
                Objects.requireNonNull(chatModel,
                        "StreamingChatModel must not be null"),
                Objects.requireNonNull(customizer,
                        "Customizer must not be null"));
    }

    /**
     * Constructor with a non-streaming chat model and a customizer of the AI
     * service the provider is built on. The customizer receives LangChain4j's
     * {@link AiServices} builder before the service is built and can configure
     * retrieval augmentation, tool providers such as MCP, guardrails and the
     * other features of the builder. See the class documentation for what the
     * provider adds to the service and which of its defaults the customizer can
     * replace.
     *
     * <pre>
     * new LangChain4JLLMProvider(chatModel, service -&gt; service
     *         .contentRetriever(retriever).toolProvider(mcpToolProvider));
     * </pre>
     *
     * @param chatModel
     *            the non-streaming chat model, not {@code null}
     * @param customizer
     *            the customizer of the AI service builder, not {@code null}
     * @throws NullPointerException
     *             if chatModel or customizer is {@code null}
     * @since 25.4
     */
    public LangChain4JLLMProvider(ChatModel chatModel,
            Consumer<AiServices<?>> customizer) {
        this(Objects.requireNonNull(chatModel, "ChatModel must not be null"),
                null, Objects.requireNonNull(customizer,
                        "Customizer must not be null"));
    }

    private LangChain4JLLMProvider(ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            Consumer<AiServices<?>> customizer) {
        this.streaming = streamingChatModel != null;
        var builder = AiServices.builder(ChatService.class);
        prepare(builder);
        if (customizer != null) {
            customizer.accept(builder);
        } else {
            // The service has only the provider's own tools, whose failures
            // never leave their executors, so no tool reaches these handlers.
            // Set explicitly, LangChain4j does not log its notice that asks
            // for them. With a customizer, the tools it adds are handled as
            // LangChain4j handles them.
            builder.toolArgumentsErrorHandler(
                    ToolArgumentsErrorHandler.failInvocation());
            builder.toolExecutionErrorHandler(
                    ToolExecutionErrorHandler.failInvocation());
        }
        complete(builder, chatModel, streamingChatModel);
        this.service = builder.build();
    }

    /**
     * Applies the defaults the provider wants on the service before the
     * application gets to customize it, so that each can be replaced: the
     * provider's own chat memory, a tool-calling loop bounded by the provider's
     * tool call limits rather than by LangChain4j's round trip count, and a
     * tool the model made up being reported back to it instead of failing the
     * turn.
     */
    private void prepare(AiServices<ChatService> builder) {
        // The service reads the memory before it adds the user message of
        // the turn, so a request carries the window plus that message. The
        // window is sized one short to keep a request within MAX_MESSAGES.
        // A system message set in the customizer is stored in the memory;
        // kept first, it stays ahead of a conversation that setHistory
        // restored.
        var durable = MessageWindowChatMemory.builder()
                .maxMessages(MAX_MESSAGES - 1)
                .alwaysKeepSystemMessageFirst(true).build();
        builder.chatMemory(new TurnScopedChatMemory(durable,
                this::getTurnMessages, this::withTurnAttachments));
        builder.maxToolCallingRoundTrips(Integer.MAX_VALUE);
        builder.hallucinatedToolNameStrategy(
                LangChain4JLLMProvider::toolNotFound);
    }

    /**
     * Adds what the provider needs on the service whatever the application
     * configured: the tools of the turn, the observers of the responses and of
     * the output guardrails, and the model. The guardrail observer marks a turn
     * whose answer the output guardrails checked, see
     * {@link Turn#partialResponse(String)}. None of them replaces a setting of
     * the customizer: tool providers and listeners add up, and the model is the
     * application's own, given to the constructor. It is set after the
     * customizer ran, so that the checks in front of it cannot be configured
     * away, and the slot of the other kind of model is cleared, since the
     * service takes only one of them.
     */
    private void complete(AiServices<ChatService> builder, ChatModel chatModel,
            StreamingChatModel streamingChatModel) {
        builder.toolProvider(new TurnToolProvider());
        builder.registerListener(new ResponseObserver());
        builder.registerListener((OutputGuardrailExecutedListener) event -> {
            Turn turn = event.invocationContext().invocationParameters()
                    .get(TURN_PARAMETER);
            if (turn != null) {
                turn.outputGuarded = true;
            }
        });
        if (chatModel != null) {
            builder.chatModel(withTurnChecks(ChatModel.class, chatModel));
            builder.streamingChatModel(null);
        } else {
            builder.streamingChatModel(withTurnChecks(StreamingChatModel.class,
                    streamingChatModel));
            builder.chatModel(null);
        }
    }

    /**
     * Returns the tool traffic of the turn running on the current thread, or
     * {@code null} when no turn runs on it.
     */
    private List<dev.langchain4j.data.message.ChatMessage> getTurnMessages() {
        var turn = turnOnThread.get();
        return turn == null ? null : turn.turnMessages;
    }

    /**
     * Returns the user message with the attachments of the turn running on the
     * current thread added, when the message holds only the text of that turn,
     * and the message as it is otherwise. With
     * {@code storeRetrievedContentInChatMemory(false)}, LangChain4j stores the
     * user message as the template rendered it, before retrieval augmentation,
     * and so also without the attachments, which the provider passes
     * separately.
     */
    private UserMessage withTurnAttachments(UserMessage message) {
        var turn = turnOnThread.get();
        if (turn == null || !message.hasSingleText()
                || !message.singleText().equals(turn.userText)) {
            return message;
        }
        var contents = new ArrayList<>(message.contents());
        contents.addAll(turn.attachments);
        return message.toBuilder().contents(contents).build();
    }

    /**
     * Runs the action with the given turn bound to the current thread, and
     * restores what was bound before afterwards.
     */
    private <T> T runInTurn(Turn turn, Supplier<T> action) {
        var previous = turnOnThread.get();
        turnOnThread.set(turn);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                turnOnThread.remove();
            } else {
                turnOnThread.set(previous);
            }
        }
    }

    /**
     * Returns a model that passes every call on to the given one, and runs the
     * checks of the turn bound to the current thread on the request of a call
     * first, see {@link Turn#beforeModelCall(ChatRequest)}.
     * <p>
     * The checks sit in front of the model, rather than in the request
     * transformer or the tool error handler of the service, because the model
     * is the provider's own argument: each of those others is a single setting
     * the application may use in the customizer. All methods of the model are
     * passed on, so the service sees the capabilities, the default parameters
     * and the listeners of the model itself.
     */
    private <M> M withTurnChecks(Class<M> modelType, M model) {
        return modelType.cast(Proxy.newProxyInstance(modelType.getClassLoader(),
                new Class<?>[] { modelType }, new TurnCheckingModel(model)));
    }

    /**
     * The calls on the model the service is given: each is passed on to the
     * model of the application. A call that carries a {@link ChatRequest} is
     * checked against the turn bound to the current thread first. The response
     * handler of a streaming call is wrapped to bind the turn again on the
     * thread that delivers the response, where the service makes its next model
     * call: a model call does not tell which turn it belongs to, and a
     * cancelled turn may still be running when the next one starts.
     */
    private final class TurnCheckingModel implements InvocationHandler {
        private final Object model;

        TurnCheckingModel(Object model) {
            this.model = model;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments)
                throws Throwable {
            var turn = turnOnThread.get();
            if (turn == null || arguments == null || arguments.length == 0
                    || !(arguments[0] instanceof ChatRequest request)) {
                // Not a model call of a turn: the service makes those only
                // on a thread the provider bound the turn to.
                return invokeModel(method, arguments);
            }
            var handler = Arrays.stream(arguments)
                    .filter(StreamingChatResponseHandler.class::isInstance)
                    .map(StreamingChatResponseHandler.class::cast).findFirst();
            ChatRequest checked;
            try {
                checked = turn.beforeModelCall(request);
            } catch (RuntimeException e) {
                if (handler.isEmpty()) {
                    throw e;
                }
                // A streaming model reports a failure to its handler. Thrown
                // instead, the exception would surface in the handler of the
                // previous response, where what becomes of it is up to the
                // model implementation.
                handler.get().onError(e);
                return null;
            }
            var passedOn = arguments.clone();
            passedOn[0] = checked;
            for (var i = 1; i < passedOn.length; i++) {
                if (passedOn[i] instanceof StreamingChatResponseHandler h) {
                    passedOn[i] = bindTurn(turn, h);
                }
            }
            var response = invokeModel(method, passedOn);
            if (handler.isEmpty() && turn.isCancelled()) {
                // Returned to the service, the response would run the tools
                // it requests, also those the provider does not guard.
                LOGGER.debug("The turn was cancelled, discarding the model "
                        + "response");
                throw new TurnCancelledException();
            }
            return response;
        }

        private Object invokeModel(Method method, Object[] arguments)
                throws Throwable {
            try {
                return method.invoke(model, arguments);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    /**
     * Returns a handler that passes every call on to the given one with the
     * turn bound to the thread that makes the call. All methods are passed on,
     * so the service receives every event the model reports, except the two
     * that run tools once the turn is cancelled: a complete tool call, where
     * LangChain4j starts a tool when it executes tools concurrently, is
     * dropped, and the complete response is replaced with an error that ends
     * the turn.
     */
    private StreamingChatResponseHandler bindTurn(Turn turn,
            StreamingChatResponseHandler handler) {
        InvocationHandler invocationHandler = (proxy, method,
                arguments) -> runInTurn(turn, () -> {
                    if (turn.isCancelled()) {
                        if ("onCompleteResponse".equals(method.getName())) {
                            LOGGER.debug("The turn was cancelled, discarding "
                                    + "the model response");
                            handler.onError(new TurnCancelledException());
                            return null;
                        }
                        if ("onCompleteToolCall".equals(method.getName())) {
                            return null;
                        }
                    }
                    try {
                        return method.invoke(handler, arguments);
                    } catch (InvocationTargetException e) {
                        throw LangChain4JLLMProvider
                                .<RuntimeException> rethrow(e.getCause());
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                });
        return (StreamingChatResponseHandler) Proxy.newProxyInstance(
                StreamingChatResponseHandler.class.getClassLoader(),
                new Class<?>[] { StreamingChatResponseHandler.class },
                invocationHandler);
    }

    /**
     * Throws the given throwable as it is, checked or not, so that a handler
     * method reached through reflection fails the way it would have when called
     * directly.
     */
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> E rethrow(Throwable throwable)
            throws E {
        throw (E) throwable;
    }

    @Override
    public Flux<String> stream(LLMRequest request) {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(request.userMessage(),
                "User message must not be null");
        var response = Flux.<String> create(sink -> {
            if (sink.isCancelled()) {
                // Checked before the question is recorded: a turn
                // abandoned before it started must leave the conversation
                // as it was, not a question the next turn would repeat.
                LOGGER.debug("The turn was cancelled before it started");
                return;
            }
            var turn = new Turn(request, sink,
                    new ToolCallLimits(maxCallsPerTool, maxTotalToolCalls));
            try {
                runInTurn(turn, () -> {
                    startTurn(turn);
                    return null;
                });
            } catch (Exception e) {
                turn.fail(e);
            }
        }, FluxSink.OverflowStrategy.BUFFER);
        if (streaming && !backgroundExecution.isEnabled()) {
            return backgroundExecution.applyToStreamingResponse(response);
        }
        // A streaming turn blocks the subscribing thread until its model call
        // starts, as retrieval and input guardrails run before it, so with
        // background execution it moves off that thread like a blocking turn.
        return backgroundExecution.applyToBlockingResponse(response);
    }

    private void startTurn(Turn turn) {
        var request = turn.request;
        turn.userText = LLMProviderHelpers.withSessionContext(
                request.userMessage(), request.sessionContext());
        turn.attachments = buildAttachmentContents(request);
        var parameters = InvocationParameters.from(TURN_PARAMETER, turn);
        if (streaming) {
            service.stream(USER_TEXT_TEMPLATE, turn.userText, turn.attachments,
                    parameters).onPartialResponse(turn::partialResponse)
                    .onCompleteResponse(turn::complete).onError(turn::fail)
                    .start();
        } else {
            turn.complete(service.call(USER_TEXT_TEMPLATE, turn.userText,
                    turn.attachments, parameters));
        }
    }

    /**
     * Gets whether the LLM call runs on a background thread.
     *
     * @return {@code true} if the call runs on a background thread,
     *         {@code false} if it runs on the thread that asks for the response
     */
    public boolean isBackgroundExecution() {
        return backgroundExecution.isEnabled();
    }

    /**
     * Sets whether to run the LLM call on a background thread. The default is
     * {@code false}, which runs it on the thread that asks for the response —
     * the UI thread, for a prompt triggered from the browser.
     * <p>
     * With a {@link StreamingChatModel}, the response arrives on the LLM
     * client's own threads either way, but the turn starts on the thread that
     * asks for it: the retrieval augmentation and the input guardrails
     * configured in the customizer run there before the model is called.
     * Retrieval from an embedding store or a web search can take seconds, so
     * enable this setting when you use retrieval or input guardrails with a
     * streaming model. The start of the turn then runs on a background thread
     * as well.
     * <p>
     * A {@link ChatModel} call blocks for the whole turn, every tool call
     * included. On the UI thread that means holding the session lock until the
     * turn ends, so nothing the turn produces reaches the browser and the
     * application appears frozen. Set this to {@code true} to run the call on a
     * background thread instead: the request completes immediately, the user's
     * message and the typing indicator render, and the response is added when
     * it arrives.
     * <p>
     * This requires the following from the application:
     * <ul>
     * <li><b>A way to deliver the response.</b> Annotate the application shell
     * or UI class with {@code @Push}, or enable polling with
     * {@link UI#setPollInterval(int)}. Manual push mode is not enough on its
     * own, because nothing calls {@code ui.push()} for you. A warning is logged
     * when neither is active.</li>
     * <li><b>Thread-safe tools.</b> On a background thread Vaadin thread locals
     * such as {@link UI#getCurrent()} and framework contexts such as Spring
     * Security's {@code SecurityContext} are not bound, and UI components must
     * not be accessed directly. Wrap component access in {@code ui.access()},
     * or capture what you need in
     * {@link com.vaadin.flow.component.ai.orchestrator.AIController#onRequest},
     * which still runs on the UI thread. This is the same requirement a
     * {@link StreamingChatModel} already has. With a streaming model, the
     * setting extends it to the retrieval and the input guardrails.</li>
     * </ul>
     *
     * <p>
     * The orchestrator processes one prompt at a time. Without background
     * execution, a message submitted while a turn is running waits for the
     * session lock and is processed when the turn ends; with it, the submit is
     * rejected and dropped with a warning — and a connected input has already
     * cleared its text.
     * </p>
     *
     * <p>
     * Like the streaming mode, the setting is not preserved when the session is
     * serialized: an application that restores sessions must re-apply it when
     * it recreates the provider.
     * </p>
     *
     * @param backgroundExecution
     *            {@code true} to run the call on a background thread,
     *            {@code false} to run it on the thread that asks for the
     *            response
     */
    public void setBackgroundExecution(boolean backgroundExecution) {
        this.backgroundExecution.setEnabled(backgroundExecution);
    }

    /**
     * Gets the maximum number of times the model may call any one tool during a
     * turn.
     *
     * @return the maximum number of calls per tool, or {@code 0} if there is no
     *         limit
     * @since 25.4
     */
    public int getMaxCallsPerTool() {
        return maxCallsPerTool;
    }

    /**
     * Sets the maximum number of times the model may call any one tool during a
     * turn. The default is {@code 40}.
     * <p>
     * A model that keeps calling the same tool instead of answering, for
     * example to look for something the application does not have, would
     * otherwise never end its turn. Once a call would exceed the limit, the
     * turn fails with a {@link ToolCallLimitExceededException} that names the
     * tool: none of the tool calls of that round are executed, the model is not
     * called again, and you receive the exception as the error of the turn. The
     * conversation stays usable: the failed turn leaves only its prompt in the
     * provider's chat memory, and the next prompt continues from there.
     * <p>
     * Calls to the tools you configured in the customizer count against the
     * limit as well, but the provider cannot hold those tools back: the ones
     * the model requested in that last round still run before the turn fails.
     * The same goes for every tool when you enable
     * {@code executeToolsConcurrently()} in the customizer with a
     * {@link StreamingChatModel}, as LangChain4j then starts a tool before the
     * response that requests it is complete.
     * <p>
     * The limit applies to each tool separately. See
     * {@link #setMaxTotalToolCalls(int)} for the limit on all tool calls of a
     * turn together. The value is read when a turn starts, so a change applies
     * from the next prompt on.
     *
     * @param maxCallsPerTool
     *            the maximum number of calls per tool during a turn, or
     *            {@code 0} to remove the limit
     * @throws IllegalArgumentException
     *             if the value is negative
     * @since 25.4
     */
    public void setMaxCallsPerTool(int maxCallsPerTool) {
        if (maxCallsPerTool < 0) {
            throw new IllegalArgumentException(
                    "maxCallsPerTool must not be negative, 0 removes the limit");
        }
        this.maxCallsPerTool = maxCallsPerTool;
    }

    /**
     * Gets the maximum number of tool calls the model may make during a turn,
     * all tools together.
     *
     * @return the maximum number of tool calls per turn, or {@code 0} if there
     *         is no limit
     * @since 25.4
     */
    public int getMaxTotalToolCalls() {
        return maxTotalToolCalls;
    }

    /**
     * Sets the maximum number of tool calls the model may make during a turn,
     * all tools together. The default is {@code 150}.
     * <p>
     * This bounds a turn that {@link #setMaxCallsPerTool(int)} does not catch
     * because the model spreads its calls over several tools. Once a call would
     * exceed the limit, the turn fails with a
     * {@link ToolCallLimitExceededException}: none of the tool calls of that
     * round are executed, the model is not called again, and you receive the
     * exception as the error of the turn. The conversation stays usable: the
     * failed turn leaves only its prompt in the provider's chat memory, and the
     * next prompt continues from there. As with
     * {@link #setMaxCallsPerTool(int)}, the tools you configured in the
     * customizer count against the limit, and those requested in that last
     * round still run.
     * <p>
     * The value is read when a turn starts, so a change applies from the next
     * prompt on.
     *
     * @param maxTotalToolCalls
     *            the maximum number of tool calls during a turn, or {@code 0}
     *            to remove the limit
     * @throws IllegalArgumentException
     *             if the value is negative
     * @since 25.4
     */
    public void setMaxTotalToolCalls(int maxTotalToolCalls) {
        if (maxTotalToolCalls < 0) {
            throw new IllegalArgumentException(
                    "maxTotalToolCalls must not be negative, 0 removes the limit");
        }
        this.maxTotalToolCalls = maxTotalToolCalls;
    }

    @Override
    public void setHistory(List<ChatMessage> history,
            Map<String, List<AIAttachment>> attachmentsByMessageId) {
        Objects.requireNonNull(history, "History must not be null");
        Objects.requireNonNull(attachmentsByMessageId,
                "Attachments map must not be null");
        var chatMemory = service.getChatMemory(DEFAULT_MEMORY_ID);
        if (chatMemory == null) {
            LOGGER.warn("History restoration was requested, but the service "
                    + "has no chat memory for the default conversation, so "
                    + "the restored conversation does not reach the LLM.");
            return;
        }
        chatMemory.clear();
        for (var message : history) {
            var attachments = message.messageId() != null
                    ? attachmentsByMessageId.getOrDefault(message.messageId(),
                            Collections.emptyList())
                    : Collections.<AIAttachment> emptyList();
            chatMemory.add(toVendorMessage(message, attachments));
        }
    }

    private static dev.langchain4j.data.message.ChatMessage toVendorMessage(
            ChatMessage message) {
        if (message.role() == ChatMessage.Role.USER) {
            return UserMessage.from(message.content());
        }
        return AiMessage.from(message.content());
    }

    private static dev.langchain4j.data.message.ChatMessage toVendorMessage(
            ChatMessage message, List<AIAttachment> attachments) {
        if (message.role() != ChatMessage.Role.USER || attachments.isEmpty()) {
            return toVendorMessage(message);
        }
        var contents = new ArrayList<Content>();
        contents.add(TextContent.from(message.content()));
        attachments.stream().map(LangChain4JLLMProvider::getAttachmentContent)
                .flatMap(Optional::stream).forEach(contents::add);
        return UserMessage.from(contents);
    }

    private static ToolExecutionResultMessage toolNotFound(
            ToolExecutionRequest toolRequest) {
        return ToolExecutionResultMessage.from(toolRequest,
                "Tool not found: " + toolRequest.name());
    }

    private static List<Content> buildAttachmentContents(LLMRequest request) {
        var attachments = request.attachments();
        if (attachments == null) {
            return List.of();
        }
        return attachments.stream()
                .map(LangChain4JLLMProvider::getAttachmentContent)
                .flatMap(Optional::stream).toList();
    }

    private static Optional<Content> getAttachmentContent(
            AIAttachment attachment) {
        LLMProviderHelpers.validateAttachment(attachment);
        var contentType = AttachmentContentType
                .fromMimeType(attachment.mimeType());
        return switch (contentType) {
        case IMAGE -> Optional.of(getImageAttachmentContent(attachment));
        case TEXT -> Optional.of(getTextAttachmentContent(attachment));
        case PDF -> Optional.of(getPdfAttachmentContent(attachment));
        case AUDIO -> Optional.of(getAudioAttachmentContent(attachment));
        case VIDEO -> Optional.of(getVideoAttachmentContent(attachment));
        case UNSUPPORTED -> Optional.empty();
        };
    }

    private static TextContent getTextAttachmentContent(
            AIAttachment attachment) {
        var textContent = LLMProviderHelpers.decodeAsUtf8(attachment.data(),
                attachment.name(), false);
        return TextContent.from(LLMProviderHelpers
                .formatTextAttachment(attachment.name(), textContent));
    }

    private static PdfFileContent getPdfAttachmentContent(
            AIAttachment attachment) {
        var base64 = LLMProviderHelpers.getBase64Data(attachment.data());
        return PdfFileContent.from(base64, attachment.mimeType());
    }

    private static ImageContent getImageAttachmentContent(
            AIAttachment attachment) {
        var base64 = LLMProviderHelpers.getBase64Data(attachment.data());
        return ImageContent.from(base64, attachment.mimeType());
    }

    private static AudioContent getAudioAttachmentContent(
            AIAttachment attachment) {
        var base64 = LLMProviderHelpers.getBase64Data(attachment.data());
        return AudioContent.from(base64, attachment.mimeType());
    }

    private static VideoContent getVideoAttachmentContent(
            AIAttachment attachment) {
        var base64 = LLMProviderHelpers.getBase64Data(attachment.data());
        return VideoContent.from(base64, attachment.mimeType());
    }

    /**
     * Provides the tools of the turn to the service: the vendor-annotated tool
     * objects and the controller's {@link LLMProvider.ToolSpec} tools of the
     * request the turn was started with, read from the invocation parameters.
     * Every executor checks the state of the turn before it runs, so no tool of
     * a round executes once a tool call limit is exceeded or the turn is
     * cancelled.
     */
    private static final class TurnToolProvider implements ToolProvider {

        @Override
        public ToolProviderResult provideTools(ToolProviderRequest request) {
            Turn turn = request.invocationParameters().get(TURN_PARAMETER);
            var result = ToolProviderResult.builder();
            if (turn == null) {
                return result.build();
            }
            var tools = turn.request.tools();
            if (tools != null) {
                for (var toolObject : tools) {
                    // An executor for each annotated method, private methods
                    // included.
                    Arrays.stream(toolObject.getClass().getDeclaredMethods())
                            .filter(method -> method
                                    .isAnnotationPresent(Tool.class))
                            .forEach(method -> result.add(
                                    ToolSpecifications
                                            .toolSpecificationFrom(method),
                                    turn.guard(toAnnotatedToolExecutor(
                                            toolObject, method))));
                }
            }
            for (var tool : turn.request.explicitTools()) {
                result.add(toToolSpecification(tool),
                        turn.guard(toExplicitToolExecutor(tool)));
            }
            return result.build();
        }
    }

    private static ToolExecutor toAnnotatedToolExecutor(Object toolObject,
            Method method) {
        var baseExecutor = new DefaultToolExecutor(toolObject, method);
        return (execReq, memoryId) -> runTool(execReq.name(),
                () -> baseExecutor.execute(execReq, memoryId));
    }

    private static ToolExecutor toExplicitToolExecutor(
            LLMProvider.ToolSpec tool) {
        var placeholderSchema = usesPlaceholderSchema(tool);
        return (execReq, memoryId) -> runTool(tool.getName(), () -> {
            if (placeholderSchema) {
                // The model saw the placeholder schema; the tool did not
                // declare a usable one, so it receives no arguments. Decided
                // before parsing, so the tool stays callable even on the
                // malformed arguments (an empty string, say) that the
                // placeholder schema exists to work around.
                return tool.execute(JacksonUtils.createObjectNode());
            }
            return tool
                    .execute(parseExplicitToolArguments(execReq.arguments()));
        });
    }

    /**
     * Runs a tool the provider supplies and turns a failure into the result for
     * the model: the message of a {@link ToolException}, which is meant for the
     * model, or a generic text for any other exception, so that internal
     * details do not reach it. No exception leaves these executors, so the tool
     * execution error handler of the service, which the application may
     * configure for its own tools, only ever sees those.
     */
    private static String runTool(String toolName, Supplier<String> tool) {
        try {
            return tool.get();
        } catch (ToolException e) {
            LOGGER.warn("Tool '{}' failed: {}", toolName, e.getMessage(), e);
            return "Error executing tool: " + e.getMessage();
        } catch (Exception e) {
            LOGGER.error("Tool '{}' failed", toolName, e);
            return "Error executing tool.";
        }
    }

    /**
     * Whether the schema sent to the LLM for this tool is
     * {@link LLMProviderHelpers#NO_PARAMETERS_SCHEMA} rather than the tool's
     * own: the tool declares no schema, or declares one that does not parse.
     * Every such tool gets an empty arguments object at execution time, so
     * whatever the model filled the placeholder with never reaches a tool.
     */
    private static boolean usesPlaceholderSchema(LLMProvider.ToolSpec tool) {
        return !LLMProviderHelpers.hasParameters(tool)
                || parseParametersSchemaOrNull(
                        tool.getParametersSchema()) == null;
    }

    private static JsonNode parseExplicitToolArguments(String arguments) {
        try {
            return LLMProviderHelpers.parseToolArguments(arguments);
        } catch (Exception e) {
            // The bad arguments came from the model itself, so the message is
            // safe to relay and lets the model repair its next attempt.
            throw new ToolException("invalid JSON arguments: " + e.getMessage(),
                    e);
        }
    }

    private static ToolSpecification toToolSpecification(
            LLMProvider.ToolSpec tool) {
        var builder = ToolSpecification.builder().name(tool.getName())
                .description(tool.getDescription());
        JsonObjectSchema parameters = null;
        if (LLMProviderHelpers.hasParameters(tool)) {
            parameters = parseParametersSchemaOrNull(
                    tool.getParametersSchema());
            if (parameters == null) {
                LOGGER.warn(
                        "Failed to parse the parameters schema of tool '{}', "
                                + "using the no-parameters schema",
                        tool.getName());
            }
        }
        if (parameters == null) {
            // A tool without a usable schema breaks some LLM APIs —
            // see LLMProviderHelpers.NO_PARAMETERS_SCHEMA. The constant is a
            // well-formed literal, so this parse cannot return null.
            parameters = parseParametersSchemaOrNull(
                    LLMProviderHelpers.NO_PARAMETERS_SCHEMA);
        }
        builder.parameters(parameters);
        return builder.build();
    }

    /**
     * Parses a JSON Schema string into the LangChain4j schema object, or
     * returns {@code null} when the string does not parse. Logs nothing: the
     * caller decides whether a failure is worth a warning, since the check runs
     * both when building the tool specification and when deciding the arguments
     * handed to the executor.
     */
    private static JsonObjectSchema parseParametersSchemaOrNull(
            String schemaJson) {
        try {
            JsonNode root = JacksonUtils.readTree(schemaJson);
            var schemaBuilder = JsonObjectSchema.builder();
            if (root.has("properties")) {
                JsonNode props = root.get("properties");
                for (String name : props.propertyNames()) {
                    schemaBuilder.addProperty(name,
                            JsonRawSchema.from(props.get(name).toString()));
                }
            }
            if (root.has("required")) {
                var required = new ArrayList<String>();
                root.get("required")
                        .forEach(e -> required.add(e.stringValue()));
                schemaBuilder.required(required);
            }
            return schemaBuilder.build();
        } catch (Exception e) {
            LOGGER.debug("Tool parameters schema failed to parse", e);
            return null;
        }
    }

    /**
     * Receives every model response of a turn, in both modes and before the
     * tools of the response run, and hands it to the turn.
     */
    private static final class ResponseObserver
            implements AiServiceListener<AiServiceResponseReceivedEvent> {

        @Override
        public Class<AiServiceResponseReceivedEvent> getEventClass() {
            return AiServiceResponseReceivedEvent.class;
        }

        @Override
        public void onEvent(AiServiceResponseReceivedEvent event) {
            Turn turn = event.invocationContext().invocationParameters()
                    .get(TURN_PARAMETER);
            if (turn != null) {
                turn.observeResponse(event.response());
            }
        }
    }

    /**
     * Ends the loop of a turn whose subscriber has cancelled.
     */
    private static final class TurnCancelledException extends RuntimeException {
        TurnCancelledException() {
            super("The turn was cancelled");
        }
    }

    /**
     * The tool call limits of one turn, and the calls counted against them so
     * far. The per-tool limit is checked before the total, and the call that
     * takes a count past its limit is the one refused. The whole round is
     * screened before anything runs, so a round containing a refused call
     * executes none of its calls.
     */
    private static final class ToolCallLimits {
        private final int maxCallsPerTool;
        private final int maxTotalToolCalls;
        private final Map<String, Integer> callsPerTool = new HashMap<>();
        private int totalToolCalls;

        ToolCallLimits(int maxCallsPerTool, int maxTotalToolCalls) {
            this.maxCallsPerTool = maxCallsPerTool;
            this.maxTotalToolCalls = maxTotalToolCalls;
        }

        /**
         * Counts one requested tool call against the limits.
         *
         * @param requestedToolName
         *            the name of the tool the model requested, possibly
         *            {@code null}
         * @return the exception describing the limit the call exceeds, or empty
         *         if the call is within the limits
         */
        Optional<ToolCallLimitExceededException> countAndCheck(
                String requestedToolName) {
            // LangChain4j does not guard the name, so it can be null; such
            // requests are counted together under "".
            var toolName = Objects.toString(requestedToolName, "");
            totalToolCalls++;
            var callsToTool = callsPerTool.merge(toolName, 1, Integer::sum);
            if (maxCallsPerTool > 0 && callsToTool > maxCallsPerTool) {
                return Optional.of(new ToolCallLimitExceededException(
                        "Tool call limit (" + maxCallsPerTool
                                + ") exceeded for tool '" + toolName + "'"));
            }
            if (maxTotalToolCalls > 0 && totalToolCalls > maxTotalToolCalls) {
                return Optional.of(new ToolCallLimitExceededException(
                        "Total tool call limit (" + maxTotalToolCalls
                                + ") exceeded for this turn"));
            }
            return Optional.empty();
        }
    }

    /**
     * The state of one turn: the request it answers, the sink its tokens go to,
     * the limits, its tool traffic and the metadata observed so far. The hooks
     * the provider registers on the service reach it through the invocation
     * parameters, the model decorator and the chat memory through the thread
     * the provider binds it to.
     */
    private static final class Turn {
        private final LLMRequest request;
        private final FluxSink<String> sink;
        private final ToolCallLimits toolCallLimits;
        private final AtomicBoolean ended = new AtomicBoolean(false);
        /**
         * Tool calls and tool results of this turn, in order. The provider's
         * chat memory returns them after the conversation while the turn runs,
         * and they are discarded with the turn.
         */
        private final List<dev.langchain4j.data.message.ChatMessage> turnMessages = new ArrayList<>();
        /**
         * The text of the user message, the session context included, as the
         * service receives it. Set when the turn starts.
         */
        private String userText;
        /**
         * The attachments of the user message, as the service receives them.
         * Set when the turn starts.
         */
        private List<Content> attachments = List.of();
        private FinishReason lastFinishReason;
        private boolean lastResponseHadFinishReason;
        private TokenUsage accumulatedUsage;
        private ToolCallLimitExceededException limitBreach;
        /**
         * Whether output guardrails checked the answer of this turn. Set on the
         * thread that then completes the turn.
         */
        private boolean outputGuarded;

        Turn(LLMRequest request, FluxSink<String> sink,
                ToolCallLimits toolCallLimits) {
            this.request = request;
            this.sink = sink;
            this.toolCallLimits = toolCallLimits;
        }

        boolean isCancelled() {
            return sink.isCancelled();
        }

        /**
         * Wraps a tool executor so that the tool does not run once the turn is
         * over: the subscriber has cancelled, or a tool call of the round
         * exceeded a limit. The round was screened when the model's response
         * arrived, so none of the tools of a refused round runs.
         * <p>
         * The executor answers such a call instead of throwing. An exception
         * from a tool goes to the tool execution error handler of the service,
         * which is the application's to configure. The turn ends in
         * {@link #beforeModelCall(ChatRequest)}, which the loop reaches next.
         */
        ToolExecutor guard(ToolExecutor executor) {
            return (toolRequest, memoryId) -> {
                if (isCancelled()) {
                    LOGGER.debug("The turn was cancelled, skipping the tool "
                            + "calls the model requested");
                    return TOOL_NOT_EXECUTED;
                }
                if (limitBreach != null) {
                    return TOOL_NOT_EXECUTED;
                }
                return executor.execute(toolRequest, memoryId);
            };
        }

        /**
         * Runs before each model call of the turn: ends the turn instead of
         * calling the model when its subscriber has cancelled or a tool call
         * exceeded a limit, and puts the system prompt of the request on the
         * model request. The limit check also bounds a loop over tools the
         * provider does not wrap, those configured in the customizer.
         * <p>
         * The system prompt the orchestrator sends takes the place of any
         * system message of the service, such as one set in the customizer. It
         * is sent as it is, not through the service's system message, which
         * LangChain4j renders as a prompt template.
         *
         * @return the request to send to the model
         * @throws TurnCancelledException
         *             if the subscriber of the turn has cancelled
         * @throws ToolCallLimitExceededException
         *             if a tool call of the turn exceeded a limit
         */
        ChatRequest beforeModelCall(ChatRequest chatRequest) {
            if (isCancelled()) {
                // The subscriber is gone, typically because the
                // orchestrator's timeout fired. Another model call would only
                // cost money for a result nobody receives.
                LOGGER.debug("The turn was cancelled, skipping the model call");
                throw new TurnCancelledException();
            }
            if (limitBreach != null) {
                throw limitBreach;
            }
            var messages = chatRequest.messages();
            var prompt = request.systemPrompt();
            if (prompt != null && !prompt.isBlank()) {
                var replaced = new ArrayList<dev.langchain4j.data.message.ChatMessage>();
                replaced.add(SystemMessage.from(prompt.trim()));
                messages.stream()
                        .filter(message -> !(message instanceof SystemMessage))
                        .forEach(replaced::add);
                messages = replaced;
            }
            return chatRequest.toBuilder().messages(messages).build();
        }

        /**
         * Records one model response: whether the tool calls it requests stay
         * within the limits of the turn, and its metadata.
         */
        void observeResponse(ChatResponse response) {
            // The limits come first: LangChain4j drops what a listener
            // throws, and a metadata sink that fails must not leave the tool
            // calls of the response unscreened.
            screenToolCalls(response);
            observeMetadata(response);
        }

        private void screenToolCalls(ChatResponse response) {
            var aiMessage = response.aiMessage();
            if (aiMessage == null || !aiMessage.hasToolExecutionRequests()
                    || limitBreach != null) {
                return;
            }
            for (var toolRequest : aiMessage.toolExecutionRequests()) {
                var exceededLimit = toolCallLimits
                        .countAndCheck(toolRequest.name());
                if (exceededLimit.isPresent()) {
                    limitBreach = exceededLimit.get();
                    return;
                }
            }
        }

        /**
         * Passes on a token of a streaming turn. With output guardrails,
         * LangChain4j holds the tokens of the answer back until the guardrails
         * have run and then replays them as the model sent them, also when a
         * guardrail replaced the answer. Those are dropped:
         * {@link #complete(ChatResponse)} passes on the answer the guardrails
         * returned instead.
         */
        void partialResponse(String token) {
            if (!outputGuarded) {
                sink.next(token);
            }
        }

        /**
         * Ends a streaming turn whose answer has arrived. Its text was passed
         * on as partial responses, unless output guardrails checked it.
         */
        void complete(ChatResponse response) {
            if (!ended.compareAndSet(false, true)) {
                return;
            }
            warnOnMissingFinishReason();
            if (outputGuarded) {
                emit(response.aiMessage().text());
            }
            sink.complete();
        }

        /**
         * Ends a non-streaming turn with the text of its answer. The text a
         * model sent next to its tool calls, such as a note on what it is about
         * to look up, comes first, but only with an answer that no output
         * guardrail checked: the guardrails do not see that text. A tool of the
         * customizer that returns immediately ends the turn without an answer,
         * before the output guardrails could run.
         */
        void complete(Result<String> result) {
            if (!ended.compareAndSet(false, true)) {
                return;
            }
            warnOnMissingFinishReason();
            if (!outputGuarded
                    && result.finishReason() != FinishReason.TOOL_EXECUTION) {
                result.intermediateResponses()
                        .forEach(response -> emit(response.aiMessage().text()));
            }
            emit(result.content());
            sink.complete();
        }

        private void emit(String text) {
            if (text != null && !text.isEmpty()) {
                sink.next(text);
            }
        }

        /**
         * Ends the turn with an error. Its tool traffic is discarded with it,
         * so the next turn continues from the chat memory as the last completed
         * turn left it.
         */
        void fail(Throwable error) {
            if (!ended.compareAndSet(false, true)) {
                return;
            }
            if (error instanceof TurnCancelledException) {
                // Nobody is listening; the cancellation is the subscriber's
                // own doing.
                return;
            }
            sink.error(error);
        }

        /**
         * Records the metadata of one model round trip and passes the state
         * known so far to the metadata sink right away, so that a turn whose
         * later round trip fails has still reported what the earlier ones cost.
         * The reason that ends the turn wins; token usage accumulates across
         * the round trips.
         */
        private void observeMetadata(ChatResponse response) {
            lastResponseHadFinishReason = response.finishReason() != null;
            if (response.finishReason() == null
                    && response.tokenUsage() == null) {
                // Nothing new in this round trip, nothing to re-publish.
                return;
            }
            if (response.finishReason() != null) {
                lastFinishReason = response.finishReason();
            }
            var usage = response.tokenUsage();
            if (usage != null) {
                accumulatedUsage = accumulatedUsage == null ? usage
                        : accumulatedUsage.add(usage);
            }
            publishMetadata();
        }

        private void publishMetadata() {
            // LangChain4j collapses the model's own word onto its
            // FinishReason enum before handing the response over — per
            // integration, and losing whatever it does not recognize — so the
            // constant name is the most faithful value left to publish. See
            // ResponseMetadata, which documents what that costs.
            var finishReason = lastFinishReason == null ? null
                    : lastFinishReason.name();
            var tokenUsage = accumulatedUsage == null ? null
                    : new ResponseMetadata.TokenUsage(
                            accumulatedUsage.inputTokenCount(),
                            accumulatedUsage.outputTokenCount(),
                            accumulatedUsage.totalTokenCount());
            request.metadataSink()
                    .accept(new ResponseMetadata(finishReason, tokenUsage));
        }

        /**
         * Warns when the round trip that ends the turn carries no finish
         * reason.
         * <p>
         * Only that round trip counts. A reason reported by an earlier round
         * trip describes why <i>that</i> round trip stopped,
         * {@code TOOL_EXECUTION} typically, and says nothing about how the turn
         * ended, so it must not silence the warning. This mirrors
         * {@code SpringAILLMProvider}, whose terminal-chunk check likewise
         * ignores a reason that arrives with tool calls still pending. Called
         * only where the turn ends with the model's own answer; a turn ended by
         * a tool call limit or by a cancelled sink does not pass through here,
         * since neither is a normal completion.
         * <p>
         * A missing finish reason is the one terminal state left that may
         * indicate a silent abnormal termination. It does not prove the model
         * reported nothing: LangChain4j also leaves the reason unset when its
         * integration does not recognize the value the model sent.
         */
        private void warnOnMissingFinishReason() {
            if (lastResponseHadFinishReason) {
                return;
            }
            LOGGER.warn("LLM turn ended with no finish reason for its final "
                    + "round trip. Either the model reported none, which may "
                    + "indicate a silent abnormal termination such as an upstream "
                    + "error, or LangChain4j dropped a value its integration does "
                    + "not recognize; if the response appears truncated this "
                    + "warning is the signal.");
        }
    }
}
