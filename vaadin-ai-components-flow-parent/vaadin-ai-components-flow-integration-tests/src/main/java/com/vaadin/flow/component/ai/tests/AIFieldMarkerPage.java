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

import com.vaadin.flow.component.ai.common.ConfidenceLevel;
import com.vaadin.flow.component.ai.common.ValueSource;
import com.vaadin.flow.component.ai.form.FieldMarkerI18n;
import com.vaadin.flow.component.ai.form.FormAIController;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Test page for the AI field marker that {@link FormAIController} applies to
 * the fields it fills. Drives one AI turn in two steps, each its own server
 * round trip, so that both halves of the turn are observable without push or
 * background threads: "start-turn" puts the fields into the "AI is working"
 * state, "finish-turn" writes the values and marks the fields that changed.
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

    static final String NAME_VALUE = "Ada Lovelace";
    static final String COMPANY_VALUE = "Analytical Engines Ltd.";
    static final String UNCHANGED_VALUE = "Unchanged";
    static final String LOCKED_VALUE = "CC-1024";
    static final String CONFIDENT_VALUE = "42";

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

        // Filled together with a source reporting a confidence level, so its
        // marker must show the confidence indicator.
        var confident = new TextField("Quantity");
        confident.setId("confident");

        var form = new VerticalLayout(name, company, unchanged, locked,
                confident);
        form.setId("form");

        var controller = new FormAIController(form);
        controller.setFieldMarkerI18n(new FieldMarkerI18n().setMessage(MESSAGE)
                .setRevert(REVERT).setBadgeLabel(BADGE_LABEL)
                .setBadgeTooltip(BADGE_TOOLTIP));

        var startTurn = new NativeButton("Start turn",
                event -> controller.onRequest());
        startTurn.setId("start-turn");

        var finishTurn = new NativeButton("Finish turn", event -> {
            // Written before name: once name's listener has made this field
            // read-only, a real fill_form write to it would be rejected.
            locked.setValue(LOCKED_VALUE);
            name.setValue(NAME_VALUE);
            company.setValue(COMPANY_VALUE);
            unchanged.setValue(UNCHANGED_VALUE);
            confident.setValue(CONFIDENT_VALUE);
            // Attach a source to the value just written, standing in for the
            // one a real fill would report, so the marker applied at turn end
            // shows its confidence level.
            controller.restoreFieldSource(confident,
                    new ValueSource(ConfidenceLevel.HIGH, null));
            controller.onResponse(null);
        });
        finishTurn.setId("finish-turn");

        add(form, startTurn, finishTurn);
    }
}
