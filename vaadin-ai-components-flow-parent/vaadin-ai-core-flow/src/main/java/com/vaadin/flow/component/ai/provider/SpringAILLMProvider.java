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
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.AttachmentContentType;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.internal.JacksonUtils;

import reactor.core.publisher.Flux;
import tools.jackson.databind.JsonNode;

/**
 * Spring AI implementation of {@link LLMProvider}.
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
 * With the {@link #SpringAILLMProvider(ChatModel)} constructor the provider
 * maintains its own chat memory, and {@link #setHistory(List, Map)} restores a
 * saved conversation into it. To share conversation history across components,
 * reuse the same provider instance. With the
 * {@link #SpringAILLMProvider(ChatClient)} constructor the application owns the
 * chat memory, so giving the LLM its context is up to the application and
 * {@link #setHistory(List, Map)} does nothing. Restoring a conversation through
 * {@code AIOrchestrator.Builder.withHistory(List, Map)} still matters on that
 * path: the message list the user sees and the orchestrator's own conversation
 * history are restored by the orchestrator, not by the provider.
 * </p>
 * <p>
 * <b>Note:</b> SpringAILLMProvider is not serializable. If your application
 * uses session persistence, you will need to create a new provider instance
 * after session restore.
 * </p>
 *
 * @author Vaadin Ltd
 * @since 25.1
 */
