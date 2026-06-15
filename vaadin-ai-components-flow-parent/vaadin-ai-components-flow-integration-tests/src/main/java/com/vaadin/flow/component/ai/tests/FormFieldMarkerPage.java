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

import java.util.Map;

import com.vaadin.flow.component.ai.form.FormAIController;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Manual test page for the AI field marker that {@link FormAIController} applies
 * to fields it fills. "Fill with AI" simulates an AI turn that writes values
 * into the form and marks every changed field. The marker shows an "AI" badge
 * with a popover that explains the fill and offers a revert control; reverting
 * restores the field's pre-fill value and clears the marker.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-ai/form-field-marker")
public class FormFieldMarkerPage extends VerticalLayout {

    public FormFieldMarkerPage() {
        var name = new TextField("Name");
        name.setId("name");
        var email = new EmailField("Email");
        email.setId("email");
        var company = new TextField("Company");
        company.setId("company");
        var bio = new TextArea("Bio");
        bio.setId("bio");

        // Pre-existing user input that the simulated AI fill will overwrite, so
        // reverting visibly differs from the AI-filled value.
        company.setValue("Acme Inc.");

        var form = new VerticalLayout(name, email, company, bio);
        form.setId("form");

        var controller = new FormAIController(form);
        // Mark every field the AI changed during the turn.
        controller.addFieldValueChangedListener(changes -> changes
                .forEach(change -> controller.showHighlight(change.field())));

        var filled = Map.of(name, "Ada Lovelace", email, "ada@example.com",
                company, "Analytical Engines Ltd.", bio,
                "Mathematician and writer, known for work on the "
                        + "Analytical Engine.");

        var fill = new NativeButton("Fill with AI", e -> {
            // Drive one controller turn: snapshot pre-fill values, write the
            // simulated AI values, then let the controller diff and mark.
            controller.onRequest();
            filled.forEach((field, value) -> field.setValue(value));
            controller.onResponse(null);
        });
        fill.setId("fill");

        add(form, fill);
    }
}
