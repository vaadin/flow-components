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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClient.DefaultChatClientRequestSpec;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.MemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.common.AiAttachment;
import com.vaadin.flow.component.ai.common.AttachmentContentType;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.internal.JacksonUtils;

import reactor.core.publisher.Flux;
import tools.jackson.databind.JsonNode;

/**
 * Spring AI implementation of {@link LlmProvider}.
 * <p>
 * Supports both streaming and non-streaming Spring AI models. Tool calling is
 * supported through Spring AI's {@link Tool} annotation.
 * </p>
 * <p>
 * <b>Streaming vs. non-streaming:</b> Streaming is enabled by default. To
 * disable it, call {@link #setStreaming(boolean) setStreaming(false)}.
 * Streaming mode pushes partial responses to the UI as they arrive, which
 * requires automatic server push or polling to deliver them. Annotate your UI
 * class or application shell with {@code @Push}, or enable polling with
 * {@code UI.setPollInterval()}, before using streaming mode. A warning is
 * logged at runtime when neither is active.
 * </p>
 * <p>
 * <b>Blocking the request thread:</b> in non-streaming mode the LLM call blocks
 * the thread that subscribes to the response, which is the UI thread for a
 * prompt triggered from the browser. Call
 * {@link #setBackgroundExecution(boolean) setBackgroundExecution(true)} to run
 * the call on a background thread instead, so the request completes and the
 * user's message renders while the LLM works.
 * </p>
 * <p>
 * <b>Tool call limits:</b> Spring AI runs the tool-calling loop itself and
 * bounds it: once the model has requested more than {@code 40} calls to any one
 * tool, or more than {@code 150} tool calls in total, within a turn, Spring AI
 * stops the loop and replies with its own message about the exceeded limit. The
 * provider passes that reply on as the answer, so the turn completes like any
 * other. The finish reason {@code toolCallLimitExceeded} in the
 * {@link ResponseMetadata response metadata} tells you that a turn ended this
 * way. The limits belong to the {@code ToolCallingAdvisor} of the
 * {@link ChatClient}: a provider created from a {@link ChatModel} builds its
 * own client and keeps Spring AI's defaults. To change them, build the client
 * yourself, passing {@code ChatClient.builder} a
 * {@code ToolCallingAdvisor.Builder} that carries a
 * {@code DefaultToolCallingManager} with your limits, and create the provider
 * from that client. Its {@code maxCallsPerTool} and {@code maxTotalToolCalls}
 * set a limit, {@code unlimitedCallsPerTool()} and
 * {@code unlimitedTotalToolCalls()} remove one. In a Spring Boot application
 * the {@code spring.ai.tools.limits} properties configure the same limits on
 * the auto-configured {@code ChatClient.Builder}, so passing that client to
 * {@link #SpringAiLlmProvider(ChatClient)} needs no builder code.
 * </p>
 * <p>
 * The provider maintains its own chat memory, and
 * {@link #setHistory(List, Map)} restores a saved conversation into it. To
 * share conversation history across components, reuse the same provider
 * instance. When the client given to {@link #SpringAiLlmProvider(ChatClient)}
 * already has a memory advisor, or the provider cannot inspect that client, the
 * application owns the chat memory instead: giving the LLM its context is up to
 * the application and {@link #setHistory(List, Map)} does nothing. Restoring a
 * conversation through {@code AiOrchestrator.Builder.withHistory(List, Map)}
 * still matters on that path: the message list the user sees and the
 * orchestrator's own conversation history are restored by the orchestrator, not
 * by the provider.
 * </p>
 * <p>
 * <b>Note:</b> SpringAiLlmProvider is not serializable. If your application
 * uses session persistence, you will need to create a new provider instance
 * after session restore.
 * </p>
 *
 * @author Vaadin Ltd
 * @since 25.3
 */
