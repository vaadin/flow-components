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
@NpmPackage(value = "@vaadin/field-highlighter", version = "25.3.0-alpha11")
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
        var json = toI18nJson(i18n);
        // Re-applied every turn, so skip the write when the texts have not
        // changed since the marker last got them.
        if (!json.equals(marker.getPropertyRaw("i18n"))) {
            marker.setPropertyJson("i18n", json);
        }
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
     * @return whether the field's marker is in the "AI is working" state set
     *         through {@link #setWorking(Element, boolean)}; {@code false} when
     *         the field has no marker
     */
    static boolean isWorking(Element field) {
        return find(field).map(marker -> marker.getProperty("working", false))
                .orElse(false);
    }

    /**
     * Re-asserts a read-only state set on the field's server side while the "AI
     * is working" read-only guard held the field. The guard keeps the
     * client-side {@code readonly} at {@code true}, so Flow's client engine
     * drops the server's own {@code readonly=true} write as a no-op — and when
     * the guard lifts, the web component restores the pre-turn state over it.
     * <p>
     * The restore is not immediate: the web component holds the guard through a
     * wind-down delay after the working state ends, and a plain re-assert would
     * run before it and be overwritten. Re-appending the marker element is what
     * defuses that: appending an attached node removes and re-inserts it, and
     * on removal the web component completes a pending restore right away — its
     * documented lifecycle, so a restore cannot overwrite state set after
     * removal — while the re-insert re-marks the field. The re-assert then runs
     * with no restore pending, so it always sticks. Runs in one synchronous
     * script, so the marker never misses a frame and the swap cannot be
     * observed visually.
     */
    static void forceClientReadOnly(Element field) {
        field.executeJs("""
                const marker = this.querySelector(':scope > %s');
                if (marker) {
                  this.append(marker);
                }
                this.readonly = true;""".formatted(TAG));
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
