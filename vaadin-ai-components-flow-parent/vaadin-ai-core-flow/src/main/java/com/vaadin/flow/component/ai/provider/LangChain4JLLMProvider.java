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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

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
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonRawSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.observability.api.event.AiServiceResponseReceivedEvent;
import dev.langchain4j.observability.api.listener.AiServiceListener;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.AiServiceContext;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.memory.ChatMemoryAccess;
import dev.langchain4j.service.memory.ChatMemoryService;
import dev.langchain4j.service.tool.DefaultToolExecutor;
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
 * The provider drives a {@link ChatService} that LangChain4j's
 * {@link AiServices} implements. The service owns the conversation memory and
 * the tool-calling loop, and everything LangChain4j offers on
 * {@code AiServices} (retrieval augmentation, tool providers such as MCP,
 * guardrails, moderation, listeners) can be configured on it. The provider adds
 * what the orchestrator needs: the tools of the turn, the attachments and the
 * session context as message contents, the system prompt, response metadata,
 * the tool call limits and the tool error policy for
 * {@link LLMProvider.ToolSpec} tools.
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
 * warning is logged at runtime when neither is active.
 * </p>
 * <p>
 * <b>Blocking the request thread:</b> a {@link ChatModel} call blocks the
 * thread that subscribes to the response, which is the UI thread for a prompt
 * triggered from the browser. Call {@link #setBackgroundExecution(boolean)
 * setBackgroundExecution(true)} to run the call on a background thread instead,
 * so the request completes and the user's message renders while the LLM works.
 * </p>
 * <p>
 * <b>Tool call limits:</b> a model that keeps requesting tool calls instead of
 * answering would never end its turn, and each round costs another model call.
 * The provider ends such a turn with a {@link ToolCallLimitExceededException}
 * once the model has requested more than {@code 40} calls to any one tool, or
 * more than {@code 150} tool calls in total, within the turn. Adjust or remove
 * them with {@link #setMaxCallsPerTool(int)} and
 * {@link #setMaxTotalToolCalls(int)}.
 * </p>
 * <p>
 * Each provider instance maintains its own chat memory. To share conversation
 * history across components, reuse the same provider instance. The memory holds
 * the user messages and the assistant's final answer of each turn; tool calls
 * and their results are sent to the model only within the turn they belong to
 * and are not replayed on later turns. A final answer without text, which a
 * model may give once a tool call has done what was asked, is not stored
 * either, so that the turn leaves only its user message in the memory.
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
     * The user message of a turn is passed as a list of contents, so that
     * attachments and the session context reach the model as they are, and the
     * state of the turn travels in the invocation parameters.
     */
    public interface ChatService extends ChatMemoryAccess {

        /**
         * Runs a turn against a streaming model.
         *
         * @param message
         *            the contents of the user message
         * @param parameters
         *            the invocation parameters carrying the turn
         * @return the token stream of the turn
         */
        TokenStream stream(
                @dev.langchain4j.service.UserMessage List<Content> message,
                InvocationParameters parameters);

        /**
         * Runs a turn against a blocking model.
         *
         * @param message
         *            the contents of the user message
         * @param parameters
         *            the invocation parameters carrying the turn
         * @return the result of the turn
         */
        Result<String> call(
                @dev.langchain4j.service.UserMessage List<Content> message,
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
    static final String TURN_PARAMETER = LangChain4JLLMProvider.class.getName()
            + ".turn";

    private final transient ChatService service;
    private final transient boolean streaming;
    private final transient AtomicReference<Turn> currentTurn = new AtomicReference<>();
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
        this(builder(chatModel));
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
        this(builder(chatModel));
    }

    private LangChain4JLLMProvider(Builder settings) {
        this.streaming = settings.streamingChatModel != null;
        this.service = createService(settings);
    }

    /**
     * Creates a builder for a provider on a streaming chat model. The builder
     * takes the system prompt and lets the application customize the AI service
     * the provider is built on, see {@link Builder#customize}.
     *
     * @param chatModel
     *            the streaming chat model, not {@code null}
     * @return the builder
     * @throws NullPointerException
     *             if chatModel is {@code null}
     * @since 25.4
     */
    public static Builder builder(StreamingChatModel chatModel) {
        return new Builder(null, Objects.requireNonNull(chatModel,
                "StreamingChatModel must not be null"));
    }

    /**
     * Creates a builder for a provider on a non-streaming chat model. The
     * builder takes the system prompt and lets the application customize the AI
     * service the provider is built on, see {@link Builder#customize}.
     *
     * @param chatModel
     *            the non-streaming chat model, not {@code null}
     * @return the builder
     * @throws NullPointerException
     *             if chatModel is {@code null}
     * @since 25.4
     */
    public static Builder builder(ChatModel chatModel) {
        return new Builder(
                Objects.requireNonNull(chatModel, "ChatModel must not be null"),
                null);
    }

    private ChatService createService(Builder settings) {
        var context = AiServiceContext.create(ChatService.class);
        var builder = AiServices.<ChatService> builder(context);
        if (settings.chatModel != null) {
            builder.chatModel(settings.chatModel);
        }
        if (settings.streamingChatModel != null) {
            builder.streamingChatModel(settings.streamingChatModel);
        }
        prepare(builder);
        if (settings.customizer != null) {
            settings.customizer.accept(builder);
        }
        wire(context, builder, settings.systemPrompt);
        return builder.build();
    }

    /**
     * Applies the defaults the provider wants on the service before the
     * application gets to customize it, so that each can be overridden: the
     * tool-calling loop is bounded by the provider's own tool call limits
     * rather than by LangChain4j's round trip count, and a tool the model made
     * up is reported back to it instead of failing the turn.
     */
    private void prepare(AiServices<ChatService> builder) {
        builder.maxToolCallingRoundTrips(Integer.MAX_VALUE);
        builder.hallucinatedToolNameStrategy(this::toolNotFound);
    }

    /**
     * Installs the hooks the provider needs on the service: the memory, the
     * tools of the turn, the system prompt, the metadata and tool call limit
     * observer, the cancellation check before each model call, and the tool
     * error handling that lets a limit or a cancellation end the turn. Whatever
     * the service already carries is kept and composed with.
     * <p>
     * The system prompt is placed on the model request by the request
     * transformer rather than through the service's system message provider:
     * the service renders that one as a prompt template, which would fail on a
     * prompt containing braces, such as a JSON example.
     */
    private void wire(AiServiceContext context, AiServices<ChatService> builder,
            String systemPrompt) {
        if (!context.hasChatMemory()) {
            // The service reads the memory before it adds the user message
            // of the turn, so a request carries the window plus that
            // message. The window is sized one short to keep a request
            // within MAX_MESSAGES.
            builder.chatMemory(new TurnScopedChatMemory(
                    MessageWindowChatMemory.withMaxMessages(MAX_MESSAGES - 1)));
        }
        builder.toolProvider(new TurnToolProvider());
        builder.registerListener(new ResponseObserver());
        if (context.retrievalAugmentor != null) {
            context.retrievalAugmentor = new AttachmentKeepingAugmentor(
                    context.retrievalAugmentor);
        }

        BiFunction<ChatRequest, Object, ChatRequest> configuredTransformer = context.chatRequestTransformer;
        context.chatRequestTransformer = (request, memoryId) -> {
            var turn = currentTurn.get();
            if (turn != null && turn.isCancelled()) {
                // The subscriber is gone, typically because the
                // orchestrator's timeout fired. Another model call would only
                // cost money for a result nobody receives.
                LOGGER.debug("The turn was cancelled, skipping the model call");
                throw new TurnCancelledException();
            }
            var prompt = effectiveSystemPrompt(turn, systemPrompt);
            return configuredTransformer.apply(prompt == null ? request
                    : withSystemMessage(request, prompt), memoryId);
        };

        var configuredErrorHandler = context.toolService
                .executionErrorHandler();
        builder.toolExecutionErrorHandler((error, errorContext) -> {
            if (error instanceof ToolCallLimitExceededException
                    || error instanceof TurnCancelledException) {
                throw (RuntimeException) error;
            }
            return configuredErrorHandler.handle(error, errorContext);
        });
    }

    /**
     * Returns the system prompt of the turn: the one its request carries, else
     * the one the builder was given, or {@code null} when there is neither and
     * the service's own configuration applies.
     */
    private static String effectiveSystemPrompt(Turn turn,
            String builderPrompt) {
        var fromRequest = turn == null ? null : turn.request.systemPrompt();
        if (fromRequest != null && !fromRequest.isBlank()) {
            return fromRequest.trim();
        }
        if (builderPrompt != null && !builderPrompt.isBlank()) {
            return builderPrompt.trim();
        }
        return null;
    }

    /**
     * Returns the request with the given system message in place of the one it
     * starts with, or ahead of its messages when it has none.
     */
    private static ChatRequest withSystemMessage(ChatRequest request,
            String prompt) {
        var messages = new ArrayList<>(request.messages());
        if (!messages.isEmpty()
                && messages.getFirst() instanceof SystemMessage) {
            messages.set(0, SystemMessage.from(prompt));
        } else {
            messages.addFirst(SystemMessage.from(prompt));
        }
        return request.toBuilder().messages(messages).build();
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
            currentTurn.set(turn);
            try {
                var contents = buildContents(request);
                var parameters = InvocationParameters.from(TURN_PARAMETER,
                        turn);
                if (streaming) {
                    service.stream(contents, parameters)
                            .onPartialResponse(sink::next)
                            .onCompleteResponse(turn::complete)
                            .onError(turn::fail).start();
                } else {
                    turn.complete(service.call(contents, parameters));
                }
            } catch (Exception e) {
                turn.fail(e);
            }
        }, FluxSink.OverflowStrategy.BUFFER);
        return streaming
                ? backgroundExecution.applyToStreamingResponse(response)
                : backgroundExecution.applyToBlockingResponse(response);
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
     * the UI thread, for a prompt triggered from the browser. The setting has
     * no effect with a {@link StreamingChatModel}, whose response already
     * arrives on the LLM client's own threads.
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
     * {@link StreamingChatModel} already has.</li>
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
     * chat memory, and the next prompt continues from there.
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
     * failed turn leaves only its prompt in the chat memory, and the next
     * prompt continues from there.
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
        var chatMemory = getChatMemory();
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

    private ChatMemory getChatMemory() {
        return service.getChatMemory(ChatMemoryService.DEFAULT);
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

    private ToolExecutionResultMessage toolNotFound(
            ToolExecutionRequest toolRequest) {
        var turn = currentTurn.get();
        if (turn != null) {
            turn.beforeTool();
        }
        return ToolExecutionResultMessage.from(toolRequest,
                "Tool not found: " + toolRequest.name());
    }

    private static List<Content> buildContents(LLMRequest request) {
        var contents = new ArrayList<Content>();
        contents.add(TextContent.from(LLMProviderHelpers.withSessionContext(
                request.userMessage(), request.sessionContext())));
        var attachments = request.attachments();
        if (attachments != null) {
            attachments.stream()
                    .map(LangChain4JLLMProvider::getAttachmentContent)
                    .flatMap(Optional::stream).forEach(contents::add);
        }
        return contents;
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
     * Every executor checks the state of the turn before it runs, so a tool
     * call limit or a cancellation ends the turn before any tool of the round
     * executes.
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
                    Arrays.stream(toolObject.getClass().getDeclaredMethods())
                            .filter(method -> method
                                    .isAnnotationPresent(Tool.class))
                            .forEach(method -> result.add(
                                    ToolSpecifications
                                            .toolSpecificationFrom(method),
                                    turn.guard(getToolExecutor(toolObject,
                                            method))));
                }
            }
            for (var tool : turn.request.explicitTools()) {
                result.add(toToolSpecification(tool),
                        turn.guard(toExplicitToolExecutor(tool)));
            }
            return result.build();
        }

        private static ToolExecutor getToolExecutor(Object toolObject,
                Method method) {
            var baseExecutor = new DefaultToolExecutor(toolObject, method);
            return baseExecutor::execute;
        }

        private static ToolExecutor toExplicitToolExecutor(
                LLMProvider.ToolSpec tool) {
            var placeholderSchema = usesPlaceholderSchema(tool);
            return (execReq, memoryId) -> {
                try {
                    if (placeholderSchema) {
                        // The model saw the placeholder schema; the tool did
                        // not declare a usable one, so it receives no
                        // arguments. Decided before parsing, so the tool
                        // stays callable even on the malformed arguments —
                        // an empty string, say — that the placeholder schema
                        // exists to work around.
                        return tool.execute(JacksonUtils.createObjectNode());
                    }
                    return tool.execute(
                            parseExplicitToolArguments(execReq.arguments()));
                } catch (ToolException e) {
                    LOGGER.warn("Tool '{}' failed: {}", tool.getName(),
                            e.getMessage(), e);
                    return "Error executing tool: " + e.getMessage();
                } catch (Exception e) {
                    LOGGER.error("Tool '{}' failed", tool.getName(), e);
                    return "Error executing tool.";
                }
            };
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
     * tools of the response run: publishes the metadata observed so far and
     * screens the tool calls of the round against the limits.
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
     * Lets retrieval augmentation work on a turn that carries attachments.
     * LangChain4j's default content injector expects a user message with a
     * single text content and fails on one that also carries an image or a
     * file. This wrapper hands the augmentor the text alone and puts the other
     * contents back on the augmented message.
     */
    private static final class AttachmentKeepingAugmentor
            implements RetrievalAugmentor {
        private final RetrievalAugmentor delegate;

        AttachmentKeepingAugmentor(RetrievalAugmentor delegate) {
            this.delegate = delegate;
        }

        @Override
        public AugmentationResult augment(AugmentationRequest request) {
            if (!(request.chatMessage() instanceof UserMessage userMessage)
                    || userMessage.hasSingleText()) {
                return delegate.augment(request);
            }
            var texts = userMessage.contents().stream()
                    .filter(TextContent.class::isInstance).toList();
            if (texts.size() != 1) {
                // Not a message the provider built; the augmentor decides
                return delegate.augment(request);
            }
            var textOnly = userMessage.toBuilder().contents(texts).build();
            var result = delegate.augment(
                    new AugmentationRequest(textOnly, request.metadata()));
            if (!(result.chatMessage() instanceof UserMessage augmented)) {
                return result;
            }
            var contents = new ArrayList<>(augmented.contents());
            userMessage.contents().stream()
                    .filter(content -> !(content instanceof TextContent))
                    .forEach(contents::add);
            return AugmentationResult.builder()
                    .chatMessage(
                            augmented.toBuilder().contents(contents).build())
                    .contents(result.contents()).build();
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
     * the limits, and the metadata observed so far. Shared with the hooks the
     * provider registers on the service through the invocation parameters and
     * the provider's current-turn reference.
     */
    private final class Turn {
        private final LLMRequest request;
        private final FluxSink<String> sink;
        private final ToolCallLimits toolCallLimits;
        private final AtomicBoolean ended = new AtomicBoolean(false);
        private FinishReason lastFinishReason;
        private boolean lastResponseHadFinishReason;
        private TokenUsage accumulatedUsage;
        private ToolCallLimitExceededException limitBreach;

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
         * Wraps a tool executor so that the state of the turn is checked before
         * the tool runs.
         */
        ToolExecutor guard(ToolExecutor executor) {
            return (toolRequest, memoryId) -> {
                beforeTool();
                return executor.execute(toolRequest, memoryId);
            };
        }

        /**
         * Ends the turn instead of running a tool when the subscriber has
         * cancelled or a tool call of the round exceeded a limit. The round was
         * screened when the model's response arrived, so the first tool of a
         * refused round is the one that ends the turn.
         */
        void beforeTool() {
            if (isCancelled()) {
                LOGGER.debug("The turn was cancelled, skipping the tool "
                        + "calls the model requested");
                throw new TurnCancelledException();
            }
            if (limitBreach != null) {
                throw limitBreach;
            }
        }

        /**
         * Records one model response: its metadata, and whether the tool calls
         * it requests stay within the limits of the turn.
         */
        void observeResponse(ChatResponse response) {
            observeMetadata(response);
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

        void complete(ChatResponse finalResponse) {
            if (!ended.compareAndSet(false, true)) {
                return;
            }
            warnOnMissingFinishReason();
            currentTurn.compareAndSet(this, null);
            sink.complete();
        }

        void complete(Result<String> result) {
            if (!ended.compareAndSet(false, true)) {
                return;
            }
            warnOnMissingFinishReason();
            currentTurn.compareAndSet(this, null);
            var text = result.content();
            if (text != null && !text.isEmpty()) {
                sink.next(text);
            }
            sink.complete();
        }

        void fail(Throwable error) {
            if (!ended.compareAndSet(false, true)) {
                return;
            }
            currentTurn.compareAndSet(this, null);
            // The traffic of a failed turn is discarded with it, so the next
            // turn continues from the chat memory as the last completed turn
            // left it.
            if (getChatMemory() instanceof TurnScopedChatMemory memory) {
                memory.endTurn();
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
         * reason. Only that round trip counts: a reason reported by an earlier
         * round trip describes why that round trip stopped, typically
         * {@code TOOL_EXECUTION}, and says nothing about how the turn ended. A
         * missing finish reason does not prove the model reported nothing:
         * LangChain4j also leaves the reason unset when its integration does
         * not recognize the value the model sent.
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

    /**
     * Builds a {@link LangChain4JLLMProvider} on a chat model, with the system
     * prompt and the LangChain4j features the application wants on the AI
     * service behind the provider.
     *
     * <pre>
     * var provider = LangChain4JLLMProvider.builder(streamingChatModel)
     *         .systemPrompt("You are a support assistant.")
     *         .customize(service -&gt; service.contentRetriever(retriever)
     *                 .toolProvider(mcpToolProvider)
     *                 .inputGuardrails(new PromptInjectionGuardrail()))
     *         .build();
     * </pre>
     *
     * @since 25.4
     */
    public static final class Builder {
        private final ChatModel chatModel;
        private final StreamingChatModel streamingChatModel;
        private String systemPrompt;
        private Consumer<AiServices<ChatService>> customizer;

        private Builder(ChatModel chatModel,
                StreamingChatModel streamingChatModel) {
            this.chatModel = chatModel;
            this.streamingChatModel = streamingChatModel;
        }

        /**
         * Sets the system prompt the model sees on every turn. A system prompt
         * carried by the request of a turn takes precedence, and when neither
         * is given the service's own configuration applies, for example a
         * {@code systemMessage(...)} set in {@link #customize}. Unlike that
         * one, the prompts given here or on a request are sent as they are, not
         * rendered as LangChain4j prompt templates, so they may contain braces.
         *
         * @param systemPrompt
         *            the system prompt, or {@code null} for none
         * @return this builder
         */
        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        /**
         * Customizes the AI service the provider is built on. The customizer
         * receives LangChain4j's {@link AiServices} builder with the model
         * already set, and may configure anything the builder offers: retrieval
         * augmentation, tool providers such as MCP, tool objects, guardrails,
         * moderation, listeners, model request transformation, and so on.
         * Customizers run in the order they were added.
         * <p>
         * The provider completes the service after the customizers ran, and
         * keeps what they configured: it adds its own chat memory only when
         * none was configured, adds the tool provider that supplies the tools
         * of each turn next to any tool providers of the customizer, composes
         * its system prompt, its cancellation check and its tool error handling
         * with the request transformer and the error handler configured,
         * registers a listener for the response metadata, and wraps the
         * retrieval augmentor so that retrieval also works on a turn with
         * attachments, which LangChain4j's default content injector would
         * otherwise reject. Before the customizers run it lifts LangChain4j's
         * round trip limit, as the provider bounds the tool-calling loop with
         * its own tool call limits, and makes a tool the model made up an error
         * result for the model rather than a failure of the turn; a customizer
         * may override both.
         *
         * @param customizer
         *            the customizer, not {@code null}
         * @return this builder
         * @throws NullPointerException
         *             if customizer is {@code null}
         */
        public Builder customize(Consumer<AiServices<ChatService>> customizer) {
            Objects.requireNonNull(customizer, "Customizer must not be null");
            this.customizer = this.customizer == null ? customizer
                    : this.customizer.andThen(customizer);
            return this;
        }

        /**
         * Builds the provider.
         *
         * @return the provider
         */
        public LangChain4JLLMProvider build() {
            return new LangChain4JLLMProvider(this);
        }
    }
}
