/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.ai.provider.springai;

import com.vaadin.flow.component.ai.provider.LLMProvider;
import reactor.core.publisher.Flux;

/**
 * Spring AI implementation of LLMProvider.
 * <p>
 * This provider integrates with Spring AI's chat models to provide LLM
 * capabilities. It handles conversation memory internally using Spring AI's
 * ChatMemory and supports tool calling, RAG, and multimodal inputs.
 * </p>
 * <p>
 * Note: This class requires Spring AI dependencies to be present on the
 * classpath. The actual implementation is currently commented out to avoid
 * mandatory dependencies. Uncomment and customize as needed for your project.
 * </p>
 * <p>
 * Example usage (when uncommented):
 * </p>
 *
 * <pre>
 * ChatModel chatModel = ... // Obtain from Spring context
 * ChatMemory chatMemory = ... // Obtain from Spring context
 * LLMProvider provider = new SpringAiProvider(chatModel, chatMemory);
 * provider.setSystemPrompt("You are a helpful assistant.");
 *
 * LLMRequest request = LLMRequest.of("conversationId1",
 *         "Hello, how are you?");
 * Flux&lt;String&gt; response = provider.stream(request);
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class SpringAiProvider implements LLMProvider {

    /*
     * Uncomment the following implementation when Spring AI dependencies are
     * available:
     *
     * private final ChatModel chatModel; private final ChatMemory chatMemory;
     * private VectorStore vectorStore; private String defaultSystemPrompt;
     *
     * public SpringAiProvider(ChatModel chatModel, ChatMemory chatMemory) {
     * this.chatModel = chatModel; this.chatMemory = chatMemory; }
     *
     * public void setVectorStore(VectorStore vectorStore) { this.vectorStore =
     * vectorStore; }
     *
     * @Override public void setSystemPrompt(String systemPrompt) {
     * this.defaultSystemPrompt = systemPrompt; }
     *
     * @Override public Flux<String> stream(LLMRequest request) { var
     * processedAttachments = processAttachments(request.attachments());
     *
     * var options = chatModel.getDefaultOptions(); if (request.modelName() !=
     * null) { try { var method = options.getClass().getMethod("setModel",
     * String.class); method.setAccessible(true); method.invoke(options,
     * request.modelName()); } catch (Exception e) { // Handle reflection error
     * } }
     *
     * var chatClient = buildChatClient();
     *
     * var systemPrompt = request.systemPrompt() != null ?
     * request.systemPrompt() : defaultSystemPrompt; var tools =
     * request.tools() != null ? request.tools() : new Object[0];
     *
     * var promptSpec = chatClient.prompt().options(options);
     *
     * if (systemPrompt != null && !systemPrompt.isEmpty()) {
     * promptSpec.system(systemPrompt); }
     *
     * promptSpec.tools(tools) .user(u -> {
     * u.text(request.userMessage()+processedAttachments.documentContent());
     * u.media(processedAttachments.mediaList().toArray(Media[]::new)); })
     * .advisors(a -> a.param(ChatMemory.CONVERSATION_ID,
     * request.conversationId()));
     *
     * return promptSpec.stream().content(); }
     *
     * private ChatClient buildChatClient() { var builder =
     * ChatClient.builder(chatModel)
     * .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build());
     *
     * if (vectorStore != null) { builder.defaultAdvisors(
     * RetrievalAugmentationAdvisor.builder() .queryTransformers(
     * RewriteQueryTransformer.builder()
     * .chatClientBuilder(ChatClient.builder(chatModel).build().mutate())
     * .build()) .queryAugmenter(
     * ContextualQueryAugmenter.builder().allowEmptyContext(true).build())
     * .documentRetriever( VectorStoreDocumentRetriever.builder()
     * .similarityThreshold(0.50) .vectorStore(vectorStore) .build())
     * .build()); }
     *
     * return builder.build(); }
     *
     * private ProcessedAttachments processAttachments(List<Attachment>
     * attachments) { var documentList = attachments.stream() .filter(attachment
     * -> attachment.contentType().contains("text") ||
     * attachment.contentType().contains("pdf")) .toList();
     *
     * var documentBuilder = new StringBuilder("\n");
     * documentList.forEach(attachment -> { var data = new
     * ByteArrayResource(attachment.data()); var documents = new
     * TikaDocumentReader(data).read(); var content = String.join("\n",
     * documents.stream().map(Document::getText).toList());
     * documentBuilder.append( String.format("<attachment filename=\"%s\">\n%s\n</attachment>\n",
     * attachment.fileName(), content)); });
     *
     * var mediaList = attachments.stream() .filter(attachment ->
     * attachment.contentType().contains("image")) .map(attachment -> new
     * Media( MimeType.valueOf(attachment.contentType()), new
     * ByteArrayResource(attachment.data()))) .toList();
     *
     * return new ProcessedAttachments(documentBuilder.toString(), mediaList); }
     *
     * private record ProcessedAttachments(String documentContent, List<Media>
     * mediaList) {}
     */

    @Override
    public Flux<String> stream(LLMRequest request) {
        throw new UnsupportedOperationException(
                "SpringAiProvider is not yet implemented. "
                        + "Please uncomment the implementation in SpringAiProvider.java "
                        + "and add Spring AI dependencies to your project.");
    }
}
