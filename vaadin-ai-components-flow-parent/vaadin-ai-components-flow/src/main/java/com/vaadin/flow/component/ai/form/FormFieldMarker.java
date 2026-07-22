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
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

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
     * Annotates the field as AI-filled using the given texts. Idempotent on the
     * client — repeated calls keep a single annotation on the field and refresh
     * its texts. Texts left {@code null}, or a {@code null} {@code i18n}
     * altogether, fall back to the web component's built-in defaults.
     */
    static void mark(Element field, FieldMarkerI18n i18n) {
        var options = toMarkOptions(i18n);
        if (options == null) {
            mark(field);
        } else {
            field.executeJs(
                    "customElements.get('vaadin-ai-field-marker').mark(this, $0)",
                    options);
        }
    }

    /**
     * @return the texts set on {@code i18n} as the options object the web
     *         component's {@code mark} accepts, or {@code null} when there is
     *         no text to pass
     */
    private static ObjectNode toMarkOptions(FieldMarkerI18n i18n) {
        if (i18n == null) {
            return null;
        }
        var options = JacksonUtils.createObjectNode();
        if (i18n.getMessage() != null) {
            options.put("message", i18n.getMessage());
        }
        if (i18n.getRevertText() != null) {
            options.put("revertText", i18n.getRevertText());
        }
        if (i18n.getBadgeLabel() != null) {
            options.put("badgeLabel", i18n.getBadgeLabel());
        }
        if (i18n.getBadgeTooltip() != null) {
            options.put("badgeTooltip", i18n.getBadgeTooltip());
        }
        return options.isEmpty() ? null : options;
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
     * Marks the field as being worked on by the AI. The web component shows the
     * "AI is working" shimmer and applies a client-side read-only guard so the
     * user cannot edit a value the AI is about to overwrite. The guard is a
     * client-only UX measure — the field's server-side read-only state is never
     * touched.
     */
    static void startWorking(Element field) {
        field.executeJs(
                "customElements.get('vaadin-ai-field-marker').startWorking(this)");
    }

    /**
     * Clears the "AI is working" state set by {@link #startWorking}: the web
     * component removes the shimmer and restores the field's client-side
     * read-only state, leaving any AI marker the fill applied in place.
     */
    static void stopWorking(Element field) {
        field.executeJs(
                "customElements.get('vaadin-ai-field-marker').stopWorking(this)");
    }
}
