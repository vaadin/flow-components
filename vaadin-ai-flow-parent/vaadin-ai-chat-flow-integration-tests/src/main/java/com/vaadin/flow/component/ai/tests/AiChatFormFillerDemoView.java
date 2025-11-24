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
package com.vaadin.flow.component.ai.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ai.chat.AiChatOrchestrator;
import com.vaadin.flow.component.ai.orchestrator.ParameterDescription;
import com.vaadin.flow.component.ai.orchestrator.Tool;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Demo view showing AI-powered form filling with chat interface.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/ai-chat-form-filler-demo")
public class AiChatFormFillerDemoView extends VerticalLayout {

    private TextField firstNameField;
    private TextField lastNameField;
    private EmailField emailField;
    private TextField phoneField;
    private DatePicker dateOfBirthField;
    private TextField addressField;

    public AiChatFormFillerDemoView() {

        // Enable push for streaming responses
        getUI().ifPresent(ui -> ui.getPushConfiguration()
                .setPushMode(PushMode.AUTOMATIC));

        setSpacing(true);
        setPadding(true);
        setHeightFull();

        H2 title = new H2("AI Chat Form Filler Demo");
        add(title);

        // Check for API key
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            Div error = new Div();
            error.setText(
                    "Error: OPENAI_API_KEY environment variable is not set. "
                            + "Please set it to use this demo.");
            error.getStyle().set("color", "red").set("padding", "20px");
            add(error);
            return;
        }

        // Create layout with form and chat side by side
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setHeightFull();
        mainLayout.setSpacing(true);

        // Create form section
        Component formSection = createFormSection();
        formSection.getElement().getStyle().set("flex", "1");

        // Create chat section
        Component chatSection = createChatSection(apiKey);
        chatSection.getElement().getStyle().set("flex", "1");

        mainLayout.add(formSection, chatSection);
        add(mainLayout);
        setFlexGrow(1, mainLayout);
    }

    private Component createFormSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(false);
        section.setPadding(false);
        section.setHeightFull();

        H3 formTitle = new H3("Person Information Form");
        section.add(formTitle);

        // Create form
        FormLayout formLayout = new FormLayout();

        firstNameField = new TextField("First Name");
        lastNameField = new TextField("Last Name");
        emailField = new EmailField("Email");
        phoneField = new TextField("Phone");
        dateOfBirthField = new DatePicker("Date of Birth");
        addressField = new TextField("Address");

        formLayout.add(firstNameField, lastNameField, emailField, phoneField,
                dateOfBirthField, addressField);

        Div instructions = new Div();
        instructions.setText(
                "Ask the AI to fill the form by providing information in the chat, "
                        + "or upload a document/image with person information.");
        instructions.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("margin-top", "var(--lumo-space-m)");

        section.add(formLayout, instructions);
        return section;
    }

    private Component createChatSection(String apiKey) {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(false);
        section.setPadding(false);
        section.setHeightFull();

        H3 chatTitle = new H3("AI Assistant");
        section.add(chatTitle);

        // Upload component for attachments
        Upload upload = new Upload();
        upload.setWidthFull();
        upload.setMaxFiles(5);
        upload.setMaxFileSize(5 * 1024 * 1024); // 5 MB
        upload.setAcceptedFileTypes("image/*", "application/pdf",
                "text/plain");

        // Create chat components
        MessageList messageList = new MessageList();
        messageList.setWidthFull();
        messageList.getStyle().set("flex-grow", "1");

        MessageInput messageInput = new MessageInput();
        messageInput.setWidthFull();

        // Create LLM provider
        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey).modelName("gpt-4o-mini").build();
        LLMProvider provider = new LangChain4JLLMProvider(model);

        // Create and configure orchestrator with form filling tools
        AiChatOrchestrator.create(provider).withMessageList(messageList)
                .withInput(messageInput).withFileReceiver(upload)
                .setTools(this).build();

        // Chat container
        Div chatContainer = new Div(messageList);
        chatContainer.setWidthFull();
        chatContainer.getStyle().set("flex-grow", "1").set("overflow", "auto");

        Div inputContainer = new Div(messageInput);
        inputContainer.setWidthFull();

        upload.getElement().appendChild(inputContainer.getElement());
        section.add(chatContainer, upload);
        section.setFlexGrow(1, chatContainer);

        return section;
    }

    @Tool("Fills the person information form with the provided data. Use this when the user provides person details.")
    private String fillFormFields(
            @ParameterDescription("Person's first name") String firstName,
            @ParameterDescription("Person's last name") String lastName,
            @ParameterDescription("Person's email address") String email,
            @ParameterDescription("Person's phone number") String phone,
            @ParameterDescription("Person's date of birth in ISO format (YYYY-MM-DD)") String dateOfBirth,
            @ParameterDescription("Person's address") String address) {

        // Update fields if values are provided
        // Note: UI.access() is automatically handled by BaseAiOrchestrator
        if (firstName != null && !firstName.trim().isEmpty()) {
            firstNameField.setValue(firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            lastNameField.setValue(lastName.trim());
        }
        if (email != null && !email.trim().isEmpty()) {
            emailField.setValue(email.trim());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            phoneField.setValue(phone.trim());
        }
        if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(dateOfBirth.trim(),
                        DateTimeFormatter.ISO_LOCAL_DATE);
                dateOfBirthField.setValue(date);
            } catch (DateTimeParseException e) {
                System.err.println(
                        "Failed to parse date: " + dateOfBirth);
            }
        }
        if (address != null && !address.trim().isEmpty()) {
            addressField.setValue(address.trim());
        }

        return "Form fields have been updated successfully.";
    }
}
