/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.form;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The texts shown by the marker the {@link FormAIController} applies to the
 * fields the AI filled: the "AI" badge, its tooltip, and the popover that
 * explains the AI fill and offers a revert control. Use to localize the marker;
 * any text left {@code null} falls back to the built-in English default.
 *
 * @author Vaadin Ltd
 * @since 25.3
 * @see FormAIController#setFieldMarkerI18n(FieldMarkerI18n)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldMarkerI18n implements Serializable {

    private String message;
    private String revert;
    private String badgeLabel;
    private String badgeTooltip;

    /**
     * Gets the message shown in the popover explaining the AI fill.
     *
     * @return the popover message, or {@code null} when the built-in default is
     *         used
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message shown in the popover explaining the AI fill. The message
     * is also announced to screen readers when a field is marked.
     *
     * @param message
     *            the popover message, or {@code null} to use the built-in
     *            default
     * @return this instance, for chaining
     */
    public FieldMarkerI18n setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Gets the label of the revert control in the popover.
     *
     * @return the revert control label, or {@code null} when the built-in
     *         default is used
     */
    public String getRevert() {
        return revert;
    }

    /**
     * Sets the label of the revert control in the popover.
     *
     * @param revert
     *            the revert control label, or {@code null} to use the built-in
     *            default
     * @return this instance, for chaining
     */
    public FieldMarkerI18n setRevert(String revert) {
        this.revert = revert;
        return this;
    }

    /**
     * Gets the accessible label of the badge button and the popover dialog.
     *
     * @return the badge label, or {@code null} when the built-in default is
     *         used
     */
    public String getBadgeLabel() {
        return badgeLabel;
    }

    /**
     * Sets the accessible label of the badge button and the popover dialog.
     *
     * @param badgeLabel
     *            the badge label, or {@code null} to use the built-in default
     * @return this instance, for chaining
     */
    public FieldMarkerI18n setBadgeLabel(String badgeLabel) {
        this.badgeLabel = badgeLabel;
        return this;
    }

    /**
     * Gets the tooltip text of the badge button.
     *
     * @return the badge tooltip text, or {@code null} when the built-in default
     *         is used
     */
    public String getBadgeTooltip() {
        return badgeTooltip;
    }

    /**
     * Sets the tooltip text of the badge button.
     *
     * @param badgeTooltip
     *            the badge tooltip text, or {@code null} to use the built-in
     *            default
     * @return this instance, for chaining
     */
    public FieldMarkerI18n setBadgeTooltip(String badgeTooltip) {
        this.badgeTooltip = badgeTooltip;
        return this;
    }
}
