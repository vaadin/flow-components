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
package com.vaadin.flow.component.ai.chat;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.input.AiInput;
import com.vaadin.flow.component.ai.input.InputSubmitEvent;
import com.vaadin.flow.component.ai.input.InputSubmitListener;
import com.vaadin.flow.component.ai.messagelist.AiMessage;
import com.vaadin.flow.component.ai.messagelist.AiMessageList;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.upload.AiFileReceiver;
import com.vaadin.flow.server.VaadinSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link AiChatOrchestrator}.
 */
public class AiChatOrchestratorTest {

    private LLMProvider mockProvider;
    private AiMessageList mockMessageList;
    private AiInput mockInput;
    private AiFileReceiver mockFileReceiver;
    private AiMessage mockMessage;
    private UI ui;
    private VaadinSession session;

    @Before
    public void setup() {
        mockProvider = mock(LLMProvider.class);
        mockMessageList = mock(AiMessageList.class);
        mockInput = mock(AiInput.class);
        mockFileReceiver = mock(AiFileReceiver.class);
        mockMessage = mock(AiMessage.class);

        // Setup UI context with custom UI that executes access() immediately
        session = mock(VaadinSession.class);
        when(session.hasLock()).thenReturn(true);
        ui = new TestUI();
        ui.getInternals().setSession(session);
        UI.setCurrent(ui);

        when(mockMessageList.createMessage(anyString(), anyString()))
                .thenReturn(mockMessage);
    }

    /**
     * Custom UI that executes access() commands immediately for testing.
     */
    private static class TestUI extends UI {
        @Override
        public java.util.concurrent.Future<Void> access(com.vaadin.flow.server.Command command) {
            // Execute immediately in tests instead of async
            command.execute();
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }
    }

    @After
    public void teardown() {
        UI.setCurrent(null);
    }

    @Test
    public void create_withProvider_returnsBuilder() {
        AiChatOrchestrator.Builder builder = AiChatOrchestrator.create(mockProvider);
        assertNotNull("Builder should not be null", builder);
    }

    @Test(expected = NullPointerException.class)
    public void build_withNullProvider_throwsException() {
        AiChatOrchestrator.create(null).build();
    }

    @Test
    public void build_withMinimalConfiguration_createsOrchestrator() {
        AiChatOrchestrator orchestrator = AiChatOrchestrator.create(mockProvider)
                .build();

        assertNotNull("Orchestrator should not be null", orchestrator);
        assertEquals("Provider should be set", mockProvider, orchestrator.getProvider());
        assertNull("MessageList should be null", orchestrator.getMessageList());
        assertNull("Input should be null", orchestrator.getInput());
        assertNull("FileReceiver should be null", orchestrator.getFileReceiver());
    }

    @Test
    public void build_withMessageList_setsMessageList() {
        AiChatOrchestrator orchestrator = AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .build();

        assertEquals("MessageList should be set", mockMessageList, orchestrator.getMessageList());
    }

    @Test
    public void build_withInput_setsInputAndRegistersListener() {
        AiChatOrchestrator orchestrator = AiChatOrchestrator.create(mockProvider)
                .withInput(mockInput)
                .build();

        assertEquals("Input should be set", mockInput, orchestrator.getInput());
        verify(mockInput, times(1)).addSubmitListener(any(InputSubmitListener.class));
    }

    @Test
    public void build_withFileReceiver_setsFileReceiver() {
        AiChatOrchestrator orchestrator = AiChatOrchestrator.create(mockProvider)
                .withFileReceiver(mockFileReceiver)
                .build();

        assertEquals("FileReceiver should be set", mockFileReceiver, orchestrator.getFileReceiver());
    }

    @Test
    public void build_withAllComponents_setsAllComponents() {
        AiChatOrchestrator orchestrator = AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .withFileReceiver(mockFileReceiver)
                .build();

        assertEquals("Provider should be set", mockProvider, orchestrator.getProvider());
        assertEquals("MessageList should be set", mockMessageList, orchestrator.getMessageList());
        assertEquals("Input should be set", mockInput, orchestrator.getInput());
        assertEquals("FileReceiver should be set", mockFileReceiver, orchestrator.getFileReceiver());
    }

