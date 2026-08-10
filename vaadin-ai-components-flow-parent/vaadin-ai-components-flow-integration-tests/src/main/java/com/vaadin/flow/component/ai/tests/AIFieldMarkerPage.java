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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.form.FieldMarkerI18n;
import com.vaadin.flow.component.ai.form.FormAIController;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;

import tools.jackson.databind.JsonNode;

/**
 * Test page for the AI field marker that {@link FormAIController} applies to
 * the fields it fills. Drives one AI turn in two steps, each its own server
 * round trip, so that both halves of the turn are observable: "start-turn" puts
 * the fields into the "AI is working" state, "finish-turn" writes the values
 * and marks the fields that changed. The "confidence-turn" button instead runs
 * a whole turn at once, writing the values through the controller's
 * {@code fill_form} tool wrapped in the confidence-reporting envelope, so the
 * confidence indicators show. That turn runs on a background thread, so the
 * page enables push for its own UI.
 * <p>
 * The controller is configured with {@link FieldMarkerI18n} texts that differ
 * from the web component's defaults, so a test can tell the texts sent by the
 * server apart from the built-in ones.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/ai-field-marker")
public class AIFieldMarkerPage extends VerticalLayout {

    static final String MESSAGE = "Filled from the uploaded document.";
    static final String REVERT = "Undo this value";
    static final String BADGE_LABEL = "Value provided by AI";
    static final String BADGE_TOOLTIP = "This value came from the AI.";
    static final String CONFIDENCE_HIGH_TEXT = "Copied from the source";
    static final String CONFIDENCE_LOW_TEXT = "Guessed value";

    static final String NAME_VALUE = "Ada Lovelace";
    static final String COMPANY_VALUE = "Analytical Engines Ltd.";
    static final String UNCHANGED_VALUE = "Unchanged";

    public AIFieldMarkerPage() {
        UI.getCurrent().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        var name = new TextField("Name");
        name.setId("name");

        // Pre-filled, so reverting this field restores a value the user can
        // tell apart from an empty field.
        var company = new TextField("Company");
        company.setId("company");
        company.setValue("Acme Inc.");

        // Written with the value it already has, so the turn leaves it
        // unchanged and it must not end up marked.
        var unchanged = new TextField("Unchanged");
        unchanged.setId("unchanged");
        unchanged.setValue(UNCHANGED_VALUE);

        var form = new VerticalLayout(name, company, unchanged);
        form.setId("form");

        var controller = new FormAIController(form);
        controller.setFieldConfidenceEnabled(true);
        controller.setFieldMarkerI18n(new FieldMarkerI18n().setMessage(MESSAGE)
                .setRevert(REVERT).setBadgeLabel(BADGE_LABEL)
                .setBadgeTooltip(BADGE_TOOLTIP)
                .setConfidence(new FieldMarkerI18n.Confidence()
                        .setHigh(CONFIDENCE_HIGH_TEXT)
                        .setLow(CONFIDENCE_LOW_TEXT)));

        var startTurn = new NativeButton("Start turn",
                event -> controller.onRequest());
        startTurn.setId("start-turn");

        var finishTurn = new NativeButton("Finish turn", event -> {
            name.setValue(NAME_VALUE);
            company.setValue(COMPANY_VALUE);
            unchanged.setValue(UNCHANGED_VALUE);
            controller.onResponse(null);
        });
        finishTurn.setId("finish-turn");

        var confidenceTurn = new NativeButton("Confidence turn",
                event -> runConfidenceTurn(controller));
        confidenceTurn.setId("confidence-turn");

        add(form, startTurn, finishTurn, confidenceTurn);
    }

    /**
     * Runs one AI turn whose values go through the {@code fill_form} tool the
     * way the LLM sends them with confidence reporting on: each value wrapped
     * in an envelope carrying a confidence level. The field ids are resolved
     * from the {@code get_form_state} tool output, like the LLM resolves them.
     * <p>
     * {@code fill_form} hops through {@code ui.access} and blocks until the
     * writes land, so it must not run on the UI thread that is handling the
     * button click: the command would only be run once the click request
     * releases the session lock, which never happens while the handler waits
     * for it. The tool is therefore executed on a background thread, like a
     * real LLM provider executes tools.
     */
    private static void runConfidenceTurn(FormAIController controller) {
        var ui = UI.getCurrent();
        controller.onRequest();
        var tools = controller.getTools();
        var state = JacksonUtils
                .readTree(findTool(tools, "get_form_state").execute(null));
        var values = JacksonUtils.createObjectNode();
        values.putObject(fieldId(state, "Name")).put("value", NAME_VALUE)
                .put("confidence", "high");
        values.putObject(fieldId(state, "Company")).put("value", COMPANY_VALUE)
                .put("confidence", "low");
        var arguments = JacksonUtils.createObjectNode();
        arguments.set("values", values);
        new Thread(() -> {
            findTool(tools, "fill_form").execute(arguments);
            ui.access(() -> controller.onResponse(null));
        }).start();
    }

    private static LLMProvider.ToolSpec findTool(
            List<LLMProvider.ToolSpec> tools, String name) {
        return tools.stream().filter(tool -> name.equals(tool.getName()))
                .findFirst().orElseThrow();
    }

    /**
     * @return the id of the form-state field whose description carries the
     *         given label
     */
    private static String fieldId(JsonNode state, String label) {
        for (var field : state.get("fields")) {
            if (field.path("description").asString("").contains(label)) {
                return field.get("id").asString();
            }
        }
        throw new IllegalStateException("No field labeled " + label);
    }
}