public class SpringAILLMProvider implements LLMProvider {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SpringAILLMProvider.class);

    private static final int MAX_MESSAGES = 30;
    private static final String CONVERSATION_ID = "default";

    private final transient ChatClient chatClient;
    private final transient MessageWindowChatMemory chatMemory;
    private final boolean hasManagedMemory;
    private boolean isStreaming = true;
    private final BackgroundExecution backgroundExecution = new BackgroundExecution(
            SpringAILLMProvider.class);

    /**
     * Constructor with a chat model.
     *
     * @param chatModel
     *            the chat model, not {@code null}
     * @throws NullPointerException
     *             if chatModel is {@code null}
     */
    public SpringAILLMProvider(ChatModel chatModel) {
        Objects.requireNonNull(chatModel, "ChatModel must not be null");
        chatMemory = MessageWindowChatMemory.builder().maxMessages(MAX_MESSAGES)
                .build();
        chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        hasManagedMemory = true;
    }

    /**
     * Constructor with a chat client. Conversation memory must be configured on
     * the {@link ChatClient} itself, for example with a
     * {@link MessageChatMemoryAdvisor} and a default
     * {@link ChatMemory#CONVERSATION_ID} advisor parameter.
     * <p>
     * The application owns that memory, so {@link #setHistory(List, Map)} does
     * nothing on a provider created this way. A conversation loaded from
     * external storage must be written into the {@link ChatMemory} before the
     * client is passed here. Passing the same conversation to
     * {@code AIOrchestrator.Builder.withHistory(List, Map)} still restores the
     * message list and the orchestrator's own history snapshot.
     *
     * @param chatClient
     *            the chat client, not {@code null}
     * @throws NullPointerException
     *             if chatClient is {@code null}
     */
    public SpringAILLMProvider(ChatClient chatClient) {
        Objects.requireNonNull(chatClient, "ChatClient must not be null");
        this.chatClient = chatClient;
        chatMemory = null;
        hasManagedMemory = false;
    }

    @Override
    public Flux<String> stream(LLMRequest request) {
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
     * @since 25.3
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
     * @since 25.3
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
     * the assistant placeholder render, and the response is added when it
     * arrives.
     * <p>
     * This requires three things from the application:
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
     * {@link com.vaadin.flow.component.ai.orchestrator.AIController#onRequest()},
     * which still runs on the UI thread. This is the same requirement streaming
     * mode already has.</li>
     *
     * <li><b>A gated input.</b> The orchestrator processes one prompt at a
     * time. Without background execution, a message submitted while a turn is
     * running waits for the session lock and is processed when the turn ends;
     * with it, the submit is rejected and dropped with a warning — and a
     * connected input has already cleared its text. Disable the input while a
     * turn is running, for example from
     * {@link com.vaadin.flow.component.ai.orchestrator.AIController#onRequest()}
     * and
     * {@link com.vaadin.flow.component.ai.orchestrator.AIController#onResponse(ResponseListener.ResponseEvent)}.</li>
     * </ul>
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
     * @since 25.3
     */
    public void setBackgroundExecution(boolean backgroundExecution) {
        this.backgroundExecution.setEnabled(backgroundExecution);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Restores the conversation into the provider's own chat memory. Does
     * nothing when the provider was created with the
     * {@link #SpringAILLMProvider(ChatClient) ChatClient} constructor, because
     * the application owns the chat memory in that case and is expected to have
     * populated it before passing the client in. A warning is logged when that
     * client is missing the chat memory configuration the conversation would
     * need. Whether the memory actually holds the conversation is not visible
     * to the provider, so a client configured correctly but never loaded is
     * indistinguishable from one that was. Doing nothing here does not reduce
     * what the caller restores: an orchestrator rebuilds the message list and
     * its own conversation history itself.
     */
    @Override
    public void setHistory(List<ChatMessage> history,
            Map<String, List<AIAttachment>> attachmentsByMessageId) {
        Objects.requireNonNull(history, "History must not be null");
        Objects.requireNonNull(attachmentsByMessageId,
                "Attachments map must not be null");
        if (!hasManagedMemory) {
            warnIfClientMemoryUnusable();
            return;
        }
        chatMemory.clear(CONVERSATION_ID);
        var messages = history.stream().map(message -> {
            var attachments = message.messageId() != null
                    ? attachmentsByMessageId.getOrDefault(message.messageId(),
                            Collections.emptyList())
                    : Collections.<AIAttachment> emptyList();
            return toVendorMessage(message, attachments);
        }).toList();
        chatMemory.add(CONVERSATION_ID, messages);
    }

    /**
     * Reports a client that cannot hold the conversation the application asked
     * to restore. The provider cannot populate an application-owned chat
     * memory, but it can tell that a client carrying no memory advisor, or one
     * whose memory has no conversation to read, is very likely to never see the
     * restored messages -- the first case forgets every turn, the second fails
     * each prompt inside the standard advisor. Both are worth a warning at the
     * point where the application asks for a restore that cannot happen.
     * Neither is certain, since a client can carry the conversation in ways
     * this check cannot see, so both messages say what was observed rather than
     * promising the outcome.
     * <p>
     * Only clients built by {@link ChatClient#builder(ChatModel)} can be
     * inspected. Anything else is left alone, since a custom implementation may
     * carry the conversation in its own way.
     */
    private void warnIfClientMemoryUnusable() {
        var requestSpec = inspectableRequestSpec();
        if (requestSpec == null) {
            // inspectableRequestSpec() logged why it could not be read
            return;
        }
        if (requestSpec.getAdvisors().stream()
                .noneMatch(MemoryAdvisor.class::isInstance)) {
            LOGGER.warn("History restoration was requested, but no chat "
                    + "memory advisor was found on the ChatClient given to "
                    + "this provider. Unless that client keeps the "
                    + "conversation some other way, the LLM will see neither "
                    + "the restored conversation nor the turns that follow. "
                    + "Add for example a MessageChatMemoryAdvisor to the "
                    + "client, and load the restored conversation into its "
                    + "ChatMemory before passing the client to the provider.");
            return;
        }
        if (requestSpec.getAdvisorParams()
                .get(ChatMemory.CONVERSATION_ID) == null) {
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
     * cannot be read. Inspection is only a diagnostic, so a client that rejects
     * a bare {@link ChatClient#prompt()} must not break the restore it is being
     * asked about.
     *
     * @return the request spec to inspect, or {@code null} if there is none to
     *         read
     */
    private DefaultChatClientRequestSpec inspectableRequestSpec() {
        try {
            if (chatClient
                    .prompt() instanceof DefaultChatClientRequestSpec spec) {
                return spec;
            }
        } catch (RuntimeException e) {
            LOGGER.debug("Skipping history restoration: the provider was "
                    + "created with a ChatClient whose chat memory the "
                    + "application owns, and which did not accept a bare "
                    + "prompt() for inspection.", e);
            return null;
        }
        LOGGER.debug("Skipping history restoration: the provider was "
                + "created with a ChatClient whose chat memory the "
                + "application owns, and whose configuration cannot be "
                + "inspected.");
        return null;
    }

    private static org.springframework.ai.chat.messages.Message toVendorMessage(
            ChatMessage message, List<AIAttachment> attachments) {
        if (ChatMessage.Role.ASSISTANT.equals(message.role())) {
            return new AssistantMessage(message.content());
        }
        var mediaList = attachments.stream()
                .map(SpringAILLMProvider::getAttachmentMedia)
                .flatMap(Optional::stream).toList();
        if (mediaList.isEmpty()) {
            return new UserMessage(message.content());
        }
        return UserMessage.builder().text(message.content()).media(mediaList)
                .build();
    }

    private Flux<String> executeStreamingChat(LLMRequest request) {
        try {
            var collector = new ResponseMetadataCollector(
                    request.metadataSink());
            var chatResponses = getPromptSpec(request).stream().chatResponse()
                    .doOnNext(collector::observe);
            return warnOnMissingFinishReason(chatResponses)
                    .map(SpringAILLMProvider::getAssistantText)
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

    private ChatClient.ChatClientRequestSpec getPromptSpec(LLMRequest request) {
        var promptSpec = chatClient.prompt();
        if (hasManagedMemory) {
            promptSpec = promptSpec.advisors(
                    a -> a.param(ChatMemory.CONVERSATION_ID, CONVERSATION_ID));
        }
        promptSpec = promptSpec.user(userSpec -> {
            userSpec.text(request.userMessage());
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
                    .map(SpringAILLMProvider::toToolCallback)
                    .toArray(ToolCallback[]::new);
            promptSpec = promptSpec.toolCallbacks(callbacks);
        }
        return promptSpec;
    }

    private Flux<String> executeNonStreamingChat(LLMRequest request) {
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

    private Media[] buildMedia(LLMRequest request) {
        var attachments = request.attachments();
        if (attachments == null) {
            return new Media[0];
        }
        return attachments.stream().map(SpringAILLMProvider::getAttachmentMedia)
                .filter(Optional::isPresent).map(Optional::get)
                .toArray(Media[]::new);
    }

    private static Optional<Media> getAttachmentMedia(AIAttachment attachment) {
        LLMProviderHelpers.validateAttachment(attachment);
        var contentType = AttachmentContentType
                .fromMimeType(attachment.mimeType());
        return switch (contentType) {
        case TEXT -> Optional.of(getTextMedia(attachment));
        case IMAGE, PDF, AUDIO, VIDEO -> Optional.of(getMedia(attachment));
        case UNSUPPORTED -> Optional.empty();
        };
    }

    private static Media getMedia(AIAttachment attachment) {
        var mimeType = MimeType.valueOf(attachment.mimeType());
        var resource = new ByteArrayResource(attachment.data());
        return Media.builder().mimeType(mimeType).data(resource).build();
    }

    private static Media getTextMedia(AIAttachment attachment) {
        var textContent = LLMProviderHelpers.decodeAsUtf8(attachment.data(),
                attachment.name(), false);
        var formattedText = LLMProviderHelpers
                .formatTextAttachment(attachment.name(), textContent);
        return Media.builder().mimeType(MimeType.valueOf("text/plain"))
                .data(formattedText).build();
    }

    private static ToolCallback toToolCallback(LLMProvider.ToolSpec tool) {
        return new ToolCallback() {
            @Override
            public ToolDefinition getToolDefinition() {
                var schema = tool.getParametersSchema();
                // A tool without a declared schema breaks some LLM APIs —
                // see ToolSpec.NO_PARAMETERS_SCHEMA.
                return DefaultToolDefinition.builder().name(tool.getName())
                        .description(tool.getDescription())
                        .inputSchema(schema != null && !schema.isBlank()
                                ? schema
                                : LLMProvider.ToolSpec.NO_PARAMETERS_SCHEMA)
                        .build();
            }

            @Override
            public String call(String arguments) {
                JsonNode parsed;
                try {
                    parsed = parseArguments(arguments);
                } catch (Exception e) {
                    // The malformed JSON came from the model itself, so
                    // the parser message is safe to relay and lets the
                    // model repair its next attempt.
                    LOGGER.warn("Tool '{}' received malformed JSON arguments",
                            tool.getName(), e);
                    return "Error executing tool: invalid JSON arguments: "
                            + e.getMessage();
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

    private static JsonNode parseArguments(String arguments) {
        if (arguments == null || arguments.isBlank()) {
            return JacksonUtils.createObjectNode();
        }
        return JacksonUtils.readTree(arguments);
    }
}
