/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.HasElement;

/**
 * Mixin interface for fields with {@code autocapitalize} attribute.
 */
public interface HasAutocapitalize extends HasElement {

    /**
     * Name of @{code autocapitalize} attribute.
     */
    String AUTOCAPITALIZE_ATTRIBUTE = "autocapitalize";

    /**
     * Sets the {@link Autocapitalize} attribute for indicating whether the
     * value of this component can be automatically completed by the browser.
     * <p>
     * If not set, devices may apply their own default.
     * <p>
     * <em>Note:</em> <a href=
     * "https://developer.mozilla.org/en-US/docs/Web/HTML/Global_attributes/autocapitalize">
     * This attribute doesn't affect behavior when typing on a physical
     * keyboard. Instead, it affects the behavior of other input mechanisms,
     * such as virtual keyboards on mobile devices and voice input. It is only
     * supported by Chrome and Safari</a>.
     *
     * @param autocapitalize
     *            the {@code autocapitalize} value, or {@code null} to unset
     */
    default void setAutocapitalize(Autocapitalize autocapitalize) {
        if (autocapitalize == null) {
            getElement().removeAttribute(AUTOCAPITALIZE_ATTRIBUTE);
        } else {
            getElement().setAttribute(AUTOCAPITALIZE_ATTRIBUTE,
                    autocapitalize.value);
        }
    }

    /**
     * Gets the {@link Autocapitalize} for indicating whether the value of this
     * component can be automatically completed by the browser.
     *
     * @return the {@code autocapitalize} value, or {@code null} if not set
     */
    default Autocapitalize getAutocapitalize() {
        String autocapitalize = getElement()
                .getAttribute(AUTOCAPITALIZE_ATTRIBUTE);
        if (autocapitalize == null) {
            // Not set, may inherit behavior from parent form.
            return null;
        } else if ("".equals(autocapitalize)) {
            // Default behavior for empty attribute.
            return Autocapitalize.SENTENCES;
        } else {
            return Autocapitalize.valueOf(autocapitalize.toUpperCase());
        }
    }
}
