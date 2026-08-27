/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.form;

import java.util.Locale;
import java.util.Optional;

import com.vaadin.flow.component.ai.common.ConfidenceLevel;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.internal.JacksonUtils;
import com.vaadin.flow.internal.nodefeature.VirtualChildrenList;

import tools.jackson.databind.node.ObjectNode;

/**
 * Bridges the {@link FormAIController}'s marking of the fields the AI filled to
 * the {@code vaadin-ai-field-marker} web component, which annotates a field as
 * AI-filled and offers a popover to review and revert the value. It also
 * toggles the field's "AI is working" shimmer ({@link #setWorking}) shown while
 * a fill is in progress, and applies the application-supplied popover content
 * ({@link #setContent}). The annotations on this class load the web component
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
@NpmPackage(value = "@vaadin/field-highlighter", version = "25.3.0-alpha14")
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
     * Sets the confidence level the field's marker shows, or clears the
     * indicator when {@code confidence} is {@code null}. A missing level means
     * the model did not judge itself, not that it was unsure, so the marker
     * then shows no indicator rather than a doubtful one. A no-op when the
     * field has no marker.
     */
    static void setConfidence(Element field, ConfidenceLevel confidence) {
        find(field).ifPresent(marker -> {
            if (confidence == null) {
                marker.removeProperty("confidence");
            } else {
                marker.setProperty("confidence",
                        confidence.name().toLowerCase(Locale.ROOT));
            }
        });
    }

    /**
     * Removes the field's marker, clearing the annotation. A no-op when the
     * field has no marker.
     */
    static void remove(Element field) {
        find(field).ifPresent(Element::removeFromParent);
    }

    /**
     * Replaces the custom content shown in the popover of the field's marker.
     * The content lives in a wrapper element that travels as a virtual child of
     * the marker element — it has no place among the marker's DOM children,
     * which the web component's own rendering manages — and is handed to the
     * web component through its {@code content} property, which renders it into
     * the popover. The wrapper is created and bound on the first content and
     * stays for the marker's lifetime; replacing content only swaps the
     * wrapper's children, detaching the previous content so it is free for the
     * provider to hand out again. A {@code null} content empties and hides the
     * wrapper, restoring the popover's default parts — or, with no wrapper ever
     * created, is a complete no-op, so a marker never given content costs no
     * client traffic. Content the wrapper already carries is left untouched. A
     * no-op when the field has no marker.
     *
     * @param field
     *            the field whose marker gets the content, not {@code null}
     * @param content
     *            the content element to show, or {@code null} to clear
     */
    static void setContent(Element field, Element content) {
        var marker = find(field).orElse(null);
        if (marker == null) {
            return;
        }
        var wrapper = findWrapper(marker);
        if (wrapper == null) {
            if (content == null) {
                return;
            }
            wrapper = new Element("div");
            wrapper.getStyle().setDisplay(Style.Display.CONTENTS);
            marker.appendVirtualChild(wrapper);
            assignContent(marker, wrapper);
        }
        if (content != null && wrapper.equals(content.getParent())) {
            return;
        }
        wrapper.removeAllChildren();
        if (content != null) {
            wrapper.appendChild(content);
        }
        wrapper.setVisible(content != null);
    }

    /**
     * @return the wrapper element carrying the custom popover content of the
     *         field's marker, or {@code null} when no content was ever set on
     *         it
     */
    static Element contentWrapperOf(Element field) {
        return find(field).map(FormFieldMarker::findWrapper).orElse(null);
    }

    /**
     * Re-asserts the {@code content} property on the field's marker after the
     * field was detached and re-attached. Flow re-creates the marker element
     * and the wrapper it carries verbatim, but the property assignment is a
     * one-off script, so it must run again for the new client elements. A no-op
     * when the field has no marker or the marker no wrapper.
     */
    static void reassignContent(Element field) {
        find(field).ifPresent(marker -> {
            var wrapper = findWrapper(marker);
            if (wrapper != null) {
                assignContent(marker, wrapper);
            }
        });
    }

    /**
     * @return the content wrapper among the marker's virtual children — the
     *         only virtual child it ever gets — or {@code null} when none was
     *         created yet. Appending the wrapper is the only thing that ever
     *         creates the list and nothing removes it again, so an initialized
     *         list always holds the wrapper.
     */
    private static Element findWrapper(Element marker) {
        return marker.getNode()
                .getFeatureIfInitialized(VirtualChildrenList.class)
                .map(list -> Element.get(list.get(0))).orElse(null);
    }

    /**
     * Hands the content wrapper to the web component, which renders it into the
     * popover between the message and the revert control. Node-valued
     * properties have no place in Flow's state tree, so the assignment is a
     * script and does not survive a re-attach on its own — see
     * {@link #reassignContent(Element)}.
     */
    private static void assignContent(Element marker, Element wrapper) {
        marker.executeJs("this.content = $0;", wrapper);
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
