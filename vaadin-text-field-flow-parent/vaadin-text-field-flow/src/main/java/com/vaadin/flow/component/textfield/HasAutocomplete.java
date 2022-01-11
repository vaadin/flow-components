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
 * Mixin interface for fields with {@code autocomplete} attribute.
 */
public interface HasAutocomplete extends HasElement {

    /**
     * Name of @{code autocomplete} attribute.
     */
    String AUTOCOMPLETE_ATTRIBUTE = "autocomplete";

    /**
     * Sets the {@link Autocomplete} attribute for indicating whether the value
     * of this component can be automatically completed by the browser.
     * <p>
     * If not set, devices may apply their own defaults.
     * <p>
     * See <a href=
     * "https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input#attr-autocomplete">autocomplete
     * attribute</a> for more information.
     *
     * @param autocomplete
     *            the {@code autocomplete} value, or {@code null} to unset
     */
    default void setAutocomplete(Autocomplete autocomplete) {
        if (autocomplete == null) {
            getElement().removeAttribute(AUTOCOMPLETE_ATTRIBUTE);
        } else {
            getElement().setAttribute(AUTOCOMPLETE_ATTRIBUTE,
                    autocomplete.value);
        }
    }

    /**
     * Gets the {@link Autocomplete} option of the field.
     *
     * @return the {@code autocomplete} value, or {@code null} if not set
     */
    default Autocomplete getAutocomplete() {
        String autocomplete = getElement().getAttribute(AUTOCOMPLETE_ATTRIBUTE);
        if (autocomplete == null) {
            return null;
        } else if ("".equals(autocomplete)) {
            // Default behavior for empty attribute.
            return Autocomplete.OFF;
        } else {
            return Autocomplete
                    .valueOf(autocomplete.toUpperCase().replaceAll("-", "_"));
        }
    }
}
