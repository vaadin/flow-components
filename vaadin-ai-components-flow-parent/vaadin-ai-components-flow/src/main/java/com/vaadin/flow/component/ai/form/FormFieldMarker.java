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
package com.vaadin.flow.component.ai.form;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;

/**
 * Bridges {@link FormAIController#showFieldHighlight} /
 * {@link FormAIController#hideFieldHighlight} to the
 * {@code vaadin-ai-field-marker} web component, which annotates a field as
 * AI-filled and offers a popover to review and revert the value. It also
 * toggles the field's "AI is working" shimmer ({@link #startWorking} /
 * {@link #stopWorking}) shown while a fill is in progress. The annotations on
 * this class load the web component on the client.
 */
@NpmPackage(value = "@vaadin/field-base", version = "25.2.0-beta2")
@JsModule("@vaadin/field-base/src/vaadin-ai-field-marker.js")
final class FormFieldMarker {

    private FormFieldMarker() {
    }

    /**
     * Annotates the field as AI-filled. Idempotent on the client — repeated
     * calls keep a single annotation on the field.
     */
    static void mark(Element field) {
        field.executeJs(
                "customElements.get('vaadin-ai-field-marker').mark(this)");
    }

    /**
     * Removes the AI-filled annotation from the field. A no-op on the client
     * when the field is not marked.
     */
    static void unmark(Element field) {
        field.executeJs(
                "customElements.get('vaadin-ai-field-marker').unmark(this)");
    }

    /**
     * Marks the field as being worked on by the AI: shows the "AI is working"
     * shimmer (via the {@code ai-working} class the web component styles) and
     * makes the field read-only on the client so the user cannot edit a value
     * the AI is about to overwrite. The read-only state is set on the client
     * only — it is a UX guard, not a server-side state change — and the field's
     * own inputs are updated too for a {@code vaadin-custom-field}, which does
     * not propagate {@code readonly} to them. The shimmer animation lives in
     * the marker's stylesheet, which is adopted into the DOM only by
     * {@code mark} / {@code unmark}; calling {@code unmark} on a field that has
     * no marker yet adopts the stylesheet without other effect, so it is
     * invoked only when no marker is present (a marked field keeps its badge
     * through the working state).
     */
    static void startWorking(Element field) {
        field.executeJs(
                """
                        if (!this.querySelector('vaadin-ai-field-marker')) {
                          customElements.get('vaadin-ai-field-marker').unmark(this);
                        }
                        this.classList.add('ai-working');
                        this.readonly = true;
                        if (this.localName === 'vaadin-custom-field') {
                          (this.inputs ?? []).forEach((input) => { input.readonly = true; });
                        }""");
    }

    /**
     * Clears the "AI is working" state set by {@link #startWorking}: removes
     * the shimmer and the client-side read-only guard, leaving any AI marker
     * the fill applied in place.
     */
    static void stopWorking(Element field) {
        field.executeJs(
                """
                        this.classList.remove('ai-working');
                        this.readonly = false;
                        if (this.localName === 'vaadin-custom-field') {
                          (this.inputs ?? []).forEach((input) => { input.readonly = false; });
                        }""");
    }
}
