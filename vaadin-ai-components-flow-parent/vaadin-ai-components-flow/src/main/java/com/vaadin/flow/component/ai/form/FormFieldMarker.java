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

import java.util.Optional;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

/**
 * Bridges the {@link FormAIController}'s marking of the fields the AI filled to
 * the {@code vaadin-ai-field-marker} web component, which annotates a field as
 * AI-filled and offers a popover to review and revert the value. It also
 * toggles the field's "AI is working" shimmer ({@link #setWorking}) shown while
 * a fill is in progress. The annotations on this class load the web component
 * on the client.
 * <p>
 * The web component manages the annotation through its own element lifecycle:
 * appending it as a child of a field marks the field, and removing it clears
 * the mark. So this class drives it purely through Flow's {@link Element} API
 * rather than wrapping it in a {@code Component} — it carries no
 * application-facing API of its own, and as a plain element it lives in Flow's
 * state tree, which re-creates it verbatim when the field is detached and
 * re-attached.
 * <p>
 * The texts are set on each marker instance rather than through the web
 * component's page-global i18n defaults, so two controllers with different
 * {@link FieldMarkerI18n} on the same page cannot overwrite each other's texts.
 * <p>
 * A marker on a field that is not a Vaadin field — one with no shadow root to
 * inject the badge into — stays inert on the client, so a non-Vaadin field
 * never shows a marker.
 */
@NpmPackage(value = "@vaadin/field-highlighter", version = "25.3.0-alpha8")
@JsModule("@vaadin/field-highlighter/src/vaadin-ai-field-marker.js")
final class FormFieldMarker {

    private static final String TAG = "vaadin-ai-field-marker";

    private FormFieldMarker() {
    }

    /**
     * Adds a marker to the field, or reuses the one it already has, and applies
     * the given texts to it. Texts left {@code null}, or a {@code null}
     * {@code i18n} altogether, fall back to the web component's built-in
     * defaults.
     */
    static void add(Element field, FieldMarkerI18n i18n) {
        var marker = find(field).orElseGet(() -> {
            var created = new Element(TAG);
            field.appendChild(created);
            return created;
        });
        marker.setPropertyJson("i18n", toI18nJson(i18n));
    }

    /**
     * Removes the field's marker, clearing the annotation. A no-op when the
     * field has no marker.
     */
    static void remove(Element field) {
        find(field).ifPresent(Element::removeFromParent);
    }

    /**
     * Toggles the "AI is working" state on the field's marker. While set, the
     * web component shows the shimmer, hides the badge — the value it annotates
     * is about to be replaced — and applies a client-side read-only guard so
     * the user cannot edit a value the AI is about to overwrite. The guard is a
     * client-only UX measure; the field's server-side read-only state is never
     * touched. Clearing the state restores the read-only state and brings the
     * badge back, so a cancelled or failed fill leaves an existing mark intact.
     * A no-op when the field has no marker.
     */
    static void setWorking(Element field, boolean working) {
        find(field).ifPresent(marker -> marker.setProperty("working", working));
    }

    /**
     * @return the field's marker element, or an empty optional when the field
     *         has no marker
     */
    private static Optional<Element> find(Element field) {
        return field.getChildren().filter(child -> TAG.equals(child.getTag()))
                .findFirst();
    }

    /**
     * @return the texts set on {@code i18n} as the web component's {@code i18n}
     *         object; texts left unset are absent, so the web component falls
     *         back to its defaults for them
     */
    private static ObjectNode toI18nJson(FieldMarkerI18n i18n) {
        return i18n == null ? JacksonUtils.createObjectNode()
                : JacksonUtils.beanToJson(i18n);
    }
}
