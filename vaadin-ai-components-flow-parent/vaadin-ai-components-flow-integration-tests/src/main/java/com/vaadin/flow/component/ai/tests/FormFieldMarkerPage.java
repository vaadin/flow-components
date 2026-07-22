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

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ai.form.FieldMarkerI18n;
import com.vaadin.flow.component.ai.form.FormAIController;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Manual test page for the AI field marker that {@link FormAIController}
 * applies to fields it fills. "Fill with AI" simulates an AI turn that writes
 * values into the form and marks every changed field. The marker shows an "AI"
 * badge with a popover that explains the fill and offers a revert control;
 * reverting restores the field's pre-fill value and clears the marker.
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
        // Composite custom field, to exercise the marker on a CustomField too.
        var licensePlate = new LicensePlateField();
        licensePlate.setId("license-plate");

        // Pre-existing user input that the simulated AI fill will overwrite, so
        // reverting visibly differs from the AI-filled value.
        company.setValue("Acme Inc.");
        licensePlate.setValue("ABC-123");

        // Slot custom content into this field's marker popover. The marker
        // forwards a field child with slot="ai-field-marker-popover-content"
        // into the popover, below the explanation and above the revert control.
        var explanation = new Anchor("#", "Why was this filled in?");
        explanation.getElement().setAttribute("slot",
                "ai-field-marker-popover-content");
        company.getElement().appendChild(explanation.getElement());

        var form = new VerticalLayout(name, email, company, bio, licensePlate);
        form.setId("form");

        // The controller highlights every field it changes automatically; no
        // showFieldHighlight wiring is needed.
        var controller = new FormAIController(form);

        // Replace the built-in English texts so the markers applied by the
        // simulated fill show localized content in the badge, tooltip and
        // popover.
        controller.setFieldMarkerI18n(new FieldMarkerI18n()
                .setMessage("Tekoäly täytti tämän kentän.")
                .setRevertText("Kumoa").setBadgeLabel("Tekoälyn täyttämä arvo")
                .setBadgeTooltip(
                        "Tekoälyn täyttämä arvo.\nAvaa tiedot napsauttamalla"));

        Map<HasValue<?, String>, String> filled = Map.of(name, "Ada Lovelace",
                email, "ada@example.com", company, "Analytical Engines Ltd.",
                bio, "Mathematician and writer, known for work on the "
                        + "Analytical Engine.",
                licensePlate, "AI-987");

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

    /**
     * Composite custom field that edits a license plate as separate letters and
     * numbers but exposes a single {@code "ABC-123"} string value, mirroring
     * the web-component dev page example. Lets the marker be exercised on a
     * {@link CustomField} in addition to plain fields.
     */
    private static class LicensePlateField extends CustomField<String> {

        private final TextField letters = new TextField();
        private final IntegerField numbers = new IntegerField();

        LicensePlateField() {
            setLabel("License plate");
            letters.setAriaLabel("Letters");
            letters.setPlaceholder("ABC");
            numbers.setAriaLabel("Numbers");
            numbers.setPlaceholder("123");
            add(letters, new Span("-"), numbers);
        }

        @Override
        protected String generateModelValue() {
            var prefix = letters.getValue();
            var number = numbers.getValue();
            if (prefix == null || prefix.isEmpty() || number == null) {
                return "";
            }
            return prefix + "-" + number;
        }

        @Override
        protected void setPresentationValue(String value) {
            if (value == null || value.isEmpty()) {
                letters.clear();
                numbers.clear();
                return;
            }
            var parts = value.split("-", 2);
            letters.setValue(parts[0]);
            numbers.setValue(parts.length > 1 ? parseNumber(parts[1]) : null);
        }

        /**
         * @return the parsed integer, or {@code null} when {@code text} is not
         *         a plain integer
         */
        private static Integer parseNumber(String text) {
            var trimmed = text.trim();
            return trimmed.matches("\\d+") ? Integer.valueOf(trimmed) : null;
        }
    }
}