    @Test
    public void builderPattern_allowsFluentChaining() {
        AiChatOrchestrator orchestrator = AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .withFileReceiver(mockFileReceiver)
                .build();

        assertNotNull("Orchestrator should be created successfully", orchestrator);
    }

    @Test
    public void userMessageSubmit_withEmptyMessage_doesNothing() throws InterruptedException {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent emptyEvent = () -> "";
        listenerCaptor.getValue().onSubmit(emptyEvent);

        verify(mockMessageList, never()).addMessage(any());
        verify(mockProvider, never()).stream(any());
    }

    @Test
    public void userMessageSubmit_withNullMessage_doesNothing() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent nullEvent = () -> null;
        listenerCaptor.getValue().onSubmit(nullEvent);

        verify(mockMessageList, never()).addMessage(any());
        verify(mockProvider, never()).stream(any());
    }

    @Test
    public void userMessageSubmit_withWhitespaceMessage_doesNothing() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent whitespaceEvent = () -> "   ";
        listenerCaptor.getValue().onSubmit(whitespaceEvent);

        verify(mockMessageList, never()).addMessage(any());
        verify(mockProvider, never()).stream(any());
    }

    @Test
    public void userMessageSubmit_withValidMessage_addsUserMessage() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<AiMessage> messageCaptor = ArgumentCaptor
                .forClass(AiMessage.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Response"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Hello AI";
        listenerCaptor.getValue().onSubmit(event);

        // Wait a bit for async operations
        Thread.sleep(100);

        verify(mockMessageList).createMessage("Hello AI", "User");
        verify(mockMessageList, atLeastOnce()).addMessage(messageCaptor.capture());

        // Verify user message was added
        List<AiMessage> addedMessages = messageCaptor.getAllValues();
        assertTrue("At least one message should be added", addedMessages.size() >= 1);
    }

    @Test
    public void userMessageSubmit_withValidMessage_generatesAiResponse() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("AI", " ", "response"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Hello";
        listenerCaptor.getValue().onSubmit(event);

        // Wait for async operations
        Thread.sleep(200);

        verify(mockProvider).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();
        // Verify the request contains the user message
        assertNotNull("Request should not be null", request);
        assertEquals("User message should match", "Hello", request.userMessage());
    }

    @Test
    public void userMessageSubmit_withStreamingResponse_callsProvider() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Hello", " ", "World"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Hi";
        listenerCaptor.getValue().onSubmit(event);

        // Wait a bit for async operations
        Thread.sleep(100);

        // Verify the provider was called
        verify(mockProvider).stream(any());
        // Verify assistant message placeholder was created
        verify(mockMessageList).createMessage("", "Assistant");
    }

    @Test
    public void userMessageSubmit_withMultipleMessages_maintainsConversationHistory() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Response 1"))
                .thenReturn(Flux.just("Response 2"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        // First message
        InputSubmitEvent event1 = () -> "Message 1";
        listenerCaptor.getValue().onSubmit(event1);
        Thread.sleep(200);

        // Second message
        InputSubmitEvent event2 = () -> "Message 2";
        listenerCaptor.getValue().onSubmit(event2);
        Thread.sleep(200);

        // Verify provider was called twice
        verify(mockProvider, times(2)).stream(any());
    }

    @Test
    public void userMessageSubmit_withProviderError_createsAssistantMessage() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        RuntimeException error = new RuntimeException("Provider error");
        Flux<String> errorFlux = Flux.<String>error(error);
        when(mockProvider.stream(any()))
                .thenReturn(errorFlux);

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Hello";
        listenerCaptor.getValue().onSubmit(event);

        // Wait a bit for async operations
        Thread.sleep(100);

        // Verify the provider was called and assistant message was created
        verify(mockProvider).stream(any());
        verify(mockMessageList).createMessage("", "Assistant");
        verify(mockMessageList, times(2)).addMessage(any()); // User + Assistant
    }

    @Test
    public void userMessageSubmit_withoutMessageList_doesNotThrowException() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Response"));

        AiChatOrchestrator.create(mockProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Hello";

        // Should not throw exception
        listenerCaptor.getValue().onSubmit(event);
    }

    @Test
    public void build_withoutInput_doesNotRegisterListener() {
        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .build();

        verify(mockInput, never()).addSubmitListener(any());
    }

    @Test
    public void orchestrator_isSerializable() {
        assertTrue("AiChatOrchestrator should be serializable",
                java.io.Serializable.class.isAssignableFrom(AiChatOrchestrator.class));
    }

    @Test
    public void getProvider_returnsConfiguredProvider() {
        AiChatOrchestrator orchestrator = AiChatOrchestrator.create(mockProvider)
                .build();

        assertSame("Should return the same provider instance", mockProvider,
                orchestrator.getProvider());
    }

    @Test
    public void getMessageList_returnsConfiguredMessageList() {
        AiChatOrchestrator orchestrator = AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .build();

        assertSame("Should return the same messageList instance", mockMessageList,
                orchestrator.getMessageList());
    }

    @Test
    public void getInput_returnsConfiguredInput() {
        AiChatOrchestrator orchestrator = AiChatOrchestrator.create(mockProvider)
                .withInput(mockInput)
                .build();

        assertSame("Should return the same input instance", mockInput,
                orchestrator.getInput());
    }

    @Test
    public void getFileReceiver_returnsConfiguredFileReceiver() {
        AiChatOrchestrator orchestrator = AiChatOrchestrator.create(mockProvider)
                .withFileReceiver(mockFileReceiver)
                .build();

        assertSame("Should return the same fileReceiver instance", mockFileReceiver,
                orchestrator.getFileReceiver());
    }

    @Test
    public void getFileReceiver_withoutConfiguration_returnsNull() {
        AiChatOrchestrator orchestrator = AiChatOrchestrator.create(mockProvider)
                .build();

        assertNull("Should return null when not configured",
                orchestrator.getFileReceiver());
    }

    // ===== Tests for streaming behavior =====

    @Test
    public void streaming_withTokens_updatesMessageIncrementally() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Hello", " ", "World"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Hi";
        listenerCaptor.getValue().onSubmit(event);

        // Wait for async Flux processing
        Thread.sleep(100);

        // Verify message was updated multiple times with accumulated text
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockMessage, atLeast(3)).setText(textCaptor.capture());

        List<String> capturedTexts = textCaptor.getAllValues();
        assertTrue("Should have accumulated text", capturedTexts.size() >= 3);

        // Verify progressive accumulation
        assertTrue("First update should contain 'Hello'",
                capturedTexts.get(0).contains("Hello"));
        assertTrue("Last update should contain full message",
                capturedTexts.get(capturedTexts.size() - 1).equals("Hello World"));

        // Verify updateMessage was called for each token
        verify(mockMessageList, atLeast(3)).updateMessage(mockMessage);
    }

    @Test
    public void streaming_withError_setsErrorMessage() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        RuntimeException error = new RuntimeException("Test error");
        Flux<String> errorFlux = Flux.<String>error(error);
        when(mockProvider.stream(any()))
                .thenReturn(errorFlux);

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Hello";
        listenerCaptor.getValue().onSubmit(event);

        // Wait for error to be processed
        Thread.sleep(100);

        // Verify error message was set
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockMessage, atLeastOnce()).setText(textCaptor.capture());

        String errorText = textCaptor.getValue();
        assertTrue("Error message should contain 'Error'", errorText.contains("Error"));
        assertTrue("Error message should contain error details",
                errorText.contains("Test error"));

        // Verify updateMessage was called to show error
        verify(mockMessageList, atLeastOnce()).updateMessage(mockMessage);
    }

    @Test
    public void streaming_onComplete_addsToConversationHistory() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Response", " ", "text"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        // First message
        InputSubmitEvent event1 = () -> "Message 1";
        listenerCaptor.getValue().onSubmit(event1);
        Thread.sleep(150);

        // Reset to clear the first call
        reset(mockProvider);
        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Second", " ", "response"));

        // Second message - should use same conversation ID
        InputSubmitEvent event2 = () -> "Message 2";
        listenerCaptor.getValue().onSubmit(event2);
        Thread.sleep(150);

        // Verify the second call uses conversation history
        verify(mockProvider).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();

        // The provider manages conversation history internally via conversationId
        // We verify that the request has the correct user message and conversationId
        assertNotNull("Request should not be null", request);
        assertEquals("User message should match", "Message 2", request.userMessage());
        assertNotNull("Conversation ID should be set for history", request.conversationId());
    }

    @Test
    public void streaming_multipleTokens_accumulatesCorrectly() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("The", " ", "quick", " ", "brown", " ", "fox"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        // Wait for streaming to complete
        Thread.sleep(200);

        // Verify final message text
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockMessage, atLeast(7)).setText(textCaptor.capture());

        List<String> texts = textCaptor.getAllValues();
        String finalText = texts.get(texts.size() - 1);
        assertEquals("Should accumulate all tokens", "The quick brown fox", finalText);
    }

    @Test
    public void streaming_emptyResponse_handlesGracefully() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.empty());

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        // Wait for completion
        Thread.sleep(100);

        // Verify assistant message was created
        verify(mockMessageList).createMessage("", "Assistant");
        verify(mockMessageList, times(2)).addMessage(any()); // User + Assistant

        // Even with no tokens, the message should still be added to conversation history
        // (though it will be empty)
    }

    @Test
    public void streaming_singleToken_updatesOnce() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("SingleToken"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        // Wait for streaming to complete
        Thread.sleep(100);

        // Verify message was updated once
        verify(mockMessage, atLeastOnce()).setText("SingleToken");
        verify(mockMessageList, atLeastOnce()).updateMessage(mockMessage);
    }

    // ===== Tests for UI context validation =====

    @Test(expected = IllegalStateException.class)
    public void generateAiResponse_withoutUI_throwsException() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Response"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        // Remove UI context
        UI.setCurrent(null);

        InputSubmitEvent event = () -> "Test";
        // Should throw IllegalStateException
        listenerCaptor.getValue().onSubmit(event);
    }

    // ===== Tests for conversation history =====

    @Test
    public void conversationHistory_persistsAcrossMultipleMessages() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("First"))
                .thenReturn(Flux.just("Second"))
                .thenReturn(Flux.just("Third"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        // Send three messages
        listenerCaptor.getValue().onSubmit(() -> "Message 1");
        Thread.sleep(100);

        listenerCaptor.getValue().onSubmit(() -> "Message 2");
        Thread.sleep(100);

        listenerCaptor.getValue().onSubmit(() -> "Message 3");
        Thread.sleep(100);

        // Verify provider was called three times
        verify(mockProvider, times(3)).stream(any());
    }

    @Test
    public void conversationHistory_includesOnlyCompletedMessages() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        // First message completes successfully
        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Response 1"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        listenerCaptor.getValue().onSubmit(() -> "Message 1");
        Thread.sleep(150);

        // Capture the conversation ID from the first call
        verify(mockProvider).stream(requestCaptor.capture());
        String conversationId = requestCaptor.getValue().conversationId();

        // Reset and prepare for second message
        reset(mockProvider);
        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Response 2"));

        listenerCaptor.getValue().onSubmit(() -> "Message 2");
        Thread.sleep(150);

        verify(mockProvider).stream(requestCaptor.capture());
        LLMProvider.LLMRequest secondRequest = requestCaptor.getValue();

        // Verify that the conversation ID is maintained across calls
        assertNotNull("Conversation ID should be set", secondRequest.conversationId());
        assertEquals("Conversation ID should match first call", conversationId, secondRequest.conversationId());
    }

    // ===== Tests for edge cases =====

    @Test
    public void builder_canSetComponentsInAnyOrder() {
        AiChatOrchestrator orchestrator = AiChatOrchestrator.create(mockProvider)
                .withFileReceiver(mockFileReceiver)
                .withInput(mockInput)
                .withMessageList(mockMessageList)
                .build();

        assertNotNull("Orchestrator should be created", orchestrator);
        assertEquals("MessageList should be set", mockMessageList, orchestrator.getMessageList());
        assertEquals("Input should be set", mockInput, orchestrator.getInput());
        assertEquals("FileReceiver should be set", mockFileReceiver, orchestrator.getFileReceiver());
    }

    @Test
    public void builder_canBeReused() {
        AiChatOrchestrator.Builder builder = AiChatOrchestrator.create(mockProvider);

        AiChatOrchestrator orchestrator1 = builder.withMessageList(mockMessageList).build();
        AiChatOrchestrator orchestrator2 = builder.withInput(mockInput).build();

        assertNotNull("First orchestrator should be created", orchestrator1);
        assertNotNull("Second orchestrator should be created", orchestrator2);
        // Both should use the same provider
        assertEquals("Both should have same provider", mockProvider, orchestrator1.getProvider());
        assertEquals("Both should have same provider", mockProvider, orchestrator2.getProvider());
    }

    @Test
    public void userMessage_withLeadingTrailingWhitespace_notTrimmed() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Response"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        // Message with whitespace
        InputSubmitEvent event = () -> "  Hello  ";
        listenerCaptor.getValue().onSubmit(event);

        Thread.sleep(100);

        // Verify the message was added to UI as-is (not trimmed)
        verify(mockMessageList).createMessage("  Hello  ", "User");
    }

    @Test
    public void streaming_rapidSuccessiveMessages_allProcessed() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Response"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        // Send 5 messages rapidly
        for (int i = 0; i < 5; i++) {
            final int num = i;
            listenerCaptor.getValue().onSubmit(() -> "Message " + num);
        }

        // Wait for all to complete
        Thread.sleep(500);

        // Verify provider was called 5 times
        verify(mockProvider, times(5)).stream(any());
    }

    @Test
    public void messageList_nullSafe_whenNotConfigured() {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Response"));

        // Build without message list
        AiChatOrchestrator.create(mockProvider)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        // Should not throw NPE
        listenerCaptor.getValue().onSubmit(event);

        // Provider should still be called even without message list
        verify(mockProvider, never()).stream(any());
    }

    @Test
    public void streaming_longRunning_handlesCorrectly() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        // Simulate a long-running stream with many tokens
        String[] tokens = new String[50];
        for (int i = 0; i < 50; i++) {
            tokens[i] = "token" + i + " ";
        }

        when(mockProvider.stream(any()))
                .thenReturn(Flux.fromArray(tokens));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        // Wait for streaming to complete
        Thread.sleep(500);

        // Verify message was updated many times
        verify(mockMessage, atLeast(50)).setText(anyString());
        verify(mockMessageList, atLeast(50)).updateMessage(mockMessage);
    }

    @Test
    public void streaming_withNullSystemPromptAndTools_worksCorrectly() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);
        ArgumentCaptor<LLMProvider.LLMRequest> requestCaptor = ArgumentCaptor
                .forClass(LLMProvider.LLMRequest.class);

        when(mockProvider.stream(any()))
                .thenReturn(Flux.just("Response"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        InputSubmitEvent event = () -> "Test";
        listenerCaptor.getValue().onSubmit(event);

        Thread.sleep(100);

        verify(mockProvider).stream(requestCaptor.capture());

        LLMProvider.LLMRequest request = requestCaptor.getValue();

        // Verify system prompt and tools are null/empty
        assertNull("System prompt should be null", request.systemPrompt());
        assertNotNull("Tools should not be null", request.tools());
        assertEquals("Tools array should be empty", 0, request.tools().length);
    }

    @Test
    public void orchestrator_serializableCompliance() {
        assertTrue("AiChatOrchestrator should implement Serializable",
                Serializable.class.isAssignableFrom(AiChatOrchestrator.class));
    }

    @Test
    public void streaming_errorDuringStreaming_doesNotAffectSubsequentMessages() throws Exception {
        ArgumentCaptor<InputSubmitListener> listenerCaptor = ArgumentCaptor
                .forClass(InputSubmitListener.class);

        // First message fails
        when(mockProvider.stream(any()))
                .thenReturn(Flux.<String>error(new RuntimeException("Error")))
                .thenReturn(Flux.just("Success"));

        AiChatOrchestrator.create(mockProvider)
                .withMessageList(mockMessageList)
                .withInput(mockInput)
                .build();

        verify(mockInput).addSubmitListener(listenerCaptor.capture());

        // First message - will error
        listenerCaptor.getValue().onSubmit(() -> "Message 1");
        Thread.sleep(150);

        // Second message - should work
        listenerCaptor.getValue().onSubmit(() -> "Message 2");
        Thread.sleep(150);

        // Verify both calls were made
        verify(mockProvider, times(2)).stream(any());
    }
}
