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
     * Sets the texts of the confidence indicator the marker shows in the
     * field's helper text section when the controller asks the LLM for
     * confidence levels (see
     * {@link FormAIController#setFieldConfidenceEnabled(boolean)}).
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
     * The texts of the confidence indicator, one per level. Any text left
     * {@code null} falls back to the built-in English default.
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
         * Gets the text shown for a value reported with low confidence.
         *
         * @return the low-confidence text, or {@code null} when the built-in
         *         default is used
         */
        public String getLow() {
            return low;
        }

        /**
         * Sets the text shown for a value reported with low confidence.
         *
         * @param low
         *            the low-confidence text, or {@code null} to use the
         *            built-in default
         * @return this instance, for chaining
         */
        public Confidence setLow(String low) {
            this.low = low;
            return this;
        }

        /**
         * Gets the text shown for a value reported with medium confidence.
         *
         * @return the medium-confidence text, or {@code null} when the built-in
         *         default is used
         */
        public String getMedium() {
            return medium;
        }

        /**
         * Sets the text shown for a value reported with medium confidence.
         *
         * @param medium
         *            the medium-confidence text, or {@code null} to use the
         *            built-in default
         * @return this instance, for chaining
         */
        public Confidence setMedium(String medium) {
            this.medium = medium;
            return this;
        }

        /**
         * Gets the text shown for a value reported with high confidence.
         *
         * @return the high-confidence text, or {@code null} when the built-in
         *         default is used
         */
        public String getHigh() {
            return high;
        }

        /**
         * Sets the text shown for a value reported with high confidence.
         *
         * @param high
         *            the high-confidence text, or {@code null} to use the
         *            built-in default
         * @return this instance, for chaining
         */
        public Confidence setHigh(String high) {
            this.high = high;
            return this;
        }
    }
}