public class SpringAiLlmProvider implements LlmProvider {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SpringAiLlmProvider.class);

    private static final int MAX_MESSAGES = 30;
    private static final String DEFAULT_CONVERSATION_ID = "default";

    private final transient ChatClient chatClient;
    private final transient MessageWindowChatMemory chatMemory;
    private final MessageChatMemoryAdvisor memoryAdvisor;
    private final boolean hasManagedMemory;
    private final boolean isMissingConversationId;
    private boolean isStreaming = true;
    private final BackgroundExecution backgroundExecution = new BackgroundExecution(
            SpringAiLlmProvider.class);

    /**
     * Constructor with a chat model.
     *
     * @param chatModel
     *            the chat model, not {@code null}
     * @throws NullPointerException
     *             if chatModel is {@code null}
     */
    public SpringAiLlmProvider(ChatModel chatModel) {
        this(ChatClient.builder(
                Objects.requireNonNull(chatModel, "ChatModel must not be null"))
                .build());
    }

    /**
     * Constructor with a chat client. The provider creates the request of every
     * turn with this client, so the client's default advisors, tools and
     * options apply to every turn. The client's default system text applies
     * only to a request without a system prompt; a system prompt in the request
     * replaces it.
     * <p>
     * Unless the client has a {@link MemoryAdvisor} of its own, the provider
     * adds its own chat memory to each request, keeping the last 30 messages,
     * and {@link #setHistory(List, Map)} restores a saved conversation into it.
     * Its memory advisor has Spring AI's default order for chat memory,
     * {@link Advisor#DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER}: a client advisor
     * with a later order sees the earlier messages of the conversation in each
     * request, while one with an earlier order sees only the new request, and a
     * request it rejects never reaches the memory. A
     * {@link ChatMemory#CONVERSATION_ID} advisor parameter in the requests the
     * client creates stays in them and names the conversation in that memory:
     *
     * <pre>
     * var chatClient = ChatClient.builder(chatModel)
     *         .defaultTools(mcpToolCallbacks).build();
     * var provider = new SpringAiLlmProvider(chatClient);
     * </pre>
     * <p>
     * The provider adds no memory when the client has a memory advisor, for
     * example a {@link MessageChatMemoryAdvisor}. You own that memory, so
     * {@link #setHistory(List, Map)} does nothing on a provider created this
     * way. Write a conversation loaded from external storage into the
     * {@link ChatMemory} before you pass the client here, and set a default
     * {@link ChatMemory#CONVERSATION_ID} advisor parameter on the client. The
     * same applies to a client whose requests the provider cannot inspect, that
     * is a custom {@link ChatClient} implementation that does not create its
     * requests with a client built by {@link ChatClient#builder(ChatModel)}.
     * Passing the conversation to
     * {@code AiOrchestrator.Builder.withHistory(List, Map)} restores the
     * message list and the orchestrator's own history snapshot either way.
     *
     * @param chatClient
     *            the chat client, not {@code null}
     * @throws NullPointerException
     *             if chatClient is {@code null}
     */
    public SpringAiLlmProvider(ChatClient chatClient) {
        Objects.requireNonNull(chatClient, "ChatClient must not be null");
        this.chatClient = chatClient;
        var requestSpec = inspectableRequestSpec(chatClient);
        if (requestSpec == null || requestSpec.getAdvisors().stream()
                .anyMatch(MemoryAdvisor.class::isInstance)) {
            // The application keeps the conversation itself
            chatMemory = null;
            memoryAdvisor = null;
            hasManagedMemory = false;
            isMissingConversationId = requestSpec != null && requestSpec
                    .getAdvisorParams().get(ChatMemory.CONVERSATION_ID) == null;
        } else {
            chatMemory = MessageWindowChatMemory.builder()
                    .maxMessages(MAX_MESSAGES).build();
            // Added to each request instead of to a copy of the client, so
            // that the client given here creates the request of every turn
            memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                    .build();
            hasManagedMemory = true;
            isMissingConversationId = false;
        }
    }

    @Override
    public Flux<String> stream(LlmRequest request) {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(request.userMessage(),
                "User message must not be null");
        if (isStreaming) {
            return backgroundExecution
                    .applyToStreamingResponse(executeStreamingChat(request));
        }
        return backgroundExecution
                .applyToBlockingResponse(executeNonStreamingChat(request));
    }

    /**
     * Gets whether streaming mode is used.
     *
     * @return {@code true} if streaming mode is used, {@code false} otherwise
     */
    public boolean isStreaming() {
        return isStreaming;
    }

    /**
     * Sets whether to use streaming mode. The default is {@code true}.
     *
     * @param streaming
     *            {@code true} to use streaming mode, {@code false} for
     *            non-streaming.
     */
    public void setStreaming(boolean streaming) {
        this.isStreaming = streaming;
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
     * no effect in streaming mode, where the response already arrives on the
     * LLM client's own threads.
     * <p>
     * A non-streaming call blocks for the whole turn, every tool call included.
     * On the UI thread that means holding the session lock until the turn ends,
     * so nothing the turn produces reaches the browser and the application
     * appears frozen. Set this to {@code true} to run the call on a background
     * thread instead: the request completes immediately, the user's message and
     * the typing indicator render, and the response is added when it arrives.
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
     * {@link com.vaadin.flow.component.ai.orchestrator.AiController#onRequest},
     * which still runs on the UI thread. This is the same requirement streaming
     * mode already has.</li>
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
     * {@inheritDoc}
     * <p>
     * Restores the conversation into the provider's own chat memory. Does
     * nothing when the provider was created with a
     * {@link #SpringAiLlmProvider(ChatClient) ChatClient} that has a memory
     * advisor of its own or cannot be inspected, because the application owns
     * the chat memory in that case and is expected to have populated it before
     * passing the client in. A warning is logged when that client's memory
     * advisor has no default conversation id to read. Whether the memory
     * actually holds the conversation is not visible to the provider, so a
     * client configured correctly but never loaded is indistinguishable from
     * one that was. Doing nothing here does not reduce what the caller
     * restores: an orchestrator rebuilds the message list and its own
     * conversation history itself.
     */
    @Override
    public void setHistory(List<ChatMessage> history,
            Map<String, List<AiAttachment>> attachmentsByMessageId) {
        Objects.requireNonNull(history, "History must not be null");
        Objects.requireNonNull(attachmentsByMessageId,
                "Attachments map must not be null");
        if (!hasManagedMemory) {
            warnIfClientMemoryUnusable();
            return;
        }
        // The memory advisor reads the conversation by this key
        var conversationKey = conversationIdOf(chatClient.prompt()).toString();
        chatMemory.clear(conversationKey);
        var messages = history.stream().map(message -> {
            var attachments = message.messageId() != null
                    ? attachmentsByMessageId.getOrDefault(message.messageId(),
                            Collections.emptyList())
                    : Collections.<AiAttachment> emptyList();
            return toVendorMessage(message, attachments);
        }).toList();
        chatMemory.add(conversationKey, messages);
    }

    /**
     * Reports a client that cannot hold the conversation the application asked
     * to restore. The provider cannot populate an application-owned chat
     * memory, but it can tell that a client whose memory advisor has no
     * conversation to read is very likely to never see the restored messages,
     * as every prompt fails inside the standard advisor. That is worth a
     * warning at the point where the application asks for a restore that cannot
     * happen. It is not certain, since a client can carry the conversation in
     * ways this check cannot see, so the message says what was observed rather
     * than promising the outcome.
     */
    private void warnIfClientMemoryUnusable() {
        if (isMissingConversationId) {
            LOGGER.warn("History restoration was requested, but the "
                    + "ChatClient given to this provider has no default "
                    + "{} advisor parameter. Unless something sets it per "
                    + "request, its memory advisor has no conversation to "
                    + "read and every prompt will fail. Set the parameter on "
                    + "the client, for example with "
                    + "ChatClient.Builder.defaultAdvisors(advisors -> "
                    + "advisors.param(ChatMemory.CONVERSATION_ID, id)).",
                    ChatMemory.CONVERSATION_ID);
            return;
        }
        LOGGER.debug("Skipping history restoration: the provider was created "
                + "with a ChatClient whose chat memory the application owns. "
                + "Populate that memory before passing the client to the "
                + "provider.");
    }

    /**
     * Returns the client's request spec when it is Spring AI's own
     * implementation, or {@code null} -- after logging why -- when the client
     * cannot be read. Only requests created by a client built by
     * {@link ChatClient#builder(ChatModel)} can be inspected. Any other client
     * gets no memory from the provider, since a custom implementation may carry
     * the conversation in its own way, and a client that rejects a bare
     * {@link ChatClient#prompt()} must not fail the constructor.
     *
     * @param chatClient
     *            the client to inspect
     * @return the request spec to inspect, or {@code null} if there is none to
     *         read
     */
    private static DefaultChatClientRequestSpec inspectableRequestSpec(
            ChatClient chatClient) {
        try {
            if (chatClient
                    .prompt() instanceof DefaultChatClientRequestSpec spec) {
                return spec;
            }
        } catch (RuntimeException e) {
            LOGGER.debug("The provider leaves the chat memory to the "
                    + "application: the ChatClient given to it did not "
                    + "accept a bare prompt() for inspection.", e);
            return null;
        }
        LOGGER.debug("The provider leaves the chat memory to the "
                + "application: the configuration of the ChatClient given "
                + "to it cannot be inspected.");
        return null;
    }

    /**
     * Returns the conversation of the provider's own memory for a request: the
     * {@link ChatMemory#CONVERSATION_ID} advisor parameter the client put into
     * it, or {@link #DEFAULT_CONVERSATION_ID} when there is none. Read from
     * each request rather than once, since a client may name another
     * conversation in each request it creates. The client's own value is put
     * back as it is, so its other advisors and its observations see it
     * unchanged.
     *
     * @param requestSpec
     *            the request created by the client
     * @return the conversation id, not {@code null}
     */
    private static Object conversationIdOf(
            ChatClient.ChatClientRequestSpec requestSpec) {
        var conversationId = requestSpec instanceof DefaultChatClientRequestSpec spec
                ? spec.getAdvisorParams().get(ChatMemory.CONVERSATION_ID)
                : null;
        return conversationId != null ? conversationId
                : DEFAULT_CONVERSATION_ID;
    }

    private static org.springframework.ai.chat.messages.Message toVendorMessage(
            ChatMessage message, List<AiAttachment> attachments) {
        if (ChatMessage.Role.ASSISTANT.equals(message.role())) {
            return new AssistantMessage(message.content());
        }
        var mediaList = attachments.stream()
                .map(SpringAiLlmProvider::getAttachmentMedia)
                .flatMap(Optional::stream).toList();
        if (mediaList.isEmpty()) {
            return new UserMessage(message.content());
        }
        return UserMessage.builder().text(message.content()).media(mediaList)
                .build();
    }

    private Flux<String> executeStreamingChat(LlmRequest request) {
        try {
            var collector = new ResponseMetadataCollector(
                    request.metadataSink());
            var chatResponses = getPromptSpec(request).stream().chatResponse()
                    .doOnNext(collector::observe);
            return warnOnMissingFinishReason(chatResponses)
                    .map(SpringAiLlmProvider::getAssistantText)
                    .filter(text -> !text.isEmpty());
        } catch (Exception e) {
            return Flux.error(e);
        }
    }

    /**
     * Passes the stream through unchanged, logging a warning on completion if
     * no chunk in the stream ever represented a terminal model state.
     * <p>
     * A streaming chunk is terminal when it carries a {@code finish_reason} and
     * the response has no pending tool calls. Pending tool calls mean a
     * follow-up round-trip is expected, and its chunks would be concatenated to
     * this same Flux, so a chunk with tool calls is intermediate even when it
     * carries a {@code finish_reason}. The check is sticky - once any terminal
     * chunk is observed the gate stays open - so that trailing metadata-only
     * chunks emitted by some providers after the terminal chunk cannot flip it
     * back. A stream that completes without ever seeing a terminal chunk -
     * whether because no chunk carried a {@code finish_reason} at all, or
     * because the only terminal-looking chunks still had tool calls pending -
     * may indicate abnormal termination.
     */
    private static Flux<ChatResponse> warnOnMissingFinishReason(
            Flux<ChatResponse> source) {
        var terminalSeen = new AtomicBoolean(false);
        return source.doOnNext(response -> {
            if (isTerminalChunk(response)) {
                terminalSeen.set(true);
            }
        }).concatWith(Flux.defer(() -> {
            if (!terminalSeen.get()) {
                LOGGER.warn("LLM stream ended without observing a terminal "
                        + "chunk (one carrying finish_reason and "
                        + "no pending tool calls). This may "
                        + "indicate a silent abnormal termination "
                        + "such as an upstream error or transport "
                        + "closure; if responses appear truncated "
                        + "this warning is the signal.");
            }
            return Flux.empty();
        }));
    }

    private static boolean isTerminalChunk(ChatResponse response) {
        var result = response.getResult();
        if (result == null) {
            return false;
        }
        var reason = result.getMetadata().getFinishReason();
        if (reason == null || reason.isEmpty()) {
            return false;
        }
        return !response.hasToolCalls();
    }

    private static String getAssistantText(ChatResponse response) {
        var result = response.getResult();
        if (result == null) {
            return "";
        }
        var text = result.getOutput().getText();
        return text != null ? text : "";
    }

    @SuppressWarnings("deprecation")
    private ChatClient.ChatClientRequestSpec getPromptSpec(LlmRequest request) {
        var promptSpec = chatClient.prompt();
        if (hasManagedMemory) {
            var conversationId = conversationIdOf(promptSpec);
            promptSpec = promptSpec.advisors(a -> a.advisors(memoryAdvisor)
                    .param(ChatMemory.CONVERSATION_ID, conversationId));
        }
        promptSpec = promptSpec.user(userSpec -> {
            userSpec.text(LlmProviderHelpers.withSessionContext(
                    request.userMessage(), request.sessionContext()));
            var media = buildMedia(request);
            if (media.length != 0) {
                userSpec.media(media);
            }
        });
        if (request.systemPrompt() != null
                && !request.systemPrompt().trim().isEmpty()) {
            promptSpec = promptSpec.system(request.systemPrompt().trim());
        }
        var tools = request.tools();
        if (tools != null && tools.length > 0) {
            promptSpec = promptSpec.tools(tools);
        }
        var explicitTools = request.explicitTools();
        if (explicitTools != null && !explicitTools.isEmpty()) {
            var callbacks = explicitTools.stream()
                    .map(SpringAiLlmProvider::toToolCallback)
                    .toArray(ToolCallback[]::new);
            promptSpec = promptSpec.toolCallbacks(callbacks);
        }
        return promptSpec;
    }

    private Flux<String> executeNonStreamingChat(LlmRequest request) {
        return Flux.create(sink -> {
            try {
                var promptSpec = getPromptSpec(request);
                var response = promptSpec.call().chatResponse();
                if (response == null) {
                    LOGGER.warn("LLM call returned no response at all, which "
                            + "may indicate an upstream error swallowed by "
                            + "the client.");
                } else {
                    new ResponseMetadataCollector(request.metadataSink())
                            .observe(response);
                    warnOnAbnormalCompletion(response);
                    var text = getAssistantText(response);
                    if (!text.isEmpty()) {
                        sink.next(text);
                    }
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    /**
     * Warns when a non-streaming turn did not end in a state a completed turn
     * can end in. Spring AI runs the tool-calling loop inside its own call and
     * hands back only the final response, so tool calls still pending on it
     * mean the loop stopped before the model produced its answer. The streaming
     * path has the same checks built into
     * {@link #warnOnMissingFinishReason(Flux)}.
     */
    private static void warnOnAbnormalCompletion(ChatResponse response) {
        var finishReason = ResponseMetadataCollector.getFinishReason(response);
        if (response.hasToolCalls()) {
            LOGGER.warn("LLM call ended with tool calls still pending "
                    + "(finish reason: {}). The tool-calling loop stopped "
                    + "before the model produced its answer, so the response "
                    + "is incomplete.", finishReason);
        } else if (finishReason == null) {
            LOGGER.warn("LLM call ended without a finish reason. This may "
                    + "indicate a silent abnormal termination such as an "
                    + "upstream error; if the response appears truncated "
                    + "this warning is the signal.");
        }
    }

    /**
     * Collects response metadata across the chunks of a turn and passes each
     * new state of knowledge to the metadata sink right away, so that a turn
     * that fails or times out midway has still reported what was observed
     * before the failure. The last reported finish reason and token usage win:
     * the terminal chunk carries the reason that ended the turn, and frameworks
     * that report usage do so cumulatively on the final chunk that carries it.
     */
    private static class ResponseMetadataCollector {

        private final Consumer<ResponseMetadata> metadataSink;
        private String finishReason;
        private ResponseMetadata.TokenUsage tokenUsage;

        ResponseMetadataCollector(Consumer<ResponseMetadata> metadataSink) {
            this.metadataSink = metadataSink;
        }

        void observe(ChatResponse response) {
            var reason = getFinishReason(response);
            var usage = getTokenUsage(response);
            if (reason == null && usage == null) {
                // Nothing new on this chunk, nothing to re-publish.
                return;
            }
            if (reason != null) {
                finishReason = reason;
            }
            if (usage != null) {
                tokenUsage = usage;
            }
            metadataSink.accept(new ResponseMetadata(finishReason, tokenUsage));
        }

        private static String getFinishReason(ChatResponse response) {
            var result = response.getResult();
            if (result == null) {
                return null;
            }
            var reason = result.getMetadata().getFinishReason();
            return reason == null || reason.isBlank() ? null : reason;
        }

        private static ResponseMetadata.TokenUsage getTokenUsage(
                ChatResponse response) {
            // Read through an Optional rather than dereferencing: a model
            // reports either no usage object at all or one that leaves the
            // counts it does not know at zero, and both mean the same thing
            // here. Spring AI's own models always attach a usage object, but
            // an application's ChatModel is free not to.
            var usage = Optional.ofNullable(response.getMetadata().getUsage());
            var input = usage.map(Usage::getPromptTokens)
                    .filter(count -> count > 0).orElse(null);
            var output = usage.map(Usage::getCompletionTokens)
                    .filter(count -> count > 0).orElse(null);
            var total = usage.map(Usage::getTotalTokens)
                    .filter(count -> count > 0).orElse(null);
            if (total == null && input != null && output != null) {
                // A backend that reports the components but no total still
                // reported the usage; derive rather than discard it.
                total = input + output;
            }
            if (input == null && output == null && total == null) {
                return null;
            }
            return new ResponseMetadata.TokenUsage(input, output, total);
        }
    }

    private Media[] buildMedia(LlmRequest request) {
        var attachments = request.attachments();
        if (attachments == null) {
            return new Media[0];
        }
        return attachments.stream().map(SpringAiLlmProvider::getAttachmentMedia)
                .filter(Optional::isPresent).map(Optional::get)
                .toArray(Media[]::new);
    }

    private static Optional<Media> getAttachmentMedia(AiAttachment attachment) {
        LlmProviderHelpers.validateAttachment(attachment);
        var contentType = AttachmentContentType
                .fromMimeType(attachment.mimeType());
        return switch (contentType) {
        case TEXT -> Optional.of(getTextMedia(attachment));
        case IMAGE, PDF, AUDIO, VIDEO -> Optional.of(getMedia(attachment));
        case UNSUPPORTED -> Optional.empty();
        };
    }

    private static Media getMedia(AiAttachment attachment) {
        var mimeType = MimeType.valueOf(attachment.mimeType());
        var resource = new ByteArrayResource(attachment.data());
        return Media.builder().mimeType(mimeType).data(resource).build();
    }

    private static Media getTextMedia(AiAttachment attachment) {
        var textContent = LlmProviderHelpers.decodeAsUtf8(attachment.data(),
                attachment.name(), false);
        var formattedText = LlmProviderHelpers
                .formatTextAttachment(attachment.name(), textContent);
        return Media.builder().mimeType(MimeType.valueOf("text/plain"))
                .data(formattedText).build();
    }

    private static ToolCallback toToolCallback(LlmProvider.ToolSpec tool) {
        return new ToolCallback() {
            @Override
            public ToolDefinition getToolDefinition() {
                // A tool without a declared schema breaks some LLM APIs —
                // see LlmProviderHelpers.NO_PARAMETERS_SCHEMA.
                return DefaultToolDefinition.builder().name(tool.getName())
                        .description(tool.getDescription())
                        .inputSchema(LlmProviderHelpers.hasParameters(tool)
                                ? tool.getParametersSchema()
                                : LlmProviderHelpers.NO_PARAMETERS_SCHEMA)
                        .build();
            }

            @Override
            public String call(String arguments) {
                JsonNode parsed;
                if (!LlmProviderHelpers.hasParameters(tool)) {
                    // The model saw the placeholder schema; the tool declared
                    // no parameters, so it receives none. Decided before
                    // parsing, so the tool stays callable even on the
                    // malformed arguments — an empty string, say — that the
                    // placeholder schema exists to work around.
                    parsed = JacksonUtils.createObjectNode();
                } else {
                    try {
                        parsed = LlmProviderHelpers
                                .parseToolArguments(arguments);
                    } catch (Exception e) {
                        // The bad arguments came from the model itself, so
                        // the message is safe to relay and lets the model
                        // repair its next attempt.
                        LOGGER.warn(
                                "Tool '{}' received malformed JSON arguments",
                                tool.getName(), e);
                        return "Error executing tool: invalid JSON arguments: "
                                + e.getMessage();
                    }
                }
                try {
                    return tool.execute(parsed);
                } catch (ToolException e) {
                    LOGGER.warn("Tool '{}' failed: {}", tool.getName(),
                            e.getMessage(), e);
                    return "Error executing tool: " + e.getMessage();
                } catch (Exception e) {
                    LOGGER.error("Tool '{}' failed", tool.getName(), e);
                    return "Error executing tool.";
                }
            }
        };
    }

}
