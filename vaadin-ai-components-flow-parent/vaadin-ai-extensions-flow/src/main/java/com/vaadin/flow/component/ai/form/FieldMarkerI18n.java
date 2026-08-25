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
 * fields the AI filled: the "AI" badge, its tooltip, the popover that explains
 * the AI fill and offers a revert control, and the confidence indicator. Use to
 * localize the marker; any text left {@code null} falls back to the built-in
 * English default.
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
    private Confidence confidence;

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

    /**
     * Gets the texts of the confidence indicator.
     *
     * @return the confidence indicator texts, or {@code null} when the built-in
     *         defaults are used
     */
    public Confidence getConfidence() {
        return confidence;
    }

    /**
     * Sets the texts of the confidence indicator shown for a value whose source
     * reports a confidence level.
     *
     * @param confidence
     *            the confidence indicator texts, or {@code null} to use the
     *            built-in defaults
     * @return this instance, for chaining
     */
    public FieldMarkerI18n setConfidence(Confidence confidence) {
        this.confidence = confidence;
        return this;
    }

    /**
     * The texts of the confidence indicator, one per
     * {@link com.vaadin.flow.component.ai.common.ConfidenceLevel confidence
     * level}. The text is shown next to the field and is included in the
     * field's accessible description. Any text left {@code null} falls back to
     * the built-in English default.
     *
     * @author Vaadin Ltd
     * @since 25.3
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Confidence implements Serializable {

        private String low;
        private String medium;
        private String high;

        /**
         * Gets the indicator text for the low confidence level.
         *
         * @return the low-level text, or {@code null} when the built-in default
         *         is used
         */
        public String getLow() {
            return low;
        }

        /**
         * Sets the indicator text for the low confidence level.
         *
         * @param low
         *            the low-level text, or {@code null} to use the built-in
         *            default
         * @return this instance, for chaining
         */
        public Confidence setLow(String low) {
            this.low = low;
            return this;
        }

        /**
         * Gets the indicator text for the medium confidence level.
         *
         * @return the medium-level text, or {@code null} when the built-in
         *         default is used
         */
        public String getMedium() {
            return medium;
        }

        /**
         * Sets the indicator text for the medium confidence level.
         *
         * @param medium
         *            the medium-level text, or {@code null} to use the built-in
         *            default
         * @return this instance, for chaining
         */
        public Confidence setMedium(String medium) {
            this.medium = medium;
            return this;
        }

        /**
         * Gets the indicator text for the high confidence level.
         *
         * @return the high-level text, or {@code null} when the built-in
         *         default is used
         */
        public String getHigh() {
            return high;
        }

        /**
         * Sets the indicator text for the high confidence level.
         *
         * @param high
         *            the high-level text, or {@code null} to use the built-in
         *            default
         * @return this instance, for chaining
         */
        public Confidence setHigh(String high) {
            this.high = high;
            return this;
        }
    }
}
