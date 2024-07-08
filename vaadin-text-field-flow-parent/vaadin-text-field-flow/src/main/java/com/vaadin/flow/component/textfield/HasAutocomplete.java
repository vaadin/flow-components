/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
