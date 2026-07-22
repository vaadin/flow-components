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
package com.vaadin.flow.component.ai.tests;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.form.FormAIController;
import com.vaadin.flow.component.ai.form.ValueOptions;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LangChain4JLLMProvider;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * Test page for trying out FormAIController with a real AI chat, backed by
 * OpenAI's {@value #MODEL_NAME}. Reads the {@code OPENAI_API_KEY} environment
 * variable.
 * <p>
 * Open at {@code http://localhost:8080/vaadin-ai/form-ai-chat}.
 */
@PreserveOnRefresh
@Route("vaadin-ai/form-ai-chat")
public class FormAIChatPage extends Div {

    private static final String MODEL_NAME = "gpt-5.4-mini";

    private static final String SYSTEM_PROMPT = """
            You help the user fill in an expense entry form from a \
            free-text prompt. Currency is one of EUR, USD, GBP; category \
            is one of Travel, Meals, Software, Office, Other. The tax \
            rate is a decimal (0.20 = 20%). Follow the form-tool workflow \
            described in get_form_instructions.""";

    private static final List<Country> COUNTRIES = List.of(
            new Country("AR", "Argentina"), new Country("AU", "Australia"),
            new Country("AT", "Austria"), new Country("BE", "Belgium"),
            new Country("BR", "Brazil"), new Country("CA", "Canada"),
            new Country("CN", "China"), new Country("CZ", "Czech Republic"),
            new Country("DK", "Denmark"), new Country("EE", "Estonia"),
            new Country("FI", "Finland"), new Country("FR", "France"),
            new Country("DE", "Germany"), new Country("GR", "Greece"),
            new Country("IN", "India"), new Country("IE", "Ireland"),
            new Country("IT", "Italy"), new Country("JP", "Japan"),
            new Country("LV", "Latvia"), new Country("LT", "Lithuania"),
            new Country("MX", "Mexico"), new Country("NL", "Netherlands"),
            new Country("NO", "Norway"), new Country("PL", "Poland"),
            new Country("PT", "Portugal"), new Country("RO", "Romania"),
            new Country("SK", "Slovakia"), new Country("ES", "Spain"),
            new Country("SE", "Sweden"), new Country("CH", "Switzerland"),
            new Country("GB", "United Kingdom"),
            new Country("US", "United States"));

    private static final List<Project> PROJECTS = List.of(
            new Project("P-1001", "Apollo Redesign"),
            new Project("P-1002", "Beacon Migration"),
            new Project("P-1003", "Cosmos UI"),
            new Project("P-1004", "Delta Pipeline"),
            new Project("P-1005", "Eclipse Reporting"),
            new Project("P-1006", "Falcon Mobile"),
            new Project("P-1007", "Gamma Analytics"),
            new Project("P-1008", "Helios CRM"),
            new Project("P-1009", "Iris Onboarding"),
            new Project("P-1010", "Jupiter Search"),
            new Project("P-1011", "Kepler Billing"),
            new Project("P-1012", "Lyra Localization"),
            new Project("P-1013", "Meridian Marketplace"),
            new Project("P-1014", "Nimbus Infra"),
            new Project("P-1015", "Orion Outreach"));

    private FormLayout form;
    private ComboBox<Country> country;
    private MultiSelectComboBox<Project> projects;

    public FormAIChatPage() {
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
        setSizeFull();
        getStyle().set("display", "flex");

        StreamingChatModel chatModel;
        try {
            chatModel = createModel();
        } catch (IllegalArgumentException e) {
            add(new Div(e.getMessage()));
            return;
        }

        var modelLabel = new Span("Model: " + MODEL_NAME);
        modelLabel.getStyle().set("padding", "8px").set("font-weight", "bold");

        form = buildExpenseForm();

        var formController = new FormAIController(form);
        formController.fieldValueOptions(ValueOptions.forField(country)
                .options((filter, limit) -> filterCountries(filter).limit(limit)
                        .toList()));
        formController.fieldValueOptions(ValueOptions.forField(projects)
                .options((filter, limit) -> filterProjects(filter).limit(limit)
                        .toList()));

        var messageList = new MessageList();
        messageList.setWidthFull();
        messageList.getStyle().set("flex", "1");

        var messageInput = new MessageInput();
        messageInput.setWidthFull();

        var llmProvider = new LangChain4JLLMProvider(chatModel);

        AIOrchestrator.builder(llmProvider, SYSTEM_PROMPT)
                .withMessageList(messageList).withInput(messageInput)
                .withController(formController).build();

        var chatLayout = new Div(modelLabel, messageList, messageInput);
        chatLayout.getStyle().set("display", "flex");
        chatLayout.getStyle().set("flex-direction", "column");
        chatLayout.getStyle().set("flex", "1");
        chatLayout.getStyle().set("min-width", "0");

        var formWrapper = new Div(form);
        formWrapper.getStyle().set("flex", "1");
        formWrapper.getStyle().set("min-width", "0");
        formWrapper.getStyle().set("padding", "16px");

        add(formWrapper, chatLayout);
    }

    private FormLayout buildExpenseForm() {
        var merchant = new TextField("Merchant");
        merchant.setHelperText("Business or vendor name");

        var amount = new NumberField("Amount");
        var taxRate = new BigDecimalField("Tax rate");
        taxRate.setHelperText("As a decimal, e.g. 0.21 for 21%");

        var quantity = new IntegerField("Receipt count");

        var currency = new ComboBox<String>("Currency");
        currency.setItems("EUR", "USD", "GBP");

        var category = new ComboBox<String>("Category");
        category.setItems("Travel", "Meals", "Software", "Office", "Other");

        country = new ComboBox<>("Country");
        country.setItemLabelGenerator(Country::name);
        country.setItems(query -> filterCountries(query.getFilter().orElse(""))
                .skip(query.getOffset()).limit(query.getLimit()));

        projects = new MultiSelectComboBox<>("Projects");
        projects.setItemLabelGenerator(FormAIChatPage::projectLabel);
        projects.setItems(query -> filterProjects(query.getFilter().orElse(""))
                .skip(query.getOffset()).limit(query.getLimit()));

        var date = new DatePicker("Date");

        var reimbursable = new Checkbox("Reimbursable by employer");

        var email = new EmailField("Submitter email");

        var notes = new TextArea("Notes");

        return new FormLayout(merchant, amount, taxRate, quantity, currency,
                category, country, projects, date, reimbursable, email, notes);
    }

    private static Stream<Country> filterCountries(String filter) {
        if (filter == null || filter.isEmpty()) {
            return COUNTRIES.stream();
        }
        var needle = filter.toLowerCase(Locale.ROOT);
        return COUNTRIES.stream().filter(
                c -> c.name().toLowerCase(Locale.ROOT).contains(needle));
    }

    private static Stream<Project> filterProjects(String filter) {
        if (filter == null || filter.isEmpty()) {
            return PROJECTS.stream();
        }
        var needle = filter.toLowerCase(Locale.ROOT);
        return PROJECTS.stream().filter(
                p -> projectLabel(p).toLowerCase(Locale.ROOT).contains(needle));
    }

    private static String projectLabel(Project project) {
        return project.code() + " " + project.name();
    }

    /**
     * Country option for the lazy-loaded country ComboBox. Kept as a record so
     * the ComboBox's generic type is something other than String, matching a
     * typical bean-backed form field.
     */
    private record Country(String code, String name) {
    }

    /**
     * Project option for the lazy-loaded projects MultiSelectComboBox. The
     * value type of the field is therefore {@code Set<Project>}, mirroring a
     * typical bean-backed multi-select.
     */
    private record Project(String code, String name) {
    }

    private static StreamingChatModel createModel() {
        var apiKey = requireEnv("OPENAI_API_KEY");
        return OpenAiStreamingChatModel.builder().apiKey(apiKey)
                .strictTools(null).modelName(MODEL_NAME).build();
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Set the " + name
                    + " environment variable to use this model.");
        }
        return value;
    }
}
