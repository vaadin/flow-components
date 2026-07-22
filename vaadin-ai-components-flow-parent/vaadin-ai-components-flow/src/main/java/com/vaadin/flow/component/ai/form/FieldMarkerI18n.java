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

/**
 * The texts shown by the AI field highlight applied via
 * {@link FormAIController#showFieldHighlight}: the "AI" badge, its tooltip, and
 * the popover that explains the AI fill and offers a revert control. Use to
 * localize the highlight; any text left {@code null} falls back to the built-in
 * English default.
 *
 * @author Vaadin Ltd
 * @see FormAIController#setFieldMarkerI18n(FieldMarkerI18n)
 */
public class FieldMarkerI18n implements Serializable {

    private String message;
    private String revertText;
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
     * is also announced to screen readers when a field is highlighted.
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
    public String getRevertText() {
        return revertText;
    }

    /**
     * Sets the label of the revert control in the popover.
     *
     * @param revertText
     *            the revert control label, or {@code null} to use the built-in
     *            default
     * @return this instance, for chaining
     */
    public FieldMarkerI18n setRevertText(String revertText) {
        this.revertText = revertText;
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
