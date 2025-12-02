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

import com.vaadin.flow.component.ai.orchestrator.AiOrchestrator;
import com.vaadin.flow.component.ai.provider.langchain4j.LangChain4JLLMProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Route;

import dev.langchain4j.agent.tool.Tool;
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
@CssImport("@vaadin/vaadin-lumo-styles/lumo.css")
public class AiChatFormFillerDemoView extends VerticalLayout {

    private TextField firstNameField;
    private TextField lastNameField;
    private EmailField emailField;
    private TextField phoneField;
    private DatePicker dateOfBirthField;
    private TextField addressField;

    public AiChatFormFillerDemoView() {
        setSizeFull();

        // Create layout with form and chat side by side
        var mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();

        // Create form section
        var formLayout = new FormLayout();
        firstNameField = new TextField("First Name");
        lastNameField = new TextField("Last Name");
        emailField = new EmailField("Email");
        phoneField = new TextField("Phone");
        dateOfBirthField = new DatePicker("Date of Birth");
        addressField = new TextField("Address");
        formLayout.add(firstNameField, lastNameField, emailField, phoneField,
                dateOfBirthField, addressField);

        // Upload component for attachments
        var upload = new Upload();
        upload.setWidthFull();
        upload.setMaxFiles(5);
        upload.setMaxFileSize(5 * 1024 * 1024); // 5 MB
        upload.setAcceptedFileTypes("image/*", "application/pdf", "text/plain");

        // Create LLM provider
        var model = OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o-mini").build();
        var provider = new LangChain4JLLMProvider(model);

        // Create and configure orchestrator with form filling tools
        var orchestrator = AiOrchestrator.builder(provider)
                .withFileReceiver(upload)
                .withTools(this).build();

        // Fill button to trigger AI form filling
        var fillButton = new Button("Fill");
        fillButton.setWidthFull();
        fillButton.addClickListener(event -> {
            orchestrator.sendMessage(
                    "Please analyze the uploaded files and fill the person information form with any data you can extract.");
        });

        mainLayout.add(formLayout, new VerticalLayout(upload, fillButton));
        mainLayout.setFlexGrow(1, formLayout);
        add(mainLayout);
    }

    // TODO: Instead of defining custom Tool annotations, consider allowing the user to just use SpringAi/Langchain annotations directly.
    @Tool("Fills the person information form with the provided data. Use this when the user provides person details.")
    private String fillFormFields(String firstName, String lastName, String emailAddress, String phoneNumber, String dateOfBirth, String address) {

        // Update fields if values are provided
        // Note: UI.access() is automatically handled by AiOrchestrator
        if (firstName != null && !firstName.trim().isEmpty()) {
            firstNameField.setValue(firstName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            lastNameField.setValue(lastName.trim());
        }
        if (emailAddress != null && !emailAddress.trim().isEmpty()) {
            emailField.setValue(emailAddress.trim());
        }
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            phoneField.setValue(phoneNumber.trim());
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
