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
import java.util.UUID;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.ai.form.FieldMarkerI18n;
import com.vaadin.flow.component.ai.form.FormAIController;
import com.vaadin.flow.component.ai.orchestrator.RequestListener;
import com.vaadin.flow.component.ai.orchestrator.ResponseListener;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.router.Route;

import tools.jackson.databind.JsonNode;

/**
 * Test page for the AI field marker that {@link FormAIController} applies to
 * the fields it fills. Drives one AI turn in two steps, each its own server
 * round trip, so that both halves of the turn are observable without push or
 * background threads: "start-turn" puts the fields into the "AI is working"
 * state, "finish-turn" writes the values and marks the fields that changed.
 * <p>
 * The controller is configured with {@link FieldMarkerI18n} texts that differ
 * from the web component's defaults, so a test can tell the texts sent by the
 * server apart from the built-in ones, and with a field-marker popover content
 * provider that adds a recognizable node to the name field's popover only.
 * Source tracking is on, so the quantity field is written through the
 * controller's {@code fill_form} tool with a source reporting a confidence
 * level, the way a real fill reports one.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/ai-field-marker")
public class AIFieldMarkerPage extends VerticalLayout {

    static final String MESSAGE = "Filled from the uploaded document.";
    static final String REVERT = "Undo this value";
    static final String BADGE_LABEL = "Value provided by AI";
    static final String BADGE_TOOLTIP = "This value came from the AI.";
    static final String CONFIDENCE_HIGH = "Varma lähde";

    static final String CONTENT_ID = "marker-content";
    static final String CONTENT_TEXT = "Source: invoice.pdf";
    static final String TEXT_CONTENT = "Recognized from the packing list";

    static final String NAME_VALUE = "Ada Lovelace";
    static final String COMPANY_VALUE = "Analytical Engines Ltd.";
    static final String UNCHANGED_VALUE = "Unchanged";
    static final String LOCKED_VALUE = "CC-1024";
    static final String CONFIDENT_VALUE = "42";
    static final String CONFIDENT_LABEL = "Quantity";

    public AIFieldMarkerPage() {
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

        // Made read-only by the name field's value-change listener below —
        // i.e. mid-turn, in reaction to one of the AI's writes. The client
        // guard's restore must not lift a read-only state set this way.
        var locked = new TextField("Cost center");
        locked.setId("locked");
        name.addValueChangeListener(event -> locked.setReadOnly(true));

        // Filled through the fill_form tool together with a source reporting
        // a confidence level, so its marker must show the confidence
        // indicator.
        var confident = new TextField(CONFIDENT_LABEL);
        confident.setId("confident");

        var form = new VerticalLayout(name, company, unchanged, locked,
                confident);
        form.setId("form");

        var controller = new FormAIController(form);
        controller.setSourceTrackingEnabled(true);
        controller.setFieldMarkerI18n(new FieldMarkerI18n().setMessage(MESSAGE)
                .setRevert(REVERT).setBadgeLabel(BADGE_LABEL)
                .setBadgeTooltip(BADGE_TOOLTIP)
                .setConfidence(new FieldMarkerI18n.Confidence()
                        .setHigh(CONFIDENCE_HIGH)));
        controller.setFieldMarkerPopoverContentProvider(change -> {
            if (change.getField() == name) {
                var content = new Span(CONTENT_TEXT);
                content.setId(CONTENT_ID);
                return content;
            }
            if (change.getField() == confident) {
                // A bare text node, no element of its own — exercises content
                // that cannot exist client-side outside the marker's wrapper.
                return new Text(TEXT_CONTENT);
            }
            return null;
        });

        var startTurn = new NativeButton("Start turn",
                event -> controller.onRequest(
                        new RequestListener.RequestEvent("Start turn",
                                UUID.randomUUID().toString(), List.of())));
        startTurn.setId("start-turn");

        var finishTurn = new NativeButton("Finish turn", event -> {
            // Written before name: once name's listener has made this field
            // read-only, a real fill_form write to it would be rejected.
            locked.setValue(LOCKED_VALUE);
            name.setValue(NAME_VALUE);
            company.setValue(COMPANY_VALUE);
            unchanged.setValue(UNCHANGED_VALUE);
            // A source only reaches a field through a fill, so this value is
            // written the way the LLM writes one: through the fill_form tool,
            // wrapped in a source envelope reporting a confidence level. The
            // marker applied at turn end then shows the level.
            fillWithSource(controller, CONFIDENT_LABEL, CONFIDENT_VALUE,
                    "high");
            controller.onResponse(
                    new ResponseListener.ResponseEvent("", null, null));
        });
        finishTurn.setId("finish-turn");

        add(form, startTurn, finishTurn);
    }

    /**
     * Writes a value to a field through the controller's {@code fill_form}
     * tool, wrapped in the envelope the LLM uses to report a source with a
     * confidence level. The field is addressed by its description in the
     * {@code get_form_state} output, which for a plain labeled field is its
     * label.
     */
    private static void fillWithSource(FormAIController controller,
            String label, String value, String confidence) {
        var tools = controller.getTools();
        var formState = JacksonUtils
                .readTree(executeTool(tools, "get_form_state", "{}"));
        executeTool(tools, "fill_form", """
                {"values": {"%s": {"value": "%s", "confidence": "%s"}}}"""
                .formatted(fieldIdOf(formState, label), value, confidence));
    }

    private static String fieldIdOf(JsonNode formState, String description) {
        for (var field : formState.path("fields")) {
            if (description.equals(field.path("description").asString())) {
                return field.path("id").asString();
            }
        }
        throw new IllegalStateException(
                "get_form_state lists no field described as " + description);
    }

    private static String executeTool(List<LLMProvider.ToolSpec> tools,
            String name, String arguments) {
        return tools.stream().filter(tool -> tool.getName().equals(name))
                .findFirst().orElseThrow()
                .execute(JacksonUtils.readTree(arguments));
    }
}
