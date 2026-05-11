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
import java.util.Map;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ChatMessage;
import com.vaadin.flow.component.ai.form.FormAIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import reactor.core.publisher.Flux;

/**
 * Test page for {@link FormAIController}. A canned async LLM provider answers
 * the prompt by calling the {@code fill_form} tool with values extracted from
 * the user message. Locks fields during the turn, releases them on completion.
 */
@Route("vaadin-ai/form-controller")
public class FormControllerPage extends Div {

    public FormControllerPage() {
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        var merchant = new TextField("Merchant");
        merchant.setId("merchant");
        var amount = new NumberField("Amount");
        amount.setId("amount");
        var currency = new ComboBox<String>("Currency");
        currency.setItems("EUR", "USD", "GBP");
        currency.setId("currency");
        var date = new DatePicker("Date");
        date.setId("date");
        var notes = new TextArea("Notes");
        notes.setId("notes");
        var form = new FormLayout(merchant, amount, currency, date, notes);
        form.setId("form");

        var input = new MessageInput();
        input.setId("message-input");

        var orchestrator = AIOrchestrator
                .builder(new FakeFormFillerProvider(), null).withInput(input)
                .withController(new FormAIController(form)).build();

        var fillTrigger = new NativeButton("Trigger fill",
                e -> orchestrator.prompt(
                        "Expense: Trattoria Toscana 58.4 EUR on 2026-05-04, dinner with the team."));
        fillTrigger.setId("trigger-fill");

        add(input, fillTrigger, form);
    }

    /**
     * Streams tokens from a background thread and invokes the {@code fill_form}
     * tool with a fixed payload. This mirrors the real Reactor scheduling that
     * exercises the controller's locking and {@code onResponseComplete}
     * lifecycle.
     */
    private static class FakeFormFillerProvider implements LLMProvider {
        @Override
        public Flux<String> stream(LLMRequest request) {
            var tools = request.explicitTools();
            return Flux.<String> create(sink -> new Thread(() -> {
                tools.stream().filter(t -> t.getName().equals("fill_form"))
                        .findFirst().orElseThrow()
                        .execute(JacksonUtils.readTree("""
                                {
                                  "merchant": "Trattoria Toscana",
                                  "amount": 58.4,
                                  "currency": "EUR",
                                  "date": "2026-05-04",
                                  "notes": "dinner with the team"
                                }"""));
                sink.next("Filled the form.");
                sink.complete();
            }).start());
        }

        @Override
        public void setHistory(List<ChatMessage> history,
                Map<String, List<AIAttachment>> attachmentsByMessageId) {
        }
    }
}
